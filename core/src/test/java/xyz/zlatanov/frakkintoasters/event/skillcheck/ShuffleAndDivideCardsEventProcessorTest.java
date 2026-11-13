package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ShuffleAndDivideCardsEventProcessorTest extends EventTestHarness<ShuffleAndDivideCardsEvent> {

    @Test
    void shouldDivideCardsAndSortThemByColorAndValue() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .cards()
                .addOnTop(
                        new SkillCard(0, EXECUTIVE_ORDER),
                        new SkillCard(0, TEST_THE_LIMITS),
                        new SkillCard(1, CONSOLIDATE_POWER),
                        new SkillCard(1, IRON_WILL),
                        new SkillCard(1, A_SECOND_CHANCE),
                        new SkillCard(1, REPAIR),
                        new SkillCard(2, AT_ANY_COST),
                        new SkillCard(2, REPAIR),
                        new SkillCard(3, LAUNCH_SCOUT),
                        new SkillCard(3, DRADIS_CONTACT),
                        new SkillCard(3, BAIT),
                        new SkillCard(4, POLITICAL_PROWESS),
                        new SkillCard(4, COMBAT_VETERAN),
                        new SkillCard(5, DOGFIGHT)
                );

        execute(new ShuffleAndDivideCardsEvent());

        assertEquals(List.of(
                        //leadership
                        new SkillCard(2, AT_ANY_COST),
                        new SkillCard(1, IRON_WILL),
                        new SkillCard(0, EXECUTIVE_ORDER),
                        //tactics
                        new SkillCard(3, LAUNCH_SCOUT),
                        new SkillCard(1, A_SECOND_CHANCE)
                ),
                game.activeSkillCheck().matchingPile().cards());

        assertEquals(List.of(
                        //politics
                        new SkillCard(4, POLITICAL_PROWESS),
                        new SkillCard(1, CONSOLIDATE_POWER),
                        //piloting
                        new SkillCard(5, DOGFIGHT),
                        new SkillCard(4, COMBAT_VETERAN),
                        //engineering
                        new SkillCard(2, REPAIR),
                        new SkillCard(1, REPAIR),
                        new SkillCard(0, TEST_THE_LIMITS),
                        //treachery
                        new SkillCard(3, BAIT),
                        new SkillCard(3, DRADIS_CONTACT)
                ),
                game.activeSkillCheck().nonMatchingPile().cards());

        assertFollowup(all(
                new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                        new SkillCard(1, IRON_WILL),
                        new SkillCard(5, DOGFIGHT),
                        new SkillCard(3, BAIT),
                        new SkillCard(3, DRADIS_CONTACT))),
                new ResolveConsequencesEvent(),
                new TotalSkillCheckStrengthEvent()));
    }

    @Test
    void shouldTotalTheStrengthWhenNoCardHasAnAbility() {
        game.startSkillCheck(ADMIRALS_QUARTERS).cards().addOnTop(new SkillCard(3, LAUNCH_SCOUT));

        execute(new ShuffleAndDivideCardsEvent());

        assertFollowup(new TotalSkillCheckStrengthEvent());
    }

    // "Restore Order": "Do not resolve skill check abilities while resolving this skill check." The consequence of a
    // crisis is triggered by the icon, not by the ability.
    @Test
    void shouldNotResolveAbilitiesAfterRestoreOrder() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .playedBefore(2, List.of(new SkillCard(3, RESTORE_ORDER)))
                .cards().addOnTop(new SkillCard(5, DOGFIGHT));

        execute(new ShuffleAndDivideCardsEvent());

        assertFollowup(all(new ResolveConsequencesEvent(), new TotalSkillCheckStrengthEvent()));
    }

    // After resolving all skill abilities during a Reckless skill check (rule 10.4.4).
    @Test
    void shouldRevealTreacheryCardsInARecklessCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS)
                .playedBefore(2, List.of(new SkillCard(1, JURY_RIGGED)))
                .cards().addOnTop(new SkillCard(5, DOGFIGHT));

        execute(new ShuffleAndDivideCardsEvent());

        assertFollowup(all(
                new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(new SkillCard(5, DOGFIGHT))),
                new ResolveConsequencesEvent(),
                new DrawAndResolveRecklessCardsEvent(),
                new TotalSkillCheckStrengthEvent()));
    }

}