package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.character.Character;

public interface PilotableShip extends Ship {

    PilotableShip pilot(Character o);

    Character pilot();

}
