package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.*;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

/**
 * Applies each kind of fact to a prepared state and checks the result.
 */
class FactApplierTest {

    Game      game  = Game.builder().build();
    SkillCard card  = new SkillCard(3, CONSOLIDATE_POWER);
    SkillCard other = new SkillCard(1, CONSOLIDATE_POWER);

    void apply(Fact fact) {
        FactApplier.apply(game, fact);
    }

    /* BoardFact */

    @Test
    void resourceDecreasedLowersTheResourceButNotBelowZero() {
        apply(new ResourceDecreased(FUEL, 3));
        apply(new ResourceDecreased(FOOD, 1));
        apply(new ResourceDecreased(MORALE, 2));
        apply(new ResourceDecreased(POPULATION, 100));

        val galactica = game.boards().galactica();
        assertEquals(5, galactica.fuel());
        assertEquals(7, galactica.food());
        assertEquals(8, galactica.morale());
        assertEquals(0, galactica.population());
    }

    @Test
    void resourceIncreasedRaisesTheResourceButNotAboveTheMaximum() {
        apply(new ResourceIncreased(FUEL, 3));
        apply(new ResourceIncreased(FOOD, 1));
        apply(new ResourceIncreased(MORALE, 2));
        apply(new ResourceIncreased(POPULATION, 100));

        val galactica = game.boards().galactica();
        assertEquals(11, galactica.fuel());
        assertEquals(9, galactica.food());
        assertEquals(12, galactica.morale());
        assertEquals(15, galactica.population());
    }

    @Test
    void jumpPreparationIncreasedMovesTheFleetTokenAhead() {
        apply(new JumpPreparationIncreased());

        assertEquals(JumpPreparation.POSITION_1, game.boards().galactica().jumpPreparation());
    }

    @Test
    void jumpPreparationDecreasedMovesTheFleetTokenBack() {
        val galactica = game.boards().galactica();
        galactica.advanceJumpPreparation();

        apply(new JumpPreparationDecreased());

        assertEquals(JumpPreparation.START, galactica.jumpPreparation());
    }

    @Test
    void locationDamagedMarksTheLocationOnTheBoardThatHasIt() {
        apply(new LocationDamaged(ARMORY));
        apply(new LocationDamaged(PEGASUS_CIC));

        assertEquals(java.util.Set.of(ARMORY), game.boards().galactica().damagedLocations());
        assertEquals(java.util.Set.of(PEGASUS_CIC), game.boards().pegasus().damagedLocations());
    }

    @Test
    void boardingPartyAdvancedMovesEveryCenturionOneStep() {
        game.boards().galactica().boardGalactica(new Centurion(1));

        apply(new BoardingPartyAdvanced());

        assertEquals(BoardingParty.values()[BoardingParty.START.ordinal() + 1],
                game.boards().galactica().boardingPartyTrack().get(new Centurion(1)));
    }

    @Test
    void pursuitAdvancedAndResetMoveThePursuitTrack() {
        apply(new PursuitAdvanced());
        assertEquals(Pursuit.ONE_CIVILIAN_SHIP, game.boards().cylonFleet().pursuitTrack());

        apply(new PursuitReset());
        assertEquals(Pursuit.START, game.boards().cylonFleet().pursuitTrack());
    }

    @Test
    void engineRoomActivatedActivatesTheEngineRoom() {
        apply(new EngineRoomActivated());

        assertTrue(game.boards().pegasus().engineRoomActivated());
    }

    /* DeckFact */

    @Test
    void deckShuffledSetsTheDrawOrderAndClearsTheDiscardPile() {
        val deck = game.decks().<SkillCard>deck(DeckId.POLITICS);
        deck.discard(card);

        apply(new DeckShuffled(DeckId.POLITICS, List.of(other, card)));

        assertEquals(List.of(other, card), deck.cards());
        assertEquals(0, deck.discardSize());
    }

