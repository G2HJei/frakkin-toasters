package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FIGHTING_BLIND;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;

class DontTrustAnyoneEventProcessorTest extends EventTestHarness<DontTrustAnyoneEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(DANNA_BIERS);
        moveTo(RESURRECTION_SHIP, DANNA_BIERS);
    }

    @Test
    void drawsTwoSuperCrisisCardsForHerMiracleToken() {
        superCrisisDeck.nextCard(THE_FARM, FIGHTING_BLIND);

        execute(new DontTrustAnyoneEvent(1));

        assertEquals(2, player(1).superCrisisCards().size());
        assertEquals(0, player(1).miracleTokens());
    }

    @Test
    void notWithoutAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new DontTrustAnyoneEvent(1));
    }
}
