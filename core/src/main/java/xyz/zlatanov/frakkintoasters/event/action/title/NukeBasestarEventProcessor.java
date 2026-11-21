package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.DestroyRaidersEventProcessor.destroyRaidersIn;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.title.NukeResultEventProcessor.ROLL_THAT_DESTROYS_RAIDERS;

public class NukeBasestarEventProcessor extends EventProcessor<NukeBasestarEvent> {

    private static final int HIGHEST_ROLL_TO_DAMAGE = 2;

    @Override
    protected boolean isValid() {
        return event.result() >= 1
                && event.result() <= ROLL_THAT_DESTROYS_RAIDERS
                && galacticaBoard.shipsInSpace(Basestar.class).stream().anyMatch(b -> b.id() == event.basestarId());
    }

    @Override
    public Followup process() {
        if (event.result() <= HIGHEST_ROLL_TO_DAMAGE) {
            return all(new DamageBasestarEvent(event.basestarId()), new DamageBasestarEvent(event.basestarId()));
        }
        val area = galacticaBoard.locate(galacticaBoard.shipInSpace(event.basestarId(), Basestar.class));
        ops.ship(event.basestarId()).destroy();
        val raiders = event.result() == ROLL_THAT_DESTROYS_RAIDERS ? destroyRaidersIn(game, area, 3) : Followup.NONE;
        return allOf(raiders, single(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed())));
    }
}
