package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

public class AnalyzeEnemyFighterPassEventProcessor extends EventProcessor<AnalyzeEnemyFighterPassEvent> {

    // Raptors are identical, so any destroyed one will do. Nothing happens if none was destroyed.
    @Override
    public Followup process() {
        game.removedComponents().items().stream()
                .filter(Raptor.class::isInstance)
                .map(Raptor.class::cast)
                .findFirst()
                .ifPresent(raptor -> ops.ship(raptor.id()).repair());
        return Followup.NONE;
    }
}
