package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class PrimacyEventProcessor extends CharacterActionEventProcessor<PrimacyEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.PRIMACY;
    }

    // He can always use it: with no basestar to place he may still take one off or place the raiders.
    public static boolean canUse(GameView game, PlayerView player) {
        return true;
    }

    // A basestar is placed, from the Cylon Fleet board when the supply is out (rule 11.4.2). Only when every basestar is
    // in play, none left in the supply, may he take one off the main board instead.
    @Override
    protected boolean isValidUse() {
        if (event.removedBasestarId() == null) {
            return true;
        }
        return !basestarLeft(game) && galacticaBoard.shipsInSpace(Basestar.class).stream()
                .anyMatch(basestar -> basestar.id() == event.removedBasestarId());
    }

    @Override
    protected Followup resolve() {
        val basestars = event.removedBasestarId() == null ? 1 : 0;
        if (event.removedBasestarId() != null) {
            ops.ship(event.removedBasestarId()).destroy(); // back to the supply, not destroyed in combat
        }
        val more = event.placeRaidersAndCivilianShip() ? 1 : 0;
        return basestars + more > 0
                ? single(new PlaceShipsEvent(basestars, 3 * more, more, null))
                : NONE;
    }

    private static boolean basestarLeft(GameView game) {
        return game.cylonShips().available(ShipType.BASESTAR) > 0;
    }
}
