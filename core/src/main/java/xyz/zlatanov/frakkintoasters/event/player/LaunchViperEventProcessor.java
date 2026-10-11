package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;
import static xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView.VIPER_LAUNCH_SPACES;

public class LaunchViperEventProcessor extends EventProcessor<LaunchViperEvent> {

    private static final Set<ShipType>   VALID_LAUNCH_VIPER_SHIP_TYPES = Set.of(VIPER, VIPER_MARK_VII, ASSAULT_RAPTOR);
    private static final List<Character> PILOT_CHARACTERS              = Arrays.stream(Character.values()).filter(c -> c.skillSet().stream().anyMatch(ss -> ss.colors().contains(PILOTING))).toList();

    @Override
    protected boolean isValid() {
        val pilot = event.pilot();
        val unmannedViperId = event.unmannedViperId();
        val validLocation = VIPER_LAUNCH_SPACES.contains(event.location());
        val validShipType = VALID_LAUNCH_VIPER_SHIP_TYPES.contains(event.shipType());
        val validPilot = pilot == null || PILOT_CHARACTERS.contains(pilot);
        //todo  && player.character() == pilot; OR REFACTOR INTO PLAYER EVENT??
        val validMandatoryPilot = !event.mustLaunchInViper() || pilot != null;
        val reserveFighterAvailable = galacticaBoard.reserves().stream().anyMatch(s -> s instanceof HumanFighterView);
        val noPilotNoViperToLand = pilot == null && unmannedViperId == null;
        val pilotingFromReserves = pilot != null && reserveFighterAvailable && unmannedViperId == null;
        val pilotingUnmannedViper = pilot != null && !reserveFighterAvailable && unmannedViperId != null && viperToLandingIsInSpace();
        val validUnmannedViperId = noPilotNoViperToLand || pilotingFromReserves || pilotingUnmannedViper;
        return validLocation &&
                validShipType &&
                validPilot &&
                validMandatoryPilot &&
                validUnmannedViperId;
    }

    @Override
    public Followup process() {
        val humanFighterId = ops.galactica().launchHumanFighter(event.shipType(), event.location(), event.unmannedViperId());
        if (event.pilot() != null) {
            ops.character(event.pilot()).pilot(humanFighterId);
        }
        return Followup.NONE;
    }

    private boolean viperToLandingIsInSpace() {
        return galacticaBoard.shipsInSpace(event.shipType().shipClass())
                .stream()
                .anyMatch(s -> s.id() == event.unmannedViperId());
    }

}