    @Test
    void cardsDrawnRemovesTheTopCards() {
        val deck = game.decks().<SkillCard>deck(DeckId.POLITICS);
        val top = deck.top(2);
        val size = deck.size();

        apply(new CardsDrawn(DeckId.POLITICS, top));

        assertEquals(size - 2, deck.size());
        assertNotEquals(top, deck.top(2));
    }

    @Test
    void cardsDrawnFailsWhenTheTopCardsAreNotTheRecordedOnes() {
        val deck = game.decks().<SkillCard>deck(DeckId.POLITICS);
        deck.addOnTop(card);

        assertThrows(FrakCallTheAdmiralException.class, () -> apply(new CardsDrawn(DeckId.POLITICS, List.of(other))));
    }

    @Test
    void cardsAddedOnTopAndToBottomPutCardsAtTheEnds() {
        val deck = game.decks().<SkillCard>deck(DeckId.POLITICS);
        val size = deck.size();

        apply(new CardsAddedOnTop(DeckId.POLITICS, List.of(card, other)));
        apply(new CardsAddedToBottom(DeckId.POLITICS, List.of(card)));

        assertEquals(List.of(card, other), deck.top(2));
        assertEquals(card, deck.cards().getLast());
        assertEquals(size + 3, deck.size());
    }

    @Test
    void cardsDiscardedGoToTheDiscardPile() {
        apply(new CardsDiscarded(DeckId.POLITICS, List.of(card, other)));

        assertEquals(List.of(card, other), game.decks().<SkillCard>deck(DeckId.POLITICS).discardedCards());
    }

    /* GameFact */

    @Test
    void gameStartedAndDieRolledChangeNothing() {
        val before = GameSnapshot.of(game);

        apply(new GameStarted(3, game.objective()));
        apply(new DieRolled(5));

        assertEquals(before, GameSnapshot.of(game));
    }

    @Test
    void gameFactsChangeTheFlowOfTheGame() {
        apply(new StepChanged(MOVEMENT));
        apply(new TurnStarted(4));
        apply(new CurrentPlayerChanged(2));
        apply(new ActingPlayerChanged(3));

        assertEquals(MOVEMENT, game.turn().step());
        assertEquals(4, game.turn().number());
        assertEquals(2, game.turn().currentPlayerNumber());
        assertEquals(3, game.turn().actingPlayerNumber());

        apply(new ActingPlayerChanged(null));
        assertEquals(2, game.turn().actingPlayerNumber());

        apply(new CrisisCardPlayed(CrisisCard.RAIDERS_INBOUND));
        apply(new CrisisCardPlayed(CrisisCard.LABOR_DISPUTE));
        assertEquals(List.of(CrisisCard.RAIDERS_INBOUND, CrisisCard.LABOR_DISPUTE), game.turn().crisisCards());

        apply(new CrisisCardsCleared(List.of(CrisisCard.RAIDERS_INBOUND)));
        assertEquals(List.of(CrisisCard.LABOR_DISPUTE), game.turn().crisisCards());
        assertEquals(List.of(), game.decks().crisis().discardedCards()); // the discard pile is a separate CardsDiscarded
    }

    @Test
    void gameEndedSetsTheOutcome() {
        assertEquals(Optional.empty(), game.outcome());

        apply(new GameEnded(CYLONS));

        assertEquals(Optional.of(CYLONS), game.outcome());
    }

    /* PlayerFact */

    @Test
    void characterSelectedSetsTheCharacter() {
        apply(new CharacterSelected(2, FELIX_GAETA));

        assertEquals(FELIX_GAETA, game.player(2).character());
    }

    @Test
    void characterMovedLeavesEveryBoardAndEntersTheOneWithTheLocation() {
        apply(new CharacterMoved(FELIX_GAETA, HANGAR_DECK));
        assertEquals(HANGAR_DECK, game.boards().galactica().characters().get(FELIX_GAETA));

        apply(new CharacterMoved(FELIX_GAETA, PEGASUS_CIC));

        assertFalse(game.boards().galactica().characters().containsKey(FELIX_GAETA));
        assertEquals(PEGASUS_CIC, game.boards().pegasus().characters().get(FELIX_GAETA));
    }

