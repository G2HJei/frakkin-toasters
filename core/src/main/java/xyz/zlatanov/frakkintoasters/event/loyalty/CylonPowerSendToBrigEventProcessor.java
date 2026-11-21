package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class CylonPowerSendToBrigEventProcessor extends EventProcessor<CylonPowerSendToBrigEvent> {

    // A character on Galactica, not already in the "Brig".
    public static boolean canTarget(GameView game, int targetPlayerNumber) {
        return canBeSentToBrig(game, targetPlayerNumber)
                && CylonRevealPowers.isOnGalactica(game, game.player(targetPlayerNumber));
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && canTarget(game, event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        return single(new SendToBrigEvent(event.targetPlayerNumber()));
    }
}
