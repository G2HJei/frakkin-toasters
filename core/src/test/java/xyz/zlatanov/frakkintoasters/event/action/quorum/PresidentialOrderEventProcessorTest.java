package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeCagEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.Title.*;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PRESIDENTIAL_ORDER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class PresidentialOrderEventProcessorTest extends EventTestHarness<PresidentialOrderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        presidentHand.addOnTop(PRESIDENTIAL_ORDER);
    }

    @Test
    void anotherPlayerBecomesAdmiral() {
        execute(new PresidentialOrderEvent(1, 3, ADMIRAL));

        assertFollowup(all(new MakeAdmiralEvent(KARA_STARBUCK_THRACE),
                new DiscardPlayedQuorumCardEvent(PRESIDENTIAL_ORDER)));
    }

    @Test
    void thePresidentMayTakeTheCagTitle() {
        execute(new PresidentialOrderEvent(1, 1, CAG));

        assertFollowup(all(new MakeCagEvent(LAURA_ROSLIN), new DiscardPlayedQuorumCardEvent(PRESIDENTIAL_ORDER)));
    }

    @Test
    void notTheTitleHeAlreadyHolds() {
        assertInvalid(new PresidentialOrderEvent(1, 2, ADMIRAL));
    }

    @Test
    void notThePresidency() {
        assertInvalid(new PresidentialOrderEvent(1, 3, PRESIDENT));
    }
}
