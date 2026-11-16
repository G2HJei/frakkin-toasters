package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

class GasCloudEventProcessorTest extends EventTestHarness<GasCloudEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @Test
    void theAdmiralLooksAtTheTopThreeCards() {
        crisisDeck.nextCard(RAIDERS_INBOUND, RETURN_TO_DUTY, DETENTE);

        execute(new GasCloudEvent(2));

        assertFollowup(new GasCloudPlaceEvent(2, List.of(RAIDERS_INBOUND, RETURN_TO_DUTY, DETENTE)));
    }
}
