package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class CriticalSituationEventProcessorTest extends EventTestHarness<CriticalSituationEvent> {

    private static final SkillCard CRITICAL_SITUATION_3 = new SkillCard(3, CRITICAL_SITUATION);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(1).gainSkillCards(CRITICAL_SITUATION_3);
    }

    @Test
    void thePlayerTakesAnAction() {
        execute(new CriticalSituationEvent(1, CRITICAL_SITUATION_3));

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActionEvent.class), new NoOpEvent(1)));
        assertEquals(List.of(CRITICAL_SITUATION), game.turn().playedSkillCards());
    }

    // "Only 1 "Critical Situation" or "Executive Order" card may be used per turn."
    @Test
    void notAfterAnExecutiveOrder() {
        player(1).gainSkillCards(new SkillCard(1, EXECUTIVE_ORDER));
        ops.player(1).play(new SkillCard(1, EXECUTIVE_ORDER));

        assertInvalid(new CriticalSituationEvent(1, CRITICAL_SITUATION_3));
    }

    // The action would have no legal answer.
    @Test
    void notWithoutAnActionToTake() {
        moveTo(SICKBAY, WILLIAM_ADAMA);

        assertInvalid(new CriticalSituationEvent(1, CRITICAL_SITUATION_3));
    }
}
