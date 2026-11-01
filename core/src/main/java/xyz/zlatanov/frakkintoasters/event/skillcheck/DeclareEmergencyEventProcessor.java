package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DECLARE_EMERGENCY;

public class DeclareEmergencyEventProcessor extends EventProcessor<DeclareEmergencyEvent> {

    @Override
    protected boolean isValid() {
        val card = event.declareEmergencyCard();
        val skillCheck = game.activeSkillCheck();
        return card.type() == DECLARE_EMERGENCY
                && player.skillCards().cards().contains(card)
                && !skillCheck.emergencyDeclared()
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.declareEmergencyCard());
        ops.skillCheck().declareEmergency();
        return Followup.NONE;
    }
}
