package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Objects;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DestroyRaidersEventProcessor extends EventProcessor<DestroyRaidersEvent> {

    @Override
    protected boolean isValid() {
        val raiderIds = galacticaBoard.shipsInSpace(Raider.class).stream().map(Ship::id).collect(toSet());
        return event.shipIds() != null && !event.shipIds().isEmpty()
                && event.shipIds().stream().allMatch(id -> Objects.nonNull(id) && raiderIds.contains(id));
    }

    @Override
    public Followup process() {
        event.shipIds().stream().sorted().forEach(id -> ops.ship(id).destroy());
        return NONE;
    }

    public static Followup destroyRaidersIn(GameView game, Location area, int count) {
        // Raiders are all alike, so nobody has to choose which ones are destroyed.
        val raiderIds = game.boards().galactica().shipsIn(area, Raider.class)
                .stream()
                .map(Ship::id)
                .sorted()
                .limit(count)
                .collect(toSet());
        return raiderIds.isEmpty() ? NONE : single(new DestroyRaidersEvent(raiderIds));
    }
}
