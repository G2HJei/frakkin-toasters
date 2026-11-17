package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.journal.GameReplayer;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.ScriptedScenario;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;

class JournalCodecTest {

    private final JournalCodec codec = new JournalCodec();

    @TestFactory
    Stream<DynamicTest> everyEventGoesToJsonAndBack() {
        return EventCatalog.types().stream()
                .filter(Class::isRecord)
                .map(type -> DynamicTest.dynamicTest(type.getSimpleName(), () -> {
                    val event = (Event) SampleValues.of(type);
                    val entry = new ScopeOpened(1, 0, event, 2);
                    assertEquals(entry, codec.decode(codec.encode(entry)));
                }));
    }

    @TestFactory
    Stream<DynamicTest> everyFactGoesToJsonAndBack() {
        return Arrays.stream(Fact.class.getPermittedSubclasses())
                .flatMap(area -> Arrays.stream(area.getPermittedSubclasses()))
                .map(type -> DynamicTest.dynamicTest(type.getSimpleName(), () -> {
                    val entry = new JournalEntry.FactRecorded(3, (Fact) SampleValues.of(type));
                    assertEquals(entry, codec.decode(codec.encode(entry)));
                }));
    }

    @Test
    void theCatalogHoldsTheEventsWithoutAProcessorAndTheInterfaces() {
        val types = EventCatalog.types();

        assertTrue(types.containsAll(List.of(PlayerDecisionEvent.class, ActionEvent.class, Event.class, MoveEvent.class)));
        assertEquals(types.size(), types.stream().map(Class::getSimpleName).distinct().count(),
                "An event is named by its simple class name, so two events can't share it");
    }

    @Test
    void anEventIsNamedByItsSimpleNameAndAFactWithItsArea() {
        val json = codec.toJson(new ScopeOpened(1, 0, new MoveEvent(2, COMMAND, null), 2));

        assertEquals("{\"kind\":\"ScopeOpened\",\"scopeId\":1,\"parentId\":0,"
                        + "\"event\":{\"kind\":\"MoveEvent\",\"playerNumber\":2,\"destination\":\"COMMAND\",\"discardCard\":null},"
                        + "\"actor\":2}",
                JournalCodec.stringify(json));
        assertEquals("{\"kind\":\"FactRecorded\",\"scopeId\":1,\"fact\":{\"kind\":\"SkillCheckFact.CardsDrawn\",\"pile\":\"MATCHING\",\"cards\":[]}}",
                codec.encode(new JournalEntry.FactRecorded(1,
                        new xyz.zlatanov.frakkintoasters.engine.journal.fact.SkillCheckFact.CardsDrawn(
                                xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile.MATCHING, List.of()))));
    }

    @Test
    void aChoiceKeepsTheOrderOfItsOptions() {
        val options = new LinkedHashSet<Followup>();
        for (val card : List.of(CrisisCard.DETENTE, CrisisCard.RIOTS_NO_JUMP, CrisisCard.values()[0])) {
            options.add(single(new ResolveCrisisCardEvent(card)));
        }
        options.add(single(new NoOpEvent(1)));
        val entry = new JournalEntry.ScopeClosed(4, new Followup.OneOf(options));

        val decoded = (JournalEntry.ScopeClosed) codec.decode(codec.encode(entry));

        assertEquals(List.copyOf(options), List.copyOf(((Followup.OneOf) decoded.followup()).options()));
    }

    @Test
    void jsonCantNameAClassOutsideTheRegistry() {
        assertThrows(CodecException.class, () -> codec.decode("{\"kind\":\"java.lang.Runtime\"}"));
        assertThrows(CodecException.class, () -> codec.decode(
                "{\"kind\":\"ScopeOpened\",\"scopeId\":1,\"parentId\":0,\"actor\":1,\"event\":{\"kind\":\"Runtime\"}}"));
        // a decision names its action by type name, and only an event fits
        assertThrows(CodecException.class, () -> codec.decode("""
                {"kind":"ScopeOpened","scopeId":1,"parentId":0,"actor":1,"event":
                {"kind":"PlayerDecisionEvent","playerNumber":1,"action":"FactRecorded","eventConstraints":[]}}"""));
    }

    @Test
    void unknownFieldsAndWrongTypesAreRejected() {
        assertThrows(CodecException.class, () -> codec.readEvent(JournalCodec.parse(
                "{\"kind\":\"NoOpEvent\",\"playerNumber\":1,\"extra\":2}"), Event.class));
        assertThrows(CodecException.class, () -> codec.readEvent(JournalCodec.parse(
                "{\"kind\":\"NoOpEvent\",\"playerNumber\":\"1\"}"), Event.class));
        assertThrows(CodecException.class, () -> codec.readEvent(JournalCodec.parse(
                "{\"kind\":\"NoOpEvent\"}"), Event.class));
        assertThrows(CodecException.class, () -> codec.readEvent(JournalCodec.parse(
                "{\"kind\":\"MoveEvent\",\"playerNumber\":1,\"destination\":\"NOWHERE\"}"), Event.class));
        assertThrows(CodecException.class, () -> codec.readEvent(JournalCodec.parse(
                "{\"kind\":\"NoOpEvent\",\"playerNumber\":1}"), MoveEvent.class));
    }

    @Test
    void theScriptedScenarioReplaysFromJson() {
        val journal = new Journal();
        val live = ScriptedScenario.play(journal);

        val decoded = roundTrip(journal);

        assertEquals(journal.entries(), decoded.entries());
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(GameReplayer.replay(decoded)));
    }

    @Test
    void aSeededGameRestoresFromJsonAtEveryPause() {
        val engine = ScriptedTurns.start();
        assertRestoresFromJson(engine);
        for (val input : ScriptedTurns.inputs()) {
            input.apply(engine.view()).applyTo(engine);
            assertRestoresFromJson(engine);
        }
        assertEquals(5, engine.view().turn().number());
    }

    private void assertRestoresFromJson(GameEngine engine) {
        val restored = GameEngine.restore(roundTrip(engine.journal()));

        assertEquals(GameSnapshot.of((Game) engine.view()), GameSnapshot.of((Game) restored.view()));
        assertEquals(IntStream.rangeClosed(1, 3).mapToObj(engine::status).toList(),
                IntStream.rangeClosed(1, 3).mapToObj(restored::status).toList());
    }

    private Journal roundTrip(Journal journal) {
        return new Journal(journal.entries().stream().map(codec::encode).map(codec::decode).toList());
    }
}
