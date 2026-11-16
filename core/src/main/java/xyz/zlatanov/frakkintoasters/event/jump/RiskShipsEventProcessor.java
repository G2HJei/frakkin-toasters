package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class RiskShipsEventProcessor extends EventProcessor<RiskShipsEvent> {

    /**
     * @return whether the ships can be risked: there have to be that many of the type in the "Reserves" (rule 13.5).
     * An assault raptor counts as a viper, not as a raptor, and a destroyed ship isn't in the "Reserves"
     */
    public static boolean canRisk(GameView game, RiskShipsEvent.Risk risk) {
        val reserves = game.boards().galactica().reserves().stream();
        val available = risk.vipers()
                ? reserves.filter(Viper.class::isInstance).count()
                : reserves.filter(Raptor.class::isInstance).count();
        return available >= risk.count();
    }

    @Override
    protected boolean isValid() {
        return event.risk() != null
                && game.playerNumber(game.admiral()) == event.playerNumber()
                && canRisk(game, event.risk());
    }

    @Override
    public Followup process() {
        return rollDie(new RiskResultEvent(event.risk()));
    }
}
