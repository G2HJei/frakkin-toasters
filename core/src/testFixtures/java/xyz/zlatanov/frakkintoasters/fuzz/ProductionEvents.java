package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;

import java.util.List;

/**
 * The events of the rules, for the report's list of the events no game ran.
 */
final class ProductionEvents {

    private static final String MAIN = codeSource(GameEngine.class);

    private ProductionEvents() {
    }

    /**
     * @return the simple names of the events a game can run: the records of the rules with a processor, not the
     * sandbox's setup events
     */
    static List<String> types() {
        return EventCatalog.types().stream()
                .filter(Class::isRecord)
                .filter(ProcessorRegistry::hasProcessor)
                .filter(type -> !SandboxEvent.class.isAssignableFrom(type))
                .filter(type -> MAIN.equals(codeSource(type)))
                .map(Class::getSimpleName)
                .sorted()
                .toList();
    }

    private static String codeSource(Class<?> type) {
        val source = type.getProtectionDomain().getCodeSource();
        return source == null ? "" : source.getLocation().toString();
    }
}
