package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.isInt;
import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.raw;
import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.typeArgument;

/**
 * Walks the types of a {@link TypeRegistry} and every record, enum and interface their components reach, in the shapes
 * the {@link ValueCodec} writes them. The format lock prints them, and the wire schema of the frontend is built from them.
 */
public final class TypeWalker {

    private TypeWalker() {
    }

    /**
     * @return the registered types and the types they reach, sorted by name
     */
    public static List<Class<?>> reachable(Collection<Class<?>> roots) {
        val seen = new LinkedHashSet<Class<?>>();
        val queue = new ArrayDeque<Class<?>>(roots);
        while (!queue.isEmpty()) {
            val type = queue.poll();
            if (!seen.add(type)) {
                continue;
            }
            if (type.isRecord()) {
                for (val component : type.getRecordComponents()) {
                    queue.addAll(named(component.getGenericType()));
                }
            }
            // the card lists beside a deck or a pile
            if (type == DeckId.class) {
                Arrays.stream(DeckId.values()).map(DeckId::cardType).forEach(queue::add);
            }
            if (type == PlayerPile.class) {
                Arrays.stream(PlayerPile.values()).map(PlayerPile::cardType).forEach(queue::add);
            }
        }
        return seen.stream()
                .sorted(Comparator.comparing(Class::getName))
                .toList();
    }

    /**
     * @return the records, enums and interfaces a component type names, e.g. {@code SkillCard} for a {@code List<SkillCard>}
     */
    public static Set<Class<?>> named(Type type) {
        val result = new LinkedHashSet<Class<?>>();
        if (ValueCodec.isWildcardList(type)) {
            return result; // the cards of a deck or a pile: DeckId and PlayerPile name the card types
        }
        val raw = raw(type);
        if (raw == List.class || raw == Set.class || raw == Collection.class) {
            result.addAll(named(typeArgument(type, 0)));
        } else if (raw == Map.class) {
            result.addAll(named(typeArgument(type, 0)));
            result.addAll(named(typeArgument(type, 1)));
        } else if (raw == Class.class) {
            if (type instanceof ParameterizedType) {
                result.addAll(named(typeArgument(type, 0)));
            }
        } else if (raw.isRecord() || raw.isEnum() || raw.isInterface()) {
            result.add(raw);
        }
        return result;
    }

    /**
     * @return the type as the codec writes it, e.g. {@code List<SkillCard>}, {@code Integer?} for a nullable int, or
     * {@code List<card of deck>} for the cards of a deck or a pile
     */
    public static String describe(Type type) {
        if (ValueCodec.isWildcardList(type)) {
            return "List<card of deck or pile>";
        }
        val raw = raw(type);
        if (raw == int.class) {
            return "int";
        }
        if (isInt(raw)) {
            return "int?";
        }
        if (raw == long.class) {
            return "long";
        }
        if (raw == Long.class) {
            return "long?";
        }
        if (raw == boolean.class || raw == Boolean.class) {
            return raw == boolean.class ? "boolean" : "boolean?";
        }
        if (raw == String.class) {
            return "String";
        }
        if (raw == List.class || raw == Set.class || raw == Collection.class) {
            return raw.getSimpleName() + "<" + describe(typeArgument(type, 0)) + ">";
        }
        if (raw == Map.class) {
            return "Map<" + describe(typeArgument(type, 0)) + ", " + describe(typeArgument(type, 1)) + ">";
        }
        if (raw == Class.class) {
            return "Class<" + (type instanceof ParameterizedType ? describe(typeArgument(type, 0)) : "?") + ">";
        }
        return raw.getSimpleName();
    }

    /**
     * @return whether the type is part of the application, not a test fixture
     */
    public static boolean isProduction(Class<?> type) {
        val source = type.getProtectionDomain().getCodeSource();
        if (source == null) {
            return true; // the JDK
        }
        val location = source.getLocation().toString();
        return !location.contains("testFixtures") && !location.contains("test-fixtures") && !location.contains("/test/");
    }

    /**
     * @return the fingerprint lines of the types: a record with its type name and components, an enum with its
     * constants, an interface with its name
     */
    public static List<String> fingerprint(TypeRegistry registry) {
        val roots = registry.types().stream().filter(TypeWalker::isProduction).toList();
        return reachable(roots).stream()
                .filter(TypeWalker::isProduction)
                .map(type -> describeType(registry, type))
                .sorted()
                .toList();
    }

    private static String describeType(TypeRegistry registry, Class<?> type) {
        val name = registry.types().contains(type) ? registry.nameOf(type) : type.getSimpleName();
        if (type.isEnum()) {
            val constants = new StringBuilder();
            for (val constant : type.getEnumConstants()) {
                constants.append(constants.isEmpty() ? "" : ", ").append(((Enum<?>) constant).name());
            }
            return "enum " + name + " = " + constants;
        }
        if (type.isRecord()) {
            val components = new StringBuilder();
            for (val component : type.getRecordComponents()) {
                components.append(components.isEmpty() ? "" : ", ")
                        .append(component.getName()).append(": ").append(describe(component.getGenericType()));
            }
            return "record " + name + "(" + components + ")";
        }
        return "interface " + name;
    }
}
