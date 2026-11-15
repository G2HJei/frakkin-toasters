package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class AttackViperResultEventProcessorTest extends EventTestHarness<AttackViperResultEvent> {

    enum Outcome { NONE, DAMAGED, DESTROYED }

    @ParameterizedTest
    @CsvSource({
            "VIPER, 4, NONE", "VIPER, 5, DAMAGED", "VIPER, 7, DAMAGED", "VIPER, 8, DESTROYED",
            "MARK_VII, 5, NONE", "MARK_VII, 6, DAMAGED", "MARK_VII, 8, DESTROYED",
            "ASSAULT_RAPTOR, 6, NONE", "ASSAULT_RAPTOR, 7, DESTROYED"})
    void shouldResolveTheAttackTable(String fighterType, int roll, Outcome outcome) {
        val fighter = switch (fighterType) {
            case "VIPER" -> viperAt(GALACTICA_SPACE_4_OCLOCK);
            case "MARK_VII" -> viperMarkVIIAt(GALACTICA_SPACE_4_OCLOCK);
            default -> assaultRaptorAt(GALACTICA_SPACE_4_OCLOCK);
        };
        rolled(roll);

        execute(new AttackViperResultEvent(fighter.id()));

        assertEquals(outcome == Outcome.NONE, inSpace(fighter));
        assertEquals(outcome == Outcome.DAMAGED, galacticaBoard.damagedShips().contains(fighter));
        assertEquals(outcome == Outcome.DESTROYED, game.removedComponents().contains(fighter));
    }

    // A pilot whose viper is damaged goes to "Sickbay" (rule 11.2.2).
    @Test
    void shouldSendThePilotToSickbay() {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(1).pilot(viper.id());
        rolled(5);

        execute(new AttackViperResultEvent(viper.id()));

        assertEquals(Optional.of(SICKBAY), game.locate(KARA_STARBUCK_THRACE));
        assertTrue(galacticaBoard.damagedShips().contains(viper));
    }

    private boolean inSpace(Ship ship) {
        return galacticaBoard.shipsIn(GALACTICA_SPACE_4_OCLOCK).contains(ship);
    }
}
