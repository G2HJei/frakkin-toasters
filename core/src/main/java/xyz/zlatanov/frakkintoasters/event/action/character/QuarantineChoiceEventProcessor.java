package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class QuarantineChoiceEventProcessor extends EventProcessor<QuarantineChoiceEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(ship -> ship.id() == event.civilianShipId());
    }

    // A new ship takes its place, if one is left, then it is shuffled back into the deck.
    @Override
    public Followup process() {
        val ship = galacticaBoard.shipsInSpace(CivilianShip.class).stream()
                .filter(s -> s.id() == event.civilianShipId())
                .findFirst()
                .orElseThrow();
        val area = galacticaBoard.locate(ship);
        if (game.decks().civilianShipsLeft() > 0) {
            ops.galactica().placeCivilianShip(area);
        }
        ops.ship(event.civilianShipId()).escort();
        return NONE;
    }
}
