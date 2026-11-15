package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.AttackCylonShipEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class WeaponsControlEventProcessor extends LocationActionProcessor<WeaponsControlEvent> {

    public static boolean canAct(GameView game) {
        return !game.boards().galactica().shipsInSpace(CylonShip.class).isEmpty();
    }

    @Override
    protected boolean isValidAction() {
        return galacticaBoard.shipsInSpace(CylonShip.class).stream().anyMatch(s -> s.id() == event.cylonShipId());
    }

    @Override
    public Followup process() {
        return single(AttackCylonShipEvent.byGalactica(event.cylonShipId()));
    }
}
