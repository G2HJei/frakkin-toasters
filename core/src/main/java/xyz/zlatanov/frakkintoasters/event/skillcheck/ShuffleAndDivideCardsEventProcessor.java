package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RESTORE_ORDER;

public class ShuffleAndDivideCardsEventProcessor extends EventProcessor<ShuffleAndDivideCardsEvent> {

    @Override
    public Followup process() {
        val cardsWithAbilities = ops.skillCheck().shuffleAndDivideCards().stream()
                .filter(SkillCard::hasSkillCheckAbility)
                .toList();
        val followups = new ArrayList<Followup>();
        // "Restore Order": "Do not resolve skill check abilities while resolving this skill check."
        if (!cardsWithAbilities.isEmpty() && !playedBefore(RESTORE_ORDER)) {
            followups.add(single(new DetermineSkillCheckAbilitiesOrderEvent(game.turn().currentPlayerNumber(), cardsWithAbilities)));
        }
        // The skill check ability icon triggers the "consequence" result of a crisis, whatever happens to the
        // ability (rule 10.4.3).
        if (!cardsWithAbilities.isEmpty()) {
            followups.add(single(new ResolveConsequencesEvent()));
        }
        // After all skill abilities (rule 10.4.4).
        if (reckless()) {
            followups.add(single(new DrawAndResolveRecklessCardsEvent()));
        }
        followups.add(single(new TotalSkillCheckStrengthEvent()));
        return all(followups.toArray(Followup[]::new));
    }

    private boolean reckless() {
        return game.activeSkillCheck().playedBeforeSkillCheck().stream().anyMatch(card -> card.type().reckless());
    }

    private boolean playedBefore(SkillCardType type) {
        return game.activeSkillCheck().playedBeforeSkillCheck().stream().anyMatch(card -> card.type() == type);
    }
}
