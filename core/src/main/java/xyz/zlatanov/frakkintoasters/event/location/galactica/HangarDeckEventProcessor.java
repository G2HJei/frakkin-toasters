package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;

public class HangarDeckEventProcessor extends LocationActionProcessor<HangarDeckEvent> {

    public static boolean canAct(GameView game, PlayerView player) {
        val galactica = game.boards().galactica();
        val fighterInReserves = galactica.reserves().stream().anyMatch(HumanFighterView.class::isInstance);
        val unmannedFighterInSpace = galactica.shipsInSpace(HumanFighterView.class).stream().anyMatch(s -> s.pilot() == null);
        return player.character().canPilot()
                && (fighterInReserves || unmannedFighterInSpace);
    }

    @Override
    public Followup process() {
        return all(
                new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER),
                // The launched pilot is in space, so his 1 more action is his viper's or a Skill Card's (rule 11.2.1),
                // offered once he is there.
                new GrantEvent(player.number(), ACTION, HANGAR_DECK));
    }
}
