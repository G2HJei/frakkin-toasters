package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class AdmiralsQuartersEventProcessor extends LocationActionProcessor<AdmiralsQuartersEvent> {

    public static boolean canAct(GameView game) {
        return game.players().stream().anyMatch(p -> canBeSentToBrig(game, p.number()));
    }

    @Override
    protected boolean isValidAction() {
        // The pass result sends the target to the "Brig", so he has to be someone who can go there.
        return isPlayer(event.targetPlayer()) && canBeSentToBrig(game, event.targetPlayer());
    }

    @Override
    public Followup process() {
        ops.skillCheck().start(ADMIRALS_QUARTERS, event.targetPlayer());
        return FollowupUtils.beforeSkillCheckFollowups(game);
    }
}
