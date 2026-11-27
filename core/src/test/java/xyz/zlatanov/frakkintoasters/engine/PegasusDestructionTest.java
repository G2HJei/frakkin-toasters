package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.DamageLocationEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.MoveCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

/**
 * Pegasus' destruction (rule 11.7.2.4). Laura Roslin is player 1 and the current player, "Chief" Galen Tyrol player 2,
 * William Adama (Admiral) player 3, Kara "Starbuck" Thrace player 4.
 */
class PegasusDestructionTest {

    // Only the end of the game empties the agenda, so the script ends it.
    private static final Followup CYLONS_WIN = single(new CylonsWinEvent());

    private GameEngine engine;

    // Three Pegasus locations are damaged, "Chief" Galen Tyrol stands in one of them and William Adama in the last one. A hit
    // that the current player puts on Pegasus damages the "Airlock", the last token: Pegasus is destroyed and both go to
    // "Sickbay". The next hit can only be Galactica's.
    @Test
    void theFourthDamagedLocationDestroysPegasus() {
        start(all(setUp(),
                all(new DamageLocationEvent(PEGASUS_CIC), new DamageLocationEvent(MAIN_BATTERIES),
                        new DamageLocationEvent(ENGINE_ROOM)),
                all(new MoveCharacterEvent(CHIEF_GALEN_TYROL, MAIN_BATTERIES), new MoveCharacterEvent(WILLIAM_ADAMA, AIRLOCK)),
                single(new DamageBattlestarEvent()),
                single(new DamageBattlestarEvent()),
                CYLONS_WIN));
        val galacticaTokens = game().decks().deck(DeckId.GALACTICA_DAMAGE).size();

        // the current player may draw a Pegasus damage token instead (rule 11.7.2)
        assertEquals(new AwaitingChoice(1, Set.of(single(new DamageGalacticaEvent()), single(new DamagePegasusEvent()))),
                engine.pendingInput().orElseThrow());
        play(engine -> engine.choose(1, single(new DamagePegasusEvent())));

        val pegasus = game().boards().pegasus();
        assertTrue(pegasus.destroyed());
        assertEquals(SICKBAY, game().locate(CHIEF_GALEN_TYROL).orElseThrow());
        assertEquals(SICKBAY, game().locate(WILLIAM_ADAMA).orElseThrow());
        assertTrue(game().isDestroyed(PEGASUS_CIC));
        // the second hit drew a Galactica token without asking
        assertEquals(galacticaTokens - 1, game().decks().deck(DeckId.GALACTICA_DAMAGE).size());
        assertEquals(EngineStatus.finished(CYLONS), engine.status(1));
    }

    private static Followup setUp() {
        return all(
                new SelectCharacterEvent(1, LAURA_ROSLIN),
                new SelectCharacterEvent(2, CHIEF_GALEN_TYROL),
                new SelectCharacterEvent(3, WILLIAM_ADAMA),
                new SelectCharacterEvent(4, KARA_STARBUCK_THRACE),
                new MakePresidentEvent(LAURA_ROSLIN),
                new MakeAdmiralEvent(WILLIAM_ADAMA));
    }

    private void start(Followup script) {
        engine = new GameEngine(4, Randomness.seeded(42), new ScriptEvent(script));
        assertRestoresTheSameGame();
    }

    private void play(Consumer<GameEngine> input) {
        input.accept(engine);
        assertRestoresTheSameGame();
    }

    private Game game() {
        return (Game) engine.view();
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(snapshot(engine), snapshot(restored));
        assertEquals(statuses(engine), statuses(restored));
    }

    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, 4).mapToObj(engine::status).toList();
    }
}
