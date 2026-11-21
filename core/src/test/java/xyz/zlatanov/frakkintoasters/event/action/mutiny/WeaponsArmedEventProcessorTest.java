package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class WeaponsArmedEventProcessorTest extends EventTestHarness<WeaponsArmedEvent> {

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
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);
    }

    @Test
    void aRaptorBecomesAnAssaultRaptorThenTheBasestarsLaunch() {
        val raptors = reserves(Raptor.class);
        val assaultRaptors = reserves(AssaultRaptor.class);

        execute(new WeaponsArmedEvent(1));

        assertEquals(raptors - 1, reserves(Raptor.class));
        assertEquals(assaultRaptors + 1, reserves(AssaultRaptor.class));
        assertFollowup(all(LaunchRaidersEvent.fromEachBasestar(2), new DiscardPlayedMutinyCardEvent(WEAPONS_ARMED)));
    }

    @Test
    void notWithoutARaptorInTheReserves() {
        while (galacticaBoard.removeFromReserves(Raptor.class).isPresent()) {
            // empties the "Reserves" of raptors
        }

        assertInvalid(new WeaponsArmedEvent(1));
    }

    private long reserves(Class<?> type) {
        return galacticaBoard.reserves().stream().filter(type::isInstance).count();
    }
}
