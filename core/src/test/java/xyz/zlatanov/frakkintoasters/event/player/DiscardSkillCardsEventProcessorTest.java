package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_3;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class DiscardSkillCardsEventProcessorTest extends EventTestHarness<DiscardSkillCardsEvent> {

    private final SkillCard politics    = new SkillCard(1, CONSOLIDATE_POWER);
    private final SkillCard leadership  = new SkillCard(2, EXECUTIVE_ORDER);
    private final SkillCard engineering = new SkillCard(3, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).gainSkillCards(politics, leadership, engineering);
    }

    @Test
    void shouldDiscardChosenCards() {
        execute(new DiscardSkillCardsEvent(1, List.of(politics, engineering)));

        assertSkillCards(1, leadership);
        assertEquals(List.of(politics), politicsDeck.discardedCards());
        assertEquals(List.of(engineering), engineeringDeck.discardedCards());
    }

    @Test
    void shouldDiscardExactNumberOfCards() {
        execute(new DiscardSkillCardsEvent(1, List.of(politics, leadership), DISCARD_EXACTLY_2));

        assertSkillCards(1, engineering);
    }

    @Test
    void shouldRejectFewerCardsThanRequired() {
        assertInvalid(new DiscardSkillCardsEvent(1, List.of(politics), DISCARD_EXACTLY_2));
    }

    @Test
    void shouldRejectMoreCardsThanRequired() {
        assertInvalid(new DiscardSkillCardsEvent(1, List.of(politics, leadership, engineering), DISCARD_EXACTLY_2));
    }

    @Test
    void shouldDiscardWholeHandWhenHavingFewerCardsThanRequired() {
        val engineering2 = new SkillCard(4, REPAIR);
        val politics2 = new SkillCard(5, CONSOLIDATE_POWER);
        player(2).gainSkillCards(engineering2, politics2);

        execute(new DiscardSkillCardsEvent(2, List.of(engineering2, politics2), DISCARD_EXACTLY_3));

        assertNoSkillCards(2);
    }

    @Test
    void shouldDiscardNothingWithEmptyHand() {
        execute(new DiscardSkillCardsEvent(2, List.of(), DISCARD_EXACTLY_2));

        assertNoSkillCards(2);
    }

    @Test
    void shouldRejectCardsNotInHand() {
        assertInvalid(new DiscardSkillCardsEvent(1, List.of(new SkillCard(4, REPAIR))));
    }

    @Test
    void shouldDiscardDownToTheHandLimit() {
        player(1).gainSkillCards(repairs(9)); // 12 cards

        execute(new DiscardSkillCardsEvent(1, List.of(politics, leadership), DISCARD_DOWN_TO_HAND_LIMIT));

        assertEquals(10, player(1).skillCards().size());
    }

    @Test
    void shouldRejectStayingOverTheHandLimit() {
        player(1).gainSkillCards(repairs(9));

        assertInvalid(new DiscardSkillCardsEvent(1, List.of(politics), DISCARD_DOWN_TO_HAND_LIMIT));
    }

    @Test
    void shouldRejectDiscardingBelowTheHandLimit() {
        player(1).gainSkillCards(repairs(9));

        assertInvalid(new DiscardSkillCardsEvent(1, List.of(politics, leadership, engineering), DISCARD_DOWN_TO_HAND_LIMIT));
    }

    // "Reckless - Your hand limit is 8 (instead of 10)."
    @Test
    void shouldDiscardDownToTheHandLimitOfTheCharacter() {
        player(2).character(CHIEF_GALEN_TYROL);
        val hand = repairs(9);
        player(2).gainSkillCards(hand);

        execute(new DiscardSkillCardsEvent(2, List.of(hand[0]), DISCARD_DOWN_TO_HAND_LIMIT));

        assertEquals(8, player(2).skillCards().size());
    }

    @Test
    void shouldRejectSameCardTwice() {
        assertInvalid(new DiscardSkillCardsEvent(1, List.of(politics, politics), DISCARD_EXACTLY_2));
    }

    @ParameterizedTest
    @EnumSource(value = SkillCardType.class, names = {"BAIT", "DRADIS_CONTACT"})
    void shouldDrawMutinyCardWhenChoosingToDiscard(SkillCardType type) {
        val treachery = new SkillCard(4, type);
        player(1).gainSkillCards(treachery);

        execute(new DiscardSkillCardsEvent(1, List.of(treachery, politics), DISCARD_EXACTLY_2));

        assertSkillCards(1, leadership, engineering);
        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void shouldDrawOnly1MutinyCardWhenDiscardingBaitAndDradisContact() {
        val bait = new SkillCard(4, BAIT);
        val dradisContact = new SkillCard(5, DRADIS_CONTACT);
        player(1).gainSkillCards(bait, dradisContact);

        execute(new DiscardSkillCardsEvent(1, List.of(bait, dradisContact), DISCARD_EXACTLY_2));

        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void shouldNotDrawMutinyCardForOtherTreacheryCards() {
        val treachery = new SkillCard(4, A_BETTER_MACHINE);
        player(1).gainSkillCards(treachery);

        execute(new DiscardSkillCardsEvent(1, List.of(treachery)));

        assertSkillCards(1, politics, leadership, engineering);
    }

    private static SkillCard[] repairs(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new SkillCard(100 + i, REPAIR))
                .toArray(SkillCard[]::new);
    }

    // The decks hold equal cards, e.g. 8 "Executive Order" 1s.
    @Test
    void shouldDiscardEqualCards() {
        player(1).gainSkillCards(leadership);

        execute(new DiscardSkillCardsEvent(1, List.of(leadership, leadership), DISCARD_EXACTLY_2));

        assertSkillCards(1, politics, engineering);
    }
}
