package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

// A Personal Goal is revealed as an action while its condition holds (rule 5.3.1), and up to 6 distance its player draws
// a new Loyalty Card (rule 5.3.1.1).
class RevealPersonalGoalEventProcessorTest extends EventTestHarness<RevealPersonalGoalEvent> {

    private static final SkillCard FIVE = new SkillCard(5, EXECUTIVE_ORDER);
    private static final SkillCard NINE = new SkillCard(9, CONSOLIDATE_POWER);

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        loyaltyNotCylonDeck.addOnTop(NOT_CYLON);
    }

    @Test
    void shouldRevealTheCardAndDrawANewOneUpToSixDistance() {
        holds(NOT_CYLON_SELF_DESTRUCTION);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1)));
        assertTrue(Actions.canTakeOtherAction(game, player(1)));

        execute(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of()));

        assertEquals(List.of(NOT_CYLON_SELF_DESTRUCTION), player(1).loyaltyCards().revealedCards());
        assertFalse(player(1).loyaltyCards().cards().contains(NOT_CYLON_SELF_DESTRUCTION));
        assertTrue(loyaltyDeck.cards().contains(NOT_CYLON)); // shuffled into the Loyalty deck
        assertFollowup(new DealLoyaltyCardsEvent(1, 1, false));
    }

    @Test
    void shouldNotDrawFromSevenDistanceOn() {
        holds(NOT_CYLON_SELF_DESTRUCTION);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        ops.galactica().reachDestination(DESOLATE_MOON); // 3
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(ICY_MOON); // 1

        execute(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of()));

        assertTrue(player(1).loyaltyCards().revealedCards().contains(NOT_CYLON_SELF_DESTRUCTION));
        assertFalse(loyaltyDeck.cards().contains(NOT_CYLON));
    }

    @Test
    void selfDestructionNeedsTheBrigOrSickbay() {
        holds(NOT_CYLON_SELF_DESTRUCTION);

        assertFalse(RevealPersonalGoalEventProcessor.canReveal(game, player(1)));
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of()));
    }

    @Test
    void acquirePowerNeedsTwoTitles() {
        holds(NOT_CYLON_ACQUIRE_POWER);
        assertFalse(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_ACQUIRE_POWER));

        admiral(KARA_STARBUCK_THRACE);

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_ACQUIRE_POWER));
        execute(new RevealPersonalGoalEvent(1, NOT_CYLON_ACQUIRE_POWER, List.of()));
        assertTrue(player(1).loyaltyCards().revealedCards().contains(NOT_CYLON_ACQUIRE_POWER));
        assertFollowup(new DealLoyaltyCardsEvent(1, 1, false));
    }

    @Test
    void devastationNeedsTheNukesGone() {
        holds(NOT_CYLON_DEVASTATION);
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_DEVASTATION, List.of()));

        while (game.nukes() > 0) {
            ops.discardNuke();
        }

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_DEVASTATION));
    }

    @Test
    void politicalIntrigueNeedsThePresidentInTheBrig() {
        holds(NOT_CYLON_POLITICAL_INTRIGUE);
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_POLITICAL_INTRIGUE, List.of()));

        moveTo(BRIG, LAURA_ROSLIN);

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_POLITICAL_INTRIGUE));
    }

    // Damaged and destroyed vipers count together, Mark VIIs and assault raptors as vipers (rules 11.3.1.1, 11.3.2.2).
    @Test
    void sacrificeNeedsSixVipersDamagedOrDestroyed() {
        holds(NOT_CYLON_SACRIFICE);
        while (galacticaBoard.removeFromDamagedShips(ViperMarkVII.class).isPresent()) {
            // a clean damaged vipers box
        }
        for (var i = 0; i < 3; i++) {
            ops.ship(viperAt(GALACTICA_SPACE_3_OCLOCK).id()).damage();
        }
        ops.ship(viperAt(GALACTICA_SPACE_5_OCLOCK).id()).destroy();
        ops.ship(assaultRaptorAt(GALACTICA_SPACE_5_OCLOCK).id()).destroy();
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SACRIFICE, List.of()));

        ops.ship(viperAt(GALACTICA_SPACE_7_OCLOCK).id()).destroy();

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_SACRIFICE));
    }

    @Test
    void standAndFightNeedsTenRaiders() {
        holds(NOT_CYLON_STAND_AND_FIGHT);
        for (var i = 0; i < 9; i++) {
            raiderAt(GALACTICA_SPACE_3_OCLOCK);
        }
        heavyRaiderAt(GALACTICA_SPACE_3_OCLOCK); // not a raider
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_STAND_AND_FIGHT, List.of()));

        raiderAt(GALACTICA_SPACE_9_OCLOCK);

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_STAND_AND_FIGHT));
    }

    @Test
    void useCautionNeedsADestinationOfDistanceOne() {
        holds(NOT_CYLON_USE_CAUTION);
        ops.galactica().reachDestination(DEEP_SPACE);
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_USE_CAUTION, List.of()));

        ops.galactica().reachDestination(ICY_MOON);

        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_USE_CAUTION));
    }

    // "Selfish" discards Skill Cards worth 20 strength to reveal it.
    @Test
    void selfishDiscardsTwentyStrength() {
        holds(NOT_CYLON_SELFISH);
        val five = new SkillCard(5, EXECUTIVE_ORDER);
        val six = new SkillCard(6, EXECUTIVE_ORDER);
        val nine = new SkillCard(9, CONSOLIDATE_POWER);
        val one = new SkillCard(1, EVASIVE_MANOEUVRES);
        player(1).gainSkillCards(five, six, nine, one);
        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_SELFISH));

        execute(new RevealPersonalGoalEvent(1, NOT_CYLON_SELFISH, List.of(five, six, nine)));

        assertSkillCards(1, one);
        assertTrue(player(1).loyaltyCards().revealedCards().contains(NOT_CYLON_SELFISH));
        assertFollowup(new DealLoyaltyCardsEvent(1, 1, false));
    }

    @Test
    void selfishRejectsTooLittleStrength() {
        holds(NOT_CYLON_SELFISH);
        player(1).gainSkillCards(FIVE, NINE);
        assertFalse(RevealPersonalGoalEventProcessor.canReveal(game, player(1), NOT_CYLON_SELFISH));

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELFISH, List.of(FIVE, NINE)));
    }

    // The hand counts copies: he holds one "Consolidate Power" 9.
    @Test
    void selfishRejectsCardsNotHeld() {
        holds(NOT_CYLON_SELFISH);
        player(1).gainSkillCards(FIVE, NINE);

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELFISH, List.of(NINE, NINE, NINE)));
    }

    @Test
    void selfishRejectsNoDiscards() {
        holds(NOT_CYLON_SELFISH);
        player(1).gainSkillCards(NINE, NINE, NINE);

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELFISH, null));
    }

    @Test
    void shouldRejectDiscardsForTheOtherCards() {
        holds(NOT_CYLON_SELF_DESTRUCTION);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        val card = new SkillCard(5, EXECUTIVE_ORDER);
        player(1).gainSkillCards(card);

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of(card)));
    }

    @Test
    void shouldRejectACardHeDoesNotHold() {
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of()));
    }

    @Test
    void shouldRejectACardThatIsNoPersonalGoal() {
        holds(NOT_CYLON);

        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON, List.of()));
    }

    @Test
    void shouldRejectNoCard() {
        assertInvalid(new RevealPersonalGoalEvent(1, null, List.of()));
    }

    // A hidden Cylon is a human player until he reveals, a revealed Cylon takes no action but his location's (rule
    // 6.1.3.3).
    @Test
    void aHiddenCylonMayRevealButARevealedOneMayNot() {
        holds(CYLON_SEND_TO_BRIG, NOT_CYLON_SELF_DESTRUCTION);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        assertTrue(RevealPersonalGoalEventProcessor.canReveal(game, player(1)));

        player(1).loyaltyCards().reveal(CYLON_SEND_TO_BRIG);

        assertFalse(RevealPersonalGoalEventProcessor.canReveal(game, player(1)));
        assertInvalid(new RevealPersonalGoalEvent(1, NOT_CYLON_SELF_DESTRUCTION, List.of()));
    }

    private void holds(LoyaltyCard... cards) {
        player(1).loyaltyCards().addOnTop(cards);
    }
}
