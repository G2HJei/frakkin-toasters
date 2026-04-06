package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class PresidentsOfficeActionEventTest {

    @Test
    void shouldDraw2PoliticsCards() {
        val game = new Game(KOBOL, 3);
        new PresidentsOfficeActionEvent(1).apply(game);
        assertHas2PoliticsCards(game);
    }

    private void assertHas2PoliticsCards(Game game) {
        val colorOfCards = game.player(1)
                .skillCards().cards()
                .stream()
                .map(c -> c.type().color())
                .toList();
        assertEquals(List.of(POLITICS, POLITICS), colorOfCards);
    }
}