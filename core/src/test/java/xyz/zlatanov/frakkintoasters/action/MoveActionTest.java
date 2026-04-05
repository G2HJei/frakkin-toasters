package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
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
    void shouldNotBeAbleToHazardousLocation() {
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

    @Test
    void shouldValidateMovementAdjacency() {
        //todo do for ass raptor too
        //todo merge bottom test with this one and make parameterized
        // test all variants - it is simple
        val viper = new Viper().pilot(KARA_STARBUCK_THRACE);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, viper);
        assertFalse(new MoveAction(1, GALACTICA_SPACE_6_OCLOCK, null).isValid(game));
        assertFalse(new MoveAction(1, GALACTICA_SPACE_8_OCLOCK, null).isValid(game));
        assertFalse(new MoveAction(1, GALACTICA_SPACE_10_OCLOCK, null).isValid(game));
    }

    @Test
    void shouldAllowDoubleMovementForViperMarkVii() {
        val viper = new ViperMarkVII().pilot(KARA_STARBUCK_THRACE);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, viper);

        new MoveAction(1, GALACTICA_SPACE_6_OCLOCK, null).execute(game);

        assertEquals(GALACTICA_SPACE_6_OCLOCK, game.locate(KARA_STARBUCK_THRACE));
    }
}