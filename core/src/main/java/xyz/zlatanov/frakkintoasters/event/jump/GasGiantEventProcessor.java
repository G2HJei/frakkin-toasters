package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Comparator;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

public class GasGiantEventProcessor extends EventProcessor<GasGiantEvent> {

    // Simplified: only a viper in the "Reserves" is destroyed, the one with the lowest id, so no piloted viper is lost.
    private static Optional<Ship> viper(GameView game) {
        return game.boards().galactica().reserves().stream()
                .filter(Viper.class::isInstance)
                .min(Comparator.comparingInt(Ship::id));
    }

    public static boolean canDestroyAViper(GameView game) {
        return viper(game).isPresent();
    }

    @Override
    protected boolean isValid() {
        return game.playerNumber(game.admiral()) == event.playerNumber() && canDestroyAViper(game);
    }

    @Override
    public Followup process() {
        ops.ship(viper(game).orElseThrow().id()).destroy();
        ops.galactica().increaseResource(FUEL, 1);
        return Followup.NONE;
    }
}
