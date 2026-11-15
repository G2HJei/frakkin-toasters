package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforePilotedViperAttack;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Objects;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class AttackCylonShipEventProcessor extends EventProcessor<AttackCylonShipEvent> {

    @Override
    protected boolean isValid() {
        return canAttack(game, event.humanFighterId(), event.cylonShipId());
    }

    @Override
    public Followup process() {
        val piloted = fighter(game, event.humanFighterId()).map(HumanFighterView::pilot).isPresent();
        InterruptWindow before = piloted ? new BeforePilotedViperAttack() : new BeforeDieRoll();
        return rollDie(before, new AfterDieRoll(), new AttackCylonShipResultEvent(event.humanFighterId(), event.cylonShipId()));
    }

    public static boolean canAttack(GameView game, Integer humanFighterId, int cylonShipId) {
        val galactica = game.boards().galactica();
        val target = galactica.shipsInSpace(CylonShip.class).stream().filter(s -> s.id() == cylonShipId).findFirst();
        if (target.isEmpty()) {
            return false;
        }
        // Galactica attacks any Cylon ship in space, a fighter one in its own space area (rule 11.1.1).
        return humanFighterId == null || fighter(game, humanFighterId)
                .filter(fighter -> Objects.equals(galactica.locate(fighter), galactica.locate(target.get())))
                .isPresent();
    }

    public static Optional<HumanFighterView> fighter(GameView game, Integer humanFighterId) {
        return game.boards().galactica().shipsInSpace(HumanFighterView.class).stream()
                .filter(fighter -> Objects.equals(fighter.id(), humanFighterId))
                .findFirst();
    }
}
