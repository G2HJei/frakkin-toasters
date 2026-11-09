package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import java.util.List;
import java.util.stream.Stream;

/**
 * The only place that calls component write methods on behalf of a fact. Used for live play (through
 * {@code AbstractOps.record}) and for replay, so both share one code path.
 */
public final class FactApplier {

    private FactApplier() {
    }

    public static void apply(Game game, Fact fact) {
        switch (fact) {
            case BoardFact f -> apply(game, f);
            case DeckFact f -> apply(game, f);
            case GameFact f -> apply(game, f);
            case PlayerFact f -> apply(game, f);
            case ShipFact f -> apply(game, f);
            case SkillCheckFact f -> apply(game, f);
            case TitleFact f -> apply(game, f);
        }
    }

    private static void apply(Game game, BoardFact fact) {
        val galactica = game.boards().galactica();
        switch (fact) {
            case BoardFact.ResourceDecreased f -> {
                switch (f.resource()) {
                    case FUEL -> galactica.decreaseFuel(f.amount());
                    case FOOD -> galactica.decreaseFood(f.amount());
                    case MORALE -> galactica.decreaseMorale(f.amount());
                    case POPULATION -> galactica.decreasePopulation(f.amount());
                }
            }
            case BoardFact.ResourceIncreased f -> {
                switch (f.resource()) {
                    case FUEL -> galactica.increaseFuel(f.amount());
                    case FOOD -> galactica.increaseFood(f.amount());
                    case MORALE -> galactica.increaseMorale(f.amount());
                    case POPULATION -> galactica.increasePopulation(f.amount());
                }
            }
            case BoardFact.JumpPreparationDecreased ignored -> galactica.decreaseJumpPreparation();
            case BoardFact.LocationDamaged f -> {
                val board = Stream.of(galactica, game.boards().pegasus())
                        .filter(b -> b.locations().contains(f.location()))
                        .findFirst()
                        .orElseThrow(FrakCallTheAdmiralException::new);
                board.damage(f.location());
            }
            case BoardFact.BoardingPartyAdvanced ignored -> galactica.advanceBoardingParty();
            case BoardFact.PursuitAdvanced ignored -> game.boards().cylonFleet().advancePursuit();
            case BoardFact.PursuitReset ignored -> game.boards().cylonFleet().resetPursuit();
            case BoardFact.EngineRoomActivated ignored -> game.boards().pegasus().engineRoomActivated(true);
        }
    }

    private static void apply(Game game, DeckFact fact) {
        switch (fact) {
            case DeckFact.DeckShuffled f -> game.decks().deck(f.deck()).setDrawOrder(f.drawOrder());
            case DeckFact.CardsDrawn f -> {
                val deck = game.decks().deck(f.deck());
                assertSame(f.cards(), deck.removeTop(f.cards().size()));
            }
            case DeckFact.CardsAddedOnTop f -> addOnTop(game.decks().deck(f.deck()), f.cards());
            case DeckFact.CardsAddedToBottom f -> addToBottom(game.decks().deck(f.deck()), f.cards());
            case DeckFact.CardsDiscarded f -> discard(game.decks().deck(f.deck()), f.cards());
        }
    }

    private static void apply(Game game, GameFact fact) {
        switch (fact) {
            case GameFact.GameStarted ignored -> {
                // the replayer already built the game from it
            }
            case GameFact.DieRolled ignored -> {
                // the result is the fact, nothing to change
            }
            case GameFact.StepChanged f -> game.turn().step(f.step());
            case GameFact.TurnStarted f -> game.turn().number(f.turn());
            case GameFact.CurrentPlayerChanged f -> game.turn().currentPlayerNumber(f.playerNumber());
            case GameFact.ActingPlayerChanged f -> game.turn().actingPlayerNumber(f.playerNumber());
        }
    }

    private static void apply(Game game, PlayerFact fact) {
        switch (fact) {
            case PlayerFact.CharacterSelected f -> game.player(f.playerNumber()).character(f.character());
            case PlayerFact.CharacterMoved f -> {
                game.boards().all().forEach(b -> b.remove(f.character()));
                game.boards().all().stream()
                        .filter(b -> b.locations().contains(f.to()))
                        .findFirst()
                        .orElseThrow(FrakCallTheAdmiralException::new)
                        .place(f.to(), f.character());
            }
            case PlayerFact.CharacterRemovedFromBoards f -> game.boards().all().forEach(b -> b.remove(f.character()));
            case PlayerFact.CardsGained f -> addOnTop(game.player(f.playerNumber()).pile(f.pile()), f.cards());
            case PlayerFact.CardsLost f -> remove(game.player(f.playerNumber()).pile(f.pile()), f.cards());
            case PlayerFact.MiracleTokenGained f -> game.player(f.playerNumber()).gainMiracleToken();
            case PlayerFact.InfiltrationStarted f -> game.player(f.playerNumber()).infiltrateGalactica();
            case PlayerFact.CivilianShipRevealed f ->
                    game.player(f.playerNumber()).revealedCivilianShipIds().add(f.shipId());
            case PlayerFact.OwnTurnStarted f -> game.player(f.playerNumber()).countTurn();
        }
    }

