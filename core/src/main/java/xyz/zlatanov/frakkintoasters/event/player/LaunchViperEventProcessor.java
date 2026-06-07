package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard.VIPER_LAUNCH_SPACES;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;

public class LaunchViperEventProcessor extends EventProcessor<LaunchViperEvent> {

    private static final Set<ShipType>   VALID_LAUNCH_VIPER_SHIP_TYPES = Set.of(VIPER, VIPER_MARK_VII, ASSAULT_RAPTOR);
    private static final List<Character> PILOT_CHARACTERS              = Arrays.stream(Character.values()).filter(c -> c.skillSet().stream().anyMatch(ss -> ss.colors().contains(PILOTING))).toList();

    @Override
    protected boolean isValid() {
        val validLocation = VIPER_LAUNCH_SPACES.contains(event.location());
        val validShipType = VALID_LAUNCH_VIPER_SHIP_TYPES.contains(event.shipType());
        val validPilot = event.pilot() == null || PILOT_CHARACTERS.contains(event.pilot());
        return validLocation && validShipType && validPilot;
    }

    @Override
    public Followup process() {
        val humanFighter = launchShip();
        if (event.pilot() != null) {
            pilotShip(humanFighter);
        }
        return Followup.NONE;
    }

    private HumanFighter launchShip() {
        val humanFighter = (switch (event.shipType()) {
            case VIPER -> galacticaBoard.removeFromReserves(Viper.class);
            case VIPER_MARK_VII -> galacticaBoard.removeFromReserves(ViperMarkVII.class);
            case ASSAULT_RAPTOR -> galacticaBoard.removeFromReserves(AssaultRaptor.class);
            default -> throw new FrakCallTheAdmiralException();
        })
                .map(HumanFighter.class::cast)
                .orElseGet(() -> landUnmannedViperToPilotIt()); //todo
        galacticaBoard.place(event.location(), humanFighter);
        return humanFighter;
    }

    private void pilotShip(HumanFighter humanFighter) {
        val pilot = event.pilot();
        galacticaBoard.remove(pilot);
        humanFighter.pilot(pilot);
    }

    private HumanFighter landUnmannedViperToPilotIt() {
        val ship = galacticaBoard.shipInSpace(event.viperToLand(), HumanFighter.class);
        galacticaBoard.addToReserves(ship);
        return ship;
    }

}
