package xyz.zlatanov.frakkintoasters.state;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class GameTest {

    Game game = Game.builder(2).build();

    @Test
    void shouldCreateGame() {
        assertEquals(KOBOL, game.objective());
        assertEquals(2, game.players().size());
    }

    @Test
    void shouldStartSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertEquals(new SkillCheckHolder(ADMIRALS_QUARTERS), game.activeSkillCheck());
    }

}