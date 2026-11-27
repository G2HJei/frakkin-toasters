package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.GALACTICA_DAMAGE;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.JUMP_OR_PURSUIT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.COMMAND;

class BasestarBridgeAbilityResultEventProcessorTest extends EventTestHarness<BasestarBridgeAbilityResultEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
        moveTo(BASESTAR_BRIDGE, CAVIL);
    }

    // "Roll a die: 1-3: Decrease the jump track by 1"
    @Test
    void shouldDecreaseTheJumpTrackOnALowRoll() {
        rolled(3);
        execute(new BasestarBridgeAbilityResultEvent(1, JUMP_OR_PURSUIT));
        assertFollowup(new DecreaseJumpPreparationEvent());
    }

    // "4-8: Increase the pursuit track by 1"
    @Test
    void shouldIncreaseThePursuitTrackOnAHighRoll() {
        rolled(4);
        execute(new BasestarBridgeAbilityResultEvent(1, JUMP_OR_PURSUIT));
        assertFollowup(new AdvancePursuitTrackEvent());
    }

    // "Roll a die. If the result is less than the number of raiders on the main game board, draw 2 Galactica damage
    // tokens and choose 1 to resolve."
    @Test
    void shouldDrawTwoDamageTokensWhenTheRollIsBelowTheRaiders() {
        raiderAt(GALACTICA_SPACE_3_OCLOCK);
        raiderAt(GALACTICA_SPACE_3_OCLOCK);
        raiderAt(GALACTICA_SPACE_5_OCLOCK);
        rolled(2);
        galacticaDamage.nextCard(ARMORY, COMMAND);
        val size = galacticaDamage.size();

        execute(new BasestarBridgeAbilityResultEvent(1, GALACTICA_DAMAGE));

        assertEquals(size - 2, galacticaDamage.size());
        assertFollowup(one(
                new ChooseGalacticaDamageEvent(1, ARMORY, COMMAND),
                new ChooseGalacticaDamageEvent(1, COMMAND, ARMORY)));
    }

    @Test
    void shouldDrawNoDamageTokenWhenTheRollIsNotBelowTheRaiders() {
        raiderAt(GALACTICA_SPACE_3_OCLOCK);
        raiderAt(GALACTICA_SPACE_3_OCLOCK);
        rolled(2);

        execute(new BasestarBridgeAbilityResultEvent(1, GALACTICA_DAMAGE));
    }

    @Test
    void shouldResolveOnlyAnAbilityThatRolls() {
        assertInvalid(new BasestarBridgeAbilityResultEvent(1, BasestarBridgeAbility.BASESTAR_OR_RAIDERS));
    }
}
