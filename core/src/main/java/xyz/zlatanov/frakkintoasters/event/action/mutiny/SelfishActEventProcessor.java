package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public class SelfishActEventProcessor extends MutinyCardEventProcessor<SelfishActEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.SELFISH_ACT;
    }

    // The card is out of his hand before the new one comes, so it doesn't count towards the "Brig" (rule 5.2.3.2).
    @Override
    protected Followup resolve() {
        return all(new PlayerDecisionEvent<>(event.playerNumber(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2),
                thenDiscard(),
                new DrawMutinyCardEvent(event.playerNumber()));
    }
}
