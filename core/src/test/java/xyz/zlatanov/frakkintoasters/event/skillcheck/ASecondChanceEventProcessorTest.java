package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAVIL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ASecondChanceEventProcessorTest extends EventTestHarness<ASecondChanceEvent> {

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldGiveCurrentPlayerAMiracleToken() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).exhaustMiracleToken();

        execute(new ASecondChanceEvent());

        assertTrue(player(1).hasMiracleToken());
    }

    @Test
    void shouldNotGiveMiracleTokenToRevealedCylon() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).exhaustMiracleToken();
        revealCylon();

        execute(new ASecondChanceEvent());

        assertFalse(player(1).hasMiracleToken());
    }

    @Test
    void shouldGiveMiracleTokenToCylonLeaderNotInfiltrating() {
        player(1).character(CAVIL);
        player(1).exhaustMiracleToken();

        execute(new ASecondChanceEvent());

        assertTrue(player(1).hasMiracleToken());
    }
}
