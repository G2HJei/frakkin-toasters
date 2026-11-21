package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardPlays;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class PopularInfluenceChoiceEventProcessor extends EventProcessor<PopularInfluenceChoiceEvent> {

    @Override
    protected boolean isValid() {
        return event.given() != null && event.other() != null;
    }

    // The decision takes only the event of that card, so a President who plays "Popular Influence" can't play another
    // card of his hand instead. "You" and "another player" on the card mean the player of "Popular Influence".
    @Override
    public Followup process() {
        ops.titles().giveQuorumCardToPresident(event.given());
        ops.player(player.number()).holdQuorumCard(event.other());
        val discard = new DiscardHeldQuorumCardEvent(player.number(), event.other());
        return QuorumCardPlays.canPlay(game, player, event.other())
                ? one(new PlayerDecisionEvent<>(player.number(), QuorumCardPlays.eventClass(event.other())), discard)
                : single(discard);
    }
}
