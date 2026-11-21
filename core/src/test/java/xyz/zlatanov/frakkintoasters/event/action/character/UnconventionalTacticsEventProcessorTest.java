package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;

class UnconventionalTacticsEventProcessorTest extends EventTestHarness<UnconventionalTacticsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, TOM_ZAREK);
    }

    @Test
    void tradesAPopulationForAnotherResource() {
        execute(new UnconventionalTacticsEvent(1, FUEL));

        assertFollowup(all(new LoseResourceEvent(POPULATION, 1), new GainResourceEvent(FUEL, 1)));
    }

    @Test
    void notForPopulation() {
        assertInvalid(new UnconventionalTacticsEvent(1, POPULATION));
    }
}
