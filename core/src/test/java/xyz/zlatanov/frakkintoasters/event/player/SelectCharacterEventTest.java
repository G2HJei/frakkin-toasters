package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

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
        executeAndAssertNoFollowup(select(KARA_STARBUCK_THRACE));
        assertEquals(KARA_STARBUCK_THRACE, player(1).character());
    }

    @Test
    void shouldPlaceInSetupLocation() {
        executeAndAssertNoFollowup(select(LOUANNE_KAT_KATRAINE));
        assertEquals(HANGAR_DECK, locate(LOUANNE_KAT_KATRAINE));
    }

    @Test
    void shouldReceiveMiracleToken() {
        executeAndAssertNoFollowup(select(CHIEF_GALEN_TYROL));
        assertTrue(player(1).hasMiracleToken());
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        selectCharacter(4, LOUANNE_KAT_KATRAINE);
        selectCharacter(3, KARA_STARBUCK_THRACE);
        selectCharacter(2, TOM_ZAREK);

        executeAndAssertFollowup(select(HELENA_CAIN),
                one(
                        new MoveEvent(1, PEGASUS_CIC, null),
                        new MoveEvent(1, COMMAND, null)));
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        executeAndAssertNoFollowup(select(KARL_HELO_AGATHON));
        assertNull(locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        executeAndAssertFollowup(select(LEE_APOLLO_ADAMA), single(new PlayerDecisionEvent<>(1, LaunchViperEvent.class)));
    }

    @Test
    void shouldNotAllowDoubleSelection() {
        executeAndAssertNoFollowup(select(KARA_STARBUCK_THRACE));
        assertInvalid(new SelectCharacterEvent(2, KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldNotAllowDoubleSelectionOfAlternateVersion() {
        selectCharacter(2, GAIUS_BALTAR);
        assertInvalid(select(GAIUS_BALTAR_ALT));
    }

    @Test
    void shouldObserveCharacterTypeLimits() {
        assertInvalid(select(LAURA_ROSLIN));
        assertInvalid(select(HELENA_CAIN));
        assertValid(select(BRENDAN_HOTDOG_COSTANZA));
        assertValid(select(GAIUS_BALTAR_ALT));
        assertValid(select(CAVIL));
    }

    @Test
    void shouldAllowOnlyOneCylonLeader() {
        selectCharacter(2, CAPRICA_SIX);
        assertInvalid(select(SIMON_ONEIL));
    }

    @Test
    void shouldNotAllowCylonLeaderIn3PlayerGame() {
        setUpGame();
        assertInvalid(select(SHARON_ATHENA_AGATHON));
    }

    @Test
    void shouldRequireCylonLeaderIn7PlayerGame() {
        setUpGame(Game.builder(7).build());
        selectCharacter(2, CHIEF_GALEN_TYROL);
        selectCharacter(3, ANASTASIA_DEE_DUALLA);
        selectCharacter(4, CALLANDRA_CALLY_TYROL);
        selectCharacter(5, GAIUS_BALTAR_ALT);
        selectCharacter(6, SHERMAN_DOC_COTTLE);
        selectCharacter(7, SAMUEL_T_ANDERS);
        assertInvalid(select(WILLIAM_ADAMA));
        assertValid(select(DANNA_BIERS));
    }

    static SelectCharacterEvent select(Character character) {
        return new SelectCharacterEvent(1, character);
    }

}