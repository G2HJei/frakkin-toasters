package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.title.NukeResultEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.action.title.LaunchNukeEventProcessor.isSpaceArea;

public class UnauthorizedUsageEventProcessor extends MutinyCardEventProcessor<UnauthorizedUsageEvent> {

    public static boolean canPlay(GameView game) {
        return game.nukes() > 0;
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.UNAUTHORIZED_USAGE;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game) && isSpaceArea(event.area());
    }

    // The launched nuke is spent and the others are discarded, so every token goes before the roll. The player picks the
    // basestar when there are several.
    @Override
    protected Followup resolve() {
        for (int i = game.nukes(); i > 0; i--) {
            ops.discardNuke();
        }
        return all(rollDie(new NukeResultEvent(event.playerNumber(), event.area())), single(thenRemove()));
    }
}
