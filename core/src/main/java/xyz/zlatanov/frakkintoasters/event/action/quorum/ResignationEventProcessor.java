package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;

public class ResignationEventProcessor extends QuorumCardEventProcessor<ResignationEvent> {

    // Only the President has a title to give, and someone else has to be able to take it.
    public static boolean canPlay(GameView game, PlayerView player) {
        return player.character() != null
                && player.character() == game.president()
                && game.players().stream().anyMatch(other -> canBecomePresident(game, other.character()));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.RESIGNATION;
    }

    @Override
    protected boolean isValidPlay() {
        if (event.discards() == null || event.discards().stream().anyMatch(Objects::isNull) || !canPlay(game, player)) {
            return false;
        }
        // The discards come from the hand that is left once "Resignation" is played.
        val hand = new ArrayList<>(game.presidentHand().cards());
        hand.remove(card());
        return event.discards().stream().allMatch(hand::remove);
    }

    @Override
    protected Followup resolve() {
        if (!event.discards().isEmpty()) {
            ops.titles().discardQuorumCards(event.discards());
        }
        val draws = Stream.<Event>generate(DrawQuorumCardEvent::new).limit(event.discards().size());
        return all(Stream.concat(draws, Stream.of(
                        new PlayerDecisionEvent<>(player.number(), MakePresidentEvent.class),
                        thenDiscard()))
                .toArray(Event[]::new));
    }
}
