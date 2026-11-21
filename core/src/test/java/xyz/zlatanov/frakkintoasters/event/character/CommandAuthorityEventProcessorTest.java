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
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.COMMAND_AUTHORITY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class CommandAuthorityEventProcessorTest extends EventTestHarness<CommandAuthorityEvent> {

    private final SkillCard leadership = new SkillCard(3, EXECUTIVE_ORDER);
    private final SkillCard tactics    = new SkillCard(2, LAUNCH_SCOUT);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        player(2).gainSkillCards(leadership, tactics);
    }

    private void resolvedCheck(xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType type) {
        ops.skillCheck().start(type);
        ops.skillCheck().playCards(2, leadership, tactics);
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().totalStrength(5);
        ops.skillCheck().determineResult(PASS);
    }

    // The cards of the resolved check go to his hand, so the end of the check discards none of them.
    @Test
    void takesTheCardsOfTheCheck() {
        resolvedCheck(WATER_SABOTAGED);

        execute(new CommandAuthorityEvent(1));

        assertEquals(List.of(), game.activeSkillCheck().dividedCards());
        assertTrue(player(1).skillCards().cards().containsAll(List.of(leadership, tactics)));
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, COMMAND_AUTHORITY));
    }

    @Test
    void notOnASuperCrisisCheck() {
        resolvedCheck(INBOUND_NUKES);

        assertEquals(NONE, CommandAuthorityEventProcessor.offer(game));
        assertInvalid(new CommandAuthorityEvent(1));
    }

    @Test
    void onlyWithAMiracleToken() {
        resolvedCheck(WATER_SABOTAGED);
        ops.player(1).loseMiracleTokens();

        assertFalse(CommandAuthorityEventProcessor.mayBeOffered(game));
        assertInvalid(new CommandAuthorityEvent(1));
    }

    @Test
    void notSomeoneElse() {
        resolvedCheck(WATER_SABOTAGED);

        val roslin = new CommandAuthorityEvent(2);
        assertInvalid(roslin);
    }
}
