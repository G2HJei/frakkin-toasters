package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;


class SelectCharacterEventTest extends EventTest {

    @BeforeEach
    void setUp() {
        setUpGame(Game.builder(4).build());
    }

    @Test
    void shouldSelectCharacter() {
        execute(select(KARA_STARBUCK_THRACE));
        assertEquals(KARA_STARBUCK_THRACE, player(1).character());
    }

    @Test
    void shouldPlaceInSetupLocation() {
        execute(select(LOUANNE_KAT_KATRAINE));
        assertEquals(HANGAR_DECK, game.locate(LOUANNE_KAT_KATRAINE));
    }

    @Test
    void shouldReceiveMiracleToken() {
        execute(select(CHIEF_GALEN_TYROL));
        assertTrue(player(1).hasMiracleToken());
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        player(4).selectCharacter(LOUANNE_KAT_KATRAINE);
        player(3).selectCharacter(KARA_STARBUCK_THRACE);
        player(2).selectCharacter(TOM_ZAREK);

        val followup = execute(select(HELENA_CAIN));

        val expected = one(
                new MoveEvent(1, PEGASUS_CIC, null),
                new MoveEvent(1, COMMAND, null));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        execute(select(KARL_HELO_AGATHON));
        assertNull(game.locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        val followup = execute(select(LEE_APOLLO_ADAMA));
        val expected = single(new PlayerDecisionEvent<>(1, LaunchViperEvent.class));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotAllowDoubleSelection() {
        execute(select(KARA_STARBUCK_THRACE));
        val invalidAction = new SelectCharacterEvent(2, KARA_STARBUCK_THRACE);
        assertThrows(InvalidActionException.class, () -> execute(invalidAction));
    }

    @Test
    void shouldNotAllowDoubleSelectionOfAlternateVersion() {
        player(2).selectCharacter(GAIUS_BALTAR);
        assertFalse(isValid(select(GAIUS_BALTAR_ALT)));
    }

    @Test
    void shouldObserveCharacterTypeLimits() {
        assertFalse(isValid(select(LAURA_ROSLIN)));
        assertFalse(isValid(select(HELENA_CAIN)));
        assertTrue(isValid(select(BRENDAN_HOTDOG_COSTANZA)));
        assertTrue(isValid(select(GAIUS_BALTAR_ALT)));
        assertTrue(isValid(select(CAVIL)));
    }

    @Test
    void shouldAllowOnlyOneCylonLeader() {
        player(2).selectCharacter(CAPRICA_SIX);
        assertFalse(isValid(select(SIMON_ONEIL)));
    }

    @Test
    void shouldNotAllowCylonLeaderIn3PlayerGame() {
        setUpGame();
        assertFalse(isValid(select(SHARON_ATHENA_AGATHON)));
    }

    @Test
    void shouldRequireCylonLeaderIn7PlayerGame() {
        setUpGame(Game.builder(7).build());
        player(2).selectCharacter(CHIEF_GALEN_TYROL);
        player(3).selectCharacter(ANASTASIA_DEE_DUALLA);
        player(4).selectCharacter(CALLANDRA_CALLY_TYROL);
        player(5).selectCharacter(GAIUS_BALTAR_ALT);
        player(6).selectCharacter(SHERMAN_DOC_COTTLE);
        player(7).selectCharacter(SAMUEL_T_ANDERS);
        assertFalse(isValid(select(WILLIAM_ADAMA)));
        assertTrue(isValid(select(DANNA_BIERS)));
    }

    static SelectCharacterEvent select(Character character) {
        return new SelectCharacterEvent(1, character);
    }

}