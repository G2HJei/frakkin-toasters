package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.SecretEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.DetermineSkillCheckResultEventProcessor;
import xyz.zlatanov.frakkintoasters.projection.GameProjection.*;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;
import xyz.zlatanov.frakkintoasters.state.view.BattlestarBoardView;
import xyz.zlatanov.frakkintoasters.state.view.BoardView;
import xyz.zlatanov.frakkintoasters.state.view.DeckView;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;

/**
 * Builds a {@link GameProjection}. What lies open on the table is public. The rest:
 * <ul>
 *     <li>Skill Cards: the holder sees them, the others how many he holds. A face down card doesn't show its color</li>
 *     <li>Loyalty, Mutiny, Super Crisis and Motive Cards: the holder sees them, the others the count and the revealed
 *     ones. The Mutineer card is faceup once someone holds it ("immediately reveal this card")</li>
 *     <li>the President's Quorum Cards: the President sees them, the others the count</li>
 *     <li>the Loyalty Cards a player looked at, and the cargo of the civilian ships he looked at: only he</li>
 *     <li>decks: the counts, and the cards of a discard pile that lies faceup</li>
 *     <li>the cards played before a skill check: the count per player, until they are revealed. The pile the cards are
 *     played into: the count</li>
 *     <li>the cards in an open interrupt window: the player who played them. The others count them in his hand</li>
 *     <li>the pending input: the player who answers it, with its legal answers. The others learn who is asked, but
 *     not while it is secret</li>
 *     <li>what a player may do at any pause ({@code AnytimeEvent}s): only he</li>
 *     <li>everything, once the game has ended, and to the user of a sandbox, who sees the draw piles in order too</li>
 * </ul>
 */
final class GameProjector {

    // The discard piles that lie face down, or that no card is ever discarded to.
    private static final Set<DeckId> FACEDOWN_DISCARDS = EnumSet.of(CIVILIAN_SHIPS, GALACTICA_DAMAGE, PEGASUS_DAMAGE,
            BASESTAR_DAMAGE, DESTINATION, DESTINY, LOYALTY, LOYALTY_NOT_CYLON, MOTIVE);

    private final GameView               game;
    private final Optional<PendingInput> pendingInput;
    private final Optional<ScopeOpened>  asking;
    private final Viewer                 viewer;
    private final boolean                ended;

    GameProjector(GameView game, Optional<PendingInput> pendingInput, Optional<ScopeOpened> asking, Viewer viewer) {
        this.game = game;
        this.pendingInput = pendingInput;
        this.asking = asking;
        this.viewer = viewer;
        this.ended = game.outcome().isPresent();
    }

    GameProjection project() {
        return new GameProjection(
                viewer,
                game.objective(),
                turn(),
                game.players().stream().map(this::player).toList(),
                new Titles(game.president(), game.admiral(), game.cag()),
                galactica(),
                pegasus(),
                cylonFleet(),
                new CylonSupply(game.cylonShips().available(ShipType.BASESTAR), game.cylonShips().available(ShipType.RAIDER),
                        game.cylonShips().available(ShipType.HEAVY_RAIDER), game.cylonShips().availableCenturions()),
                decks(),
                presidentHand(),
                game.quorumCardsInPlay(), // face up in front of their holders
                skillCheck(),
                game.nukes(),
                game.crisisCardsInPlay(),
                game.destinations(),
                game.distanceTraveled(),
                game.removedComponents().items().stream()
                        .filter(Character.class::isInstance)
                        .map(Character.class::cast)
                        .toList(),
                game.removedComponents().items().stream()
                        .filter(Ship.class::isInstance)
                        .map(ship -> ship((Ship) ship, null, true))
                        .toList(),
                input(),
                anytime(),
                game.outcome().map(winner -> new Outcome(winner, game.winners())).orElse(null));
    }

    private boolean seesEverything() {
        return ended || viewer instanceof Viewer.Sandbox;
    }

    private boolean sees(int owner) {
        return seesEverything() || viewer.is(owner);
    }

    private TurnProjection turn() {
        val turn = game.turn();
        val dieRoll = turn.dieRoll();
        return new TurnProjection(
                turn.number(),
                turn.step(),
                turn.currentPlayerNumber(),
                turn.actingPlayerNumber(),
                turn.crisisCards(),
                turn.endedEarly(),
                turn.returnsToCrisisStep(),
                dieRoll == null ? null : new DieRollProjection(dieRoll.value(), dieRoll.cards(), dieRoll.modifier(),
                        dieRoll.value() == null ? null : dieRoll.result(), dieRoll.abilities(), dieRoll.fixed()),
                turn.interruptWindow().orElse(null),
                turn.windowCards().stream().filter(card -> sees(card.playerNumber())).toList(),
                turn.windowAbilities().stream().filter(used -> sees(used.playerNumber())).toList(),
                turn.resolvedInterruptCards(),
                turn.raidersToMiss(),
                lossReductions(turn.lossReductions()));
    }

