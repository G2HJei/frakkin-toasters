package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.Objects;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardPlays;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class AbusePowerChoiceEventProcessor extends EventProcessor<AbusePowerChoiceEvent> {

    @Override
    protected boolean isValid() {
        return event.chosen() != null && event.others() != null && event.others().stream().noneMatch(Objects::isNull)
                && MutinyCardPlays.canPlay(game, player, event.chosen());
    }

    // He holds the card only to play it at once: holding it sends nobody to the "Brig".
    @Override
    public Followup process() {
        ops.player(event.playerNumber()).putMutinyCardsAtBottom(event.others());
        ops.player(event.playerNumber()).holdMutinyCard(event.chosen());
        return single(new PlayerDecisionEvent<>(event.playerNumber(), MutinyCardPlays.eventClass(event.chosen())));
    }
}
