package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class DetermineNextPresidentEventProcessorTest extends EventTestHarness<DetermineNextPresidentEvent> {


    @BeforeEach
    void setUp() {
        setUpGame(Game.builder(2).build());
        selectCharacter(1, GAIUS_BALTAR);
        selectCharacter(2, LAURA_ROSLIN);

        executeAndAssertNoFollowup(new DetermineNextPresidentEvent());
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