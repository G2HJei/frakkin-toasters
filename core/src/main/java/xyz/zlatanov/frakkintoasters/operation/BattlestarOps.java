package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.LocationDamaged;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.LocationRepaired;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.BattlestarBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

abstract class BattlestarOps extends GameOps {

    BattlestarOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    protected abstract BattlestarBoard board();

    /**
     * Repairs a damaged location: its damage token goes back to the pile, which is shuffled (rule 11.7.2.2).
     */
    public void repairLocation(Location location) {
        record(new LocationRepaired(location));
        DamageTokens.of(location).ifPresent(token -> {
            discardToDeck(token);
            shuffle(DamageTokens.deckOf(token));
        });
    }

    protected void damageLocation(Location location) {
        assert board().locations().contains(location);
        record(new LocationDamaged(location));
        board().charactersIn(location).stream()
                .map(this::player)
                .forEach(p -> p.moveTo(SICKBAY));
    }
}
