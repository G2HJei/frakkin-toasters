package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.SUPPORT_THE_PEOPLE;

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
        if (supportThePeoplePlayed()) {
            val events = game.players().stream()
                    .filter(PlayerView::isHuman)
                    .filter(p -> p.skillCards().size() < 4)
                    .map(PlayerView::number)
                    .map(n -> one(
                            new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, EventConstraint.DRAW_EXACTLY_2),
                            new NoOpEvent(n)))
                    .collect(Collectors.toCollection(ArrayList::new));
            events.add(single(new PlayFromDestinyDeckEvent()));
            return all(events.toArray(Followup[]::new));
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

    private boolean supportThePeoplePlayed() {
        return game.activeSkillCheck()
                .playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .anyMatch(SUPPORT_THE_PEOPLE::equals);
    }

    private boolean gutsAndInitiativeWasPlayed() {
        return game.activeSkillCheck()
                .playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .anyMatch(GUTS_AND_INITIATIVE::equals);
    }
}
