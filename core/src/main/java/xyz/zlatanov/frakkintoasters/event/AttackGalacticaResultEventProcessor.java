package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static xyz.zlatanov.frakkintoasters.event.AttackGalacticaEventProcessor.canAttackGalactica;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class AttackGalacticaResultEventProcessor extends EventProcessor<AttackGalacticaResultEvent> {

    private static final int LOWEST_ROLL_FOR_A_RAIDER   = 8;
    private static final int LOWEST_ROLL_FOR_A_BASESTAR = 4;

    @Override
    protected boolean isValid() {
        return canAttackGalactica(game, event.cylonShipId());
    }

    @Override
    public Followup process() {
        val attacker = galacticaBoard.shipInSpace(event.cylonShipId(), Ship.class);
        val lowestRoll = attacker instanceof Raider ? LOWEST_ROLL_FOR_A_RAIDER : LOWEST_ROLL_FOR_A_BASESTAR;
        if (game.turn().dieRoll().result() < lowestRoll) {
            return NONE;
        }
        // The current player may draw a Pegasus damage token instead (rule 11.7.2).
        return one(new DamageGalacticaEvent(), new DamagePegasusEvent());
    }
}
