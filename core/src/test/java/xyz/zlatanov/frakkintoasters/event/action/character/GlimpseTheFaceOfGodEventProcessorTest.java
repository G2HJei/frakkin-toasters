package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEOBEN_CONOY;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.GLIMPSE_THE_FACE_OF_GOD;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class GlimpseTheFaceOfGodEventProcessorTest extends EventTestHarness<GlimpseTheFaceOfGodEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT   = new SkillCard(2, LAUNCH_SCOUT);

    @BeforeEach
    void setUp() {
        player(1).character(LEOBEN_CONOY);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
    }

    // He takes the top 2 Destiny cards, then chooses 2 to put back.
    @Test
    void drawsTheTopTwoDestinyCards() {
        destinyDeck.nextCard(ORDER, SCOUT);

        execute(new GlimpseTheFaceOfGodEvent(1));

        assertSkillCards(1, ORDER, SCOUT);
        assertTrue(game.turn().hasUsed(1, GLIMPSE_THE_FACE_OF_GOD));
        assertFollowup(new PlayerDecisionEvent<>(1, GlimpseTheFaceOfGodPlaceEvent.class));
    }
}
