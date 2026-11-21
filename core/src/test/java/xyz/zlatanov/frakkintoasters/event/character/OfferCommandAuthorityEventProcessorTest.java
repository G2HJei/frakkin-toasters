package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class OfferCommandAuthorityEventProcessorTest extends EventTestHarness<OfferCommandAuthorityEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        ops.skillCheck().start(WATER_SABOTAGED);
    }

    @Test
    void asksWilliamAdamaWhenTheCheckHasCards() {
        val card = new SkillCard(3, EXECUTIVE_ORDER);
        player(1).gainSkillCards(card);
        ops.skillCheck().playCards(1, card);
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().totalStrength(3);
        ops.skillCheck().determineResult(FAIL);

        execute(new OfferCommandAuthorityEvent());

        assertFollowup(one(new CommandAuthorityEvent(1), new NoOpEvent(1)));
    }

    // Nothing to take: the result came without cards, e.g. after "Political Prowess".
    @Test
    void nothingWithoutCards() {
        ops.skillCheck().determineResult(FAIL);

        execute(new OfferCommandAuthorityEvent());
    }
}
