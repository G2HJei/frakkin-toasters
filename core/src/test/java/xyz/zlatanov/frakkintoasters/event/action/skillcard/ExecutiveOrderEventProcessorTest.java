package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CRITICAL_SITUATION;

class ExecutiveOrderEventProcessorTest extends EventTestHarness<ExecutiveOrderEvent> {

    private static final SkillCard EXECUTIVE_ORDER_1 = new SkillCard(1, SkillCardType.EXECUTIVE_ORDER);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(1).gainSkillCards(EXECUTIVE_ORDER_1);
        player(2).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // The target chooses: move and then act, or act twice without moving.
    @Test
    void theTargetMovesAndActsOrActsTwice() {
        execute(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2));

        assertFollowup(all(
                one(all(new GrantEvent(2, MOVE, EXECUTIVE_ORDER), new GrantEvent(2, ACTION, EXECUTIVE_ORDER)),
                        all(new GrantEvent(2, ACTION, EXECUTIVE_ORDER), new GrantEvent(2, ACTION, EXECUTIVE_ORDER))),
                single(new ResetActingPlayerEvent())));
        assertEquals(List.of(SkillCardType.EXECUTIVE_ORDER),
                game.turn().playedSkillCards());
    }

    // Characters in the "Brig" may not move (rule 9.2), so he acts twice.
    @Test
    void aTargetInTheBrigActsTwice() {
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2));

        assertFollowup(all(
                all(new GrantEvent(2, ACTION, EXECUTIVE_ORDER), new GrantEvent(2, ACTION, EXECUTIVE_ORDER)),
                single(new ResetActingPlayerEvent())));
    }

    @Test
    void notOnYourself() {
        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 1));
    }

    @Test
    void theTargetHasToBeNamed() {
        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, null));
    }

    @Test
    void theTargetHasToBeAPlayer() {
        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 4));
    }

    // A revealed Cylon can't be targeted (A2.3.8.4).
    @Test
    void notOnARevealedCylon() {
        player(2).gainSkillCards(EXECUTIVE_ORDER_1);
        revealCylon(); // player 1

        assertInvalid(new ExecutiveOrderEvent(2, EXECUTIVE_ORDER_1, 1));
    }

    // A character off the board ("Stranded" Karl "Helo" Agathon) can neither move nor act.
    @Test
    void notOnAPlayerWhoCanDoNothing() {
        player(3).character(KARL_HELO_AGATHON);

        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 3));
    }

    // "Only 1 "Critical Situation" or "Executive Order" card may be used per turn."
    @Test
    void notAfterACriticalSituation() {
        player(1).gainSkillCards(new SkillCard(3, CRITICAL_SITUATION));
        ops.player(1).play(new SkillCard(3, CRITICAL_SITUATION));

        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2));
    }

    @Test
    void oneAPerTurn() {
        player(1).gainSkillCards(new SkillCard(2, SkillCardType.EXECUTIVE_ORDER));
        ops.player(1).play(new SkillCard(2, SkillCardType.EXECUTIVE_ORDER));

        assertInvalid(new ExecutiveOrderEvent(1, EXECUTIVE_ORDER_1, 2));
    }
}
