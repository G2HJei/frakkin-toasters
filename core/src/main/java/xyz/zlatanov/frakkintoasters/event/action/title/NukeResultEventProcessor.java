package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static xyz.zlatanov.frakkintoasters.event.DestroyRaidersEventProcessor.destroyRaidersIn;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class NukeResultEventProcessor extends EventProcessor<NukeResultEvent> {

    static final int ROLL_THAT_DESTROYS_EVERYTHING = 8;
    static final int ROLL_THAT_DESTROYS_RAIDERS    = 7;

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null && LaunchNukeEventProcessor.isSpaceArea(event.area());
    }

    @Override
    public Followup process() {
        val roll = game.turn().dieRoll().result();
        if (roll >= ROLL_THAT_DESTROYS_EVERYTHING) {
            return destroyEverything();
        }
        val basestars = galacticaBoard.shipsIn(event.area(), Basestar.class);
        if (basestars.isEmpty()) {
            // No basestar there: on a 7 the raiders are still destroyed, otherwise the nuke has no effect.
            return roll == ROLL_THAT_DESTROYS_RAIDERS ? destroyRaidersIn(game, event.area(), 3) : NONE;
        }
        // With more than one basestar, the player who launched the nuke chooses (rule 11.6.1).
        return one(basestars.stream()
                .map(basestar -> new NukeBasestarEvent(event.playerNumber(), basestar.id(), roll))
                .toArray(Event[]::new));
    }

    // "every ship in that space area is destroyed, including raiders, heavy raiders, basestars, vipers, and civilian
    // ships": a pilot goes to "Sickbay" and a civilian ship costs its resources, as any destruction does.
    private Followup destroyEverything() {
        val ships = galacticaBoard.shipsIn(event.area(), Ship.class);
        val basestarDestroyed = ships.stream().anyMatch(Basestar.class::isInstance);
        ships.stream().map(Ship::id).sorted().forEach(id -> ops.ship(id).destroy());
        return basestarDestroyed ? single(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed())) : NONE;
    }
}
