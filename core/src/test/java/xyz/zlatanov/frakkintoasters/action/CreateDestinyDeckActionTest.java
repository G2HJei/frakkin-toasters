package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;

import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

class CreateDestinyDeckActionTest {

    @Test
    void shouldCreateValidDeck() {
        val game = new Game(ObjectiveCard.KOBOL, 3);
        new CreateDestinyDeckAction().execute(game);
        assertDestinyDeckComposition(game);
    }

    private static void assertDestinyDeckComposition(Game game) {
        val expectedDistribution = Map.of(
                POLITICS, 2,
                LEADERSHIP, 2,
                TACTICS, 2,
                PILOTING, 2,
                ENGINEERING, 2,
                TREACHERY, 2);
        val actualDistribution = game.decks().destiny().cards()
                .stream()
                .collect(groupingBy(
                        card -> card.type().color(),
                        summingInt(e -> 1)
                ));
        assertEquals(expectedDistribution, actualDistribution);
    }

}