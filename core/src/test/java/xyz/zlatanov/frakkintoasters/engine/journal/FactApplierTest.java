package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.InterruptFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.*;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Applies each kind of fact to a prepared state and checks the result.
 */
class FactApplierTest {

    Game      game              = Game.builder().build();
    SkillCard card              = new SkillCard(3, CONSOLIDATE_POWER);
    SkillCard other             = new SkillCard(1, CONSOLIDATE_POWER);
    SkillCard strategicPlanning = new SkillCard(3, STRATEGIC_PLANNING);
    SkillCard majorVictory      = new SkillCard(1, MAJOR_VICTORY);

    static final SkillCheckType DETECTOR_SABOTAGE_CHECK = SkillCheckType.DETECTOR_SABOTAGE;

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

        assertEquals(Set.of(ARMORY), game.boards().galactica().damagedLocations());
        assertEquals(Set.of(PEGASUS_CIC), game.boards().pegasus().damagedLocations());
    }

    @Test
    void locationRepairedClearsTheDamage() {
        apply(new LocationDamaged(ARMORY));
        apply(new LocationDamaged(PEGASUS_CIC));

        apply(new LocationRepaired(ARMORY));
        apply(new LocationRepaired(PEGASUS_CIC));

        assertEquals(Set.of(), game.boards().galactica().damagedLocations());
        assertEquals(Set.of(), game.boards().pegasus().damagedLocations());
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

    @Test
    void cardsPulledLeaveTheDrawPileWithoutGoingAnywhere() {
        val deck = game.decks().<SkillCard>deck(DeckId.POLITICS);
        val pulled = deck.cards().get(2);
        val size = deck.size();
        val copies = deck.cards().stream().filter(pulled::equals).count();

        apply(new DeckFact.CardsPulled(DeckId.POLITICS, List.of(pulled)));

        assertEquals(size - 1, deck.size());
        assertEquals(copies - 1, deck.cards().stream().filter(pulled::equals).count());
        assertEquals(List.of(), deck.discardedCards());
    }

    /* GameFact */

    @Test
    void gameStartedChangesNothing() {
        val before = GameSnapshot.of(game);

        apply(new GameStarted(3, game.objective()));

        assertEquals(before, GameSnapshot.of(game));
    }

    @Test
    void dieRollFactsStartRollAndModifyTheRoll() {
        apply(new DieRollStarted());
        assertNull(game.turn().dieRoll().value());

        apply(new DieRollCardPlayed(STRATEGIC_PLANNING, 2));
        apply(new DieRolled(5));
        apply(new DieRollCardPlayed(CALCULATIONS, 1));
        assertEquals(5, game.turn().dieRoll().value());
        assertEquals(List.of(STRATEGIC_PLANNING, CALCULATIONS), game.turn().dieRoll().cards());
        assertEquals(8, game.turn().dieRoll().result());

        apply(new DieRolled(1)); // a reroll keeps the cards played on the roll
        assertEquals(4, game.turn().dieRoll().result());

        apply(new DieRollStarted());
        assertEquals(List.of(), game.turn().dieRoll().cards());
    }

    @Test
    void dieRollResultIsBetween1And8() {
        apply(new DieRollStarted());
        apply(new DieRolled(7));
        apply(new DieRollCardPlayed(STRATEGIC_PLANNING, 2));
        assertEquals(8, game.turn().dieRoll().result());

        apply(new DieRollStarted());
        apply(new DieRolled(2));
        apply(new DieRollCardPlayed(EVASIVE_MANOEUVRES, -2));
        apply(new DieRollCardPlayed(CALCULATIONS, -1));
        assertEquals(1, game.turn().dieRoll().result());
    }

    @Test
    void dieRolledNeedsAStartedRoll() {
        assertThrows(FrakCallTheAdmiralException.class, () -> apply(new DieRolled(5)));
    }

    @Test
    void turnStartedForgetsTheDieRollAndTheInterruptCardsOfTheTurn() {
        apply(new DieRollStarted());
        apply(new RaidersWillMiss(GALACTICA_SPACE_4_OCLOCK, 4));
        val played = new PlayedInterruptCard(2, majorVictory, null);
        apply(new InterruptWindowOpened(new AfterBasestarOrCenturionDestroyed()));
        apply(new InterruptCardPlayed(played));
        apply(new InterruptCardResolved(played));
        apply(new InterruptWindowClosed());

        apply(new TurnStarted(2));

        assertNull(game.turn().dieRoll());
        assertEquals(List.of(), game.turn().resolvedInterruptCards());
        assertEquals(Map.of(), game.turn().raidersToMiss());
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

        apply(new TurnEndedEarly());
        assertTrue(game.turn().endedEarly());
        apply(new TurnStarted(5));
        assertFalse(game.turn().endedEarly()); // a new turn is under way

        apply(new CrisisCardPlayed(CrisisCard.RAIDERS_INBOUND));
        apply(new CrisisCardPlayed(CrisisCard.LABOR_DISPUTE));
        assertEquals(List.of(CrisisCard.RAIDERS_INBOUND, CrisisCard.LABOR_DISPUTE), game.turn().crisisCards());

        apply(new CrisisCardsCleared(List.of(CrisisCard.RAIDERS_INBOUND)));
        assertEquals(List.of(CrisisCard.LABOR_DISPUTE), game.turn().crisisCards());
        assertEquals(List.of(), game.decks().crisis().discardedCards()); // the discard pile is a separate CardsDiscarded
    }

    @Test
    void crisisCardKeptInPlayIsRemembered() {
        assertEquals(List.of(), game.crisisCardsInPlay());

        apply(new CrisisCardKeptInPlay(DETECTOR_SABOTAGE_CHECK));

        assertEquals(List.of(DETECTOR_SABOTAGE_CHECK), game.crisisCardsInPlay());
    }

    @Test
    void returnToCrisisStepChangedSetsTheFlagOfTheTurnAndANewTurnClearsIt() {
        assertFalse(game.turn().returnsToCrisisStep());

        apply(new ReturnToCrisisStepChanged(true));
        assertTrue(game.turn().returnsToCrisisStep());

        apply(new ReturnToCrisisStepChanged(false));
        assertFalse(game.turn().returnsToCrisisStep());

        apply(new ReturnToCrisisStepChanged(true));
        apply(new TurnStarted(2));
        assertFalse(game.turn().returnsToCrisisStep());
    }

    @Test
    void nukeDiscardedTakesAwayOneToken() {
        apply(new NukeDiscarded());

        assertEquals(1, game.nukes());
    }

    @Test
    void nukeGainedAddsOneToken() {
        apply(new NukeGained());

        assertEquals(3, game.nukes());
    }

    // The card itself moves with CardsLost and CardsDiscarded: the turn only remembers that it was played.
    @Test
    void skillCardPlayedIsKeptByTheTurn() {
        apply(new SkillCardPlayed(1, new SkillCard(1, EXECUTIVE_ORDER)));
        apply(new SkillCardPlayed(2, new SkillCard(6, BUILD_NUKE)));

        assertEquals(List.of(EXECUTIVE_ORDER, BUILD_NUKE), game.turn().playedSkillCards());

        apply(new TurnStarted(2));
        assertEquals(List.of(), game.turn().playedSkillCards());
    }

    @Test
    void cylonShipsHeldForTheRestOfTheTurn() {
        apply(new CylonShipsHeld());
        assertTrue(game.turn().cylonShipsHeld());

        apply(new TurnStarted(2));
        assertFalse(game.turn().cylonShipsHeld());
    }

    @Test
    void aLossReductionLastsUntilItIsUsedOrTheTurnEnds() {
        apply(new LossReductionAdded(FOOD));
        apply(new LossReductionAdded(MORALE));
        assertEquals(Set.of(FOOD, MORALE), game.turn().lossReductions());

        apply(new LossReduced(FOOD));
        assertEquals(Set.of(MORALE), game.turn().lossReductions());

        apply(new TurnStarted(2));
        assertEquals(Set.of(), game.turn().lossReductions());
    }

    @Test
    void gameEndedSetsTheOutcome() {
        assertEquals(Optional.empty(), game.outcome());

        apply(new GameEnded(CYLONS, List.of()));

        assertEquals(Optional.of(CYLONS), game.outcome());
    }

    /* InterruptFact */

    @Test
    void interruptCardsGoIntoTheOpenWindowAndLeaveItWhenReturnedOrResolved() {
        val returned = new PlayedInterruptCard(1, strategicPlanning, null);
        val resolved = new PlayedInterruptCard(2, strategicPlanning, null);

        apply(new InterruptWindowOpened(new BeforeDieRoll()));
        apply(new InterruptCardPlayed(returned));
        apply(new InterruptCardPlayed(resolved));
        assertEquals(Optional.of(new BeforeDieRoll()), game.turn().interruptWindow());
        assertEquals(List.of(returned, resolved), game.turn().windowCards());

        apply(new InterruptCardReturned(returned));
        apply(new InterruptCardResolved(resolved));
        assertEquals(List.of(), game.turn().windowCards());
        assertEquals(List.of(STRATEGIC_PLANNING), game.turn().resolvedInterruptCards());
        assertEquals(0, game.player(1).skillCards().size()); // the hand and the discard pile are separate facts

        apply(new InterruptWindowClosed());
        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void anInterruptWindowClosesOnlyOnceItsCardsAreGone() {
        apply(new InterruptWindowOpened(new BeforeDieRoll()));
        apply(new InterruptCardPlayed(new PlayedInterruptCard(1, strategicPlanning, null)));

        assertThrows(FrakCallTheAdmiralException.class, () -> apply(new InterruptWindowClosed()));
        assertThrows(FrakCallTheAdmiralException.class, () -> apply(new InterruptWindowOpened(new AfterDieRoll())));
        assertThrows(FrakCallTheAdmiralException.class,
                () -> apply(new InterruptCardResolved(new PlayedInterruptCard(2, strategicPlanning, null))));
    }

    @Test
    void raidersMissUntilTheCountRunsOut() {
        apply(new RaidersWillMiss(GALACTICA_SPACE_4_OCLOCK, 2));
        apply(new RaidersWillMiss(GALACTICA_SPACE_6_OCLOCK, 1));

        apply(new RaiderMissed(GALACTICA_SPACE_4_OCLOCK));
        apply(new RaiderMissed(GALACTICA_SPACE_6_OCLOCK));
        assertEquals(Map.of(GALACTICA_SPACE_4_OCLOCK, 1), game.turn().raidersToMiss());

        apply(new RaiderMissed(GALACTICA_SPACE_4_OCLOCK));
        assertEquals(Map.of(), game.turn().raidersToMiss());
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

    // Only the player who looked knows the card, and the card stays where it is.
    @Test
    void loyaltyCardSeenIsKnownToTheViewerOnly() {
        apply(new CardsGained(2, PlayerPile.LOYALTY, List.of(LoyaltyCard.NOT_CYLON)));

        apply(new LoyaltyCardSeen(1, 2, LoyaltyCard.NOT_CYLON));

        assertEquals(List.of(new SeenLoyaltyCard(2, LoyaltyCard.NOT_CYLON)), game.player(1).seenLoyaltyCards());
        assertEquals(List.of(), game.player(2).seenLoyaltyCards());
        assertEquals(List.of(LoyaltyCard.NOT_CYLON), game.player(2).loyaltyCards().cards());
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

        assertEquals(Set.of(2005), game.player(2).revealedCivilianShipIds());
    }

    @Test
    void civilianShipForgottenForgetsTheShipForThePlayerOnly() {
        apply(new CivilianShipRevealed(2, 2005));
        apply(new CivilianShipRevealed(3, 2005));

        apply(new PlayerFact.CivilianShipForgotten(2, 2005));

        assertEquals(Set.of(), game.player(2).revealedCivilianShipIds());
        assertEquals(Set.of(2005), game.player(3).revealedCivilianShipIds());
    }

    @Test
    void ownTurnStartedCountsATurnForThePlayerOnly() {
        apply(new OwnTurnStarted(2));
        apply(new OwnTurnStarted(2));

        assertEquals(2, game.player(2).turns());
        assertEquals(0, game.player(1).turns());
        assertEquals(0, game.turn().number());
    }

    // An executed character leaves the game: his player has none, and the turns count again for the next one.
    @Test
    void characterRemovedTakesTheCharacterOutOfTheGame() {
        apply(new CharacterSelected(2, FELIX_GAETA));
        apply(new OwnTurnStarted(2));

        apply(new CharacterRemoved(2, FELIX_GAETA));

        assertNull(game.player(2).character());
        assertEquals(0, game.player(2).turns());
        assertTrue(game.removedComponents().contains(FELIX_GAETA));
    }

    @Test
    void miracleTokenLostSpendsTheToken() {
        apply(new MiracleTokenLost(1));

        assertFalse(game.player(1).hasMiracleToken());
    }

    @Test
    void loyaltyCardRevealedTurnsTheCardFaceup() {
        apply(new CardsGained(1, PlayerPile.LOYALTY, List.of(LoyaltyCard.CYLON_DAMAGE_GALACTICA, LoyaltyCard.NOT_CYLON)));

        apply(new LoyaltyCardRevealed(1, LoyaltyCard.CYLON_DAMAGE_GALACTICA));

        assertEquals(List.of(LoyaltyCard.NOT_CYLON), game.player(1).loyaltyCards().cards());
        assertEquals(List.of(LoyaltyCard.CYLON_DAMAGE_GALACTICA), game.player(1).loyaltyCards().revealedCards());
        assertTrue(game.player(1).isRevealedCylon());
    }

    @Test
    void infiltrationEndedReturnsTheCylonLeader() {
        apply(new CharacterSelected(1, AARON_DORAL));
        apply(new InfiltrationStarted(1));

        apply(new InfiltrationEnded(1));

        assertFalse(game.player(1).isInfiltrating());
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

    @Test
    void abilityResolved() {
        val check = startCheck();

        apply(new SkillCheckFact.AbilityResolved(SkillCardType.IRON_WILL));

        assertEquals(List.of(SkillCardType.IRON_WILL), check.resolvedAbilities());
    }

    @Test
    void skillCheckEndedClearsTheCheck() {
        startCheck();

        apply(new SkillCheckFact.SkillCheckEnded());

        assertNull(game.activeSkillCheck());
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
