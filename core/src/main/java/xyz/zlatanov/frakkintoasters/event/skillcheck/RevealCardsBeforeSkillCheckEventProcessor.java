package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        if (gutsAndInitiativeWasPlayed()) {
            return NONE;
        }
        if (shouldTieBreak()) {
            return all(
                    new PlayerDecisionEvent<>(game.currentPlayerNumber(), TiebreakCardsBeforeSkillCheckEvent.class),
                    new PlayFromDestinyDeckEvent());
        }
        return all(new PlayFromDestinyDeckEvent());
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

    private boolean gutsAndInitiativeWasPlayed() {
        return game.activeSkillCheck()
                .playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .anyMatch(GUTS_AND_INITIATIVE::equals);
    }
}
