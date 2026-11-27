package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactApplier;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameStarted;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.SkillCheckFact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact;
import xyz.zlatanov.frakkintoasters.projection.GameReport.GameStatistics;
import xyz.zlatanov.frakkintoasters.projection.GameReport.Moment;
import xyz.zlatanov.frakkintoasters.projection.GameReport.MomentKind;
import xyz.zlatanov.frakkintoasters.projection.GameReport.PlayerReport;
import xyz.zlatanov.frakkintoasters.projection.GameReport.PlayerStatistics;
import xyz.zlatanov.frakkintoasters.projection.GameReport.TurnResources;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.ship.ShipZone;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * It applies the facts one by one to a game of its own, as a restore does, so each fact is read with the game as it
 * stood then. Nothing it reads is hidden once the game has ended.
 */
final class GameReporter {

    private final List<JournalEntry> journal;

    private int                             turn;
    private Integer                         currentPlayer;
    private final Map<Integer, int[]>       statistics      = new HashMap<>();
    private final Map<Integer, Integer>     revealedOnTurn  = new HashMap<>();
    private final Map<Integer, List<Character>> former      = new HashMap<>();
    private final List<Moment>              story           = new ArrayList<>();
    private final List<TurnResources>       resources       = new ArrayList<>();
    private final Map<SkillCheckResult, Integer> skillChecks = new EnumMap<>(SkillCheckResult.class);
    private final Map<ShipType, Integer>    shipsLost       = new EnumMap<>(ShipType.class);
    private int                             crisisCards;
    private int                             jumps;
    private int                             vipersDamaged;
    private int                             locationsDamaged;
    private int                             nukesUsed;
    // a Skill Card that just left a hand: placed into the check next, it was played into it
    private PlayerFact.CardsLost            leftHand;

    private static final int GAINED = 0, INTO_CHECKS = 1, STRENGTH = 2, PLAYED = 3, CRISIS = 4, ABILITIES = 5, BRIG = 6;

    GameReporter(List<JournalEntry> journal) {
        this.journal = journal;
    }

    Optional<GameReport> report() {
        val facts = journal.stream()
                .filter(FactRecorded.class::isInstance)
                .map(entry -> ((FactRecorded) entry).fact())
                .toList();
        if (facts.isEmpty() || !(facts.getFirst() instanceof GameStarted started)) {
            return Optional.empty();
        }
        val randomness = new Randomness();
        val game = Game.builder(started.playerCount())
                .objective(started.objective())
                .randomness(randomness)
                .die(new Die(randomness))
                .build();
        for (val fact : facts.subList(1, facts.size())) {
            val brigged = fact instanceof PlayerFact.CharacterMoved(var character, var to) && to == Location.BRIG
                    ? playerOf(game, character)
                    : null;
            FactApplier.apply(game, fact);
            tally(game, fact);
            if (brigged != null) {
                count(brigged, BRIG, 1);
            }
        }
        if (game.outcome().isEmpty()) {
            return Optional.empty();
        }
        resources.add(resourcesOf(game, game.turn().number(), true));
        val players = game.players().stream()
                .map(player -> new PlayerReport(player.number(), player.character(),
                        List.copyOf(former.getOrDefault(player.number(), List.of())),
                        Stream.concat(player.loyaltyCards().revealedCards().stream(), player.loyaltyCards().cards().stream()).toList(),
                        Stream.concat(player.motiveCards().revealedCards().stream(), player.motiveCards().cards().stream()).toList(),
                        !player.isHuman(),
                        revealedOnTurn.get(player.number()),
                        player.turns(),
                        statisticsOf(player.number())))
                .toList();
        return Optional.of(new GameReport(game.outcome().get(), game.winners(), game.turn().number(),
                game.distanceTraveled(), players,
                new GameStatistics(crisisCards, Map.copyOf(skillChecks), jumps, Map.copyOf(shipsLost), vipersDamaged,
                        locationsDamaged, nukesUsed),
                List.copyOf(resources), List.copyOf(story)));
    }

