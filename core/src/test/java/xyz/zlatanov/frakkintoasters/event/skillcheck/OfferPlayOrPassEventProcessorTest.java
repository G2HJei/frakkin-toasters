package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DECLARE_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class OfferPlayOrPassEventProcessorTest extends EventTestHarness<OfferPlayOrPassEvent> {

    private static final SkillCard DECLARE_EMERGENCY_3 = new SkillCard(3, DECLARE_EMERGENCY);

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LAURA_ROSLIN);
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS).totalStrength(5);
    }

    @Test
    void aHolderMayPlayTheCardOrPass() {
        player(1).gainSkillCards(DECLARE_EMERGENCY_3);

        execute(new OfferPlayOrPassEvent(1, DECLARE_EMERGENCY));

        assertFollowup(one(new PlayerDecisionEvent<>(1, DeclareEmergencyEvent.class), new NoOpEvent(1)));
    }

    // He is asked all the same, so the table doesn't learn that he holds none (F-7c295f).
    @Test
    void aPlayerWithoutTheCardIsOfferedOnlyThePass() {
        player(2).gainSkillCards(new SkillCard(1, REPAIR));

        execute(new OfferPlayOrPassEvent(2, DECLARE_EMERGENCY));

        assertFollowup(new Followup.OneOf(Set.of(single(new NoOpEvent(2)))));
    }

    // The offer is built when he is asked: a "Declare Emergency" another player played first leaves only the pass.
    @Test
    void aCardThatCanNoLongerBePlayedLeavesOnlyThePass() {
        player(1).gainSkillCards(DECLARE_EMERGENCY_3);
        skillCheck.declareEmergency();

        execute(new OfferPlayOrPassEvent(1, DECLARE_EMERGENCY));

        assertFollowup(new Followup.OneOf(Set.of(single(new NoOpEvent(1)))));
    }

    @Test
    void onlyForTheCardsPlayedOrPassed() {
        assertInvalid(new OfferPlayOrPassEvent(1, REPAIR));
    }

    @Test
    void notForNoCard() {
        assertInvalid(new OfferPlayOrPassEvent(1, null));
    }
}
