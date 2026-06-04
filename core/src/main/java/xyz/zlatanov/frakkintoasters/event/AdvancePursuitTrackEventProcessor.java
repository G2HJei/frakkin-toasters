package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.HashMap;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_GALACTICA_MAP;

public class AdvancePursuitTrackEventProcessor extends EventProcessor<AdvancePursuitTrackEvent> {
    @Override
    public Followup process() {
        val pursuit = cylonFleetBoard.advancePursuit();
        switch (pursuit) {
            case ONE_CIVILIAN_SHIP:
                return single(decisionEvent());
            case TWO_CIVILIAN_SHIPS:
                return all(decisionEvent(), decisionEvent());
            case AUTO_ATTACK:
                transferAllShipsToMainBoard();
            default:
                return Followup.NONE;
        }
    }

    private PlayerDecisionEvent<PlaceCivilianShipEvent> decisionEvent() {
        val cagPlayerNumber = game.players()
                .stream()
                .filter(p -> p.character() == game.cag())
                .findFirst()
                .map(Player::number)
                .orElseThrow(FrakCallTheAdmiralException::new);
        return new PlayerDecisionEvent<>(cagPlayerNumber, PlaceCivilianShipEvent.class);
    }

    private void transferAllShipsToMainBoard() {
        cylonFleetBoard.resetPursuit();
        new HashMap<>(cylonFleetBoard.shipsInSpace()).forEach(this::moveShip);
    }

    private void moveShip(Ship ship, Location cylonFleetBoardLocation) {
        val targetLocation = MOVE_TO_GALACTICA_MAP.get(cylonFleetBoardLocation);
        // todo add utility method for this kind of remove > place action if used often enough
        cylonFleetBoard.remove(ship);
        galacticaBoard.place(targetLocation, ship);
    }
}
