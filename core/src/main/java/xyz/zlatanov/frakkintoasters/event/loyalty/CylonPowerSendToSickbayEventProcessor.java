package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscards;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class CylonPowerSendToSickbayEventProcessor extends EventProcessor<CylonPowerSendToSickbayEvent> {

    static final int CARDS_DISCARDED = 5;

    // A human on Galactica. One in "Sickbay" or the "Brig" still discards, and stays where he is.
    public static boolean canTarget(GameView game, int targetPlayerNumber) {
        val target = game.player(targetPlayerNumber);
        return target.isHuman() && CylonRevealPowers.isOnGalactica(game, target);
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && canTarget(game, event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        val target = event.targetPlayerNumber();
        val discards = game.player(target).skillCards().isEmpty() ? NONE : single(ForcedDiscards.discard(game, target, CARDS_DISCARDED));
        return allOf(discards, single(new SendToSickbayEvent(target)));
    }
}
