package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;


class SelectCharacterEventProcessorTest extends EventTestHarness<SelectCharacterEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
    }

    @Test
    void shouldSelectCharacter() {
        execute(select(KARA_STARBUCK_THRACE));
        assertEquals(KARA_STARBUCK_THRACE, player(1).character());
    }

    @Test
    void shouldPlaceInSetupLocation() {
        execute(select(LOUANNE_KAT_KATRAINE));
        assertEquals(HANGAR_DECK, locate(LOUANNE_KAT_KATRAINE));
    }

    @Test
    void shouldReceiveMiracleToken() {
        execute(select(CHIEF_GALEN_TYROL));
        assertTrue(player(1).hasMiracleToken());
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        player(4).character(LOUANNE_KAT_KATRAINE);
        player(3).character(KARA_STARBUCK_THRACE);
        player(2).character(TOM_ZAREK);

        execute(select(HELENA_CAIN));

        assertFollowup(
                one(
                        new MoveEvent(1, PEGASUS_CIC, null),
                        new MoveEvent(1, COMMAND, null)));
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        execute(select(KARL_HELO_AGATHON));
        assertThrows(AssertionError.class, () -> locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        execute(select(LEE_APOLLO_ADAMA));
        assertFollowup(new PlayerDecisionEvent<>(1, LaunchViperEvent.class));
    }

    @Test
    void shouldNotAllowDoubleSelection() {
        player(1).character(KARA_STARBUCK_THRACE);
        assertInvalid(new SelectCharacterEvent(2, KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldNotAllowDoubleSelectionOfAlternateVersion() {
        player(2).character(GAIUS_BALTAR);
        assertInvalid(select(GAIUS_BALTAR_ALT));
    }

    @Test
    void shouldObserveCharacterTypeLimits() {
        assertInvalid(select(LAURA_ROSLIN));
    }

    @Test
    void shouldObserveCharacterTypeLimits2() {
        assertInvalid(select(HELENA_CAIN));
    }

    @Test
    void shouldObserveCharacterTypeLimits3() {
        execute(select(GAIUS_BALTAR_ALT));
    }

    @Test
    void shouldAllowOnlyOneCylonLeader() {
        player(2).character(CAPRICA_SIX);
        assertInvalid(select(SIMON_ONEIL));
    }

    @Test
    void shouldNotAllowCylonLeaderIn3PlayerGame() {
        setUpGame();
        assertInvalid(select(SHARON_ATHENA_AGATHON));
    }

    @Test
    void shouldRequireCylonLeaderIn7PlayerGame() {
        setup7playerGame();
        execute(select(CAVIL));
    }

    @Test
    void shoudlForbidNonCylonLeaderIn7PlayerGame() {
        setup7playerGame();
        assertInvalid(select(WILLIAM_ADAMA));
    }

    private void setup7playerGame() {
        setUpGame(7);
        player(2).character(CHIEF_GALEN_TYROL);
        player(3).character(ANASTASIA_DEE_DUALLA);
        player(4).character(CALLANDRA_CALLY_TYROL);
        player(5).character(GAIUS_BALTAR_ALT);
        player(6).character(SHERMAN_DOC_COTTLE);
        player(7).character(SAMUEL_T_ANDERS);
    }

    static SelectCharacterEvent select(Character character) {
        return new SelectCharacterEvent(1, character);
    }

}