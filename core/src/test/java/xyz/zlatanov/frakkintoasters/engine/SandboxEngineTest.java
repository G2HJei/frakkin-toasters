package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.EventInjected;
import xyz.zlatanov.frakkintoasters.event.sandbox.*;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class SandboxEngineTest {

    @Test
    void aSeededGameUndoneAtEveryPauseGivesTheGameOfThatPause() {
        val engine = ScriptedTurns.start();
        val snapshots = new ArrayList<GameSnapshot>();
        snapshots.add(snapshot(engine));
        for (val input : ScriptedTurns.inputs()) {
            input.apply(engine.view()).applyTo(engine);
            snapshots.add(snapshot(engine));
        }

        for (int pause = snapshots.size() - 2; pause >= 0; pause--) {
            engine.undo();
            assertEquals(snapshots.get(pause), snapshot(engine), "pause " + pause);
        }
        assertThrows(InvalidActionException.class, engine::undo); // before the first pause there is nothing to undo
    }

    @Test
    void undoThenTheSameInputGivesTheSameGame() {
        val engine = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        for (val input : inputs.subList(0, 6)) {
            input.apply(engine.view()).applyTo(engine);
        }
        val before = snapshot(engine);

        engine.undo();
        inputs.get(5).apply(engine.view()).applyTo(engine);

        assertEquals(before, snapshot(engine));
    }

    @Test
    void theSameSeedAndInputsWithARestoreInTheMiddleGiveTheSameGame() {
        val straight = ScriptedTurns.play();
        var engine = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        for (int i = 0; i < inputs.size(); i++) {
            if (i == 4 || i == 11) {
                engine = GameEngine.restore(engine.journal(), ScriptedTurns.SEED);
            }
            inputs.get(i).apply(engine.view()).applyTo(engine);
        }

        assertEquals(snapshot(straight), snapshot(engine));
    }

    @Test
    void aRestoredSeededGameUndoesItsInputsToo() {
        val engine = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        inputs.get(0).apply(engine.view()).applyTo(engine);
        val first = snapshot(engine);
        inputs.get(1).apply(engine.view()).applyTo(engine);

        val restored = GameEngine.restore(engine.journal(), ScriptedTurns.SEED);
        restored.undo();

        assertEquals(first, snapshot(restored));
    }

    @TestFactory
    Stream<DynamicTest> eachSetupEventInjectedAndRestoredGivesTheSameGame() {
        List<Function<GameView, SandboxEvent>> setups = List.of(
                game -> new PutCrisisCardOnTopEvent(game.decks().<CrisisCard>deck(DeckId.CRISIS).cards().get(5)),
                game -> new PutDestinationCardOnTopEvent(game.decks().<DestinationCard>deck(DeckId.DESTINATION).cards().get(3)),
                game -> new PutSkillCardOnTopEvent(game.decks().<SkillCard>deck(DeckId.TACTICS).cards().get(4)),
                game -> new PutDestinyCardOnTopEvent(game.decks().<SkillCard>deck(DeckId.PILOTING).cards().get(2)),
                game -> new GiveSkillCardToPlayerEvent(2, game.decks().<SkillCard>deck(DeckId.ENGINEERING).cards().get(1)),
                game -> new MoveCharacterEvent(WILLIAM_ADAMA, ARMORY),
                game -> new PlaceShipEvent(ShipType.BASESTAR, GALACTICA_SPACE_2_OCLOCK),
                game -> new PlaceShipEvent(ShipType.HEAVY_RAIDER, GALACTICA_SPACE_8_OCLOCK),
                game -> new PlaceShipEvent(ShipType.CIVILIAN, GALACTICA_SPACE_10_OCLOCK),
                game -> new ChangeResourceEvent(Resource.FOOD, -3),
                game -> new SetJumpPreparationEvent(JumpPreparation.MINUS_3),
                game -> new DamageLocationEvent(ARMORY),
                game -> new AssignTitleEvent(Title.PRESIDENT, WILLIAM_ADAMA));
        return IntStream.range(0, setups.size()).mapToObj(index -> DynamicTest.dynamicTest("setup " + index, () -> {
            val engine = afterTheCharacters();
            val setup = setups.get(index).apply(engine.view());
            val before = engine.journal().size();

            engine.inject(setup);

            assertEquals(new EventInjected(setup), engine.journal().entries().get(before));
            val restored = GameEngine.restore(engine.journal(), ScriptedTurns.SEED);
            assertEquals(snapshot(engine), snapshot(restored));
            assertEquals(engine.pendingInput(), restored.pendingInput()); // the input it was injected ahead of
        }));
    }

    @Test
    void aSetupChangesTheGameAsItSays() {
        val engine = afterTheCharacters();
        val crisis = engine.view().decks().<CrisisCard>deck(DeckId.CRISIS).cards().get(7);
        val crisisCards = engine.view().decks().crisisCardsLeft();

        engine.inject(new PutCrisisCardOnTopEvent(crisis));
        engine.inject(new AssignTitleEvent(Title.ADMIRAL, LAURA_ROSLIN));

        assertEquals(crisis, engine.view().decks().<CrisisCard>deck(DeckId.CRISIS).cards().getFirst());
        assertEquals(crisisCards, engine.view().decks().crisisCardsLeft()); // no copy appeared
        assertEquals(LAURA_ROSLIN, engine.view().admiral());
    }

    @Test
    void undoAfterAnInjectedEventRemovesIt() {
        val engine = afterTheCharacters();
        val before = snapshot(engine);
        val size = engine.journal().size();

        engine.inject(new ChangeResourceEvent(Resource.MORALE, -2));
        engine.undo();

        assertEquals(before, snapshot(engine));
        assertEquals(size, engine.journal().size());
    }

    @Test
    void anInvalidSetupChangesNothingAndASetupIsNeverAnAnswer() {
        val engine = afterTheCharacters();
        val size = engine.journal().size();

        assertThrows(InvalidActionException.class, () -> engine.inject(new ChangeResourceEvent(Resource.FUEL, 20)));
        assertThrows(InvalidActionException.class, () -> engine.inject(new DamageLocationEvent(GALACTICA_SPACE_2_OCLOCK)));
        assertEquals(size, engine.journal().size());
        val asked = engine.pendingInput().orElseThrow().player();
        assertThrows(InvalidActionException.class, () -> engine.submit(asked, new ChangeResourceEvent(Resource.FUEL, 1)));
    }

    private static GameEngine afterTheCharacters() {
        val engine = ScriptedTurns.start();
        for (val input : ScriptedTurns.inputs().subList(0, 3)) {
            input.apply(engine.view()).applyTo(engine);
        }
        return engine;
    }

    private static GameSnapshot snapshot(GameEngine engine) {
        return GameSnapshot.of((Game) engine.view());
    }
}
