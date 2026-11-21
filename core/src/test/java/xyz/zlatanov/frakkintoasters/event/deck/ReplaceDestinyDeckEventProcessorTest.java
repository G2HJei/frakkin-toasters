package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class ReplaceDestinyDeckEventProcessorTest extends EventTestHarness<ReplaceDestinyDeckEvent> {

    @Test
    void shouldDiscardTheDestinyDeckAndBuildOneFromTreachery() {
        val politics = new SkillCard(1, CONSOLIDATE_POWER);
        destinyDeck.addOnTop(politics);

        execute(new ReplaceDestinyDeckEvent(TREACHERY, 6));

        assertEquals(6, game.decks().destiny().size());
        assertTrue(game.decks().destiny().cards().stream().allMatch(card -> card.type().color() == TREACHERY));
        assertEquals(List.of(politics), politicsDeck.discardedCards());
    }

    @Test
    void shouldRejectNoColor() {
        assertInvalid(new ReplaceDestinyDeckEvent(null, 6));
    }
}
