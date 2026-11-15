package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class BasestarBridgeAbilityEventProcessor extends EventProcessor<BasestarBridgeAbilityEvent> {

    @Override
    protected boolean isValid() {
        return event.ability() != null;
    }

    @Override
    public Followup process() {
        return switch (event.ability()) {
            case CAG_PLACES_CIVILIAN_SHIP -> cagPlacesCivilianShip();
            case JUMP_OR_PURSUIT, GALACTICA_DAMAGE ->
                    rollDie(new BasestarBridgeAbilityResultEvent(player.number(), event.ability()));
            case BASESTAR_OR_RAIDERS ->
                    single(new PlayerDecisionEvent<>(player.number(), BasestarBridgeShipsEvent.class));
        };
    }

    private Followup cagPlacesCivilianShip() {
        val cag = game.cag();
        if (cag == null || game.decks().civilianShipsLeft() == 0) {
            return NONE; // nothing to place without a civilian ship left, or without a CAG to place it
        }
        return single(new PlayerDecisionEvent<>(game.playerNumber(cag), PlaceCivilianShipEvent.class));
    }
}
