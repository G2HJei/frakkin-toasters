package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class DischargeOfAFirearmEventProcessorTest extends EventTestHarness<DischargeOfAFirearmEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CALLANDRA_CALLY_TYROL);
        player(2).character(KARA_STARBUCK_THRACE);
        player(3).character(LAURA_ROSLIN);
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void executesAnotherCharacterAtHerLocation() {
        execute(new DischargeOfAFirearmEvent(1, 2));

        assertFollowup(new ExecuteEvent(2));
    }

    @Test
    void notAtAnotherLocation() {
        assertInvalid(new DischargeOfAFirearmEvent(1, 3));
    }

    @Test
    void notHerself() {
        assertInvalid(new DischargeOfAFirearmEvent(1, 1));
    }
}
