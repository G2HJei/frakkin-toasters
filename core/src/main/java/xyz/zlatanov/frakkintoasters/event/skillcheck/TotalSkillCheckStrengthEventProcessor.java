package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playOrPassDecisions;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

public class TotalSkillCheckStrengthEventProcessor extends EventProcessor<TotalSkillCheckStrengthEvent> {

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.totalStrength() == null
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        val skillCheck = game.activeSkillCheck();
        val matchingStrength = skillCheck.matchingPile().cards().stream()
                .mapToInt(skillCheck::cardValue)
                .sum();
        val nonMatchingStrength = skillCheck.nonMatchingPile().cards().stream()
                .mapToInt(this::nonMatchingCardStrength)
                .sum();
        val totalStrength = matchingStrength + nonMatchingStrength + allHandsOnDeckBonus();
        ops.skillCheck().totalStrength(totalStrength);
        val followups = new ArrayList<Followup>();
        // reducing the difficulty is pointless when the printed difficulty is already reached
        if (totalStrength < skillCheck.type().difficulty()) {
            followups.addAll(playOrPassDecisions(game, DeclareEmergencyEvent.class));
        }
        followups.add(single(new DetermineSkillCheckResultEvent()));
        return all(followups.toArray(Followup[]::new));
    }

    // a non-matching card subtracts its strength, unless a card played before the check makes it count as positive
    private int nonMatchingCardStrength(SkillCard card) {
        val strength = game.activeSkillCheck().cardValue(card);
        return countsAsPositive(card) ? strength : -strength;
    }

    private boolean countsAsPositive(SkillCard card) {
        return switch (card.type().color()) {
            case ENGINEERING -> playedBefore(SCIENTIFIC_RESEARCH);
            case TREACHERY -> playedBefore(AT_ANY_COST);
            default -> false;
        };
    }

    // All Hands on Deck: for each skill card in this check with a strength of 0, add 1 to the check's total strength
    private int allHandsOnDeckBonus() {
        val skillCheck = game.activeSkillCheck();
        if (!skillCheck.resolvedAbilities().contains(ALL_HANDS_ON_DECK)) {
            return 0;
        }
        val zeroStrengthCards = Stream.concat(
                        skillCheck.matchingPile().cards().stream(),
                        skillCheck.nonMatchingPile().cards().stream())
                .filter(card -> skillCheck.cardValue(card) == 0)
                .count();
        return (int) zeroStrengthCards;
    }

    private boolean playedBefore(SkillCardType type) {
        return game.activeSkillCheck().playedBeforeSkillCheck().stream()
                .map(SkillCard::type)
                .anyMatch(type::equals);
    }
}
