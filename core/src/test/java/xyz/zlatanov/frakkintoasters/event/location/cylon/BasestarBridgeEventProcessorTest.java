package xyz.zlatanov.frakkintoasters.event.location.cylon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BASESTAR_BRIDGE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;

class BasestarBridgeEventProcessorTest extends EventTestHarness<BasestarBridgeEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(CAVIL);
        moveTo(BASESTAR_BRIDGE, CAVIL);
    }

    // "he chooses two of the four listed abilities to resolve, one at a time. Note that he may only resolve each ability
    // once per activation of the location." (rule 9.7)
    @Test
    void shouldResolveTheFirstAbilityThenOfferTheOthers() {
        assertTrue(LocationEvent.canAct(game, player(1)));

        execute(new BasestarBridgeEvent(1, JUMP_OR_PURSUIT));

        assertFollowup(all(
                single(new BasestarBridgeAbilityEvent(1, JUMP_OR_PURSUIT)),
                one(new BasestarBridgeAbilityEvent(1, CAG_PLACES_CIVILIAN_SHIP),
                        new BasestarBridgeAbilityEvent(1, BASESTAR_OR_RAIDERS),
                        new BasestarBridgeAbilityEvent(1, GALACTICA_DAMAGE))));
    }

    @Test
    void shouldNameTheFirstAbility() {
        assertInvalid(new BasestarBridgeEvent(1, null));
    }

    @Test
    void shouldNotAllowElsewhere() {
        moveTo(CYLON_FLEET, CAVIL);
        assertInvalid(new BasestarBridgeEvent(1, JUMP_OR_PURSUIT));
    }
}
