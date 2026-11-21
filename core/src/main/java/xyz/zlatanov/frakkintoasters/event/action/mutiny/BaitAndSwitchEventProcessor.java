package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.deck.ShuffleTreacheryIntoDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;

public class BaitAndSwitchEventProcessor extends MutinyCardEventProcessor<BaitAndSwitchEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.BAIT_AND_SWITCH;
    }

    @Override
    protected Followup resolve() {
        return all(new PlayerDecisionEvent<>(event.playerNumber(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR),
                new ShuffleTreacheryIntoDestinyDeckEvent(2),
                thenDiscard());
    }
}
