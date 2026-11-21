package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.EngineRoomActivated;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.PegasusDestroyed;
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

    /**
     * Takes the Pegasus board out of the game (rule 11.7.2.4). The characters on it go to "Sickbay" first.
     */
    public void destroy() {
        board().characters().keySet().forEach(character -> player(character).moveTo(Location.SICKBAY));
        record(new PegasusDestroyed());
    }

    public void activateEngineRoom() {
        record(new EngineRoomActivated());
    }

    @Override
    protected PegasusBoard board() {
        return game.boards().pegasus();
    }
}
