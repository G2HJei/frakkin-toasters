package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

public class PlaceShipEventProcessor extends EventProcessor<PlaceShipEvent> {

    @Override
    protected boolean isValid() {
        if (event.shipType() == null || !LocationsArea.GALACTICA_SPACE.locations().contains(event.location())) {
            return false;
        }
        return switch (event.shipType()) {
            case BASESTAR, RAIDER, HEAVY_RAIDER -> true;
            case CIVILIAN -> game.decks().civilianShipsLeft() > 0;
            case ASSAULT_RAPTOR, RAPTOR, VIPER, VIPER_MARK_VII -> false; // they launch, by the rules
        };
    }

    @Override
    public Followup process() {
        switch (event.shipType()) {
            case BASESTAR -> ops.sandbox().placeCylonShip(Basestar.class, event.location());
            case RAIDER -> ops.galactica().placeRaider(event.location());
            case HEAVY_RAIDER -> ops.sandbox().placeCylonShip(HeavyRaider.class, event.location());
            case CIVILIAN -> ops.galactica().placeCivilianShip(event.location());
            default -> throw new IllegalStateException("Not placed: " + event.shipType());
        }
        return Followup.NONE;
    }
}
