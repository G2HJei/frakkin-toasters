package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.deck.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class PressRoomEventProcessor extends EventProcessor<PressRoomEvent> {

    @Override
    public Followup process() {
        ops.player(event.targetPlayer()).drawMutinyCard();
        return all(
                single(new PlayerDecisionEvent<>(event.targetPlayer(), DiscardDownTo1MutinyCardEvent.class)),
                one(new PlayerDecisionEvent<>(event.playerNumber(), Discard1MutinyCardEvent.class),
                        new NoOpEvent(event.playerNumber()))
        );
    }

}
