package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CHANGE_OF_PLANS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

public class ChangeOfPlansEventProcessor extends EventProcessor<ChangeOfPlansEvent> {

    @Override
    protected boolean isValid() {
        val card = event.changeOfPlansCard();
        val skillCheck = game.activeSkillCheck();
        return card.type() == CHANGE_OF_PLANS
                && player.skillCards().cards().contains(card)
                && skillCheck.result() == PASS
                && !skillCheck.passResultReplaced();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.changeOfPlansCard());
        ops.skillCheck().replacePassResult();
        return all(game.players().stream()
                .filter(PlayerView::isHuman)
                .map(p -> new PlayerDecisionEvent<>(p.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
                .toArray(Event[]::new));
    }
}
