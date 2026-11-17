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
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;

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
import java.util.regex.Pattern;

import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.raw;
import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.typeArgument;

/**
 * The wire schema: every type a client reads or writes, as the codec writes it. The frontend generates its TypeScript
 * types from it, and its event builder makes a form of each event from it. Nothing copies a Java record by hand.
 * <ul>
 *     <li>a record: its components with their types, the interfaces it implements, whether it carries {@code kind},
 *     and the {@code location} of a location's action</li>
 *     <li>an interface: the records that implement it, a union told apart by {@code kind}</li>
 *     <li>an enum: its constants</li>
 *     <li>the socket messages, told apart by {@code type}</li>
 * </ul>
 * An int component gets a {@code role} from its name: a {@code player}, a {@code ship}, a journal {@code scope}, or a
 * {@code count}.
 */
public final class WireSchema {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private static final Pattern SHIP  = Pattern.compile("(?i).*(shipId|fighterId|raiderId|basestarId|viperId|centurionId|"
            + "basestarShipId|raiderShipId|civilianShipId\\d?|civilianShipIt|shipIds|cylonShipId|escortCivilianShipId|"
            + "knownCivilianShips)");
    private static final Set<String> PLAYERS = Set.of("playerNumber", "targetPlayerNumber", "targetPlayer",
            "recipientPlayerNumber", "excludedPlayerNumber", "targetHumanPlayer", "ownerPlayerNumber", "otherPlayerNumber",
            "chooser", "actor", "player", "currentPlayer", "actingPlayer", "winnerPlayerNumbers", "winners",
            "waitingForPlayerNumber", "targetPlayers");
    private static final Set<String> SCOPES = Set.of("scopeId", "parentId");
    private static final Set<String> COUNTS = Set.of("count", "amount", "value", "modifier", "distanceBefore", "number",
            "turn", "turns", "size", "discardSize", "basestars", "raiders", "heavyRaiders", "centurions", "civilianShips",
            "totalStrength", "difficulty", "fuel", "food", "morale", "population", "nukes", "distanceTraveled",
            "playerCount", "calculationsModifier", "handLimit", "option", "fuelCost", "moraleCost", "populationCost",
            "result", "formatVersion");

    private WireSchema() {
    }

    /**
     * @return the role of an int component by its name, {@code null} when the name can't be placed
     */
    public static String role(String name) {
        if (PLAYERS.contains(name)) {
            return "player";
        }
        if (SCOPES.contains(name)) {
            return "scope";
        }
        if (name.equals("id") || SHIP.matcher(name).matches()) {
            return "ship";
        }
        if (COUNTS.contains(name)) {
            return "count";
        }
        return null;
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
        lists.set("facts", sorted(types.stream()
                .filter(type -> type.isRecord() && Fact.class.isAssignableFrom(type))
                .map(names::get).toList()));
        val messages = schema.putObject("messages");
        WireMessages.TYPES.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> messages.put(entry.getValue(), names.get(entry.getKey())));
        return schema;
    }

    // The codec's name of a registered type, the simple name of any other. Two types can't share one.
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
                value.put("role", "count"); // how many of each key
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
    // The action's location is a constant of the record, whatever its fields.
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
