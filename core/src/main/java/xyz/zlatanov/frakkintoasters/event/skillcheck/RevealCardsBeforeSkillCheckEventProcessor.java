package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<ResolveCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        return shouldTieBreak() ?
                all(
                        new PlayerDecisionEvent<>(game.currentPlayerNumber(), TiebreakCardsBeforeSkillCheckEvent.class),
                        new PlayFromDestinyDeckEvent())
                : NONE;
    }

    private boolean shouldTieBreak() {
        val totalCardsCount = game.activeSkillCheck().playedBeforeSkillCheck().size();
        val uniqueCardsCount = game.activeSkillCheck().playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .collect(Collectors.toSet())
                .size();
        return totalCardsCount != uniqueCardsCount
                && totalCardsCount > 0;
    }
}
