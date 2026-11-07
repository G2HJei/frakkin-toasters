package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.EngineRoomActivated;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class PegasusOps extends BattlestarOps {

    PegasusOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public void damage() {
        PegasusDamage pegasusDamage = draw(DeckId.PEGASUS_DAMAGE);
        damageLocation(Location.valueOf(pegasusDamage.name()));
    }

    public void activateEngineRoom() {
        record(new EngineRoomActivated());
    }

    @Override
    protected PegasusBoard board() {
        return game.boards().pegasus();
    }
}
