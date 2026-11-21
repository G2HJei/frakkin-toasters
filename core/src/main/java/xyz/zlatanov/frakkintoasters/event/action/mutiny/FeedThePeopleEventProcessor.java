package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;

public class FeedThePeopleEventProcessor extends MutinyCardEventProcessor<FeedThePeopleEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.FEED_THE_PEOPLE;
    }

    @Override
    protected Followup resolve() {
        return all(gain(FOOD, 1), new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent(), thenRemove());
    }
}
