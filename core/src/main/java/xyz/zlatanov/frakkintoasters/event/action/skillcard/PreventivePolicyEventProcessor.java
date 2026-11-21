package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.PREVENTIVE_POLICY;

// Every loss of a resource goes through AbstractOps.loseResource, which applies the reduction to the next one (rule
// 13.2: a resource is lost "for any reason").
public class PreventivePolicyEventProcessor extends SkillCardEventProcessor<PreventivePolicyEvent> {

    @Override
    public SkillCardType type() {
        return PREVENTIVE_POLICY;
    }

    @Override
    protected boolean isValidPlay() {
        return event.resource() != null;
    }

    @Override
    protected Followup resolve() {
        ops.reduceNextLoss(event.resource());
        return NONE;
    }
}
