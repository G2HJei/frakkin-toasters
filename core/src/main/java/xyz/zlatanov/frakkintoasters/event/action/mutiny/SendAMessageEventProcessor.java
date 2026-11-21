package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class SendAMessageEventProcessor extends MutinyCardEventProcessor<SendAMessageEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.SEND_A_MESSAGE;
    }

    // "If possible": a centurion on the track has to be attacked when there is one.
    @Override
    protected boolean isValidPlay() {
        val track = galacticaBoard.boardingPartyTrack().keySet();
        return event.centurionId() == null
                ? track.isEmpty()
                : track.stream().anyMatch(centurion -> centurion.id() == event.centurionId());
    }

    @Override
    protected Followup resolve() {
        val attack = event.centurionId() == null ? NONE : rollDie(new SendAMessageResultEvent(event.centurionId()));
        return allOf(damageGalactica(), attack, single(thenDiscard()));
    }
}
