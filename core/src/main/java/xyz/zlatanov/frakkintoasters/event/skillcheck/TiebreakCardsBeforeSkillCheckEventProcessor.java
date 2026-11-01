package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.skillcheck.TiebreakCardsBeforeSkillCheckEvent.PlayedCard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RESTORE_ORDER;

public class TiebreakCardsBeforeSkillCheckEventProcessor extends EventProcessor<TiebreakCardsBeforeSkillCheckEvent> {

    @Override
    protected boolean isValid() {
        val conflictGroups = conflictGroups();
        val keptCards = event.keptCards();
        val onlyConflictingCardsKept = keptCards.stream()
                .allMatch(kept -> conflictGroups.stream().anyMatch(group -> group.contains(kept)));
        val oneCardKeptPerConflictGroup = conflictGroups.stream()
                .allMatch(group -> group.stream().filter(keptCards::contains).count() == 1);
        return onlyConflictingCardsKept && oneCardKeptPerConflictGroup;
    }

    @Override
    public Followup process() {
        conflictGroups().stream()
                .flatMap(Collection::stream)
                .filter(card -> !event.keptCards().contains(card))
                .forEach(card -> ops.skillCheck().giveCardToPlayer(card.skillCard(), card.playerNumber()));
        return Followup.NONE;
    }

    private List<List<PlayedCard>> conflictGroups() {
        val playedCards = playedCards();
        val recklessAndRestoreOrderCards = playedCards.stream()
                .filter(this::isRecklessOrRestoreOrder)
                .toList();
        val sameTypeCards = playedCards.stream()
                .filter(c -> !isRecklessOrRestoreOrder(c))
                .collect(groupingBy(c -> c.skillCard().type()))
                .values();
        return Stream.concat(Stream.of(recklessAndRestoreOrderCards), sameTypeCards.stream())
                .filter(group -> group.size() > 1)
                .toList();
    }

    private List<PlayedCard> playedCards() {
        return game.players().stream()
                .flatMap(p -> game.activeSkillCheck()
                        .playedBeforeSkillCheck(p.number())
                        .stream()
                        .map(c -> new PlayedCard(p.number(), c)))
                .toList();
    }

    private boolean isRecklessOrRestoreOrder(PlayedCard card) {
        val type = card.skillCard().type();
        return type.reckless() || type == RESTORE_ORDER;
    }
}
