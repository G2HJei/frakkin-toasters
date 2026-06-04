package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class DetermineNextPresidentEventProcessorTest extends EventTestHarness<DetermineNextPresidentEvent> {


    @BeforeEach
    void setUp() {
        setUpGame(2);
        player(1).character(GAIUS_BALTAR);
        player(2).character(LAURA_ROSLIN);
        execute(new DetermineNextPresidentEvent());
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(LAURA_ROSLIN, president());
    }

    @Test
    void shouldDistributeQuorumCard() {
        assertEquals(1, presidentHand.size());
    }
}