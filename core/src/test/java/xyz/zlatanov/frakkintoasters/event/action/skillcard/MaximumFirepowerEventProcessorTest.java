package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.MAXIMUM_FIREPOWER;

class MaximumFirepowerEventProcessorTest extends EventTestHarness<MaximumFirepowerEvent> {

    private static final SkillCard MAXIMUM_FIREPOWER_3 = new SkillCard(3, MAXIMUM_FIREPOWER);

    private Viper own;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(MAXIMUM_FIREPOWER_3);
        own = viperAt(GALACTICA_SPACE_5_OCLOCK);
    }

    @Test
    void theViperAttacksUpToFourTimes() {
        ops.player(1).pilot(own.id());
        raiderAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new MaximumFirepowerEvent(1, MAXIMUM_FIREPOWER_3));

        assertFollowup(new ViperActivationRoundEvent(1, own.id(), 4, true));
    }

    @Test
    void onlyWhilePiloting() {
        raiderAt(GALACTICA_SPACE_5_OCLOCK);

        assertInvalid(new MaximumFirepowerEvent(1, MAXIMUM_FIREPOWER_3));
    }

    @Test
    void notWithoutATarget() {
        ops.player(1).pilot(own.id());
        raiderAt(GALACTICA_SPACE_7_OCLOCK);

        assertInvalid(new MaximumFirepowerEvent(1, MAXIMUM_FIREPOWER_3));
    }
}
