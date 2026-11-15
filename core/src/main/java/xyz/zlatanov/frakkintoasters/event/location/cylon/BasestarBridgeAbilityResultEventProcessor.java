package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.GALACTICA_DAMAGE;
import static xyz.zlatanov.frakkintoasters.event.location.cylon.BasestarBridgeAbility.JUMP_OR_PURSUIT;

public class BasestarBridgeAbilityResultEventProcessor extends EventProcessor<BasestarBridgeAbilityResultEvent> {

    private static final int HIGHEST_ROLL_FOR_JUMP = 3;

    @Override
    protected boolean isValid() {
        return event.ability() == JUMP_OR_PURSUIT || event.ability() == GALACTICA_DAMAGE;
    }

    @Override
    public Followup process() {
        val roll = game.turn().dieRoll().result();
        return switch (event.ability()) {
            case JUMP_OR_PURSUIT -> roll <= HIGHEST_ROLL_FOR_JUMP
                    ? single(new DecreaseJumpPreparationEvent())
                    : single(new AdvancePursuitTrackEvent());
            case GALACTICA_DAMAGE -> galacticaDamage(roll);
            default -> throw new FrakCallTheAdmiralException("No die roll for " + event.ability());
        };
    }

    private Followup galacticaDamage(int roll) {
        if (roll >= galacticaBoard.shipsInSpace(Raider.class).size()) {
            return NONE;
        }
        val tokens = ops.galactica().drawDamage(2);
        val playerNumber = player.number();
        return one(
                // The token that isn't resolved goes back to the pile.
                new ChooseGalacticaDamageEvent(playerNumber, tokens.get(0), tokens.get(1)),
                new ChooseGalacticaDamageEvent(playerNumber, tokens.get(1), tokens.get(0)));
    }
}