    @Test
    void characterRemovedFromBoardsRemovesTheCharacterEverywhere() {
        apply(new CharacterMoved(FELIX_GAETA, HANGAR_DECK));

        apply(new CharacterRemovedFromBoards(FELIX_GAETA));

        assertTrue(game.boards().all().stream().noneMatch(b -> b.characters().containsKey(FELIX_GAETA)));
    }

    @Test
    void cardsGainedAndLostChangeThePlayersPile() {
        apply(new CardsGained(1, PlayerPile.SKILL, List.of(card)));
        apply(new CardsGained(1, PlayerPile.SKILL, List.of(other)));
        assertEquals(List.of(other, card), game.player(1).skillCards().cards());

        apply(new CardsLost(1, PlayerPile.SKILL, List.of(card)));
        assertEquals(List.of(other), game.player(1).skillCards().cards());
    }

    @Test
    void miracleTokenGainedGivesTheTokenBack() {
        game.player(1).exhaustMiracleToken();

        apply(new MiracleTokenGained(1));

        assertTrue(game.player(1).hasMiracleToken());
    }

    @Test
    void infiltrationStartedMakesTheCylonLeaderInfiltrate() {
        apply(new CharacterSelected(1, AARON_DORAL));

        apply(new InfiltrationStarted(1));

        assertTrue(game.player(1).isInfiltrating());
    }

    @Test
    void civilianShipRevealedRemembersTheShipForThePlayer() {
        apply(new CivilianShipRevealed(2, 2005));

        assertEquals(java.util.Set.of(2005), game.player(2).revealedCivilianShipIds());
    }

    @Test
    void ownTurnStartedCountsATurnForThePlayerOnly() {
        apply(new OwnTurnStarted(2));
        apply(new OwnTurnStarted(2));

        assertEquals(2, game.player(2).turns());
        assertEquals(0, game.player(1).turns());
        assertEquals(0, game.turn().number());
    }

    /* ShipFact */

    @Test
    void shipMovedFromTheCylonSupplyToSpace() {
        val raider = game.cylonShips().raiders().getLast();

        apply(new ShipMoved(raider.id(), CYLON_SUPPLY, GALACTICA_SPACE, GALACTICA_SPACE_8_OCLOCK));

        assertFalse(game.cylonShips().raiders().contains(raider));
        assertEquals(GALACTICA_SPACE_8_OCLOCK, game.boards().galactica().locate(raider));
    }

    @Test
    void shipMovedFromTheCylonSupplyToTheCylonFleetBoardAndBack() {
        val heavyRaider = game.cylonShips().heavyRaiders().getLast();

        apply(new ShipMoved(heavyRaider.id(), CYLON_SUPPLY, CYLON_FLEET_SPACE, CYLON_FLEET_SPACE_3));
        assertEquals(CYLON_FLEET_SPACE_3, game.boards().cylonFleet().locate(heavyRaider));

        apply(new ShipMoved(heavyRaider.id(), CYLON_FLEET_SPACE, GALACTICA_SPACE, GALACTICA_SPACE_6_OCLOCK));
        assertEquals(GALACTICA_SPACE_6_OCLOCK, game.boards().galactica().locate(heavyRaider));

        apply(new ShipMoved(heavyRaider.id(), GALACTICA_SPACE, CYLON_SUPPLY, null));
        assertTrue(game.cylonShips().heavyRaiders().contains(heavyRaider));
    }

    @Test
    void shipMovedForCenturions() {
        val centurion = game.cylonShips().centurions().getLast();

        apply(new ShipMoved(centurion.id(), CENTURION_SUPPLY, BOARDING_PARTY, null));
        assertTrue(game.boards().galactica().boardingPartyTrack().containsKey(centurion));
        assertFalse(game.cylonShips().centurions().contains(centurion));

        apply(new ShipMoved(centurion.id(), BOARDING_PARTY, CENTURION_SUPPLY, null));
        assertTrue(game.boards().galactica().boardingPartyTrack().isEmpty());
        assertTrue(game.cylonShips().centurions().contains(centurion));
    }

