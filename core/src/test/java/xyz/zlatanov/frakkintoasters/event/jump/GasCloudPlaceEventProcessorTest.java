package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

class GasCloudPlaceEventProcessorTest extends EventTestHarness<GasCloudPlaceEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    // "The Admiral may look at the top 3 cards of the Crisis deck, then place them on the top or bottom of the deck in any
    // order." One card at a time: any of the cards left, on either end.
    @Test
    void theAdmiralPlacesTheCardsOneAtATime() {
        execute(new GasCloudPlaceEvent(2, List.of(RAIDERS_INBOUND, RETURN_TO_DUTY)));

        assertFollowup(one(
                all(single(new PlaceCrisisCardOnTopEvent(2, RAIDERS_INBOUND)),
                        single(new GasCloudPlaceEvent(2, List.of(RETURN_TO_DUTY)))),
                all(single(new PlaceCrisisCardOnBottomEvent(2, RAIDERS_INBOUND)),
                        single(new GasCloudPlaceEvent(2, List.of(RETURN_TO_DUTY)))),
                all(single(new PlaceCrisisCardOnTopEvent(2, RETURN_TO_DUTY)),
                        single(new GasCloudPlaceEvent(2, List.of(RAIDERS_INBOUND)))),
                all(single(new PlaceCrisisCardOnBottomEvent(2, RETURN_TO_DUTY)),
                        single(new GasCloudPlaceEvent(2, List.of(RAIDERS_INBOUND))))));
    }

    @Test
    void nothingIsLeftToPlace() {
        execute(new GasCloudPlaceEvent(2, List.of()));
    }
}