    private static Map<Resource, Integer> lossReductions(Set<Resource> resources) {
        val reductions = new EnumMap<Resource, Integer>(Resource.class);
        // each "Preventative Policy" makes the loss 1 smaller (AbstractOps.loseResource)
        resources.forEach(resource -> reductions.put(resource, 1));
        return reductions;
    }

    private PlayerProjection player(PlayerView player) {
        val number = player.number();
        val character = player.character();
        val owner = sees(number);
        return new PlayerProjection(
                number,
                character,
                character == null ? null : game.locate(character).orElse(null),
                skillHand(number, player.skillCards()),
                loyaltyCards(player, owner),
                hidden(player.motiveCards(), owner),
                hidden(player.mutinyCards(), owner),
                hidden(player.superCrisisCards(), owner),
                hidden(player.quorumCards(), owner),
                player.miracleTokens(),
                player.isInfiltrating(),
                character == null ? null : player.isHuman(),
                character != null && player.isRevealedCylon(),
                player.isMutineer(),
                player.turns(),
                player.handLimit(),
                owner ? player.seenLoyaltyCards() : null,
                owner ? List.copyOf(player.revealedCivilianShipIds()) : null);
    }

    // A card played into an open window left the hand, but the others mustn't see that it did.
    private SkillHand skillHand(int owner, DeckView<SkillCard> hand) {
        if (sees(owner)) {
            return skillHand(hand.cards(), true);
        }
        val inWindow = game.turn().windowCards().stream().filter(card -> card.playerNumber() == owner).count();
        return new SkillHand(hand.size() + (int) inWindow, null);
    }

    private static SkillHand skillHand(List<SkillCard> cards, boolean faceUp) {
        return new SkillHand(cards.size(), faceUp ? List.copyOf(cards) : null);
    }

    // "You Are the Mutineer": "Immediately reveal this card", and whoever passes it on gives it faceup.
    private HiddenCards<LoyaltyCard> loyaltyCards(PlayerView player, boolean owner) {
        val cards = player.loyaltyCards();
        if (owner) {
            return new HiddenCards<>(cards.size(), cards.revealedCards(), cards.cards());
        }
        val revealed = new ArrayList<>(cards.revealedCards());
        val facedown = cards.cards().stream().filter(card -> card != MUTINEER).count();
        if (cards.cards().contains(MUTINEER)) {
            revealed.add(MUTINEER);
        }
        return new HiddenCards<>((int) facedown, revealed, null);
    }

    private static <T> HiddenCards<T> hidden(DeckView<T> cards, boolean owner) {
        return new HiddenCards<>(cards.size(), cards.revealedCards(), owner ? cards.cards() : null);
    }

    private HiddenCards<QuorumCard> presidentHand() {
        val hand = game.presidentHand();
        val president = game.president() == null ? null : playerOf(game.president());
        val sees = seesEverything() || (president != null && viewer.is(president));
        return new HiddenCards<>(hand.size(), List.of(), sees ? hand.cards() : null);
    }

    private GalacticaProjection galactica() {
        val board = game.boards().galactica();
        return new GalacticaProjection(
                board.fuel(),
                board.food(),
                board.morale(),
                board.population(),
                board.jumpPreparation(),
                board.colonialOneDestroyed(),
                locations(board, board),
                ships(board.shipsInSpace()),
                board.reserves().stream().map(ship -> ship(ship, null, false)).toList(),
                board.damagedShips().stream().map(ship -> ship(ship, null, false)).toList(),
                board.boardingPartyTrack().entrySet().stream()
                        .map(e -> new CenturionProjection(e.getKey().id(), e.getValue()))
                        .toList());
    }

    private PegasusProjection pegasus() {
        val board = game.boards().pegasus();
        return new PegasusProjection(board.engineRoomActivated(), board.destroyed(), locations(board, board));
    }

    private CylonFleetProjection cylonFleet() {
        val board = game.boards().cylonFleet();
        return new CylonFleetProjection(board.pursuitTrack(), locations(board, null), ships(board.shipsInSpace()));
    }

    private static List<LocationProjection> locations(BoardView board, BattlestarBoardView damage) {
        val characters = new EnumMap<Location, List<Character>>(Location.class);
        board.characters().forEach((character, location) ->
                characters.computeIfAbsent(location, ignored -> new ArrayList<>()).add(character));
        return EnumSet.copyOf(board.locations()).stream()
                .map(location -> new LocationProjection(location,
                        damage != null && damage.isDamaged(location),
                        characters.getOrDefault(location, List.of())))
                .toList();
    }

    private List<ShipProjection> ships(Map<Ship, Location> ships) {
        return ships.entrySet().stream().map(e -> ship(e.getKey(), e.getValue(), false)).toList();
    }

