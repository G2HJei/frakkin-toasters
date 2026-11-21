package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class TreatmentEventProcessorTest extends EventTestHarness<TreatmentEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT   = new SkillCard(2, LAUNCH_SCOUT);
    private static final SkillCard REPAIRS = new SkillCard(3, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(SHERMAN_DOC_COTTLE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(RESEARCH_LAB, SHERMAN_DOC_COTTLE);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // William Adama's skill set is Leadership and Tactics. The Doc then picks 2 cards of his hand to give.
    @Test
    void drawsFromTheSkillSetOfTheTargetThenOffersTheCardsToGive() {
        player(1).gainSkillCards(REPAIRS);
        leadershipDeck.nextCard(ORDER);
        tacticsDeck.nextCard(SCOUT);

        execute(new TreatmentEvent(1, 2, List.of(LEADERSHIP, TACTICS)));

        assertSkillCards(1, REPAIRS, ORDER, SCOUT);
        assertFollowup(one(
                new TreatmentGiveEvent(1, 2, List.of(ORDER, REPAIRS)),
                new TreatmentGiveEvent(1, 2, List.of(SCOUT, REPAIRS)),
                new TreatmentGiveEvent(1, 2, List.of(ORDER, SCOUT))));
    }

    @Test
    void notColorsOutsideTheTargetsSkillSet() {
        assertInvalid(new TreatmentEvent(1, 2, List.of(POLITICS, TACTICS)));
    }

    @Test
    void notHimself() {
        assertInvalid(new TreatmentEvent(1, 1, List.of(TACTICS, TACTICS)));
    }

    @Test
    void notARevealedCylon() {
        val target = player(2);
        target.loyaltyCards().addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE)
                .reveal(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE);

        assertInvalid(new TreatmentEvent(1, 2, List.of(LEADERSHIP, TACTICS)));
    }
}
