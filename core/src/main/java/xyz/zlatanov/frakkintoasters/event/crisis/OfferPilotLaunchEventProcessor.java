package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.optional;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;

public class OfferPilotLaunchEventProcessor extends EventProcessor<OfferPilotLaunchEvent> {

    @Override
    public Followup process() {
        return CrisisCardFollowups.canLaunchHimself(game, player)
                && galacticaBoard.reserves().stream().anyMatch(HumanFighterView.class::isInstance)
                ? optional(player.number(), new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class,
                MUST_LAUNCH_IN_VIPER))
                : NONE;
    }
}
