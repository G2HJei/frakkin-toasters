package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.HUMAN_PRISONER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRAL_GRILLED;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.THE_FARM;

class CharacterAbilitiesTest extends TestHarness {

    @Test
    void aMiracleNeedsTheAbilityAndTheToken() {
        player(1).character(GAIUS_BALTAR);

        assertTrue(CharacterAbilities.canUseMiracle(game, player(1), CYLON_DETECTOR));
        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), COUP)); // Felix Gaeta's
        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), DELUSIONAL_INTUITION)); // not a miracle

        ops.player(1).useAbility(CYLON_DETECTOR);

        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), CYLON_DETECTOR));
    }

    // "Broadcast" spends 3 miracle tokens (rule 4.3.1.1).
    @Test
    void broadcastNeedsThreeTokens() {
        player(1).character(GAIUS_BALTAR_ALT);

        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), BROADCAST));
        ops.player(1).gainMiracleToken();
        ops.player(1).gainMiracleToken();

        assertTrue(CharacterAbilities.canUseMiracle(game, player(1), BROADCAST));
    }

    // A revealed Cylon's abilities are ignored (rule 6.1.2.2).
    @Test
    void aRevealedCylonHasNoAbilities() {
        player(1).character(GAIUS_BALTAR);
        revealCylon();

        assertFalse(player(1).hasAbility(CYLON_DETECTOR));
        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), CYLON_DETECTOR));
    }

    // A Cylon Leader's are always in effect (rule 6.2.1.2).
    @Test
    void aCylonLeaderHasHisAbilities() {
        player(1).character(DANNA_BIERS);

        assertTrue(player(1).hasAbility(DONT_TRUST_ANYONE));
        assertTrue(CharacterAbilities.canUseMiracle(game, player(1), DONT_TRUST_ANYONE));
    }

    // "The Farm" in play: humans may not use their once-per-game abilities.
    @Test
    void theFarmBlocksTheHumansMiracles() {
        player(1).character(GAIUS_BALTAR);
        player(2).character(DANNA_BIERS);
        ops.keepCrisisCardInPlay(THE_FARM);

        assertFalse(CharacterAbilities.canUseMiracle(game, player(1), CYLON_DETECTOR));
        assertTrue(CharacterAbilities.canUseMiracle(game, player(2), DONT_TRUST_ANYONE));

        player(2).infiltrateGalactica();

        assertFalse(CharacterAbilities.canUseMiracle(game, player(2), DONT_TRUST_ANYONE));
    }

    @Test
    void aOncePerTurnAbilityIsUsedUpUntilTheNextTurn() {
        player(1).character(KARL_HELO_AGATHON);

        assertTrue(CharacterAbilities.canUseOncePerTurn(game, player(1), ECO_OFFICER));
        ops.player(1).useAbility(ECO_OFFICER);
        assertFalse(CharacterAbilities.canUseOncePerTurn(game, player(1), ECO_OFFICER));

        ops.startTurn();

        assertTrue(CharacterAbilities.canUseOncePerTurn(game, player(1), ECO_OFFICER));
    }

    // A Super Crisis Card is immune to the abilities that affect skill checks (rule 6.1.2.5).
    @Test
    void abilitiesDontAffectASuperCrisisCheck() {
        assertFalse(CharacterAbilities.canAffectSkillCheck(game));

        ops.skillCheck().start(ADMIRAL_GRILLED);
        assertTrue(CharacterAbilities.canAffectSkillCheck(game));
    }

    @Test
    void aSuperCrisisCheckIsImmune() {
        ops.skillCheck().start(HUMAN_PRISONER);

        assertFalse(CharacterAbilities.canAffectSkillCheck(game));
    }
}
