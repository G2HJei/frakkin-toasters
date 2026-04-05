package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.character.Character;

public interface Pilotable {

    Pilotable pilot(Character o);

    Character pilot();

}
