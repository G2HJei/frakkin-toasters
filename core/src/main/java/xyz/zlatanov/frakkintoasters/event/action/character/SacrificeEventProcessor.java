package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class SacrificeEventProcessor extends CharacterActionEventProcessor<SacrificeEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.SACRIFICE;
    }

    // How many of each kind she destroys: all of them when fewer are there.
    private static final Map<Class<? extends Ship>, Integer> TARGETS = Map.of(
            Raider.class, 5,
            HeavyRaider.class, 2,
            Basestar.class, 1,
            CivilianShip.class, 1);

    public static boolean canUse(GameView game, PlayerView player) {
        return area(game, player)
                .map(area -> TARGETS.keySet().stream().anyMatch(kind -> !game.boards().galactica().shipsIn(area, kind).isEmpty()))
                .orElse(false);
    }

    @Override
    protected boolean isValidUse() {
        val area = area(game, player);
        if (area.isEmpty() || event.shipIds() == null || event.shipIds().isEmpty()
                || event.shipIds().stream().anyMatch(Objects::isNull)
                || new HashSet<>(event.shipIds()).size() != event.shipIds().size()) {
            return false;
        }
        val ships = galacticaBoard.shipsIn(area.get(), Ship.class);
        val chosen = ships.stream().filter(ship -> event.shipIds().contains(ship.id())).toList();
        if (chosen.size() != event.shipIds().size()) {
            return false; // not all of them are in her space area
        }
        val kind = chosen.getFirst().getClass();
        if (!TARGETS.containsKey(kind) || chosen.stream().anyMatch(ship -> ship.getClass() != kind)) {
            return false;
        }
        val there = ships.stream().filter(ship -> ship.getClass() == kind).count();
        return chosen.size() == Math.min(TARGETS.get(kind), there);
    }

    // She lands in "Sickbay", which returns her viper to the "Reserves", then the ships go.
    @Override
    protected Followup resolve() {
        val basestar = galacticaBoard.shipsInSpace(Basestar.class).stream()
                .anyMatch(ship -> event.shipIds().contains(ship.id()));
        val civilianShips = galacticaBoard.shipsInSpace(CivilianShip.class).stream()
                .map(Ship::id)
                .filter(event.shipIds()::contains)
                .sorted()
                .toList();
        ops.player(event.playerNumber()).sendTo(SICKBAY);
        event.shipIds().stream().filter(id -> !civilianShips.contains(id)).sorted().forEach(id -> ops.ship(id).destroy());
        // A civilian ship is destroyed the usual way, so Brendan "Hot Dog" Costanza may "Escort" it.
        val window = basestar ? single(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed())) : NONE;
        return allOf(all(civilianShips.stream().map(DestroyCivilianShipEvent::new).toArray(Event[]::new)), window);
    }

    private static Optional<Location> area(GameView game, PlayerView player) {
        return game.boards().galactica().pilotedFighter(player.character()).flatMap(ignored -> game.locate(player.character()));
    }
}
