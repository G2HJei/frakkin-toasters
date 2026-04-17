package xyz.zlatanov.frakkintoasters.event.placeholder.decisionconstraint;

import xyz.zlatanov.frakkintoasters.event.player.ReceiveSkillCardsEvent;

public class Draw2SkillCards implements DecisionConstraint<ReceiveSkillCardsEvent> {
    @Override
    public boolean validConstraint(ReceiveSkillCardsEvent event) {
        return event.selection().values().stream().reduce(0, Integer::sum) == 2;
    }
}
