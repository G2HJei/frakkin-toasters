package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.COMMAND;

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

    // "Roll a die: 1-3: Decrease the jump track by 1"
    @Test
    void shouldDecreaseTheJumpTrackOnALowRoll() {
        nextRoll(3);
        execute(new BasestarBridgeAbilityEvent(1, JUMP_OR_PURSUIT));
        assertFollowup(new DecreaseJumpPreparationEvent());
    }

    // "4-8: Increase the pursuit track by 1"
    @Test
    void shouldIncreaseThePursuitTrackOnAHighRoll() {
        nextRoll(4);
        execute(new BasestarBridgeAbilityEvent(1, JUMP_OR_PURSUIT));
        assertFollowup(new AdvancePursuitTrackEvent());
    }

    @Test
    void shouldLetThePlayerPlaceABasestarOrRaiders() {
        execute(new BasestarBridgeAbilityEvent(1, BASESTAR_OR_RAIDERS));
        assertFollowup(new PlayerDecisionEvent<>(1, BasestarBridgeShipsEvent.class));
    }

    // "Roll a die. If the result is less than the number of raiders on the main game board, draw 2 Galactica damage
    // tokens and choose 1 to resolve."
    @Test
    void shouldDrawTwoDamageTokensWhenTheRollIsBelowTheRaiders() {
        raiderAt(GALACTICA_SPACE_2_OCLOCK);
        raiderAt(GALACTICA_SPACE_2_OCLOCK);
        raiderAt(GALACTICA_SPACE_4_OCLOCK);
        nextRoll(2);
        galacticaDamage.nextCard(ARMORY, COMMAND);
        val size = galacticaDamage.size();

        execute(new BasestarBridgeAbilityEvent(1, GALACTICA_DAMAGE));

        assertEquals(size - 2, galacticaDamage.size());
        assertFollowup(one(
                new ChooseGalacticaDamageEvent(1, ARMORY, COMMAND),
                new ChooseGalacticaDamageEvent(1, COMMAND, ARMORY)));
    }

    @Test
    void shouldDrawNoDamageTokenWhenTheRollIsNotBelowTheRaiders() {
        raiderAt(GALACTICA_SPACE_2_OCLOCK);
        raiderAt(GALACTICA_SPACE_2_OCLOCK);
        nextRoll(2);

        execute(new BasestarBridgeAbilityEvent(1, GALACTICA_DAMAGE));
    }
}
