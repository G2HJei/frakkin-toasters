package xyz.zlatanov.frakkintoasters.operation;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.AbilityUsed;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.MiracleTokenGained;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.MiracleTokenLost;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR_ALT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAMUEL_T_ANDERS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.BROADCAST;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.LONGSHOT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STAR_PLAYER;

class PlayerOpsAbilityTest extends TestHarness {

    @Test
    void anAbilityIsRecordedForTheTurn() {
        player(1).character(SAMUEL_T_ANDERS);

        ops.player(1).useAbility(STAR_PLAYER);

        assertEquals(List.of(new AbilityUsed(1, STAR_PLAYER)), journal.facts());
        assertTrue(game.turn().hasUsed(1, STAR_PLAYER));
        assertEquals(1, player(1).miracleTokens());
    }

    @Test
    void aMiracleSpendsItsToken() {
        player(1).character(SAMUEL_T_ANDERS);

        ops.player(1).useAbility(LONGSHOT);

        assertEquals(List.of(new AbilityUsed(1, LONGSHOT), new MiracleTokenLost(1)), journal.facts());
        assertEquals(0, player(1).miracleTokens());
    }

    // "Broadcast" discards 3 miracle tokens (rule 4.3.1.1).
    @Test
    void broadcastSpendsThreeTokens() {
        player(1).character(GAIUS_BALTAR_ALT);
        ops.player(1).gainMiracleToken();
        ops.player(1).gainMiracleToken();

        ops.player(1).useAbility(BROADCAST);

        assertEquals(0, player(1).miracleTokens());
    }

    // He can gain one only if he doesn't have one (rule 4.3.1): nothing is recorded then.
    @Test
    void aSecondTokenIsNotGained() {
        player(1).character(SAMUEL_T_ANDERS);

        ops.player(1).gainMiracleToken();

        assertEquals(List.of(), journal.facts());
    }

    @Test
    void losingTheTokensDiscardsThemAll() {
        player(1).character(GAIUS_BALTAR_ALT);
        ops.player(1).gainMiracleToken();

        ops.player(1).loseMiracleTokens();

        assertEquals(List.of(new MiracleTokenGained(1), new MiracleTokenLost(1), new MiracleTokenLost(1)), journal.facts());
        assertEquals(0, player(1).miracleTokens());
    }
}
