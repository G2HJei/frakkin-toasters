package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.skillcheck.UseArbitratorEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_ARBITRATOR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class AdmiralsQuartersEventProcessor extends LocationActionProcessor<AdmiralsQuartersEvent> {

    private static final int ACCEPT_PROPHECY_ADJUSTMENT = 2;
    private static final int ARBITRATOR_ADJUSTMENT      = 3;

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
        if (game.player(event.targetPlayer()).character() == game.president() && game.inPlay(ACCEPT_PROPHECY)) {
            // "Accept Prophecy" raises the difficulty when the President is the target, then it is discarded.
            ops.skillCheck().adjustDifficulty(ACCEPT_PROPHECY_ADJUSTMENT);
            ops.titles().discardFromPlay(ACCEPT_PROPHECY);
        }
        // The Arbitrator may use the card as the location is activated, before "Political Prowess".
        val arbitrator = game.quorumCardHolder(ASSIGN_ARBITRATOR)
                .map(holder -> one(new UseArbitratorEvent(holder, ARBITRATOR_ADJUSTMENT),
                        new UseArbitratorEvent(holder, -ARBITRATOR_ADJUSTMENT), new NoOpEvent(holder)))
                .orElse(NONE);
        return allOf(arbitrator, FollowupUtils.beforeSkillCheckFollowups(game));
    }
}
