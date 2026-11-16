package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RagnarAnchorageEventProcessor extends EventProcessor<RagnarAnchorageEvent> {

    private static final int VIPERS = 3;
    private static final int RAPTORS = 1;

    // Simplified: the Admiral repairs as many ships as possible instead of choosing, because repairing costs
    // nothing. Mark VIIs come first, then assault raptors, which count as vipers (rule 11.3.2.2), then vipers.
    @Override
    public Followup process() {
        val damaged = galacticaBoard.damagedShips().stream().map(Ship.class::cast);
        val destroyed = game.removedComponents().items().stream()
                .filter(Ship.class::isInstance)
                .map(Ship.class::cast);
        val toRepair = Stream.concat(damaged, destroyed).toList();
        repair(toRepair.stream().filter(ship -> ship instanceof Viper || ship instanceof AssaultRaptor)
                .sorted(Comparator.comparingInt(RagnarAnchorageEventProcessor::priority).thenComparingInt(Ship::id))
                .limit(VIPERS)
                .toList());
        repair(toRepair.stream().filter(Raptor.class::isInstance)
                .sorted(Comparator.comparingInt(Ship::id))
                .limit(RAPTORS)
                .toList());
        return Followup.NONE;
    }

    private void repair(List<Ship> ships) {
        ships.forEach(ship -> ops.ship(ship.id()).repair());
    }

    private static int priority(Ship ship) {
        return ship instanceof ViperMarkVII ? 0 : ship instanceof AssaultRaptor ? 1 : 2;
    }
}
