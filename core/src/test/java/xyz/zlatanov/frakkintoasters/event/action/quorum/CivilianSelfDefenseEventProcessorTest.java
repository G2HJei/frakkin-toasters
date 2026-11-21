package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyHeavyRaiderEvent;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.CIVILIAN_SELF_DEFENSE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class CivilianSelfDefenseEventProcessorTest extends EventTestHarness<CivilianSelfDefenseEvent> {

    private int civilianShip;

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(CIVILIAN_SELF_DEFENSE);
        civilianShip = civilianShipAt(GALACTICA_SPACE_2_OCLOCK).id();
    }

    // Only the raiders in the civilian ship's area, 3 at most.
    @Test
    void raidersBesideTheShipAreDestroyed() {
        val a = raiderAt(GALACTICA_SPACE_2_OCLOCK).id();
        val b = raiderAt(GALACTICA_SPACE_2_OCLOCK).id();
        raiderAt(GALACTICA_SPACE_8_OCLOCK);

        execute(new CivilianSelfDefenseEvent(1, civilianShip, RAIDER));

        assertFollowup(all(single(new DestroyRaidersEvent(Set.of(a, b))),
                rollDie(new CivilianSelfDefenseResultEvent(civilianShip))));
    }

    @Test
    void aHeavyRaiderBesideTheShip() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_2_OCLOCK).id();

        execute(new CivilianSelfDefenseEvent(1, civilianShip, HEAVY_RAIDER));

        assertFollowup(all(single(new DestroyHeavyRaiderEvent(heavyRaider)),
                rollDie(new CivilianSelfDefenseResultEvent(civilianShip))));
    }

    @Test
    void notWithoutThatShipBesideIt() {
        raiderAt(GALACTICA_SPACE_8_OCLOCK);

        assertInvalid(new CivilianSelfDefenseEvent(1, civilianShip, RAIDER));
    }

    @Test
    void onlyRaidersOrHeavyRaiders() {
        raiderAt(GALACTICA_SPACE_2_OCLOCK);

        assertInvalid(new CivilianSelfDefenseEvent(1, civilianShip, VIPER));
    }
}
