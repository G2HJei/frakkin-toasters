package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

public record DestroyCivilianShipEvent(int shipId) implements Event {
    @Override
    public Followup apply(Game game) {
        val galacticaBoard = game.boards().galactica();
        val civilianShip = galacticaBoard.shipInSpace(shipId, CivilianShip.class);
        galacticaBoard
                .decreaseFuel(civilianShip.fuelCost())
                .decreaseMorale(civilianShip.moraleCost())
                .decreasePopulation(civilianShip.populationCost());
        game.removeComponent(civilianShip);
        return Followup.NONE;
    }
}
