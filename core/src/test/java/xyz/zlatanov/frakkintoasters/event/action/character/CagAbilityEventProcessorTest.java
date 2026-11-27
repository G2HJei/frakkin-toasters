package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class CagAbilityEventProcessorTest extends EventTestHarness<CagAbilityEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
    }

    @Test
    void activatesUpToSixUnmannedVipers() {
        viperAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new CagAbilityEvent(1));

        assertFollowup(new UnmannedViperRoundEvent(1, 6));
    }

    @Test
    void notWithoutAViperToActivate() {
        // every viper and assault raptor leaves the "Reserves"
        galacticaBoard.reserves().stream()
                .filter(xyz.zlatanov.frakkintoasters.state.view.HumanFighterView.class::isInstance)
                .forEach(galacticaBoard::removeFromReserves);

        assertInvalid(new CagAbilityEvent(1));
    }
}
