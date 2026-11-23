package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static xyz.zlatanov.frakkintoasters.event.AttackGalacticaEventProcessor.canAttackGalactica;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;

public class AttackGalacticaResultEventProcessor extends EventProcessor<AttackGalacticaResultEvent> {

    static final int LOWEST_ROLL_FOR_A_RAIDER   = 8;
    static final int LOWEST_ROLL_FOR_A_BASESTAR = 4;

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
        return damageGalactica();
    }
}
