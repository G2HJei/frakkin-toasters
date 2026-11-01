package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class DetermineNextPresidentEventProcessorTest extends EventTestHarness<DetermineNextPresidentEvent> {


    @BeforeEach
    void setUp() {
        setUpGame(2);
        player(1).character(GAIUS_BALTAR);
        player(2).character(LAURA_ROSLIN);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        execute(new DetermineNextPresidentEvent());
        assertEquals(LAURA_ROSLIN, president());
    }

    @Test
    void shouldDistributeQuorumCard() {
        execute(new DetermineNextPresidentEvent());
        assertEquals(1, presidentHand.size());
    }

    @Test
    void shouldIgnoreExcludedPlayer() {
        execute(new DetermineNextPresidentEvent(2));
        assertEquals(GAIUS_BALTAR, president());
    }
}