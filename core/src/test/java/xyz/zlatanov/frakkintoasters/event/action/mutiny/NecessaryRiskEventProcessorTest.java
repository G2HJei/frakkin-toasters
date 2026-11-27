package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.NECESSARY_RISK;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class NecessaryRiskEventProcessorTest extends EventTestHarness<NecessaryRiskEvent> {

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
        player(1).mutinyCards().addOnTop(NECESSARY_RISK);
    }

    @Test
    void foodThenCylonShipsInTheChosenArea() {
        execute(new NecessaryRiskEvent(1, GALACTICA_SPACE_5_OCLOCK));

        assertFollowup(all(gain(FOOD, 1), new PlaceShipsEvent(1, 3, 0, GALACTICA_SPACE_5_OCLOCK),
                new RemoveMutinyCardFromGameEvent(NECESSARY_RISK)));
    }

    @Test
    void notOutsideASpaceArea() {
        assertInvalid(new NecessaryRiskEvent(1, COMMAND));
    }
}
