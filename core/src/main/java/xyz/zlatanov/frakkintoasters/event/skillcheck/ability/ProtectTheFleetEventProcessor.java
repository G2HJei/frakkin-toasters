package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.VIPER;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.player.LaunchViperEventProcessor.canLaunchFromReserves;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.DISTANCE_LOOKUP_TABLE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;

public class ProtectTheFleetEventProcessor extends EventProcessor<ProtectTheFleetEvent> {

    private static final int PILOTING_STRENGTH_NEEDED = 3;

    // The player may decline. Activating the unmanned viper takes 3 piloting strength in the check, and the event names
    // exactly one activation: a launch, a move, an attack or an escort (rule 11.1.1).
    @Override
    protected boolean isValid() {
        if (namedFields() == 0) {
            return true;
        }
        if (pilotingStrength() < PILOTING_STRENGTH_NEEDED || namedFields() != 2) {
            return false;
        }
        if (event.launchShipType() != null) {
            return canLaunchFromReserves(game, event.launchShipType(), event.launchLocation());
        }
        return unmannedFighterInSpace().filter(this::validActivation).isPresent();
    }

    @Override
    public Followup process() {
        if (event.launchShipType() != null) {
            return one(new LaunchViperEvent(event.playerNumber(), event.launchShipType(), event.launchLocation()));
        }
        if (event.humanFighterId() != null && event.moveLocation() != null) {
            return one(new MoveShipEvent(event.humanFighterId(), event.moveLocation()));
        }
        if (event.humanFighterId() != null && event.attackCylonShipId() != null) {
            return one(new AttackBasestarEvent(VIPER, event.attackCylonShipId())); // todo raiders and heavy raiders, phase 10
        }
        if (event.escortCivilianShipId() != null) {
            ops.ship(event.escortCivilianShipId()).escort();
        }
        return NONE;
    }

    private long namedFields() {
        return Stream.of(event.launchShipType(), event.launchLocation(), event.humanFighterId(), event.moveLocation(),
                        event.attackCylonShipId(), event.escortCivilianShipId())
                .filter(Objects::nonNull)
                .count();
    }

    private int pilotingStrength() {
        val skillCheck = game.activeSkillCheck();
        return Stream.concat(skillCheck.matchingPile().cards().stream(), skillCheck.nonMatchingPile().cards().stream())
                .filter(card -> card.type().color() == PILOTING)
                .mapToInt(skillCheck::cardValue)
                .sum();
    }

    private Optional<HumanFighterView> unmannedFighterInSpace() {
        return galacticaBoard.shipsInSpace(HumanFighterView.class)
                .stream()
                .filter(fighter -> Objects.equals(fighter.id(), event.humanFighterId()) && fighter.pilot() == null)
                .findFirst();
    }

    private boolean validActivation(HumanFighterView fighter) {
        val area = galacticaBoard.locate(fighter);
        if (event.moveLocation() != null) {
            // A mark VII may move 2 space areas (rule 11.3.1.2).
            val distance = DISTANCE_LOOKUP_TABLE.get(area).get(event.moveLocation());
            return distance != null && distance <= (fighter instanceof ViperMarkVII ? 2 : 1);
        }
        if (event.attackCylonShipId() != null) {
            return inArea(area, CylonShip.class, event.attackCylonShipId());
        }
        return event.escortCivilianShipId() != null && inArea(area, CivilianShip.class, event.escortCivilianShipId());
    }

    private boolean inArea(Location area, Class<? extends Ship> shipClass, int shipId) {
        return galacticaBoard.shipsIn(area, shipClass).stream().anyMatch(ship -> ship.id() == shipId);
    }
}