    private static void apply(Game game, ShipFact fact) {
        switch (fact) {
            case ShipFact.ShipMoved f -> ShipZones.move(game, f.shipId(), f.from(), f.to(), f.location());
            case ShipFact.PilotChanged f -> game.boards().galactica()
                    .shipInSpace(f.shipId(), HumanFighter.class)
                    .pilot(f.pilot());
            case ShipFact.BasestarDamageAttached f -> basestar(game, f.shipId()).damage(f.card());
            case ShipFact.BasestarDamageCleared f -> basestar(game, f.shipId()).clearDamage();
        }
    }

    private static void apply(Game game, SkillCheckFact fact) {
        switch (fact) {
            case SkillCheckFact.SkillCheckStarted f -> game.startSkillCheck(f.type());
            case SkillCheckFact.TargetChosen f -> game.activeSkillCheck().targetPlayerNumber(f.playerNumber());
            case SkillCheckFact.CardsPlayedBefore f ->
                    game.activeSkillCheck().playedBefore(f.playerNumber(), f.cards());
            case SkillCheckFact.PlayedBeforeRevealed ignored -> game.activeSkillCheck().revealPlayedBefore();
            case SkillCheckFact.CardsPlaced f -> {
                val pile = game.activeSkillCheck().pile(f.pile());
                if (f.onTop()) {
                    pile.addOnTop(f.cards());
                } else {
                    f.cards().forEach(pile::addToBottom);
                }
            }
            case SkillCheckFact.CardsDrawn f -> {
                val pile = game.activeSkillCheck().pile(f.pile());
                assertSame(f.cards(), pile.removeTop(f.cards().size()));
            }
            case SkillCheckFact.CardRemoved f -> game.activeSkillCheck().remove(f.card(), f.playerNumber());
            case SkillCheckFact.EmergencyDeclared ignored -> game.activeSkillCheck().declareEmergency();
            case SkillCheckFact.TotalStrengthDetermined f -> game.activeSkillCheck().totalStrength(f.totalStrength());
            case SkillCheckFact.ResultDetermined f -> game.activeSkillCheck().result(f.result());
            case SkillCheckFact.PassResultReplaced ignored -> game.activeSkillCheck().replacePassResult();
            case SkillCheckFact.FailResultPrevented ignored -> game.activeSkillCheck().preventFailResult();
        }
    }

    private static void apply(Game game, TitleFact fact) {
        switch (fact) {
            case TitleFact.PresidentAssigned f -> game.president(f.character());
            case TitleFact.AdmiralAssigned f -> game.admiral(f.character());
            case TitleFact.CagAssigned f -> game.cag(f.character());
            case TitleFact.PresidentQuorumCardGained f -> game.presidentHand().addOnTop(f.card());
            case TitleFact.PresidentQuorumCardsLost f -> remove(game.presidentHand(), f.cards());
        }
    }

    private static Basestar basestar(Game game, int id) {
        return game.boards().galactica().shipsInSpace(Basestar.class).stream()
                .filter(b -> b.id() == id)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    private static void assertSame(List<?> expected, List<?> actual) {
        if (!expected.equals(actual)) {
            throw new FrakCallTheAdmiralException(); // the state differs from the one the fact was recorded in
        }
    }

    // the lists come from facts that were recorded for a deck of the same card type
    @SuppressWarnings("unchecked")
    private static <T> void addOnTop(Deck<T> deck, List<?> cards) {
        deck.addOnTop((List<T>) cards);
    }

    @SuppressWarnings("unchecked")
    private static <T> void addToBottom(Deck<T> deck, List<?> cards) {
        cards.forEach(card -> deck.addToBottom((T) card));
    }

    @SuppressWarnings("unchecked")
    private static <T> void discard(Deck<T> deck, List<?> cards) {
        deck.discard((List<T>) cards);
    }

    @SuppressWarnings("unchecked")
    private static <T> void remove(Deck<T> deck, List<?> cards) {
        deck.remove((List<T>) cards);
    }
}
