package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.jump.RiskShipsEvent.Risk;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceCivilianShipsInFrontEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceRaidersBehindEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.event.jump.DestinationCardFollowups.afterReset;
import static xyz.zlatanov.frakkintoasters.event.jump.DestinationCardFollowups.effects;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DestinationCardFollowupsTest extends TestHarness {

    private static final int ADMIRAL = 2;

    @BeforeEach
    void anAdmiral() {
        player(ADMIRAL).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @ParameterizedTest
    @EnumSource(value = DestinationCard.class, mode = EnumSource.Mode.EXCLUDE, names = "MISJUMP")
    void everyCardHasItsEffects(DestinationCard card) {
        effects(game, card); // Misjump draws another card instead, and is resolved by ResolveDestinationCardEventProcessor
    }

    @Test
    void misjumpHasNoEffectsOfItsOwn() {
        assertThrows(FrakCallTheAdmiralException.class, () -> effects(game, MISJUMP));
    }

    @Test
    void theDistanceIsPrintedOnTheCard() {
        assertEquals(3, ASTEROID_FIELD.distance());
        assertEquals(1, ICY_MOON.distance());
        assertEquals(0, MISJUMP.distance());
        assertEquals(21, DestinationCard.values().length); // "Mining Asteroid" needs the "Scar" Crisis Card
    }

    @Test
    void justLosingResources() {
        assertEquals(single(lose(FUEL, 2)), effects(game, BARREN_PLANET));
        assertEquals(single(lose(FUEL, 3)), effects(game, DESOLATE_MOON));
        assertEquals(all(lose(FUEL, 1), lose(MORALE, 1)), effects(game, DEEP_SPACE));
        assertEquals(all(lose(FUEL, 1), lose(POPULATION, 1)), effects(game, RADIOACTIVE_CLOUD));
        assertEquals(all(lose(FUEL, 1), gain(FOOD, 1)), effects(game, ALGAE_PLANET));
    }

    @Test
    void shipsArePlacedInFrontOfAndBehindGalactica() {
        assertEquals(all(lose(FUEL, 1), new PlaceShipsEvent(1, 3, 3)), effects(game, CYLON_AMBUSH));
        assertEquals(all(lose(FUEL, 2), new PlaceRaidersBehindEvent(3)), effects(game, CYLON_RAIDERS));
        assertEquals(all(lose(FUEL, 1), new DerelictBasestarEvent()), effects(game, DERELICT_BASESTAR));
        assertEquals(all(lose(FUEL, 1), new PlaceCivilianShipsInFrontEvent(1), new PlaceShipsEvent(0, 0, 1)),
                effects(game, BINARY_STAR));
    }

    @Test
    void damageAndDestruction() {
        assertEquals(all(lose(FUEL, 2), new DrawAndDestroyCivilianShipsEvent(1)), effects(game, ASTEROID_FIELD));
        assertEquals(all(lose(FUEL, 1), new DamageBattlestarEvent()), effects(game, DYING_STAR));
        assertEquals(all(lose(FUEL, 1), new DestroyRaptorEvent()), effects(game, REMOTE_PLANET));
    }

    // The Admiral may risk the ships, but only if there are enough of them in the "Reserves" (rule 13.5).
    @Test
    void theAdmiralMayRiskShips() {
        assertEquals(all(single(lose(FUEL, 1)), one(new RiskShipsEvent(ADMIRAL, Risk.ICY_MOON), new NoOpEvent(ADMIRAL))),
                effects(game, ICY_MOON));
        assertEquals(all(single(lose(FUEL, 1)), one(new RiskShipsEvent(ADMIRAL, Risk.TYLIUM_PLANET), new NoOpEvent(ADMIRAL))),
                effects(game, TYLIUM_PLANET));
        assertEquals(all(single(lose(FUEL, 1)), one(new RiskShipsEvent(ADMIRAL, Risk.CYLON_REFINERY), new NoOpEvent(ADMIRAL))),
                effects(game, CYLON_REFINERY));
    }

    @Test
    void theAdmiralCantRiskRaptorsThatAreNotInTheReserves() {
        galacticaBoard.reserves().stream()
                .filter(ship -> ship instanceof xyz.zlatanov.frakkintoasters.state.ship.Raptor)
                .forEach(raptor -> ops.ship(raptor.id()).destroy());

        assertEquals(single(lose(FUEL, 1)), effects(game, ICY_MOON));
    }

    @Test
    void theAdmiralMayTradeMoraleForFuelWithAConvoy() {
        assertEquals(all(
                        single(lose(FUEL, 3)),
                        single(gain(POPULATION, 1)),
                        one(all(lose(MORALE, 1), gain(FUEL, 1)), single(new NoOpEvent(ADMIRAL)))),
                effects(game, A_CIVILIAN_CONVOY));
    }

    @Test
    void theAdmiralMayDestroyAViperForFuel() {
        assertEquals(one(new GasGiantEvent(ADMIRAL), new NoOpEvent(ADMIRAL)), effects(game, GAS_GIANT));
    }

    @Test
    void theLionsHeadNebulaAdvancesTheTrackAfterTheReset() {
        assertEquals(single(lose(FUEL, 4)), effects(game, LIONS_HEAD_NEBULA));
        assertEquals(all(new IncreaseJumpPreparationEvent(), new IncreaseJumpPreparationEvent()), afterReset(LIONS_HEAD_NEBULA));
        assertEquals(NONE, afterReset(ICY_MOON));
    }

    @Test
    void theAdmiralLooksAtTheCrisisDeckAndRepairs() {
        assertEquals(single(new GasCloudEvent(ADMIRAL)), effects(game, GAS_CLOUD));
        assertEquals(single(new RagnarAnchorageEvent()), effects(game, RAGNAR_ANCHORAGE));
    }
}
