package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;


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
                        new PlaceInSetupLocationEvent(1, PEGASUS_CIC),
                        new PlaceInSetupLocationEvent(1, COMMAND)));
    }

    // A Colonial One start after "Bomb on Colonial One": any Galactica location that isn't hazardous.
    @Test
    void shouldStartInSickbayWhenColonialOneIsDestroyed() {
        ops.keepCrisisCardInPlay(SkillCheckType.BOMB_ON_COLONIAL_1);

        execute(select(LAURA_ROSLIN));

        // "the character starts in "Sickbay" instead" (rule 7.4)
        assertEquals(SICKBAY, locate(LAURA_ROSLIN));
    }

    // Tom Zarek (alt.)'s "Disreputable": he starts the game holding a Mutiny Card.
    @Test
    void disreputableZarekDrawsAMutinyCard() {
        execute(select(TOM_ZAREK_ALT));

        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        execute(select(KARL_HELO_AGATHON));
        assertTrue(game.locate(KARL_HELO_AGATHON).isEmpty());
    }

    @Test
    void athenaStartsTheGameInfiltrating() {
        execute(select(SHARON_ATHENA_AGATHON));

        assertTrue(player(1).isInfiltrating());
        assertEquals(HANGAR_DECK, locate(SHARON_ATHENA_AGATHON));
    }

    @Test
    void anotherCylonLeaderStartsTheGameRevealed() {
        execute(select(CAVIL));

        assertFalse(player(1).isInfiltrating());
        assertEquals(CYLON_FLEET, locate(CAVIL));
    }

    @Test
    void shouldFollowupForApollo() {
        execute(select(LEE_APOLLO_ADAMA));
        assertFollowup(new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER));
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
    void shouldAllowEveryTypeTiedForMostPlentiful() {
        execute(select(LAURA_ROSLIN));
        assertEquals(LAURA_ROSLIN, player(1).character());
    }

    @Test
    void shouldObserveCharacterTypeLimits() {
        player(2).character(TORY_FOSTER);
        assertInvalid(select(LAURA_ROSLIN));
    }

    @Test
    void shouldObserveCharacterTypeLimits2() {
        player(2).character(SAUL_TIGH);
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

    // The last player to choose in a 7 player game has to be the Cylon Leader if nobody else is.
    @Test
    void shoudlForbidNonCylonLeaderIn7PlayerGame() {
        setup7playerGame();
        assertInvalid(select(WILLIAM_ADAMA));
    }

    @Test
    void shouldAllowAHumanBeforeTheLastSeatOfA7PlayerGame() {
        setUpGame(7);
        player(2).character(CHIEF_GALEN_TYROL);

        execute(select(ANASTASIA_DEE_DUALLA));

        assertEquals(ANASTASIA_DEE_DUALLA, player(1).character());
    }

    @Test
    void shouldAllowAHumanInTheLastSeatOnceTheCylonLeaderIsChosen() {
        setUpGame(7);
        player(2).character(CHIEF_GALEN_TYROL);
        player(3).character(ANASTASIA_DEE_DUALLA);
        player(4).character(CALLANDRA_CALLY_TYROL);
        player(5).character(GAIUS_BALTAR_ALT);
        player(6).character(SHERMAN_DOC_COTTLE);
        player(7).character(CAVIL);

        execute(select(WILLIAM_ADAMA));
    }

    // Boomer and Athena aren't versions of one character: both may be in the same game (rule 6.2.4).
    @Test
    void shouldAllowBoomerAndAthenaInTheSameGame() {
        player(2).character(SHARON_BOOMER_VALERII);

        execute(select(SHARON_ATHENA_AGATHON));
    }

    @Test
    void shouldNotSelectASecondCharacter() {
        player(1).character(WILLIAM_ADAMA);
        assertInvalid(select(SAUL_TIGH));
    }

    // "He may choose any character without regard to character type" (rule 7.1). With 3 political leaders taken, the
    // type isn't the most plentiful any more.
    @Test
    void shouldIgnoreTheCharacterTypeAfterAnExecution() {
        player(2).character(LEE_ADAMA);
        player(3).character(TOM_ZAREK);
        player(4).character(LAURA_ROSLIN);
        assertFalse(SelectCharacterEventProcessor.canBeSelected(game, ELLEN_TIGH)); // at setup

        game.turn().number(5);
        execute(select(ELLEN_TIGH));

        assertEquals(ELLEN_TIGH, player(1).character());
    }

    // "This character may no longer be used during the game."
    @Test
    void shouldNotSelectAnExecutedCharacterOrItsOtherVersion() {
        game.turn().number(5);
        game.removedComponents().add(GAIUS_BALTAR);

        assertFalse(SelectCharacterEventProcessor.canBeSelected(game, GAIUS_BALTAR_ALT));
        assertInvalid(select(GAIUS_BALTAR));
    }

    @Test
    void shouldNotSelectACylonLeaderAfterAnExecution() {
        game.turn().number(5);
        assertInvalid(select(CAVIL));
    }

    // "If there are no vipers available in the reserves, he is instead placed on the "Hangar Deck."" (rule A2.1.1.12)
    @Test
    void shouldPlaceApolloOnTheHangarDeckWithoutAViperInTheReserves() {
        for (int i = 0; i < 6; i++) {
            ops.galactica().launchHumanFighter(VIPER, GALACTICA_SPACE_5_OCLOCK, null);
        }

        execute(select(LEE_APOLLO_ADAMA));

        assertEquals(HANGAR_DECK, locate(LEE_APOLLO_ADAMA));
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