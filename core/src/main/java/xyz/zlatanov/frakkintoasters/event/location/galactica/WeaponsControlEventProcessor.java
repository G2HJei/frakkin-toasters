package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.character.DutifulEventProcessor;
import xyz.zlatanov.frakkintoasters.event.AttackCylonShipEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.state.board.Location.WEAPONS_CONTROL;
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
    protected Followup act() {
        return allOf(single(AttackCylonShipEvent.byGalactica(event.cylonShipId())),
                DutifulEventProcessor.after(game, player, WEAPONS_CONTROL)); // Louis Hoshi's "Dutiful"
    }
}
