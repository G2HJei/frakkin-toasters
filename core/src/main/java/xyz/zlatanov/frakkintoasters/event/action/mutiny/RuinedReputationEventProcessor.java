package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;

public class RuinedReputationEventProcessor extends MutinyCardEventProcessor<RuinedReputationEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.RUINED_REPUTATION;
    }

    // "A human player": an Infiltrating Cylon Leader counts as one (rule 6.2.3.1), and so does the player himself.
    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber()) && game.player(event.targetPlayerNumber()).isHuman();
    }

    // The target chooses the types he draws.
    @Override
    protected Followup resolve() {
        val target = event.targetPlayerNumber();
        return all(single(new PlayerDecisionEvent<>(target, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR)),
                rollDie(new RuinedReputationResultEvent(target)),
                single(thenDiscard()));
    }
}
