package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.RemoteActions;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class DutifulAgainEventProcessor extends EventProcessor<DutifulAgainEvent> {

    @Override
    protected boolean isValid() {
        return DutifulEventProcessor.locationAction(event.location()).isPresent();
    }

    @Override
    public Followup process() {
        val here = game.locate(player.character());
        if (here.isEmpty() || here.get() == BRIG || !LocationEvent.canAct(game, player, event.location())) {
            return NONE;
        }
        // From elsewhere, e.g. after an "Unorthodox Plan", he acts there again as a remote action.
        return here.get() == event.location()
                ? single(new PlayerDecisionEvent<>(player.number(),
                DutifulEventProcessor.locationAction(event.location()).orElseThrow()))
                : RemoteActions.actAt(ops, player.number(), event.location());
    }
}
