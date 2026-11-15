package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;

public class ReturnVipersToReservesEventProcessor extends EventProcessor<ReturnVipersToReservesEvent> {

    @Override
    public Followup process() {
        // Mark VIIs and assault raptors count as vipers (rules 11.3.1.1 and 11.3.2.2).
        for (val fighter : galacticaBoard.shipsInSpace(HumanFighterView.class)) {
            if (fighter.pilot() == null) {
                ops.ship(fighter.id()).returnToReserves();
            } else {
                ops.ship(fighter.id()).land(HANGAR_DECK); // as when the fleet jumps (rule 11.2.3)
            }
        }
        return Followup.NONE;
    }
}
