package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MYSTERIOUS_INTUITION;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class MysteriousIntuitionEventProcessorTest extends EventTestHarness<MysteriousIntuitionEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SHARON_BOOMER_VALERII);
        moveTo(ARMORY, SHARON_BOOMER_VALERII);
    }

    // The check isn't made: its result is the one she chooses.
    @Test
    void choosesTheResult() {
        ops.skillCheck().start(WATER_SABOTAGED);

        execute(new MysteriousIntuitionEvent(1, true));

        assertEquals(PASS, game.activeSkillCheck().result());
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, MYSTERIOUS_INTUITION));
    }

    @Test
    void choosesAFail() {
        ops.skillCheck().start(WATER_SABOTAGED);

        execute(new MysteriousIntuitionEvent(1, false));

        assertEquals(FAIL, game.activeSkillCheck().result());
    }

    @Test
    void offeredBeforeACrisisCardsCheck() {
        ops.skillCheck().start(WATER_SABOTAGED);

        assertEquals(one(new MysteriousIntuitionEvent(1, true), new MysteriousIntuitionEvent(1, false), new NoOpEvent(1)),
                MysteriousIntuitionEventProcessor.offer(game));
        execute(new MysteriousIntuitionEvent(1, true));
        // the check is decided, so nothing more is offered
        assertEquals(NONE, MysteriousIntuitionEventProcessor.offer(game));
    }

    // Only a Crisis Card's check: not a location's, and a Super Crisis Card is immune (rule 6.1.2.5).
    @Test
    void notOnALocationOrASuperCrisisCheck() {
        ops.skillCheck().start(ADMIRALS_QUARTERS);
        assertEquals(NONE, MysteriousIntuitionEventProcessor.offer(game));
        assertInvalid(new MysteriousIntuitionEvent(1, true));
    }

    @Test
    void notOnASuperCrisisCheck() {
        ops.skillCheck().start(INBOUND_NUKES);
        assertEquals(NONE, MysteriousIntuitionEventProcessor.offer(game));
        assertInvalid(new MysteriousIntuitionEvent(1, true));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.skillCheck().start(WATER_SABOTAGED);
        ops.player(1).loseMiracleTokens();

        assertInvalid(new MysteriousIntuitionEvent(1, true));
    }
}