    // The cargo of a civilian ship shows to the players who looked at it, and to everyone once it is destroyed.
    private ShipProjection ship(Ship ship, Location location, boolean destroyed) {
        val pilot = ship instanceof HumanFighterView fighter ? fighter.pilot() : null;
        val damage = ship instanceof BasestarView basestar ? basestar.damage() : List.<BasestarDamage>of();
        Cargo cargo = null;
        if (ship instanceof CivilianShip civilian && (destroyed || knowsCargo(civilian.id()))) {
            cargo = new Cargo(civilian.fuelCost(), civilian.moraleCost(), civilian.populationCost());
        }
        return new ShipProjection(ship.id(), ShipType.of(ship.getClass()), location, pilot, damage, cargo);
    }

    private boolean knowsCargo(int shipId) {
        return seesEverything()
                || viewer instanceof Viewer.PlayerViewer(int number)
                && game.player(number).revealedCivilianShipIds().contains(shipId);
    }

    private List<DeckProjection> decks() {
        val result = new ArrayList<DeckProjection>();
        for (val id : DeckId.values()) {
            DeckView<Object> deck = game.decks().deck(id);
            val faceUp = seesEverything() || !FACEDOWN_DISCARDS.contains(id);
            result.add(new DeckProjection(id, deck.size(), deck.discardSize(),
                    faceUp ? deck.discardedCards() : null,
                    viewer instanceof Viewer.Sandbox ? deck.cards() : null));
        }
        return result;
    }

    private SkillCheckProjection skillCheck() {
        val check = game.activeSkillCheck();
        if (check == null) {
            return null;
        }
        val playedBefore = new ArrayList<PlayedBefore>();
        for (val player : game.players()) {
            val cards = check.playedBeforeSkillCheck(player.number());
            if (!cards.isEmpty()) {
                playedBefore.add(new PlayedBefore(player.number(),
                        skillHand(cards, check.playedBeforeRevealed() || sees(player.number()))));
            }
        }
        val shownCards = new ArrayList<ShownCards>();
        for (val player : game.players()) {
            val cards = check.shownCards(player.number());
            if (!cards.isEmpty()) {
                shownCards.add(new ShownCards(player.number(), cards));
            }
        }
        return new SkillCheckProjection(
                check.type(),
                check.targetPlayerNumber(),
                DetermineSkillCheckResultEventProcessor.difficulty(game),
                playedBefore,
                check.playedBeforeRevealed(),
                shownCards,
                skillHand(check.cards().cards(), seesEverything()),
                check.matchingPile().cards(),
                check.nonMatchingPile().cards(),
                check.resolvedAbilities(),
                check.emergencyDeclared(),
                check.politicsCountPositive(),
                check.zeroStrengthColor(),
                check.totalStrength(),
                check.result(),
                check.passResultReplaced(),
                check.failResultPrevented());
    }

    private InputProjection input() {
        if (ended) {
            return null;
        }
        return pendingInput.map(input -> {
            if (!answers(input)) {
                return input.secret() ? new Waiting() : (InputProjection) new WaitingFor(input.player(), topic());
            }
            return switch (input) {
                case AwaitingDecision(var decision) -> new Decide(decision.playerNumber(),
                        eventClass(decision.action()), decision.eventConstraints(), LegalAnswers.of(game, decision),
                        topic());
                case AwaitingChoice(int player, var options) -> choose(player, List.copyOf(options));
            };
        }).orElse(null);
    }

    private Choose choose(int player, List<Followup> options) {
        val answers = new ArrayList<OptionAnswers>();
        for (int option = 0; option < options.size(); option++) {
            val decision = LegalAnswers.decisionOf(options.get(option));
            if (decision != null && decision.playerNumber() == player) {
                answers.add(new OptionAnswers(option, LegalAnswers.of(game, decision)));
            }
        }
        return new Choose(player, options, answers, topic());
    }

    // Only to the player who may act: they tell what he holds.
    private List<AnytimeAnswers> anytime() {
        if (ended) {
            return List.of();
        }
        return game.players().stream()
                .filter(player -> viewer.is(player.number()) || viewer instanceof Viewer.Sandbox)
                .map(player -> new AnytimeAnswers(player.number(), LegalAnswers.anytime(game, player.number())))
                .filter(answers -> !answers.answers().isEmpty())
                .toList();
    }

    // The event that queued the input, as the log shows its scope: a secret one only to its actor, its fields only when
    // they are public.
    private Topic topic() {
        return asking
                .filter(scope -> !(scope.event() instanceof SecretEvent) || seesEverything() || viewer.is(scope.actor()))
                .map(scope -> new Topic(eventClass(scope.event().getClass()),
                        seesEverything() || Disclosure.isPublic(scope.event()) ? scope.event() : null))
                .orElse(null);
    }

    private boolean answers(PendingInput input) {
        return viewer.is(input.player()) || viewer instanceof Viewer.Sandbox;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> eventClass(Class<?> action) {
        return (Class<? extends Event>) action;
    }

    private Integer playerOf(Character character) {
        return game.players().stream()
                .filter(player -> player.character() == character)
                .map(PlayerView::number)
                .findFirst()
                .orElse(null);
    }

}
