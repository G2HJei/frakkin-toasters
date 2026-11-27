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

public final class TypeWalker {

    private TypeWalker() {
    }

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

    public static Set<Class<?>> named(Type type) {
        val result = new LinkedHashSet<Class<?>>();
        if (ValueCodec.isWildcardList(type)) {
            return result;
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

    public static boolean isProduction(Class<?> type) {
        val source = type.getProtectionDomain().getCodeSource();
        if (source == null) {
            return true;
        }
        val location = source.getLocation().toString();
        return !location.contains("testFixtures") && !location.contains("test-fixtures") && !location.contains("/test/");
    }

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
