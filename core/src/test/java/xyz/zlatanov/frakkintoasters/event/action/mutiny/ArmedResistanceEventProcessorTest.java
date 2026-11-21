package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.ARMED_RESISTANCE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ArmedResistanceEventProcessorTest extends EventTestHarness<ArmedResistanceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(ARMED_RESISTANCE);
    }

    @Test
    void theAdmiralGoesToSickbayThenThePlayerLooksAtTheTopCrisisCard() {
        execute(new ArmedResistanceEvent(1));

        assertFollowup(all(new SendToSickbayEvent(2), new LookAtTopCardEvent(1, CRISIS),
                new DiscardPlayedMutinyCardEvent(ARMED_RESISTANCE)));
        assertTrue(player(1).mutinyCards().isEmpty());
    }

    @Test
    void inTheBrigToo() {
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new ArmedResistanceEvent(1));

        assertFollowup(all(new SendToSickbayEvent(2), new LookAtTopCardEvent(1, CRISIS),
                new DiscardPlayedMutinyCardEvent(ARMED_RESISTANCE)));
    }

    @Test
    void notWithoutTheCard() {
        player(1).mutinyCards().remove(ARMED_RESISTANCE);

        assertInvalid(new ArmedResistanceEvent(1));
    }

    @Test
    void notByARevealedCylon() {
        revealCylon();

        assertInvalid(new ArmedResistanceEvent(1));
        assertEquals(1, player(1).mutinyCards().size());
    }
}
