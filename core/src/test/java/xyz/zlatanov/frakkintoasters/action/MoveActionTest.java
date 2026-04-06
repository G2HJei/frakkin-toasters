package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.PilotableShip;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ALL_HANDS_ON_DECK;

class MoveActionTest {

    Game game = new Game(KOBOL, 3);

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldMoveWithinSameShip() {
        new MoveAction(1, RESEARCH_LAB, null).execute(game);
        assertEquals(RESEARCH_LAB, game.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldDiscardToMoveBetweenShips() {
        val skillCard = new SkillCard(0, ALL_HANDS_ON_DECK);
        game.player(1).skillCards().add(skillCard);

        new MoveAction(1, PRESIDENTS_OFFICE, skillCard).execute(game);

        assertEquals(PRESIDENTS_OFFICE, game.locate(KARA_STARBUCK_THRACE));
        assertTrue(game.player(1).skillCards().cards().isEmpty());
        assertEquals(skillCard, game.decks().leadership().lastDiscarded());
    }

    @Test
    void shouldNotBeAbleToHazardousLocations() {
        assertFalse(new MoveAction(1, BRIG, null).isValid(game));
    }

    @Test
    void shouldNotAllowHumansMoveToCylonLocations() {
        assertFalse(new MoveAction(1, CAPRICA, new SkillCard(0, ALL_HANDS_ON_DECK)).isValid(game));
    }

    @Test
    void shouldMoveInSpaceWhilePiloting() {
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, new AssaultRaptor().pilot(KARA_STARBUCK_THRACE));
        new MoveAction(1, GALACTICA_SPACE_4_OCLOCK, null).execute(game);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, game.locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldLandWhilePiloting() {
        val viper = new Viper().pilot(KARA_STARBUCK_THRACE);
        val skillCard = new SkillCard(0, ALL_HANDS_ON_DECK);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, viper);
        game.player(1).skillCards().add(skillCard);

        new MoveAction(1, PRESIDENTS_OFFICE, skillCard).execute(game);

        assertEquals(PRESIDENTS_OFFICE, game.locate(KARA_STARBUCK_THRACE));
        assertTrue(game.boards().galactica().reserves().contains(viper));
        assertNull(viper.pilot());
    }

    public static Stream<Arguments> adjacencyTests() {
        return Stream.of(
                argumentSet("viper to same space", new Viper(), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("viper to space 4", new Viper(), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("viper to space 6", new Viper(), GALACTICA_SPACE_6_OCLOCK, false),
                argumentSet("viper to space 8", new Viper(), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("viper to space 10", new Viper(), GALACTICA_SPACE_10_OCLOCK, false),
                argumentSet("viper to space 12", new Viper(), GALACTICA_SPACE_12_OCLOCK, true),
                argumentSet("Assault raptor to same space", new AssaultRaptor(), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("Assault raptor to space 4", new AssaultRaptor(), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("Assault raptor to space 6", new AssaultRaptor(), GALACTICA_SPACE_6_OCLOCK, false),
                argumentSet("Assault raptor to space 8", new AssaultRaptor(), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("Assault raptor to space 10", new AssaultRaptor(), GALACTICA_SPACE_10_OCLOCK, false),
                argumentSet("Assault raptor to space 12", new AssaultRaptor(), GALACTICA_SPACE_12_OCLOCK, true),
                argumentSet("Viper Mk7 to same space", new ViperMarkVII(), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("Viper Mk7 to space 4", new ViperMarkVII(), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("Viper Mk7 to space 6", new ViperMarkVII(), GALACTICA_SPACE_6_OCLOCK, true),
                argumentSet("Viper Mk7 to space 8", new ViperMarkVII(), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("Viper Mk7 to space 10", new ViperMarkVII(), GALACTICA_SPACE_10_OCLOCK, true),
                argumentSet("Viper Mk7 to space 12", new ViperMarkVII(), GALACTICA_SPACE_12_OCLOCK, true)
        );
    }

    @ParameterizedTest
    @MethodSource("adjacencyTests")
    void shouldValidateMovementAdjacency(PilotableShip ship, Location destination, boolean isValid) {
        ship.pilot(KARA_STARBUCK_THRACE);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, ship);
        assertEquals(isValid, new MoveAction(1, destination, null).isValid(game));
    }
}