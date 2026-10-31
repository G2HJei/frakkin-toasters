package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.SUPPORT_THE_PEOPLE;

public class ResolveCardsBeforeSkillCheckEventProcessor extends EventProcessor<ResolveCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        val followUps = new ArrayList<Followup>();
        if (cardInPlay(SUPPORT_THE_PEOPLE)) {
            game.players().stream()
                    .filter(PlayerView::isHuman)
                    .filter(p -> p.skillCards().size() < 4)
                    .map(PlayerView::number)
                    .map(n -> one(
                            new PlayerDecisionEvent<>(n, DrawSkillCardsEvent.class, EventConstraint.DRAW_EXACTLY_2),
                            new NoOpEvent(n)))
                    .forEach(followUps::add);

        }
        if (!cardInPlay(GUTS_AND_INITIATIVE)) {
            followUps.add(single(new PlayFromDestinyDeckEvent()));
        }
        return all(followUps.toArray(Followup[]::new));
    }

    private boolean cardInPlay(SkillCardType skillCardType) {
        return game.activeSkillCheck()
                .playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .anyMatch(skillCardType::equals);
    }
}
