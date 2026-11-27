package xyz.zlatanov.frakkintoasters.backend.wire;

import lombok.val;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.journal.TypeRegistry;
import xyz.zlatanov.frakkintoasters.backend.journal.TypeWalker;
import xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.AttackTable;
import xyz.zlatanov.frakkintoasters.event.ComponentRole;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.EventSources;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEventProcessor;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.raw;
import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.typeArgument;

/**
 * The frontend generates its TypeScript types from the wire schema, and the sandbox's setup makes a form of each setup
 * event from it, so nothing copies a Java record by hand.
 */
public final class WireSchema {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private WireSchema() {
    }

    public static String role(String name) {
        return ComponentRole.of(name).map(role -> role.name().toLowerCase()).orElse(null);
    }

    public static ObjectNode json() {
        val registry = TypeRegistry.wire();
        val roots = new ArrayList<Class<?>>(registry.types());
        roots.addAll(WireMessages.TYPES.keySet());
        roots.add(WireMessages.ServerMessage.class);
        roots.add(WireMessages.ClientMessage.class);
        val types = TypeWalker.reachable(roots).stream()
                .filter(TypeWalker::isProduction)
                .toList();
        val names = names(registry, types);

        val schema = NODES.objectNode();
        schema.put("formatVersion", JournalCodec.FORMAT_VERSION);
        val described = new TreeMap<String, JsonNode>();
        for (val type : types) {
            described.put(names.get(type), describe(type, registry, names, types));
        }
        val typesNode = schema.putObject("types");
        described.forEach(typesNode::set);
        val lists = schema.putObject("lists");
        lists.set("events", sorted(types.stream()
                .filter(type -> type.isRecord() && Event.class.isAssignableFrom(type) && !SandboxEvent.class.isAssignableFrom(type))
                .map(names::get).toList()));
        lists.set("sandboxEvents", sorted(types.stream()
                .filter(type -> type.isRecord() && SandboxEvent.class.isAssignableFrom(type))
                .map(names::get).toList()));
        lists.set("anytimeEvents", sorted(types.stream()
                .filter(type -> type.isRecord() && AnytimeEvent.class.isAssignableFrom(type))
                .map(names::get).toList()));
        lists.set("facts", sorted(types.stream()
                .filter(type -> type.isRecord() && Fact.class.isAssignableFrom(type))
                .map(names::get).toList()));
        // the Attack Table as the game applies it, for the table's cheat sheet
        val attackTable = schema.putArray("attackTable");
        AttackTable.rows().forEach(row -> attackTable.addObject()
                .put("attacker", row.attacker())
                .put("target", row.target())
                .put("from", row.from())
                .put("to", row.to())
                .put("effect", row.effect().name()));
        schema.put("structuralDamageBonus", AttackTable.structuralDamageBonus());
        // the lines of succession as the game follows them, highest first, for the reference
        val linesOfSuccession = schema.putObject("linesOfSuccession");
        Map.of(Title.PRESIDENT, DetermineNextPresidentEventProcessor.LINE_OF_SUCCESSION,
                        Title.ADMIRAL, DetermineNextAdmiralEventProcessor.LINE_OF_SUCCESSION,
                        Title.CAG, DetermineNextCagEventProcessor.LINE_OF_SUCCESSION)
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    val line = linesOfSuccession.putArray(entry.getKey().name());
                    entry.getValue().forEach(character -> line.add(character.name()));
                });
        val messages = schema.putObject("messages");
        WireMessages.TYPES.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> messages.put(entry.getValue(), names.get(entry.getKey())));
        return schema;
    }

    private static Map<Class<?>, String> names(TypeRegistry registry, List<Class<?>> types) {
        val names = new LinkedHashMap<Class<?>, String>();
        val taken = new LinkedHashMap<String, Class<?>>();
        for (val type : types) {
            val name = registry.types().contains(type) ? registry.nameOf(type) : type.getSimpleName();
            val previous = taken.putIfAbsent(name, type);
            if (previous != null) {
                throw new IllegalStateException("Two wire types are named " + name + ": " + previous + " and " + type);
            }
            names.put(type, name);
        }
        return names;
    }

    private static ObjectNode describe(Class<?> type, TypeRegistry registry, Map<Class<?>, String> names,
                                       List<Class<?>> types) {
        val node = NODES.objectNode();
        if (type.isEnum()) {
            node.put("kind", "enum");
            val values = node.putArray("values");
            Arrays.stream(type.getEnumConstants()).forEach(constant -> values.add(((Enum<?>) constant).name()));
            if (type == SkillCardType.class) {
                // the table sorts a hand by color
                val colors = node.putObject("colors");
                Arrays.stream(SkillCardType.values()).forEach(card -> colors.put(card.name(), card.color().name()));
            }
            if (type == CharacterAbility.class) {
                val characters = node.putObject("characters");
                Arrays.stream(CharacterAbility.values()).forEach(ability -> characters.put(ability.name(), ability.character().name()));
                // the miracle tokens a use spends, for the miracle abilities only: the answers show the token
                val miracles = node.putObject("miracles");
                Arrays.stream(CharacterAbility.values())
                        .filter(CharacterAbility::isMiracle)
                        .forEach(ability -> miracles.put(ability.name(), ability.miracleTokens()));
            }
            if (type == Character.class) {
                // the type and the skill set a character sheet prints, in its order, for the strip beside a mat's
                // portrait
                val characterTypes = node.putObject("types");
                Arrays.stream(Character.values()).forEach(character -> characterTypes.put(character.name(), character.type().name()));
                val skillSets = node.putObject("skillSets");
                for (val character : Character.values()) {
                    val options = skillSets.putArray(character.name());
                    for (val option : character.skillSet()) {
                        val colors = options.addObject().put("count", option.count()).putArray("colors");
                        option.colors().forEach(color -> colors.add(color.name()));
                    }
                }
            }
            val sources = sources(type, names);
            if (!sources.isEmpty()) {
                node.set("sources", sources);
            }
            return node;
        }
        if (type.isInterface()) {
            node.put("kind", "union");
            node.put("discriminator", WireMessages.TYPES.keySet().stream().anyMatch(type::isAssignableFrom) ? "type" : "kind");
            node.set("members", sorted(types.stream()
                    .filter(member -> member.isRecord() && type.isAssignableFrom(member))
                    .map(names::get)
                    .toList()));
            return node;
        }
        node.put("kind", "record");
        val message = WireMessages.TYPES.get(type);
        if (message != null) {
            node.put("type", message);
        } else {
            node.put("tagged", registry.types().contains(type) && isMemberOfUnion(type, registry));
        }
        node.set("interfaces", sorted(types.stream()
                .filter(other -> other.isInterface() && other != type && other.isAssignableFrom(type))
                .map(names::get)
                .toList()));
        if (type.getTypeParameters().length > 0) {
            val parameters = node.putArray("typeParams");
            Arrays.stream(type.getTypeParameters()).forEach(parameter -> parameters.add(parameter.getName()));
        }
        if (LocationEvent.class.isAssignableFrom(type)) {
            node.put("location", location(type));
        }
        if (QuorumCardEvent.class.isAssignableFrom(type)) {
            node.put("quorumCard", quorumCard(type));
        }
        if (MutinyCardEvent.class.isAssignableFrom(type)) {
            node.put("mutinyCard", mutinyCard(type));
        }
        EventSources.of(type).ifPresent(source -> node.set("source", sourceRef(source, names)));
        val components = node.putArray("components");
        for (val component : type.getRecordComponents()) {
            val described = components.addObject();
            described.put("name", component.getName());
            described.set("type", typeRef(component.getGenericType(), component, names));
        }
        return node;
    }

    private static boolean isMemberOfUnion(Class<?> type, TypeRegistry registry) {
        return registry.types().stream().anyMatch(other -> other.isInterface() && other.isAssignableFrom(type));
    }

    private static ObjectNode typeRef(Type type, RecordComponent component, Map<Class<?>, String> names) {
        val node = NODES.objectNode();
        if (ValueCodec.isWildcardList(type)) {
            node.put("t", "cards");
            node.put("nullable", true);
            return node;
        }
        if (type instanceof TypeVariable<?> variable) {
            node.put("t", "var");
            node.put("name", variable.getName());
            node.put("nullable", true);
            return node;
        }
        val raw = raw(type);
        if (ValueCodec.isInt(raw) || raw == long.class || raw == Long.class) {
            node.put("t", ValueCodec.isInt(raw) ? "int" : "long");
            node.put("nullable", !raw.isPrimitive());
            node.put("role", role(component.getName()));
            return node;
        }
        if (raw == boolean.class || raw == Boolean.class) {
            node.put("t", "boolean");
            node.put("nullable", !raw.isPrimitive());
            return node;
        }
        if (raw == String.class) {
            node.put("t", "string");
            node.put("nullable", true);
            return node;
        }
        node.put("nullable", true);
        if (raw.isEnum()) {
            node.put("t", "enum");
            node.put("name", names.get(raw));
        } else if (raw == Class.class) {
            node.put("t", "class");
            node.put("of", type instanceof ParameterizedType ? names.get(raw(typeArgument(type, 0))) : null);
        } else if (raw == List.class || raw == Set.class || raw == Collection.class) {
            node.put("t", raw == Set.class ? "set" : "list");
            node.set("of", element(typeRef(typeArgument(type, 0), component, names)));
        } else if (raw == Map.class) {
            node.put("t", "map");
            node.set("key", element(typeRef(typeArgument(type, 0), component, names)));
            val value = element(typeRef(typeArgument(type, 1), component, names));
            if (value.has("role")) {
                value.put("role", "count");
            }
            node.set("value", value);
        } else {
            node.put("t", "ref");
            node.put("name", names.get(raw));
            if (type instanceof ParameterizedType parameterized) {
                val arguments = node.putArray("args");
                for (val argument : parameterized.getActualTypeArguments()) {
                    arguments.add(element(typeRef(argument, component, names)));
                }
            }
        }
        return node;
    }


    // The collections of the game never hold null: they are copies made with List.copyOf and its kind.
    private static ObjectNode element(ObjectNode ref) {
        ref.put("nullable", false);
        return ref;
    }
    private static String location(Class<?> type) {
        try {
            val components = type.getRecordComponents();
            val arguments = Arrays.stream(components).map(component -> defaultValue(component.getType())).toArray();
            val constructor = type.getDeclaredConstructor(Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new));
            constructor.setAccessible(true);
            return ((LocationEvent) constructor.newInstance(arguments)).location().name();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static ObjectNode sourceRef(Enum<?> source, Map<Class<?>, String> names) {
        val enumName = names.get(source.getDeclaringClass());
        if (enumName == null) {
            throw new IllegalStateException(source.getDeclaringClass() + " names a source but isn't a wire type");
        }
        return NODES.objectNode().put("enum", enumName).put("constant", source.name());
    }

    private static ObjectNode sources(Class<?> type, Map<Class<?>, String> names) {
        val sources = NODES.objectNode();
        for (val constant : type.getEnumConstants()) {
            val named = (Enum<?>) constant;
            EventSources.of(named).ifPresent(source -> sources.set(named.name(), sourceRef(source, names)));
        }
        return sources;
    }

    private static String quorumCard(Class<?> type) {
        try {
            val processor = Class.forName(type.getName() + "Processor").getDeclaredConstructor().newInstance();
            return ((QuorumCardEventProcessor<?>) processor).card().name();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static String mutinyCard(Class<?> type) {
        try {
            val processor = Class.forName(type.getName() + "Processor").getDeclaredConstructor().newInstance();
            return ((MutinyCardEventProcessor<?>) processor).card().name();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == boolean.class) {
            return false;
        }
        return null;
    }

    private static JsonNode sorted(Collection<String> names) {
        val array = NODES.arrayNode();
        names.stream().sorted(Comparator.naturalOrder()).forEach(array::add);
        return array;
    }
}
