package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.KleptomaniaBrigEvent;
import xyz.zlatanov.frakkintoasters.event.character.ReconEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.operation.TitleOps.QUORUM_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.state.GameStep.DISCARD;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HEADSTRONG;

public class DiscardStepEventProcessor extends EventProcessor<DiscardStepEvent> {

    @Override
    public Followup process() {
        ops.changeStep(DISCARD);
        // "Kleptomania" sends a player to the "Brig" at the end of the turn, after the discards.
        val brig = game.turn().sentToBrigAtEndOfTurn().stream().map(KleptomaniaBrigEvent::new);
        val discards = all(Stream.concat(playersFrom(game, game.turn().currentPlayerNumber()).stream().flatMap(this::discards), brig)
                .toArray(Event[]::new));
        // "Recon" comes at the very end of the turn.
        return allOf(discards, ReconEventProcessor.offer(game));
    }

    private Stream<Event> discards(PlayerView player) {
        val discards = Stream.<Event>builder();
        val excess = player.skillCards().size() - player.handLimit();
        if (excess > 0 && player.hasAbility(HEADSTRONG)) {
            // Lee "Apollo" Adama's "Headstrong": the hand limit makes him discard too, so at random.
            discards.add(new DiscardRandomSkillCardsEvent(player.number(), excess));
        } else if (excess > 0) {
            discards.add(new PlayerDecisionEvent<>(player.number(), DiscardSkillCardsEvent.class,
                    DISCARD_DOWN_TO_HAND_LIMIT));
        }
        if (player.character() == game.president() && game.presidentHand().size() > QUORUM_HAND_LIMIT) {
            discards.add(new PlayerDecisionEvent<>(player.number(), DiscardQuorumCardsEvent.class,
                    DISCARD_DOWN_TO_HAND_LIMIT));
        }
        return discards.build();
    }
}
