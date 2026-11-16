package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.endgame.EndGameEvent;
import xyz.zlatanov.frakkintoasters.event.jump.DrawDestinationCardsEvent;
import xyz.zlatanov.frakkintoasters.event.jump.MoveCylonShipsToFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.event.jump.ReturnAssaultRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

/**
 * The fleet jumps (rule 12.1.1). Each step is an event: the Admiral's choice of the Destination Card is a decision, so
 * the steps after it (the card's instructions, the Objective Card's, the reset of the Jump Preparation track) run from
 * {@code ResolveDestinationCardEvent}.
 */
public class JumpingTheFleetEventProcessor extends EventProcessor<JumpingTheFleetEvent> {

    /**
     * With the Kobol Objective, after 8 distance the next jump is the last one (rule 12.4.2).
     */
    public static final int KOBOL_DISTANCE = 8;

    @Override
    public Followup process() {
        if (game.distanceTraveled() >= KOBOL_DISTANCE) {
            // "They do not draw a Destination Card; instead, the End Game determines the winnerPlayerNumbers."
            return single(new EndGameEvent());
        }
        val steps = new ArrayList<Followup>();
        // 1. Move Vipers. Mark VIIs are vipers. An assault raptor stays in its space area if its player chooses.
        for (val raptor : galacticaBoard.shipsInSpace(AssaultRaptor.class)) {
            val player = raptor.pilot() == null ? game.turn().currentPlayerNumber() : game.playerNumber(raptor.pilot());
            steps.add(one(new ReturnAssaultRaptorEvent(player, raptor.id()), new NoOpEvent(player)));
        }
        steps.add(single(new ReturnVipersToReservesEvent(true)));
        // 2. Move Cylon Ships.
        steps.add(single(new MoveCylonShipsToFleetBoardEvent()));
        // 3. Remove Mission Cards belongs to the Earth Objective, which is out of scope.
        // 4. Choose Destination, then the rest.
        steps.add(single(new DrawDestinationCardsEvent()));
        return all(steps.toArray(Followup[]::new));
    }
}
