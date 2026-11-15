package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.AttackCylonShipEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK_ALT;

class WeaponsControlEventProcessorTest extends EventTestHarness<WeaponsControlEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(TOM_ZAREK_ALT);
        moveTo(WEAPONS_CONTROL, TOM_ZAREK_ALT);
    }

    @Test
    void shouldAttackWithGalactica() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new WeaponsControlEvent(1, heavyRaider.id()));

        assertFollowup(AttackCylonShipEvent.byGalactica(heavyRaider.id()));
    }

    @Test
    void shouldNotBeUsedWhileDamaged() {
        val raider = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        galacticaBoard.damage(WEAPONS_CONTROL);
        assertInvalid(new WeaponsControlEvent(1, raider.id()));
    }

    @Test
    void shouldOnlyAttackACylonShipInSpace() {
        assertInvalid(new WeaponsControlEvent(1, raider().id()));
    }
}
