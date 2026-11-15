package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

// "The current player may search the Destiny deck and choose 2 cards to discard. He then reshuffles the Destiny deck."
class DiscardFromDestinyDeckEventProcessorTest extends EventTestHarness<DiscardFromDestinyDeckEvent> {

    private SkillCard first;
    private SkillCard second;
    private SkillCard third;

    @BeforeEach
    void setUp() {
        first = new SkillCard(1, CONSOLIDATE_POWER);
        second = new SkillCard(2, INVESTIGATIVE_COMMITTEE);
        third = new SkillCard(3, SUPPORT_THE_PEOPLE);
        destinyDeck.addOnTop(first, second, third);
    }

    @Test
    void shouldDiscardTheChosenCardsAndKeepTheRest() {
        execute(new DiscardFromDestinyDeckEvent(1, List.of(second, third)));

        assertEquals(List.of(first), destinyDeck.cards());
        assertEquals(List.of(second, third), politicsDeck.discardedCards());
    }

    @Test
    void shouldReshuffleTheDeck() {
        val before = journal.facts().size();

        execute(new DiscardFromDestinyDeckEvent(1, List.of(first, second)));

        assertEquals(before + 3, journal.facts().size()); // 2 discards and the shuffle
    }

    // The next skill check draws 2 from the deck before it can make a new one, so it can't be left empty.
    @Test
    void shouldMakeANewDeckWhenTheLastCardsAreDiscarded() {
        destinyDeck.clear();
        destinyDeck.addOnTop(first, second);

        execute(new DiscardFromDestinyDeckEvent(1, List.of(first, second)));

        assertEquals(12, destinyDeck.size()); // 2 of each color (rule A3.5.1)
        assertEquals(List.of(first, second), politicsDeck.discardedCards());
    }

    @Test
    void shouldChooseExactlyTwoCards() {
        assertInvalid(new DiscardFromDestinyDeckEvent(1, List.of(first)));
    }

    @Test
    void shouldChooseCardsThatAreInTheDeck() {
        assertInvalid(new DiscardFromDestinyDeckEvent(1, List.of(first, new SkillCard(5, RED_TAPE))));
    }

    @Test
    void shouldNotChooseTheSameCardTwice() {
        assertInvalid(new DiscardFromDestinyDeckEvent(1, List.of(first, first)));
    }
}
