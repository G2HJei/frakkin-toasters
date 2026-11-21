package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.Objects;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardPlays;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.DiscardHeldQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class SkilledPoliticianChoiceEventProcessor extends EventProcessor<SkilledPoliticianChoiceEvent> {

    @Override
    protected boolean isValid() {
        return event.chosen() != null && event.others() != null && event.others().stream().noneMatch(Objects::isNull);
    }

    // She resolves the card she holds, President or not, as the player of "Popular Influence" does. A card she can't
    // play now is discarded.
    @Override
    public Followup process() {
        ops.titles().putDrawnQuorumCardsAtBottom(event.others());
        ops.player(event.playerNumber()).holdQuorumCard(event.chosen());
        return QuorumCardPlays.canPlay(game, player, event.chosen())
                ? single(new PlayerDecisionEvent<>(event.playerNumber(), QuorumCardPlays.eventClass(event.chosen())))
                : single(new DiscardHeldQuorumCardEvent(event.playerNumber(), event.chosen()));
    }
}
