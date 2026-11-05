package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public class FootageTransmittedFailEventProcessor extends EventProcessor<FootageTransmittedFailEvent> {

    @Override
    public Followup process() {
        game.players().stream()
                .filter(PlayerView::isRevealedCylon)
                .forEach(p -> ops.player(p.number()).drawSuperCrisisCard());
        return Followup.NONE;
    }
}
