package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

public class AttackViperResultEventProcessor extends EventProcessor<AttackViperResultEvent> {

    static final int ROLL_TO_DESTROY                  = 8;
    static final int LOWEST_ROLL_TO_DESTROY_A_RAPTOR  = 7;
    static final int LOWEST_ROLL_TO_DAMAGE_A_VIPER    = 5;
    static final int LOWEST_ROLL_TO_DAMAGE_A_MARK_VII = 6;

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
            damage(fighter.id());
        }
        return Followup.NONE;
    }

    // "Forced to Eject": Brendan "Hot Dog" Costanza's viper is destroyed instead.
    private void damage(int fighterId) {
        if (CharacterPenalties.destroyedInsteadOfDamaged(game, fighterId)) {
            ops.ship(fighterId).destroy();
        } else {
            ops.ship(fighterId).damage();
        }
    }
}
