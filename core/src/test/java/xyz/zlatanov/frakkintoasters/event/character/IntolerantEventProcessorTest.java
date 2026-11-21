package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INTOLERANT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class IntolerantEventProcessorTest extends EventTestHarness<IntolerantEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
        moveTo(COMMAND, HELENA_CAIN);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.skillCheck().start(ADMIRALS_QUARTERS, 2);
    }

    @Test
    void executesTheTargetOnAPassOfTen() {
        ops.skillCheck().totalStrength(10);
        ops.skillCheck().determineResult(PASS);

        execute(new IntolerantEvent(1, 2));

        assertFollowup(new ExecuteEvent(2));
        assertTrue(game.turn().hasUsed(1, INTOLERANT));
    }

    @Test
    void notOnAWeakerPass() {
        ops.skillCheck().totalStrength(9);
        ops.skillCheck().determineResult(PASS);

        assertEquals(java.util.Optional.empty(), IntolerantEventProcessor.user(game));
        assertInvalid(new IntolerantEvent(1, 2));
    }

    @Test
    void notOnAFail() {
        ops.skillCheck().totalStrength(12);
        ops.skillCheck().determineResult(FAIL);

        assertInvalid(new IntolerantEvent(1, 2));
    }

    @Test
    void onlyTheTargetOfTheCheck() {
        ops.skillCheck().totalStrength(10);
        ops.skillCheck().determineResult(PASS);

        assertInvalid(new IntolerantEvent(1, 1));
    }
}
