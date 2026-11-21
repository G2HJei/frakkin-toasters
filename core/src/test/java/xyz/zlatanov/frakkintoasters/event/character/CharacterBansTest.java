package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardPlays;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

class CharacterBansTest extends TestHarness {

    @Test
    void williamAdamaCantActivateTheAdmiralsQuarters() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);

        assertFalse(CharacterBans.canActivate(game, player(1), ADMIRALS_QUARTERS));
        assertFalse(LocationEvent.canAct(game, player(1)));
        assertTrue(CharacterBans.canActivate(game, player(1), COMMAND));
    }

    @Test
    void helenaCainCantActivateTheJumpLocations() {
        player(1).character(HELENA_CAIN);

        assertFalse(CharacterBans.canActivate(game, player(1), FTL_CONTROL));
        assertFalse(CharacterBans.canActivate(game, player(1), ENGINE_ROOM));
        // not even through "Unorthodox Plan"'s rules
        assertFalse(LocationEvent.canActIgnoringDamage(game, player(1), FTL_CONTROL));
        assertTrue(CharacterBans.canActivate(game, player(1), COMMAND));
    }

    // "Convicted Criminal": not a location another character is in, but the "Brig" always.
    @Test
    void tomZarekCantActivateAnOccupiedLocation() {
        player(1).character(TOM_ZAREK);
        player(2).character(SAUL_TIGH);
        moveTo(COMMAND, TOM_ZAREK);

        assertTrue(CharacterBans.canActivate(game, player(1), COMMAND));

        moveTo(COMMAND, SAUL_TIGH);
        assertFalse(CharacterBans.canActivate(game, player(1), COMMAND));

        moveTo(BRIG, TOM_ZAREK);
        moveTo(BRIG, SAUL_TIGH);
        assertTrue(CharacterBans.canActivate(game, player(1), BRIG));
    }

    // "Specialized": no "Action:" of an Engineering card.
    @Test
    void docCottleCantUseEngineeringActions() {
        player(1).character(SHERMAN_DOC_COTTLE);
        player(2).character(SAUL_TIGH);

        assertFalse(CharacterBans.canUseSkillCardAction(player(1), REPAIR));
        assertTrue(CharacterBans.canUseSkillCardAction(player(1), CONSOLIDATE_POWER));
        assertTrue(CharacterBans.canUseSkillCardAction(player(2), REPAIR));
    }

    // "Spiritual Crisis": the reveal only from the "Brig".
    @Test
    void baltarAltRevealsOnlyFromTheBrig() {
        player(1).character(GAIUS_BALTAR_ALT);
        moveTo(ADMIRALS_QUARTERS, GAIUS_BALTAR_ALT);

        assertFalse(CharacterBans.canUseCylonCardAction(game, player(1)));

        moveTo(BRIG, GAIUS_BALTAR_ALT);
        assertTrue(CharacterBans.canUseCylonCardAction(game, player(1)));
    }

    // Ellen Tigh's sheet says humans can't use the text of Treachery Cards: none has an action or a movement, so none
    // is ever offered.
    @Test
    void noTreacheryCardIsPlayedForItsText() {
        player(1).character(ELLEN_TIGH);

        assertEquals(0, Arrays.stream(SkillCardType.values())
                .filter(type -> type.color() == TREACHERY)
                .filter(type -> type.isAction() || type.isMovementAbility() || SkillCardPlays.canPlay(game, player(1), type))
                .count());
    }

    // A revealed Cylon's abilities are ignored, its bans too.
    @Test
    void aRevealedCylonsBansAreIgnored() {
        player(1).character(WILLIAM_ADAMA);
        revealCylon();

        assertTrue(CharacterBans.canActivate(game, player(1), ADMIRALS_QUARTERS));
    }
}
