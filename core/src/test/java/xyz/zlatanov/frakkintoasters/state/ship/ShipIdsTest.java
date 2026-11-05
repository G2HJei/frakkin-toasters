package xyz.zlatanov.frakkintoasters.state.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Replay relies on ship ids being the same in every game, independent of shuffles.
 */
class ShipIdsTest {

    @Test
    void shipIdsDoNotDependOnRandomness() {
        assertEquals(ids(Game.builder().build()), ids(Game.builder().build()));
    }

    @Test
    void reserveViperIsPickedDeterministically() {
        var first = Game.builder().build().boards().galactica().removeFromReserves(Viper.class).orElseThrow().id();
        var second = Game.builder().build().boards().galactica().removeFromReserves(Viper.class).orElseThrow().id();
        assertEquals(first, second);
    }

    private List<Integer> ids(Game game) {
        var ids = new ArrayList<Integer>();
        game.boards().galactica().reserves().stream().map(Ship::id).sorted().forEach(ids::add);
        game.cylonShips().basestars().forEach(s -> ids.add(s.id()));
        game.cylonShips().raiders().forEach(s -> ids.add(s.id()));
        game.cylonShips().heavyRaiders().forEach(s -> ids.add(s.id()));
        game.cylonShips().centurions().forEach(s -> ids.add(s.id()));
        game.decks().civilianShips().cards().stream().map(Ship::id).sorted().forEach(ids::add);
        return ids;
    }
}
