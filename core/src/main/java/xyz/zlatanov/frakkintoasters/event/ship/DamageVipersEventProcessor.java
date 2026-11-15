package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.RESERVES;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.SPACE;

public class DamageVipersEventProcessor extends EventProcessor<DamageVipersEvent> {

    @Override
    public Followup process() {
        // The ones that cost the least go first: unmanned vipers in the "Reserves", then in space, then piloted ones.
        // todo the player taking an action chooses which vipers (rule 11.3.2.2)
        candidates().stream()
                .sorted(Comparator.comparing(fighter -> fighter.pilot() != null)) // stable: the "Reserves" stay first
                .limit(event.count())
                .forEach(fighter -> ops.ship(fighter.id()).damage());
        return Followup.NONE;
    }

    // Assault raptors can't be damaged (rule 11.3.2.3), and a mark VII is damaged like a viper.
    private List<HumanFighterView> candidates() {
        Stream<HumanFighterView> inReserves = event.area() == SPACE ? Stream.empty() : galacticaBoard.reserves().stream()
                .filter(HumanFighterView.class::isInstance)
                .map(HumanFighterView.class::cast);
        Stream<HumanFighterView> inSpace = event.area() == RESERVES ? Stream.empty()
                : galacticaBoard.shipsInSpace(HumanFighterView.class).stream();
        return Stream.concat(inReserves, inSpace)
                .filter(Viper.class::isInstance)
                .toList();
    }
}
