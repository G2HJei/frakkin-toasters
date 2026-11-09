package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.state.GameStep.DISCARD;

public class DiscardStepEventProcessor extends EventProcessor<DiscardStepEvent> {

    @Override
    public Followup process() {
        ops.changeStep(DISCARD);
        return all(playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .flatMap(this::discards)
                .toArray(Event[]::new));
    }

    private Stream<Event> discards(PlayerView player) {
        val discards = Stream.<Event>builder();
        if (player.skillCards().size() > player.handLimit()) {
            discards.add(new PlayerDecisionEvent<>(player.number(), DiscardSkillCardsEvent.class,
                    DISCARD_DOWN_TO_HAND_LIMIT));
        }
        if (player.character() == game.president() && game.presidentHand().size() > player.handLimit()) {
            discards.add(new PlayerDecisionEvent<>(player.number(), DiscardQuorumCardsEvent.class,
                    DISCARD_DOWN_TO_HAND_LIMIT));
        }
        return discards.build();
    }
}
