package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class AttackGalacticaEventProcessor extends EventProcessor<AttackGalacticaEvent> {

    @Override
    protected boolean isValid() {
        return canAttackGalactica(game, event.cylonShipId());
    }

    @Override
    public Followup process() {
        return rollDie(new AttackGalacticaResultEvent(event.cylonShipId()));
    }

    public static boolean canAttackGalactica(GameView game, int cylonShipId) {
        return game.boards().galactica().shipsInSpace(Raider.class).stream().anyMatch(s -> s.id() == cylonShipId)
                || game.boards().galactica().shipsInSpace(Basestar.class).stream().anyMatch(s -> s.id() == cylonShipId);
    }
}
