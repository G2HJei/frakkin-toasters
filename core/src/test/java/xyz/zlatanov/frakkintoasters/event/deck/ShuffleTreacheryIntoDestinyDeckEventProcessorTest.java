package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DRADIS_CONTACT;

class ShuffleTreacheryIntoDestinyDeckEventProcessorTest extends EventTestHarness<ShuffleTreacheryIntoDestinyDeckEvent> {

    @Test
    void shouldShuffleTreacheryCardsIntoDestinyDeck() {
        val bait = new SkillCard(1, BAIT);
        val dradisContact = new SkillCard(2, DRADIS_CONTACT);
        treacheryDeck.nextCard(bait, dradisContact);
        val destinyDeckSize = destinyDeck.size();

        execute(new ShuffleTreacheryIntoDestinyDeckEvent(2));

        assertEquals(destinyDeckSize + 2, destinyDeck.size());
        assertTrue(destinyDeck.cards().containsAll(List.of(bait, dradisContact)));
        assertFalse(treacheryDeck.cards().contains(bait));
        assertTrue(destinyDeck.wasShuffled());
    }
}
