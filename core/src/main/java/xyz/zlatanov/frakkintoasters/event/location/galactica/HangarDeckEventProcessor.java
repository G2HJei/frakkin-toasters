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
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class HangarDeckEventProcessor extends LocationActionProcessor<HangarDeckEvent> {

    public static boolean canAct(GameView game, PlayerView player) {
        val galactica = game.boards().galactica();
        val fighterInReserves = galactica.reserves().stream().anyMatch(HumanFighterView.class::isInstance);
        val unmannedFighterInSpace = galactica.shipsInSpace(HumanFighterView.class).stream().anyMatch(s -> s.pilot() == null);
        // Not from the "Brig", e.g. through "Resolute" (errata A2.1.1.1): the launch would take the prisoner out of it.
        val inBrig = game.locate(player.character()).filter(BRIG::equals).isPresent();
        // Nor for a pilot, who is in space, e.g. a second "Hangar Deck" through "Resolute": he flies one ship.
        val piloting = galactica.pilotedFighter(player.character()).isPresent();
        // A Cylon player flies none of the humans' ships (the reveal sends his viper back to the "Reserves").
        return player.character().canPilot()
                && player.isHuman()
                && !inBrig
                && !piloting
                && (fighterInReserves || unmannedFighterInSpace);
    }

    @Override
    protected Followup act() {
        return all(
                new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER),
                // The launched pilot is in space, so his 1 more action is his viper's or a Skill Card's (rule 11.2.1),
                // offered once he is there.
                new GrantEvent(player.number(), ACTION, HANGAR_DECK));
    }
}
