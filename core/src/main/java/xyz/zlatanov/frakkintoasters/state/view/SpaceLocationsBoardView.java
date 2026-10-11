package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.List;

public interface SpaceLocationsBoardView {

    Location locate(Ship ship);

    <T extends Ship> List<T> shipsIn(Location location, Class<T> shipClass);

    List<Ship> shipsIn(Location location);

    <T extends Ship> T shipInSpace(int shipId, Class<T> shipClass);

    <T extends Ship> List<T> shipsInSpace(Class<T> shipClass);

    List<HumanFighterView> humanFightersIn(Location location);
}
