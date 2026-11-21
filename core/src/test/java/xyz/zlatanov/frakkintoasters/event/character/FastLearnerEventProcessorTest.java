package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ANASTASIA_DEE_DUALLA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FAST_LEARNER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class FastLearnerEventProcessorTest extends EventTestHarness<FastLearnerEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        moveTo(COMMUNICATIONS, ANASTASIA_DEE_DUALLA);
    }

    // She takes the top 3 of the deck into her hand, then chooses whether they go into the check.
    @Test
    void takesTheTopThreeCards() {
        ops.skillCheck().start(WATER_SABOTAGED);
        val cards = List.of(new SkillCard(1, LAUNCH_SCOUT), new SkillCard(2, LAUNCH_SCOUT),
                new SkillCard(3, LAUNCH_SCOUT));
        tacticsDeck.nextCard(cards.toArray(SkillCard[]::new));

        execute(new FastLearnerEvent(1, TACTICS));

        assertSkillCards(1, cards.toArray(SkillCard[]::new));
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, FAST_LEARNER));
        assertFollowup(one(new FastLearnerIntoCheckEvent(1, cards), new NoOpEvent(1)));
    }

    // A Super Crisis Card's check is immune (rule 6.1.2.5).
    @Test
    void notOnASuperCrisisCheck() {
        ops.skillCheck().start(INBOUND_NUKES);

        assertEquals(NONE, FastLearnerEventProcessor.offer(game));
        assertInvalid(new FastLearnerEvent(1, TACTICS));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.skillCheck().start(WATER_SABOTAGED);
        ops.player(1).loseMiracleTokens();

        assertEquals(NONE, FastLearnerEventProcessor.offer(game));
        assertInvalid(new FastLearnerEvent(1, TACTICS));
    }
}
