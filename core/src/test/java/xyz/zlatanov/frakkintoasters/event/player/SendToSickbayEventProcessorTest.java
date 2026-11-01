package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class SendToSickbayEventProcessorTest extends EventTestHarness<SendToSickbayEvent> {

    @Test
    void shouldSendTargetPlayerToSickbay() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);

        execute(new SendToSickbayEvent(1, 2));

        assertEquals(SICKBAY, locate(LEE_APOLLO_ADAMA));
    }
}
