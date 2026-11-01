package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RESTORE_ORDER;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().revealPlayedBefore();
        return shouldTieBreak() ?
                single(new PlayerDecisionEvent<>(game.currentPlayerNumber(), TiebreakCardsBeforeSkillCheckEvent.class))
                : NONE;
    }

    private boolean shouldTieBreak() {
        val playedBefore = game.activeSkillCheck().playedBeforeSkillCheck();
        val uniqueCardsCount = playedBefore.stream()
                .map(SkillCard::type)
                .collect(Collectors.toSet())
                .size();
        val recklessAndRestoreOrderCards = playedBefore.stream()
                .filter(c -> c.type().reckless() || c.type() == RESTORE_ORDER)
                .count();
        return playedBefore.size() != uniqueCardsCount
                || recklessAndRestoreOrderCards > 1;
    }
}
