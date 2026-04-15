package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

public record MainBatteriesActionEvent(int playerNumber, Location spaceLocation) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
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

    private List<Followup> destroyCivilianShip(Game game) {
        val civilians = game.boards().galactica().shipsIn(spaceLocation, CIVILIAN);
        if (civilians.isEmpty()) {
            return List.of();
        }
        if (civilians.size() == 1) {
            return followWith(new DestroyCivilianShipEvent(civilians.getFirst().id()));
        }
        return followWith(new PlayerDecisionEvent(playerNumber, DestroyCivilianShipEvent.class));
    }

    private List<Followup> damageViper(Game game) {
        val vipers = game.boards().galactica().shipsIn(spaceLocation, VIPER, VIPER_MARK_VII);
        if (vipers.isEmpty()) {
            return List.of();
        }
        if (vipers.size() == 1) {
            return followWith(new DamageVipersEvent(Set.of(vipers.getFirst().id())));
        }
        return followWith(new PlayerDecisionEvent(playerNumber, DamageVipersEvent.class));
    }

    private List<Followup> destroyRaiders(Game game, int count) {
        val raiders = game.boards().galactica().shipsIn(spaceLocation, RAIDER);
        if (raiders.isEmpty()) {
            return List.of();
        }
        if (raiders.size() <= count) {
            return followWith(new DestroyRaidersEvent(raiders.stream().map(Ship::id).collect(toSet())));
        }
        return followWith(new PlayerDecisionEvent(playerNumber, DestroyRaidersEvent.class));
    }
}
