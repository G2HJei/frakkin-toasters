package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;

public class ReturnAssaultRaptorEventProcessor extends EventProcessor<ReturnAssaultRaptorEvent> {

    static int decidingPlayer(GameView game, HumanFighterView raptor) {
        return raptor.pilot() == null ? game.turn().currentPlayerNumber() : game.playerNumber(raptor.pilot());
    }

    @Override
    protected boolean isValid() {
        val raptor = galacticaBoard.shipsInSpace(AssaultRaptor.class).stream()
                .filter(ship -> ship.id() == event.shipId())
                .findFirst();
        return raptor.isPresent() && decidingPlayer(game, raptor.get()) == event.playerNumber();
    }

    @Override
    public Followup process() {
        val raptor = galacticaBoard.shipInSpace(event.shipId(), HumanFighterView.class);
        if (raptor.pilot() == null) {
            ops.ship(raptor.id()).returnToReserves();
        } else {
            ops.ship(raptor.id()).land(HANGAR_DECK);
        }
        return Followup.NONE;
    }
}
