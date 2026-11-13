package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class BasestarBridgeAbilityEventProcessor extends EventProcessor<BasestarBridgeAbilityEvent> {

    private static final int HIGHEST_ROLL_FOR_JUMP = 3;

    @Override
    protected boolean isValid() {
        return event.ability() != null;
    }

    // todo phase 10: the die rolls get their windows for "Strategic Planning" and "Calculations"
    @Override
    public Followup process() {
        return switch (event.ability()) {
            case CAG_PLACES_CIVILIAN_SHIP -> cagPlacesCivilianShip();
            case JUMP_OR_PURSUIT -> ops.rollDie() <= HIGHEST_ROLL_FOR_JUMP
                    ? single(new DecreaseJumpPreparationEvent())
                    : single(new AdvancePursuitTrackEvent());
            case BASESTAR_OR_RAIDERS ->
                    single(new PlayerDecisionEvent<>(player.number(), BasestarBridgeShipsEvent.class));
            case GALACTICA_DAMAGE -> galacticaDamage();
        };
    }

    // Nothing to place without a civilian ship left, or without a CAG to place it.
    private Followup cagPlacesCivilianShip() {
        val cag = game.cag();
        if (cag == null || game.decks().civilianShipsLeft() == 0) {
            return NONE;
        }
        return single(new PlayerDecisionEvent<>(game.playerNumber(cag), PlaceCivilianShipEvent.class));
    }

    private Followup galacticaDamage() {
        val raiders = galacticaBoard.shipsInSpace(Raider.class).size();
        if (ops.rollDie() >= raiders) {
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
