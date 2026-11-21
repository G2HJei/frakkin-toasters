package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.AttackCylonShipEventProcessor.fighter;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class ViperActivationRoundEventProcessor extends EventProcessor<ViperActivationRoundEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber()) && event.activationsLeft() > 0;
    }

    // The options are concrete events, answered by index: the player can't switch vipers or add an activation.
    @Override
    public Followup process() {
        val fighter = fighter(game, event.viperId())
                .filter(f -> f.pilot() == null || f.pilot() == player.character());
        if (fighter.isEmpty()) {
            return NONE; // gone, e.g. destroyed or landed
        }
        val activations = ActivateViperEventProcessor.activations(game, fighter.get(), event.attacksOnly());
        if (activations.isEmpty()) {
            return NONE;
        }
        val options = new ArrayList<Followup>();
        for (val activation : activations) {
            val activate = single(new GrantedViperActivationEvent(event.playerNumber(), activation));
            options.add(event.activationsLeft() > 1
                    ? all(activate, single(new ViperActivationRoundEvent(event.playerNumber(), event.viperId(),
                    event.activationsLeft() - 1, event.attacksOnly())))
                    : activate);
        }
        options.add(single(new NoOpEvent(event.playerNumber())));
        return one(options.toArray(Followup[]::new));
    }
}
