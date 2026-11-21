package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.AIRLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.RESOURCES_FOR_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class ResourcesForGalacticaEventProcessorTest extends EventTestHarness<ResourcesForGalacticaEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(RESOURCES_FOR_GALACTICA);
    }

    // The Mark VIIs start damaged (rule 11.3.1).
    @Test
    void aLocationAndTwoVipers() {
        ops.sandbox().damage(COMMAND);
        List<Integer> damaged = galacticaBoard.damagedShips().stream().map(Ship::id).limit(2).toList();

        execute(new ResourcesForGalacticaEvent(1, COMMAND, damaged));

        assertFalse(galacticaBoard.isDamaged(COMMAND));
        assertTrue(galacticaBoard.reserves().stream().map(Ship::id).toList().containsAll(damaged));
        assertEquals(List.of(RESOURCES_FOR_GALACTICA), quorumDeck.discardedCards());
    }

    @Test
    void onlyVipers() {
        List<Integer> damaged = galacticaBoard.damagedShips().stream().map(Ship::id).limit(1).toList();

        execute(new ResourcesForGalacticaEvent(1, null, damaged));

        assertTrue(galacticaBoard.reserves().stream().map(Ship::id).toList().containsAll(damaged));
    }

    @Test
    void aPegasusLocation() {
        ops.sandbox().damage(AIRLOCK);

        execute(new ResourcesForGalacticaEvent(1, AIRLOCK, List.of()));

        assertFalse(pegasusBoard.isDamaged(AIRLOCK));
    }

    @Test
    void notAnUndamagedLocation() {
        assertInvalid(new ResourcesForGalacticaEvent(1, COMMAND, List.of()));
    }

    @Test
    void notNothing() {
        assertInvalid(new ResourcesForGalacticaEvent(1, null, List.of()));
    }

    @Test
    void notThreeVipers() {
        List<Integer> damaged = galacticaBoard.damagedShips().stream().map(Ship::id).limit(3).toList();

        assertInvalid(new ResourcesForGalacticaEvent(1, null, damaged));
    }
}
