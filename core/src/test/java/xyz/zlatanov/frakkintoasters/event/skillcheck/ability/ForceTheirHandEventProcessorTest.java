package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ForceTheirHandEventProcessorTest extends EventTestHarness<ForceTheirHandEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldDrawMutinyCard() {
        execute(new ForceTheirHandEvent(1, null));
        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void shouldPlaySkillCard() {
        val card = new SkillCard(0, CALCULATIONS);
        player(1).skillCards().addOnTop(card);

        execute(new ForceTheirHandEvent(1, card));

        assertNoSkillCards(1);
        assertEquals(List.of(card), game.activeSkillCheck().nonMatchingPile().cards());
    }

    @Test
    void shouldPlayACardInHand() {
        assertInvalid(new ForceTheirHandEvent(1, new SkillCard(0, CALCULATIONS)));
    }
}