    private void tally(GameView game, Fact fact) {
        val cameFromHand = leftHand;
        leftHand = null;
        switch (fact) {
            case GameFact.TurnStarted(int number) -> {
                turn = number;
                resources.add(resourcesOf(game, number, false));
            }
            case GameFact.CurrentPlayerChanged(int player) -> currentPlayer = player;
            case GameFact.CrisisCardPlayed ignored -> {
                crisisCards++;
                if (currentPlayer != null) {
                    count(currentPlayer, CRISIS, 1);
                }
            }
            case GameFact.DestinationReached(var card) -> {
                jumps++;
                story.add(new Moment(turn, MomentKind.JUMPED, null, null, card, game.distanceTraveled(), null));
            }
            case GameFact.SkillCardPlayed(int player, var ignored) -> count(player, PLAYED, 1);
            case GameFact.NukeDiscarded ignored -> nukesUsed++;
            case GameFact.GameEnded(var winner, var ignored) ->
                    story.add(new Moment(turn, MomentKind.GAME_ENDED, null, null, null, null, winner));
            case SkillCheckFact.ResultDetermined(var result) -> skillChecks.merge(result, 1, Integer::sum);
            case SkillCheckFact.CardsPlaced(var ignored, var cards, var onTop) -> {
                if (cameFromHand != null && cameFromHand.cards().equals(cards)) {
                    count(cameFromHand.playerNumber(), INTO_CHECKS, cards.size());
                    count(cameFromHand.playerNumber(), STRENGTH, cards.stream().mapToInt(SkillCard::value).sum());
                }
            }
            case PlayerFact.CardsGained(int player, var pile, var cards) when pile == PlayerPile.SKILL ->
                    count(player, GAINED, cards.size());
            case PlayerFact.CardsLost lost when lost.pile() == PlayerPile.SKILL -> leftHand = lost;
            case PlayerFact.AbilityUsed(int player, var ignored) -> count(player, ABILITIES, 1);
            case PlayerFact.LoyaltyCardRevealed(int player, LoyaltyCard card) when card.isCylon()
                    && !revealedOnTurn.containsKey(player) -> {
                revealedOnTurn.put(player, turn);
                story.add(new Moment(turn, MomentKind.CYLON_REVEALED, player, game.player(player).character(), null,
                        null, null));
            }
            case PlayerFact.CharacterRemoved(int player, var character) -> {
                former.computeIfAbsent(player, ignored -> new ArrayList<>()).add(character);
                story.add(new Moment(turn, MomentKind.CHARACTER_REMOVED, player, character, null, null, null));
            }
            case TitleFact.PresidentAssigned(var character) -> title(game, MomentKind.PRESIDENT, character);
            case TitleFact.AdmiralAssigned(var character) -> title(game, MomentKind.ADMIRAL, character);
            case TitleFact.CagAssigned(var character) -> title(game, MomentKind.CAG, character);
            case BoardFact.PegasusDestroyed ignored ->
                    story.add(new Moment(turn, MomentKind.PEGASUS_DESTROYED, null, null, null, null, null));
            case BoardFact.LocationDamaged ignored -> locationsDamaged++;
            case ShipFact.ShipMoved(int shipId, var from, var to, var ignored) -> {
                if (to == ShipZone.REMOVED) {
                    removedShip(game, shipId).ifPresent(type -> shipsLost.merge(type, 1, Integer::sum));
                } else if (to == ShipZone.DAMAGED) {
                    vipersDamaged++;
                }
            }
            default -> {
            }
        }
    }

    // The titles handed out at the setup aren't a turn of the game.
    private void title(GameView game, MomentKind kind, Character character) {
        if (turn > 0 && character != null) {
            story.add(new Moment(turn, kind, playerOf(game, character), character, null, null, null));
        }
    }

    private void count(int player, int statistic, int amount) {
        statistics.computeIfAbsent(player, ignored -> new int[BRIG + 1])[statistic] += amount;
    }

    private PlayerStatistics statisticsOf(int player) {
        val counts = statistics.getOrDefault(player, new int[BRIG + 1]);
        return new PlayerStatistics(counts[GAINED], counts[INTO_CHECKS], counts[STRENGTH], counts[PLAYED],
                counts[CRISIS], counts[ABILITIES], counts[BRIG]);
    }

    private static Integer playerOf(GameView game, Character character) {
        return game.players().stream()
                .filter(player -> player.character() == character)
                .map(player -> player.number())
                .findFirst()
                .orElse(null);
    }

    private static Optional<ShipType> removedShip(GameView game, int shipId) {
        return game.removedComponents().items().stream()
                .filter(Ship.class::isInstance)
                .map(Ship.class::cast)
                .filter(ship -> ship.id() == shipId)
                .findFirst()
                .map(ship -> ShipType.of(ship.getClass()));
    }

    private static TurnResources resourcesOf(GameView game, int turn, boolean end) {
        val galactica = game.boards().galactica();
        return new TurnResources(turn, end, galactica.fuel(), galactica.food(), galactica.morale(), galactica.population(),
                game.distanceTraveled());
    }
}