    @Test
    void shipMovedBetweenTheCivilianDeckAndSpace() {
        val civ = game.decks().civilianShips().cards().getFirst();

        apply(new ShipMoved(civ.id(), CIVILIAN_DECK, GALACTICA_SPACE, GALACTICA_SPACE_2_OCLOCK));
        assertFalse(game.decks().civilianShips().cards().contains(civ));
        assertEquals(GALACTICA_SPACE_2_OCLOCK, game.boards().galactica().locate(civ));

        apply(new ShipMoved(civ.id(), GALACTICA_SPACE, CIVILIAN_DECK, null));
        assertEquals(civ, game.decks().civilianShips().cards().getFirst());
    }

    @Test
    void shipMovedBetweenReservesDamagedAndRemoved() {
        val galactica = game.boards().galactica();

        apply(new ShipMoved(1, RESERVES, DAMAGED, null));
        assertTrue(galactica.damagedShips().stream().anyMatch(s -> s.id() == 1));
        assertTrue(galactica.reserves().stream().noneMatch(s -> s.id() == 1));

        apply(new ShipMoved(1, DAMAGED, RESERVES, null));
        assertTrue(galactica.reserves().stream().anyMatch(s -> s.id() == 1));

        apply(new ShipMoved(11, RESERVES, REMOVED, null));
        assertTrue(galactica.reserves().stream().noneMatch(s -> s.id() == 11));
        assertTrue(game.removedComponents().contains(new Raptor(11)));
    }

    @Test
    void shipMovedWithinSpaceChangesTheLocation() {
        apply(new ShipMoved(1, RESERVES, GALACTICA_SPACE, GALACTICA_SPACE_4_OCLOCK));

        apply(new ShipMoved(1, GALACTICA_SPACE, GALACTICA_SPACE, GALACTICA_SPACE_10_OCLOCK));

        assertEquals(1, game.boards().galactica().shipsInSpace().size());
        assertEquals(GALACTICA_SPACE_10_OCLOCK, game.boards().galactica().shipsInSpace().values().iterator().next());
    }

    @Test
    void shipMovedFailsWhenTheShipIsNotInTheZone() {
        assertThrows(FrakCallTheAdmiralException.class, () -> apply(new ShipMoved(1, DAMAGED, RESERVES, null)));
    }

    @Test
    void pilotChangedSetsAndClearsThePilot() {
        apply(new ShipMoved(1, RESERVES, GALACTICA_SPACE, GALACTICA_SPACE_4_OCLOCK));

        apply(new PilotChanged(1, LOUANNE_KAT_KATRAINE));
        assertEquals(LOUANNE_KAT_KATRAINE, game.boards().galactica().shipInSpace(1, HumanFighter.class).pilot());

        apply(new PilotChanged(1, null));
        assertNull(game.boards().galactica().shipInSpace(1, HumanFighter.class).pilot());
    }

    @Test
    void basestarDamageAttachedAndCleared() {
        val basestar = game.cylonShips().basestars().getLast();
        apply(new ShipMoved(basestar.id(), CYLON_SUPPLY, GALACTICA_SPACE, GALACTICA_SPACE_8_OCLOCK));

        apply(new BasestarDamageAttached(basestar.id(), BasestarDamage.STRUCTURAL_DAMAGE));
        assertEquals(List.of(BasestarDamage.STRUCTURAL_DAMAGE), basestar.damage());

        apply(new BasestarDamageCleared(basestar.id()));
        assertTrue(basestar.damage().isEmpty());
    }

    /* SkillCheckFact */

    SkillCheck startCheck() {
        apply(new SkillCheckFact.SkillCheckStarted(SkillCheckType.values()[0]));
        return game.activeSkillCheck();
    }

