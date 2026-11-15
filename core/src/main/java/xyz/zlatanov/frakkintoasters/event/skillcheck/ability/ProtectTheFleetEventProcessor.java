package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.activate;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.isValidActivation;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;

public class ProtectTheFleetEventProcessor extends EventProcessor<ProtectTheFleetEvent> {

    private static final int PILOTING_STRENGTH_NEEDED = 3;

    @Override
    protected boolean isValid() {
        if (event.activation() == null) {
            return true; // the player may decline
        }
        return pilotingStrength() >= PILOTING_STRENGTH_NEEDED
                && isValidActivation(game, event.activation(), fighter -> fighter.pilot() == null, true);
    }

    @Override
    public Followup process() {
        return event.activation() == null ? NONE : activate(ops, event.playerNumber(), event.activation());
    }

    private int pilotingStrength() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.dividedCards().stream()
                .filter(card -> card.type().color() == PILOTING)
                .mapToInt(skillCheck::cardValue)
                .sum();
    }
}
