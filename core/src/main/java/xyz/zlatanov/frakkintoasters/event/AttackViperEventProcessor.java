package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterViperAttacked;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class AttackViperEventProcessor extends EventProcessor<AttackViperEvent> {

    @Override
    protected boolean isValid() {
        val raider = galacticaBoard.shipsInSpace(Raider.class).stream()
                .filter(s -> s.id() == event.raiderId())
                .findFirst();
        val fighter = galacticaBoard.shipsInSpace(HumanFighterView.class).stream()
                .filter(s -> s.id() == event.humanFighterId())
                .findFirst();
        return raider.isPresent() && fighter.isPresent()
                && Objects.equals(galacticaBoard.locate(raider.get()), galacticaBoard.locate(fighter.get()));
    }

    @Override
    public Followup process() {
        val humanFighterId = event.humanFighterId();
        return rollDie(new BeforeDieRoll(), new AfterViperAttacked(humanFighterId), new AttackViperResultEvent(humanFighterId));
    }
}
