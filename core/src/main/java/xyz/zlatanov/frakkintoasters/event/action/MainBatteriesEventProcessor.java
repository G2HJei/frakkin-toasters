package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class MainBatteriesEventProcessor extends EventProcessor<MainBatteriesEvent> {
    @Override
    public Followup processEvent() {
        val roll = game.die().roll();
        if (roll == 1) {
            return destroyCivilianShip();
        } else if (roll <= 3) {
            return damageViper();
        } else if (roll <= 6) {
            return destroyRaiders(2);
        } else {
            return destroyRaiders(4);
        }
    }


    private Followup destroyCivilianShip() {
        val civilians = game.boards().galactica().shipsIn(event.spaceLocation(), CivilianShip.class);
        if (civilians.isEmpty()) {
            return Followup.NONE;
        }
        if (civilians.size() == 1) {
            return single(new DestroyCivilianShipEvent(civilians.getFirst().id()));
        }
        return single(new PlayerDecisionEvent<>(event.playerNumber(), DestroyCivilianShipEvent.class));
    }

    private Followup damageViper() {
        val humanFighters = game.boards().galactica().humanFightersIn(event.spaceLocation());
        if (humanFighters.isEmpty()) {
            return Followup.NONE;
        }
        if (humanFighters.size() == 1) {
            return single(new DamageHumanFighterEvent(humanFighters.getFirst().id()));
        }
        return single(new PlayerDecisionEvent<>(event.playerNumber(), DamageHumanFighterEvent.class));
    }

    private Followup destroyRaiders(int count) {
        val raiders = game.boards().galactica().shipsIn(event.spaceLocation(), Raider.class);
        if (raiders.isEmpty()) {
            return Followup.NONE;
        }
        if (raiders.size() <= count) {
            return single(new DestroyRaidersEvent(raiders.stream().map(Ship::id).collect(toSet())));
        }
        return single(new PlayerDecisionEvent<>(event.playerNumber(), DestroyRaidersEvent.class));
    }
}
