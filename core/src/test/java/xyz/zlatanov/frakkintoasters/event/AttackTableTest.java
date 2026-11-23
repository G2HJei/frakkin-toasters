package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.AttackTable.Row;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.AttackTable.Effect.*;

class AttackTableTest {

    @Test
    void theRowsAreTheThresholdsOfTheAttacks() {
        val rows = AttackTable.rows();

        assertTrue(rows.contains(new Row("VIPER", "RAIDER", 3, 8, DESTROYED)));
        assertTrue(rows.contains(new Row("RAIDER", "VIPER", 5, 7, DAMAGED)));
        assertTrue(rows.contains(new Row("RAIDER", "VIPER", 8, 8, DESTROYED)));
        assertTrue(rows.contains(new Row("BASESTAR", "GALACTICA", 4, 8, HIT)));
        assertTrue(rows.contains(new Row("ARMORY", "CENTURION", 7, 8, DESTROYED)));
        assertTrue(rows.stream().allMatch(row -> row.from() >= 1 && row.from() <= row.to() && row.to() <= 8));
        assertEquals(2, AttackTable.structuralDamageBonus());
    }
}
