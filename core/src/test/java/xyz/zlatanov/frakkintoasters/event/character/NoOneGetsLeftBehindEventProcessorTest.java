package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON_ALT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class NoOneGetsLeftBehindEventProcessorTest extends EventTestHarness<NoOneGetsLeftBehindEvent> {

    @Test
    void oneFuelForTwoPopulation() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(KARL_HELO_AGATHON_ALT);
        assertEquals(one(new NoOneGetsLeftBehindEvent(2), new NoOpEvent(2)), NoOneGetsLeftBehindEventProcessor.offer(game));

        execute(new NoOneGetsLeftBehindEvent(2));

        assertEquals(0, player(2).miracleTokens());
        assertFollowup(all(lose(FUEL, 1), gain(POPULATION, 2)));
    }

    @Test
    void onceAGame() {
        player(1).character(KARL_HELO_AGATHON_ALT);
        ops.player(1).loseMiracleTokens();

        assertEquals(NONE, NoOneGetsLeftBehindEventProcessor.offer(game));
        assertInvalid(new NoOneGetsLeftBehindEvent(1));
    }
}
