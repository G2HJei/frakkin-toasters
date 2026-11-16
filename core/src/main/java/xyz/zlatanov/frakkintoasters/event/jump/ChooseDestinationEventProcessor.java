package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ChooseDestinationEventProcessor extends EventProcessor<ChooseDestinationEvent> {

    @Override
    protected boolean isValid() {
        return event.chosen() != null && event.other() != null && event.chosen() != event.other()
                && game.playerNumber(game.admiral()) == event.playerNumber();
    }

    @Override
    public Followup process() {
        ops.galactica().returnDestinationCardToBottom(event.other());
        return single(new ResolveDestinationCardEvent(event.chosen()));
    }
}
