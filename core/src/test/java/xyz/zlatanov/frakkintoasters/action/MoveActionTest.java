package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ALL_HANDS_ON_DECK;

class MoveActionTest {

    Game game = new Game(KOBOL, 3);

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(CAPRICA_SIX);
        game.moveTo(ADMIRALS_QUARTERS, CAPRICA_SIX);
    }

    @Test
    void shouldMoveWithinSameShip() {
        new MoveAction(1, RESEARCH_LAB, null).execute(game);
        assertEquals(RESEARCH_LAB, game.locate(CAPRICA_SIX));
    }

    @Test
    void shouldDiscardToMoveBetweenShips() {
        val skillCard = new SkillCard(0, ALL_HANDS_ON_DECK);
        game.player(1).skillCards().add(skillCard);

        new MoveAction(1, PRESIDENTS_OFFICE, skillCard).execute(game);

        assertEquals(PRESIDENTS_OFFICE, game.locate(CAPRICA_SIX));
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
}