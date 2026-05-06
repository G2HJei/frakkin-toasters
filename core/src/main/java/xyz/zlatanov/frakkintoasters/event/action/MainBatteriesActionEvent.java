package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

public record MainBatteriesActionEvent(int playerNumber, Location spaceLocation) implements ActionEvent {

    @Override
    public Followup apply(Game game) {
        val roll = game.die().roll();
        if (roll == 1) {
            return destroyCivilianShip(game);
        } else if (roll <= 3) {
            return damageViper(game);
        } else if (roll <= 6) {
            return destroyRaiders(game, 2);
        } else {
            return destroyRaiders(game, 4);
        }
    }

    private Followup destroyCivilianShip(Game game) {
        val civilians = game.boards().galactica().shipsIn(spaceLocation, CIVILIAN);
        if (civilians.isEmpty()) {
            return Followup.NONE;
        }
        if (civilians.size() == 1) {
            return single(new DestroyCivilianShipEvent(civilians.getFirst().id()));
        }
        return single(new PlayerDecisionEvent<>(playerNumber, DestroyCivilianShipEvent.class));
    }

    private Followup damageViper(Game game) {
        //todo include assault raptor here?
        val vipers = game.boards().galactica().shipsIn(spaceLocation, VIPER, VIPER_MARK_VII);
        if (vipers.isEmpty()) {
            return Followup.NONE;
        }
        if (vipers.size() == 1) {
            return single(new DamageVipersEvent(Set.of(vipers.getFirst().id())));
        }
        return single(new PlayerDecisionEvent<>(playerNumber, DamageVipersEvent.class));
    }

    private Followup destroyRaiders(Game game, int count) {
        val raiders = game.boards().galactica().shipsIn(spaceLocation, RAIDER);
        if (raiders.isEmpty()) {
            return Followup.NONE;
        }
        if (raiders.size() <= count) {
            return single(new DestroyRaidersEvent(raiders.stream().map(Ship::id).collect(toSet())));
        }
        return single(new PlayerDecisionEvent<>(playerNumber, DestroyRaidersEvent.class));
    }
}
