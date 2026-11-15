package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import static xyz.zlatanov.frakkintoasters.event.AttackCylonShipEventProcessor.canAttack;
import static xyz.zlatanov.frakkintoasters.event.AttackCylonShipEventProcessor.fighter;
import static xyz.zlatanov.frakkintoasters.event.DestroyRaidersEventProcessor.destroyRaidersIn;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BEST_OF_THE_BEST;

public class AttackCylonShipResultEventProcessor extends EventProcessor<AttackCylonShipResultEvent> {

    private static final int LOWEST_ROLL_TO_DESTROY_A_RAIDER       = 3;
    private static final int LOWEST_ROLL_TO_DESTROY_A_HEAVY_RAIDER = 7;
    private static final int LOWEST_ROLL_TO_DAMAGE_WITH_GALACTICA  = 5;
    private static final int LOWEST_ROLL_TO_DAMAGE_WITH_A_RAPTOR   = 7;
    private static final int LOWEST_ROLL_TO_DAMAGE_WITH_A_VIPER    = 8;
    private static final int STRUCTURAL_DAMAGE_BONUS               = 2;

    @Override
    protected boolean isValid() {
        return canAttack(game, event.humanFighterId(), event.cylonShipId());
    }

    @Override
    public Followup process() {
        val dieRoll = game.turn().dieRoll();
        val target = galacticaBoard.shipInSpace(event.cylonShipId(), Ship.class);
        if (dieRoll.cards().contains(BEST_OF_THE_BEST)) {
            return destroyRaidersIn(game, galacticaBoard.locate(target), dieRoll.result());
        }
        val roll = dieRoll.result();
        return switch (target) {
            case Raider ignored -> destroyFrom(LOWEST_ROLL_TO_DESTROY_A_RAIDER, roll);
            case HeavyRaider ignored -> destroyFrom(LOWEST_ROLL_TO_DESTROY_A_HEAVY_RAIDER, roll);
            case BasestarView basestar -> damageBasestar(basestar, roll);
            default -> throw new FrakCallTheAdmiralException("Not a Cylon ship: " + target);
        };
    }

    private Followup destroyFrom(int lowestRoll, int roll) {
        if (roll >= lowestRoll) {
            ops.ship(event.cylonShipId()).destroy();
        }
        return NONE;
    }

    private Followup damageBasestar(BasestarView basestar, int roll) {
        // "Structural Damage": "all attacks against the basestar gain +2 to their die rolls"
        val modifiedRoll = roll + (basestar.damage().contains(STRUCTURAL_DAMAGE) ? STRUCTURAL_DAMAGE_BONUS : 0);
        val lowestRoll = fighter(game, event.humanFighterId())
                .map(this::lowestRollToDamage)
                .orElse(LOWEST_ROLL_TO_DAMAGE_WITH_GALACTICA);
        return modifiedRoll >= lowestRoll ? single(new DamageBasestarEvent(basestar.id())) : NONE;
    }

    private int lowestRollToDamage(HumanFighterView fighter) {
        return fighter instanceof AssaultRaptor ? LOWEST_ROLL_TO_DAMAGE_WITH_A_RAPTOR : LOWEST_ROLL_TO_DAMAGE_WITH_A_VIPER;
    }
}
