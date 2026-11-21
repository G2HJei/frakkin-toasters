package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class CommandEventProcessorTest extends EventTestHarness<CommandEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
    }

    @Test
    void shouldFollowupWithUpTo2ActivateViperDecisions() {
        execute(new CommandEvent(1));
        assertFollowup(
                all(
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1)),
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1))));
    }

    // "Unorthodox Plan": from elsewhere, even when the location is damaged.
    @Test
    void anywhereEvenIfDamaged() {
        moveTo(ADMIRALS_QUARTERS, SAUL_TIGH);
        ops.sandbox().damage(COMMAND);

        execute(new CommandEvent(1, true));

        assertFollowup(
                all(
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1)),
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1))));
    }

    @Test
    void otherwiseOnlyAtTheLocation() {
        moveTo(ADMIRALS_QUARTERS, SAUL_TIGH);

        assertInvalid(new CommandEvent(1, false));
    }

    // A damaged location has no action (rule 11.7.2.2).
    @Test
    void notWhenDamaged() {
        ops.sandbox().damage(COMMAND);

        assertInvalid(new CommandEvent(1));
    }

    // Without a viper to launch or an unmanned one in space, the decision would have no answer.
    @Test
    void shouldActivateNothingWithoutAnUnmannedViper() {
        viperAt(GALACTICA_SPACE_4_OCLOCK).pilot(KARA_STARBUCK_THRACE);
        galacticaBoard.reserves().stream()
                .filter(HumanFighter.class::isInstance)
                .toList()
                .forEach(galacticaBoard::removeFromReserves);

        execute(new CommandEvent(1));
    }
}