    @Test
    void skillCheckStartedAndTargetChosen() {
        val check = startCheck();
        assertEquals(SkillCheckType.values()[0], check.type());

        apply(new SkillCheckFact.TargetChosen(2));
        assertEquals(2, check.targetPlayerNumber());
    }

    @Test
    void cardsPlayedBeforeAndRevealed() {
        val check = startCheck();

        apply(new SkillCheckFact.CardsPlayedBefore(1, List.of(card)));
        apply(new SkillCheckFact.PlayedBeforeRevealed());

        assertEquals(List.of(card), check.playedBeforeSkillCheck(1));
        assertTrue(check.playedBeforeRevealed());
    }

    @Test
    void cardsPlacedOnTopOrBottomAndDrawnFromAPile() {
        val check = startCheck();

        apply(new SkillCheckFact.CardsPlaced(SkillCheckPile.CARDS, List.of(card), true));
        apply(new SkillCheckFact.CardsPlaced(SkillCheckPile.CARDS, List.of(other), false));
        assertEquals(List.of(card, other), check.cards().cards());

        apply(new SkillCheckFact.CardsDrawn(SkillCheckPile.CARDS, List.of(card)));
        assertEquals(List.of(other), check.cards().cards());
        assertThrows(FrakCallTheAdmiralException.class,
                () -> apply(new SkillCheckFact.CardsDrawn(SkillCheckPile.CARDS, List.of(card))));
    }

    @Test
    void cardRemovedLeavesTheCheckFromWhereverItWas() {
        val check = startCheck();
        apply(new SkillCheckFact.CardsPlayedBefore(1, List.of(card)));
        apply(new SkillCheckFact.CardsPlaced(SkillCheckPile.MATCHING, List.of(other), true));

        apply(new SkillCheckFact.CardRemoved(card, 1));
        apply(new SkillCheckFact.CardRemoved(other, null));

        assertTrue(check.playedBeforeSkillCheck().isEmpty());
        assertTrue(check.matchingPile().isEmpty());
    }

    @Test
    void skillCheckProgressFacts() {
        val check = startCheck();

        apply(new SkillCheckFact.EmergencyDeclared());
        apply(new SkillCheckFact.TotalStrengthDetermined(7));
        apply(new SkillCheckFact.ResultDetermined(SkillCheckResult.PASS));
        apply(new SkillCheckFact.PassResultReplaced());

        assertTrue(check.emergencyDeclared());
        assertEquals(7, check.totalStrength());
        assertEquals(SkillCheckResult.PASS, check.result());
        assertTrue(check.passResultReplaced());
    }

    @Test
    void failResultPrevented() {
        val check = startCheck();
        apply(new SkillCheckFact.ResultDetermined(SkillCheckResult.FAIL));

        apply(new SkillCheckFact.FailResultPrevented());

        assertTrue(check.failResultPrevented());
    }

    /* TitleFact */

    @Test
    void titleFactsAssignTheTitlesAndTheQuorumCard() {
        apply(new PresidentAssigned(FELIX_GAETA));
        apply(new AdmiralAssigned(GAIUS_BALTAR));
        apply(new CagAssigned(LOUANNE_KAT_KATRAINE));
        apply(new PresidentQuorumCardGained(QuorumCard.values()[0]));

        assertEquals(FELIX_GAETA, game.president());
        assertEquals(GAIUS_BALTAR, game.admiral());
        assertEquals(LOUANNE_KAT_KATRAINE, game.cag());
        assertEquals(List.of(QuorumCard.values()[0]), game.presidentHand().cards());
    }

    // The discard pile is a separate CardsDiscarded fact.
    @Test
    void presidentQuorumCardsLostLeaveTheHandOnly() {
        apply(new PresidentQuorumCardGained(QuorumCard.values()[0]));
        apply(new PresidentQuorumCardGained(QuorumCard.values()[1]));

        apply(new PresidentQuorumCardsLost(List.of(QuorumCard.values()[0])));

        assertEquals(List.of(QuorumCard.values()[1]), game.presidentHand().cards());
        assertEquals(List.of(), game.decks().quorum().discardedCards());
    }
}
