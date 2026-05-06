package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.character.Character;

public interface HumanFighter extends Ship {

    HumanFighter pilot(Character o);

    Character pilot();

}
