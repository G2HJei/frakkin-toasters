package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ARREST_ORDER;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POPULAR_INFLUENCE;

class PopularInfluenceEventProcessorTest extends EventTestHarness<PopularInfluenceEvent> {

    private static final SkillCard POPULAR_INFLUENCE_3 = new SkillCard(3, POPULAR_INFLUENCE);

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        player(1).gainSkillCards(POPULAR_INFLUENCE_3);
        player(2).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
    }

    // The player picks which card goes to the President.
    @Test
    void thePlayerDrawsTwoAndChoosesWhichToGive() {
        quorumDeck.nextCard(EULOGY, ARREST_ORDER);

        execute(new PopularInfluenceEvent(1, POPULAR_INFLUENCE_3));

        assertFollowup(one(new PopularInfluenceChoiceEvent(1, EULOGY, ARREST_ORDER),
                new PopularInfluenceChoiceEvent(1, ARREST_ORDER, EULOGY)));
        assertEquals(List.of(), presidentHand.cards());
    }

    @Test
    void withOneCardLeftItGoesToThePresident() {
        quorumDeck.clear();
        quorumDeck.addOnTop(EULOGY);

        execute(new PopularInfluenceEvent(1, POPULAR_INFLUENCE_3));

        assertEquals(List.of(EULOGY), presidentHand.cards());
    }

    @Test
    void notWithoutQuorumCardsLeft() {
        quorumDeck.clear();

        assertInvalid(new PopularInfluenceEvent(1, POPULAR_INFLUENCE_3));
    }
}
