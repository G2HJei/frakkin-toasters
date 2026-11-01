package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Collection;

public class TiebreakCardsBeforeSkillCheckEventProcessor extends EventProcessor<TiebreakCardsBeforeSkillCheckEvent> {

    @Override
    protected boolean isValid() {
        val conflictGroups = game.activeSkillCheck().playedBeforeConflicts();
        val keptCards = event.keptCards();
        val onlyConflictingCardsKept = keptCards.stream()
                .allMatch(kept -> conflictGroups.stream().anyMatch(group -> group.contains(kept)));
        val oneCardKeptPerConflictGroup = conflictGroups.stream()
                .allMatch(group -> group.stream().filter(keptCards::contains).count() == 1);
        return onlyConflictingCardsKept && oneCardKeptPerConflictGroup;
    }

    @Override
    public Followup process() {
        game.activeSkillCheck().playedBeforeConflicts().stream()
                .flatMap(Collection::stream)
                .filter(card -> !event.keptCards().contains(card))
                .forEach(card -> ops.skillCheck().giveCardToPlayer(card.skillCard(), card.playerNumber()));
        return Followup.NONE;
    }
}
