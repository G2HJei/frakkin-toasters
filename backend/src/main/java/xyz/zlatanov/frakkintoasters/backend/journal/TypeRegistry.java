package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.answer.Pick;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.projection.GameProjection.InputProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection.FactProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection.LogEntry;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The closed set of type names a codec writes into {@code kind}. A name only ever maps to a class registered here, so
 * JSON can't make the codec load a class.
 */
public final class TypeRegistry {

    private final Map<String, Class<?>> byName = new LinkedHashMap<>();
    private final Map<Class<?>, String> byType = new LinkedHashMap<>();

    /**
     * The types of a journal: every event (records and interfaces), every fact as {@code Area.Name} (two areas have a
     * {@code CardsDrawn}), the journal entries, the followups and the interrupt windows.
     */
    public static TypeRegistry journal() {
        val registry = new TypeRegistry();
        EventCatalog.types().forEach(registry::register);
        for (val area : Fact.class.getPermittedSubclasses()) {
            registry.register(area);
            for (val fact : area.getPermittedSubclasses()) {
                registry.register(fact, area.getSimpleName() + "." + fact.getSimpleName());
            }
        }
        registry.register(Fact.class);
        registry.register(JournalEntry.class);
        registry.registerSealed(JournalEntry.class);
        registry.register(Followup.class);
        registry.registerSealed(Followup.class);
        registry.register(InterruptWindow.class);
        registry.registerSealed(InterruptWindow.class);
        return registry;
    }

    /**
     * The types a client gets: those of the journal, and the sealed types of the projections and their legal answers.
     */
    public static TypeRegistry wire() {
        val registry = journal();
        for (val sealed : List.of(Viewer.class, InputProjection.class, LogEntry.class, FactProjection.class, Pick.class)) {
            registry.register(sealed);
            registry.registerSealed(sealed);
        }
        return registry;
    }

    public TypeRegistry register(Class<?> type) {
        return register(type, type.getSimpleName());
    }

    public TypeRegistry registerSealed(Class<?> sealedType) {
        for (val type : sealedType.getPermittedSubclasses()) {
            register(type);
        }
        return this;
    }

    public TypeRegistry register(Class<?> type, String name) {
        val previous = byName.putIfAbsent(name, type);
        if (previous != null && previous != type) {
            throw new IllegalStateException("Two types are named " + name + ": " + previous + " and " + type);
        }
        byType.put(type, name);
        return this;
    }

    public String nameOf(Class<?> type) {
        val name = byType.get(type);
        if (name == null) {
            throw new CodecException("Not a registered type: " + type.getName());
        }
        return name;
    }

    public Optional<Class<?>> typeOf(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    /**
     * @return the registered types, in the order they were registered
     */
    public Collection<Class<?>> types() {
        return Collections.unmodifiableCollection(byType.keySet());
    }
}
