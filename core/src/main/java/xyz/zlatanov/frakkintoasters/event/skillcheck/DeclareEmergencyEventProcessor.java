package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DECLARE_EMERGENCY;

public class DeclareEmergencyEventProcessor extends EventProcessor<DeclareEmergencyEvent> {

    @Override
    protected boolean isValid() {
        val card = event.declareEmergencyCard();
        return card != null
                && card.type() == DECLARE_EMERGENCY
                && player.skillCards().cards().contains(card)
                && canPlay(game);
    }

    // Limit of 1 "Declare Emergency" card used per skill check.
    public static boolean canPlay(GameView game) {
        val skillCheck = game.activeSkillCheck();
        return skillCheck != null
                && !skillCheck.emergencyDeclared()
                && skillCheck.totalStrength() != null
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.declareEmergencyCard());
        ops.skillCheck().declareEmergency();
        return Followup.NONE;
    }
}
