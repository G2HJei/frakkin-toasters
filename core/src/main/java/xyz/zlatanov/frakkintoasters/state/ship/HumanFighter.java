package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

public interface HumanFighter extends HumanFighterView {

    HumanFighter pilot(Character o);

}
