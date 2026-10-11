package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;

import java.util.List;

public interface BasestarView extends CylonShip {

    List<BasestarDamage> damage();
}
