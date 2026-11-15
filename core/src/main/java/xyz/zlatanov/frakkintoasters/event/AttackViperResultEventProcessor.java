package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

public class AttackViperResultEventProcessor extends EventProcessor<AttackViperResultEvent> {

    private static final int ROLL_TO_DESTROY                  = 8;
    private static final int LOWEST_ROLL_TO_DESTROY_A_RAPTOR  = 7;
    private static final int LOWEST_ROLL_TO_DAMAGE_A_VIPER    = 5;
    private static final int LOWEST_ROLL_TO_DAMAGE_A_MARK_VII = 6;

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(HumanFighterView.class).stream().anyMatch(s -> s.id() == event.humanFighterId());
    }

    @Override
    public Followup process() {
        val fighter = galacticaBoard.shipInSpace(event.humanFighterId(), HumanFighterView.class);
        val roll = game.turn().dieRoll().result();
        if (fighter instanceof AssaultRaptor) {
            if (roll >= LOWEST_ROLL_TO_DESTROY_A_RAPTOR) {
                ops.ship(fighter.id()).destroy(); // assault raptors can't be damaged
            }
        } else if (roll >= ROLL_TO_DESTROY) {
            ops.ship(fighter.id()).destroy();
        } else if (roll >= (fighter instanceof ViperMarkVII ? LOWEST_ROLL_TO_DAMAGE_A_MARK_VII : LOWEST_ROLL_TO_DAMAGE_A_VIPER)) {
            ops.ship(fighter.id()).damage();
        }
        return Followup.NONE;
    }
}
