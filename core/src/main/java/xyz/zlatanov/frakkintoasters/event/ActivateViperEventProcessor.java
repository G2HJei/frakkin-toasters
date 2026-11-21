package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.AttackCylonShipEventProcessor.canAttack;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.LaunchViperEventProcessor.canLaunchFromReserves;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.DISTANCE_LOOKUP_TABLE;

public class ActivateViperEventProcessor extends EventProcessor<ActivateViperEvent> {

    @Override
    protected boolean isValid() {
        val ownFighter = galacticaBoard.pilotedFighter(player.character());
        if (ownFighter.isPresent()) {
            // "he may instead choose to activate his viper (to move again, attack or escort a civilian ship)" (rule 11.2.1)
            return isValidActivation(game, event.activation(), fighter -> fighter.id() == ownFighter.get().id(), false);
        }
        return isValidActivation(game, event.activation(), fighter -> fighter.pilot() == null, true);
    }

    @Override
    public Followup process() {
        return activate(ops, event.playerNumber(), event.activation());
    }

    public static boolean canActivateUnmanned(GameView game) {
        // One in the "Reserves" can be launched, and one in space can at least move.
        val galactica = game.boards().galactica();
        return galactica.reserves().stream().anyMatch(HumanFighterView.class::isInstance)
                || galactica.shipsInSpace(HumanFighterView.class).stream().anyMatch(fighter -> fighter.pilot() == null);
    }

    public static boolean isValidActivation(GameView game, ViperActivation activation,
                                            Predicate<HumanFighterView> mayActivate, boolean mayLaunch) {
        if (activation == null || namedFields(activation) != 2) {
            return false; // exactly one option of rule 11.1.1
        }
        if (activation.launchShipType() != null) {
            return mayLaunch && canLaunchFromReserves(game, activation.launchShipType(), activation.launchLocation());
        }
        return game.boards().galactica().shipsInSpace(HumanFighterView.class).stream()
                .filter(fighter -> Objects.equals(fighter.id(), activation.humanFighterId()))
                .filter(mayActivate)
                .anyMatch(fighter -> isValidOption(game, fighter, activation));
    }

    public static Followup activate(GameOps ops, int playerNumber, ViperActivation activation) {
        if (activation.launchShipType() != null) {
            return single(new LaunchViperEvent(playerNumber, activation.launchShipType(), activation.launchLocation()));
        }
        if (activation.moveLocation() != null) {
            ops.ship(activation.humanFighterId()).moveTo(activation.moveLocation());
            return NONE;
        }
        if (activation.attackCylonShipId() != null) {
            return single(AttackCylonShipEvent.byFighter(activation.humanFighterId(), activation.attackCylonShipId()));
        }
        ops.ship(activation.escortCivilianShipId()).escort();
        return NONE;
    }

    /**
     * @return every legal activation of a fighter in space, as rule 11.1.1 lists them: its moves, its attacks and its
     * escorts, or its attacks only. A launch isn't one: the fighter is in space already
     */
    public static List<ViperActivation> activations(GameView game, HumanFighterView fighter, boolean attacksOnly) {
        val galactica = game.boards().galactica();
        val area = galactica.locate(fighter);
        val activations = new ArrayList<ViperActivation>();
        if (!attacksOnly) {
            LocationsArea.GALACTICA_SPACE.locations().stream()
                    .filter(to -> to != area)
                    .map(to -> ViperActivation.move(fighter.id(), to))
                    .filter(move -> isValidOption(game, fighter, move))
                    .forEach(activations::add);
        }
        galactica.shipsIn(area, CylonShip.class).stream()
                .filter(target -> canAttack(game, fighter.id(), target.id()))
                .forEach(target -> activations.add(ViperActivation.attack(fighter.id(), target.id())));
        if (!attacksOnly) {
            galactica.shipsIn(area, CivilianShip.class)
                    .forEach(ship -> activations.add(ViperActivation.escort(fighter.id(), ship.id())));
        }
        return activations;
    }

    private static long namedFields(ViperActivation activation) {
        return Stream.of(activation.launchShipType(), activation.launchLocation(), activation.humanFighterId(),
                        activation.moveLocation(), activation.attackCylonShipId(), activation.escortCivilianShipId())
                .filter(Objects::nonNull)
                .count();
    }

    private static boolean isValidOption(GameView game, HumanFighterView fighter, ViperActivation activation) {
        val galactica = game.boards().galactica();
        val area = galactica.locate(fighter);
        if (activation.moveLocation() != null) {
            // A mark VII may move 2 space areas (rule 11.3.1.2).
            val distance = DISTANCE_LOOKUP_TABLE.get(area).get(activation.moveLocation());
            return distance != null && distance <= (fighter instanceof ViperMarkVII ? 2 : 1);
        }
        if (activation.attackCylonShipId() != null) {
            return canAttack(game, fighter.id(), activation.attackCylonShipId());
        }
        return activation.escortCivilianShipId() != null && galactica.shipsIn(area, CivilianShip.class).stream()
                .anyMatch(ship -> ship.id() == activation.escortCivilianShipId());
    }
}
