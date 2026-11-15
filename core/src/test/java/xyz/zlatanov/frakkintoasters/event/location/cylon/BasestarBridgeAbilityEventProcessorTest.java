package xyz.zlatanov.frakkintoasters.event.location.cylon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BASESTAR_BRIDGE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class BasestarBridgeAbilityEventProcessorTest extends EventTestHarness<BasestarBridgeAbilityEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
        moveTo(BASESTAR_BRIDGE, CAVIL);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        cag(KARA_STARBUCK_THRACE);
    }

    // "The CAG must place 1 civilian ship (following all placement rules)."
    @Test
    void shouldLetTheCagPlaceACivilianShip() {
        execute(new BasestarBridgeAbilityEvent(1, CAG_PLACES_CIVILIAN_SHIP));
        assertFollowup(new PlayerDecisionEvent<>(2, PlaceCivilianShipEvent.class));
    }

    @Test
    void shouldPlaceNoCivilianShipWhenNoneIsLeft() {
        civilianShips.clear();
        execute(new BasestarBridgeAbilityEvent(1, CAG_PLACES_CIVILIAN_SHIP));
    }

    @Test
    void shouldRollForTheJumpOrThePursuit() {
        execute(new BasestarBridgeAbilityEvent(1, JUMP_OR_PURSUIT));
        assertFollowup(rollDie(new BasestarBridgeAbilityResultEvent(1, JUMP_OR_PURSUIT)));
    }

    @Test
    void shouldLetThePlayerPlaceABasestarOrRaiders() {
        execute(new BasestarBridgeAbilityEvent(1, BASESTAR_OR_RAIDERS));
        assertFollowup(new PlayerDecisionEvent<>(1, BasestarBridgeShipsEvent.class));
    }

    @Test
    void shouldRollForGalacticaDamage() {
        execute(new BasestarBridgeAbilityEvent(1, GALACTICA_DAMAGE));
        assertFollowup(rollDie(new BasestarBridgeAbilityResultEvent(1, GALACTICA_DAMAGE)));
    }
}
