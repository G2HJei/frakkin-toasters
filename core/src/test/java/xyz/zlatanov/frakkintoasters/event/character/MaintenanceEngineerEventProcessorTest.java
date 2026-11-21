package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.MAINTENANCE_ENGINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class MaintenanceEngineerEventProcessorTest extends EventTestHarness<MaintenanceEngineerEvent> {

    // On his turn, after his "Repair"
    @Test
    void anotherAction() {
        player(1).character(CHIEF_GALEN_TYROL);
        assertEquals(one(new MaintenanceEngineerEvent(1), new NoOpEvent(1)),
                MaintenanceEngineerEventProcessor.offer(game, player(1)));

        execute(new MaintenanceEngineerEvent(1));

        assertFollowup(new GrantEvent(1, ACTION, MAINTENANCE_ENGINEER));
        assertEquals(NONE, MaintenanceEngineerEventProcessor.offer(game, player(1))); // once a turn
    }

    @Test
    void notOnAnotherPlayersTurn() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(CHIEF_GALEN_TYROL);

        assertEquals(NONE, MaintenanceEngineerEventProcessor.offer(game, player(2)));
        assertInvalid(new MaintenanceEngineerEvent(2));
    }
}
