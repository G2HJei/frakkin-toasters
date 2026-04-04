package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;


class SelectCharacterActionTest {
    Game game = new Game(KOBOL, 4);

    @Test
    void shouldSelectCharacter() {
        select(KARA_STARBUCK_THRACE).execute(game);
        assertEquals(KARA_STARBUCK_THRACE, game.player(1).character());
    }

    @Test
    void shouldPlaceInSetupLocation() {
        select(LOUANNE_KAT_KATRAINE).execute(game);
        assertEquals(HANGAR_DECK, game.locate(LOUANNE_KAT_KATRAINE));
    }

    @Test
    void shouldReceiveMiracleToken() {
        select(CHIEF_GALEN_TYROL).execute(game);
        assertTrue(game.player(1).hasMiracleToken());
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        game.player(4).selectCharacter(LOUANNE_KAT_KATRAINE);
        game.player(3).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(TOM_ZAREK);

        val followup = select(HELENA_CAIN).execute(game);

        val expected = List.of(
                new MoveAction(1, PEGASUS_CIC, null),
                new MoveAction(1, COMMAND, null));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        select(KARL_HELO_AGATHON).execute(game);
        assertNull(game.locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        val followup = select(LEE_APOLLO_ADAMA).execute(game);
        val expected = List.of(new PlayerDecisionAction(1, LaunchViperAction.class));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotAllowDoubleSelection() {
        select(KARA_STARBUCK_THRACE).execute(game);
        val invalidAction = new SelectCharacterAction(2, KARA_STARBUCK_THRACE);
        assertThrows(InvalidActionException.class, () -> invalidAction.execute(game));
    }

    @Test
    void shouldNotAllowDoubleSelectionOfAlternateVersion() {
        game.player(2).selectCharacter(GAIUS_BALTAR);
        assertFalse(select(GAIUS_BALTAR_ALT).isValid(game));
    }

    @Test
    void shouldObserveCharacterTypeLimits() {
        assertFalse(select(LAURA_ROSLIN).isValid(game));
        assertFalse(select(HELENA_CAIN).isValid(game));
        assertTrue(select(BRENDAN_HOTDOG_COSTANZA).isValid(game));
        assertTrue(select(GAIUS_BALTAR_ALT).isValid(game));
        assertTrue(select(CAVIL).isValid(game));
    }

    @Test
    void shouldAllowOnlyOneCylonLeader() {
        game.player(2).selectCharacter(CAPRICA_SIX);
        assertFalse(select(SIMON_ONEIL).isValid(game));
    }

    @Test
    void shouldNotAllowCylonLeaderIn3PlayerGame() {
        assertFalse(select(SHARON_ATHENA_AGATHON).isValid(new Game(KOBOL, 3)));
    }

    @Test
    void shouldRequireCylonLeaderIn7PlayerGame() {
        val sevenPlayerGame = new Game(KOBOL, 7);
        sevenPlayerGame.player(2).selectCharacter(CHIEF_GALEN_TYROL);
        sevenPlayerGame.player(3).selectCharacter(ANASTASIA_DEE_DUALLA);
        sevenPlayerGame.player(4).selectCharacter(CALLANDRA_CALLY_TYROL);
        sevenPlayerGame.player(5).selectCharacter(GAIUS_BALTAR_ALT);
        sevenPlayerGame.player(6).selectCharacter(SHERMAN_DOC_COTTLE);
        sevenPlayerGame.player(7).selectCharacter(SAMUEL_T_ANDERS);
        assertFalse(select(WILLIAM_ADAMA).isValid(sevenPlayerGame));
        assertTrue(select(DANNA_BIERS).isValid(sevenPlayerGame));
    }

    static SelectCharacterAction select(Character character) {
        return new SelectCharacterAction(1, character);
    }

}