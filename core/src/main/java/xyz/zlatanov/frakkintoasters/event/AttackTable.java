package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.AttackCylonShipResultEventProcessor.*;
import static xyz.zlatanov.frakkintoasters.event.AttackGalacticaResultEventProcessor.LOWEST_ROLL_FOR_A_BASESTAR;
import static xyz.zlatanov.frakkintoasters.event.AttackGalacticaResultEventProcessor.LOWEST_ROLL_FOR_A_RAIDER;
import static xyz.zlatanov.frakkintoasters.event.AttackTable.Effect.*;
import static xyz.zlatanov.frakkintoasters.event.AttackViperResultEventProcessor.*;
import static xyz.zlatanov.frakkintoasters.event.location.galactica.ArmoryResultEventProcessor.LOWEST_ROLL_TO_DESTROY;

/**
 * The Attack Table (rule 11.6) as the attack results apply it, for the table's cheat sheet: each row is read from the
 * thresholds of the processors, so the sheet can't say other than the game does. Before card and ability modifiers.
 */
@NoArgsConstructor(access = PRIVATE)
public final class AttackTable {

    public enum Effect {
        DESTROYED, DAMAGED, HIT
    }

    public record Row(String attacker, String target, int from, int to, Effect effect) {
    }

    public static List<Row> rows() {
        return List.of(
                new Row("VIPER", "RAIDER", LOWEST_ROLL_TO_DESTROY_A_RAIDER, 8, DESTROYED),
                new Row("VIPER", "HEAVY_RAIDER", LOWEST_ROLL_TO_DESTROY_A_HEAVY_RAIDER, 8, DESTROYED),
                new Row("VIPER", "BASESTAR", LOWEST_ROLL_TO_DAMAGE_WITH_A_VIPER, 8, DAMAGED),
                new Row("ASSAULT_RAPTOR", "RAIDER", LOWEST_ROLL_TO_DESTROY_A_RAIDER, 8, DESTROYED),
                new Row("ASSAULT_RAPTOR", "HEAVY_RAIDER", LOWEST_ROLL_TO_DESTROY_A_HEAVY_RAIDER, 8, DESTROYED),
                new Row("ASSAULT_RAPTOR", "BASESTAR", LOWEST_ROLL_TO_DAMAGE_WITH_A_RAPTOR, 8, DAMAGED),
                new Row("GALACTICA", "RAIDER", LOWEST_ROLL_TO_DESTROY_A_RAIDER, 8, DESTROYED),
                new Row("GALACTICA", "HEAVY_RAIDER", LOWEST_ROLL_TO_DESTROY_A_HEAVY_RAIDER, 8, DESTROYED),
                new Row("GALACTICA", "BASESTAR", LOWEST_ROLL_TO_DAMAGE_WITH_GALACTICA, 8, DAMAGED),
                new Row("RAIDER", "VIPER", LOWEST_ROLL_TO_DAMAGE_A_VIPER, ROLL_TO_DESTROY - 1, DAMAGED),
                new Row("RAIDER", "VIPER_MARK_VII", LOWEST_ROLL_TO_DAMAGE_A_MARK_VII, ROLL_TO_DESTROY - 1, DAMAGED),
                new Row("RAIDER", "VIPER", ROLL_TO_DESTROY, 8, DESTROYED),
                new Row("RAIDER", "ASSAULT_RAPTOR", LOWEST_ROLL_TO_DESTROY_A_RAPTOR, 8, DESTROYED),
                new Row("RAIDER", "GALACTICA", LOWEST_ROLL_FOR_A_RAIDER, 8, HIT),
                new Row("BASESTAR", "GALACTICA", LOWEST_ROLL_FOR_A_BASESTAR, 8, HIT),
                new Row("ARMORY", "CENTURION", LOWEST_ROLL_TO_DESTROY, 8, DESTROYED));
    }

    public static int structuralDamageBonus() {
        return STRUCTURAL_DAMAGE_BONUS;
    }
}
