package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.FinalFiveCardShown;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ResolveFinalFiveCardEventProcessorTest extends EventTestHarness<ResolveFinalFiveCardEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(3).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // The holder shows the card to everyone and keeps it.
    @Test
    void shouldShowTheCardToEveryone() {
        player(2).loyaltyCards().addOnTop(FINAL_FIVE_DAMAGE_GALACTICA_TWICE);

        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE, 1));

        assertTrue(journal.facts().contains(new FinalFiveCardShown(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE)));
        assertEquals(List.of(FINAL_FIVE_DAMAGE_GALACTICA_TWICE), player(2).loyaltyCards().cards());
        assertFollowup(all(new DamageBattlestarEvent(), new DamageBattlestarEvent()));
    }

    // The activation icons on the card: raiders, basestars, heavy raiders.
    @Test
    void shouldActivateTheCylonShips() {
        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_CYLON_SHIPS_ACTIVATE, 1));

        assertFollowup(all(new ActivateRaidersEvent(), new ActivateCylonBasestarsEvent(),
                new ActivateHeavyRaidersAndCenturionsEvent()));
    }

    @Test
    void shouldExecuteThePlayerWhoLooked() {
        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_THEY_ARE_EXECUTED, 1));

        assertFollowup(new ExecuteEvent(1));
    }

    @Test
    void shouldSendThePlayerWhoLookedToTheBrig() {
        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG, 1));

        assertFollowup(new SendToBrigEvent(1));
    }

    // Revealed Cylons aren't affected by Final Five cards.
    @Test
    void shouldNotAffectARevealedCylonWhoLooked() {
        revealCylon();

        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_THEY_ARE_EXECUTED, 1));
    }

    // The holder is executed, the card leaves the game with him, and every other player discards 2 random Skill Cards.
    @Test
    void shouldExecuteTheHolder() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON, FINAL_FIVE_YOU_ARE_EXECUTED);

        execute(new ResolveFinalFiveCardEvent(2, FINAL_FIVE_YOU_ARE_EXECUTED, 1));

        assertEquals(List.of(NOT_CYLON), player(2).loyaltyCards().cards());
        assertTrue(game.removedComponents().contains(FINAL_FIVE_YOU_ARE_EXECUTED));
        assertFollowup(all(new ExecuteEvent(2),
                new DiscardRandomSkillCardsEvent(1, 2),
                new DiscardRandomSkillCardsEvent(3, 2)));
    }

    @Test
    void shouldRejectACardThatIsNotFinalFive() {
        assertInvalid(new ResolveFinalFiveCardEvent(2, NOT_CYLON, 1));
    }

    @Test
    void shouldRejectNoCard() {
        assertInvalid(new ResolveFinalFiveCardEvent(2, null, 1));
    }

    @Test
    void shouldRejectANonPlayer() {
        assertInvalid(new ResolveFinalFiveCardEvent(9, FINAL_FIVE_THEY_ARE_EXECUTED, 1));
    }
}
