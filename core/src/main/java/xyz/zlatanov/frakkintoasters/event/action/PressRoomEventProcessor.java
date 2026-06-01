package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class PressRoomEventProcessor extends EventProcessor<PressRoomEvent> {

    @Override
    public Followup process() {
        val cardDrawn = game.decks().mutiny().draw();
        game.player(event.targetPlayer()).mutinyCards().add(cardDrawn);
        return all(
                single(new PlayerDecisionEvent<>(event.targetPlayer(), DiscardDownTo1MutinyCardEvent.class)),
                one(new PlayerDecisionEvent<>(event.playerNumber(), Discard1MutinyCardEvent.class),
                        new NoOpEvent(event.playerNumber()))
        );
    }

}
