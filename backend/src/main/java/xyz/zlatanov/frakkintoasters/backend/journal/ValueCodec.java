package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A component type the codec doesn't know fails, so a new shape can't slip through unnoticed.
 */
public final class ValueCodec {

    public static final String KIND = "kind";

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final TypeRegistry                   registry;
    private final Map<Class<?>, Constructor<?>> constructors = new ConcurrentHashMap<>();

    public ValueCodec(TypeRegistry registry) {
        this.registry = registry;
    }

    public TypeRegistry registry() {
        return registry;
    }

    public JsonNode write(Object value, Type type) {
        if (value == null) {
            return NODES.nullNode();
        }
        val raw = raw(type);
        if (isInt(raw)) {
            return NODES.numberNode(((Number) value).intValue());
        }
        if (raw == long.class || raw == Long.class) {
            return NODES.numberNode((Long) value);
        }
        if (raw == boolean.class || raw == Boolean.class) {
            return NODES.booleanNode((Boolean) value);
        }
        if (raw == String.class) {
            return NODES.stringNode((String) value);
        }
        if (raw.isEnum()) {
            return NODES.stringNode(((Enum<?>) value).name());
        }
        if (raw == Class.class) {
            return NODES.stringNode(registry.nameOf((Class<?>) value));
        }
        if (raw == List.class || raw == Set.class || raw == Collection.class) {
            val array = NODES.arrayNode();
            val elementType = typeArgument(type, 0);
            ((Collection<?>) value).forEach(element -> array.add(write(element, elementType)));
            return array;
        }
        if (raw == Map.class) {
            val object = NODES.objectNode();
            val keyType = raw(typeArgument(type, 0));
            val valueType = typeArgument(type, 1);
            ((Map<?, ?>) value).forEach((key, element) -> object.set(writeKey(key, keyType), write(element, valueType)));
            return object;
        }
        if (raw.isRecord()) {
            return writeRecord((Record) value, false, bindings(type));
        }
        if (isPolymorphic(raw)) {
            if (!(value instanceof Record record) || !raw.isInstance(value)) {
                throw new CodecException("Not a record of " + raw.getSimpleName() + ": " + value);
            }
            return writeRecord(record, true, Map.of());
        }
        throw new CodecException("Can't write a " + type.getTypeName());
    }

    public Object read(JsonNode node, Type type) {
        val raw = raw(type);
        if (node == null || node.isNull() || node.isMissingNode()) {
            if (raw.isPrimitive()) {
                throw new CodecException("A " + raw + " can't be null");
            }
            return null;
        }
        if (isInt(raw)) {
            if (!node.isIntegralNumber() || !node.canConvertToInt()) {
                throw new CodecException("Not an int: " + node);
            }
            return node.intValue();
        }
        if (raw == long.class || raw == Long.class) {
            if (!node.isIntegralNumber() || !node.canConvertToLong()) {
                throw new CodecException("Not a long: " + node);
            }
            return node.longValue();
        }
        if (raw == boolean.class || raw == Boolean.class) {
            if (!node.isBoolean()) {
                throw new CodecException("Not a boolean: " + node);
            }
            return node.booleanValue();
        }
        if (raw == String.class) {
            return text(node);
        }
        if (raw.isEnum()) {
            return readEnum(raw, text(node));
        }
        if (raw == Class.class) {
            val name = text(node);
            val named = registry.typeOf(name).orElseThrow(() -> new CodecException("Unknown type " + name));
            val bound = raw(classBound(type));
            if (!bound.isAssignableFrom(named)) {
                throw new CodecException(name + " is not a " + bound.getSimpleName());
            }
            return named;
        }
        if (raw == List.class || raw == Collection.class) {
            requireArray(node);
            val elementType = typeArgument(type, 0);
            val list = new ArrayList<>();
            node.forEach(element -> list.add(read(element, elementType)));
            return Collections.unmodifiableList(list);
        }
        if (raw == Set.class) {
            requireArray(node);
            val elementType = typeArgument(type, 0);
            val set = new LinkedHashSet<>();
            node.forEach(element -> {
                if (!set.add(read(element, elementType))) {
                    throw new CodecException("A set holds an element twice: " + node);
                }
            });
            return Collections.unmodifiableSet(set);
        }
        if (raw == Map.class) {
            if (!node.isObject()) {
                throw new CodecException("Not an object: " + node);
            }
            val keyType = raw(typeArgument(type, 0));
            val valueType = typeArgument(type, 1);
            Map<Object, Object> map = keyType.isEnum() ? enumMap(keyType) : new LinkedHashMap<>();
            node.propertyNames().forEach(key -> map.put(readKey(key, keyType), read(node.get(key), valueType)));
            return Collections.unmodifiableMap(map);
        }
        if (raw.isRecord()) {
            return readRecord(node, raw, false);
        }
        if (isPolymorphic(raw)) {
            if (!node.isObject() || !node.hasNonNull(KIND)) {
                throw new CodecException("A " + raw.getSimpleName() + " needs a " + KIND + ": " + node);
            }
            val name = text(node.get(KIND));
            val named = registry.typeOf(name).orElseThrow(() -> new CodecException("Unknown type " + name));
            if (!raw.isAssignableFrom(named) || !named.isRecord()) {
                throw new CodecException(name + " is not a " + raw.getSimpleName());
            }
            return readRecord(node, named, true);
        }
        throw new CodecException("Can't read a " + type.getTypeName());
    }

    public static Type componentType(RecordComponent component, Map<String, Object> siblings) {
        val type = component.getGenericType();
        if (!isWildcardList(type)) {
            return type;
        }
        val cardType = siblings.values().stream()
                .map(sibling -> switch (sibling) {
                    case DeckId deck -> deck.cardType();
                    case PlayerPile pile -> pile.cardType();
                    case null, default -> null;
                })
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new CodecException("No deck or pile names the cards of " + component));
        return new Parameterized(List.class, List.of(cardType));
    }

    public static boolean isWildcardList(Type type) {
        return type instanceof ParameterizedType parameterized
                && parameterized.getRawType() == List.class
                && parameterized.getActualTypeArguments()[0] instanceof WildcardType wildcard
                && wildcard.getUpperBounds()[0] == Object.class;
    }

    public static Class<?> raw(Type type) {
        return switch (type) {
            case Class<?> cls -> cls;
            case ParameterizedType parameterized -> raw(parameterized.getRawType());
            case TypeVariable<?> variable -> raw(variable.getBounds()[0]);
            case WildcardType wildcard -> raw(wildcard.getUpperBounds()[0]);
            case GenericArrayType ignored -> throw new CodecException("Arrays aren't supported: " + type);
            default -> throw new CodecException("Unknown type " + type);
        };
    }

    public static Type typeArgument(Type type, int index) {
        if (!(type instanceof ParameterizedType parameterized)) {
            throw new CodecException("A raw " + type.getTypeName() + " has no element type");
        }
        return parameterized.getActualTypeArguments()[index];
    }

    public static boolean isPolymorphic(Class<?> raw) {
        return (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) && !raw.isPrimitive() && !raw.isArray()
                && raw != List.class && raw != Set.class && raw != Map.class && raw != Collection.class;
    }

    public static boolean isInt(Class<?> raw) {
        return raw == int.class || raw == Integer.class;
    }

    private ObjectNode writeRecord(Record record, boolean tagged, Map<TypeVariable<?>, Type> bindings) {
        val object = NODES.objectNode();
        if (tagged) {
            object.put(KIND, registry.nameOf(record.getClass()));
        }
        val values = componentValues(record);
        for (val component : record.getClass().getRecordComponents()) {
            object.set(component.getName(), write(values.get(component.getName()), substitute(componentType(component, values), bindings)));
        }
        return object;
    }

    private Object readRecord(JsonNode node, Class<?> type, boolean tagged) {
        if (!node.isObject()) {
            throw new CodecException("Not an object: " + node);
        }
        val components = type.getRecordComponents();
        val names = new HashSet<String>();
        Arrays.stream(components).forEach(component -> names.add(component.getName()));
        node.propertyNames().forEach(name -> {
            if (!names.contains(name) && !(tagged && name.equals(KIND))) {
                throw new CodecException(type.getSimpleName() + " has no " + name);
            }
        });
        // The lists of cards need the deck or pile beside them, so they come last.
        val values = new LinkedHashMap<String, Object>();
        for (val component : components) {
            if (!isWildcardList(component.getGenericType())) {
                values.put(component.getName(), read(node.get(component.getName()), component.getGenericType()));
            }
        }
        for (val component : components) {
            if (isWildcardList(component.getGenericType())) {
                values.put(component.getName(), read(node.get(component.getName()), componentType(component, values)));
            }
        }
        val arguments = Arrays.stream(components).map(component -> values.get(component.getName())).toArray();
        try {
            return constructor(type).newInstance(arguments);
        } catch (InvocationTargetException e) {
            throw new CodecException("Can't create a " + type.getSimpleName() + ": " + e.getCause().getMessage(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new CodecException("Can't create a " + type.getSimpleName(), e);
        }
    }

    private static Map<String, Object> componentValues(Record record) {
        val values = new LinkedHashMap<String, Object>();
        for (val component : record.getClass().getRecordComponents()) {
            try {
                val accessor = component.getAccessor();
                accessor.setAccessible(true);
                values.put(component.getName(), accessor.invoke(record));
            } catch (ReflectiveOperationException e) {
                throw new CodecException("Can't read " + component, e);
            }
        }
        return values;
    }

    private Constructor<?> constructor(Class<?> type) {
        return constructors.computeIfAbsent(type, ignored -> {
            val parameterTypes = Arrays.stream(type.getRecordComponents()).map(RecordComponent::getType).toArray(Class[]::new);
            try {
                val constructor = type.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new CodecException("No canonical constructor of " + type, e);
            }
        });
    }

    private String writeKey(Object key, Class<?> keyType) {
        if (keyType.isEnum()) {
            return ((Enum<?>) key).name();
        }
        if (isInt(keyType) || keyType == String.class) {
            return String.valueOf(key);
        }
        throw new CodecException("Can't write a map key of " + keyType);
    }

    private Object readKey(String key, Class<?> keyType) {
        if (keyType.isEnum()) {
            return readEnum(keyType, key);
        }
        if (isInt(keyType)) {
            try {
                return Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw new CodecException("Not an int key: " + key);
            }
        }
        if (keyType == String.class) {
            return key;
        }
        throw new CodecException("Can't read a map key of " + keyType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object readEnum(Class<?> type, String name) {
        try {
            return Enum.valueOf((Class<? extends Enum>) type, name);
        } catch (IllegalArgumentException e) {
            throw new CodecException("No " + type.getSimpleName() + " " + name);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<Object, Object> enumMap(Class<?> keyType) {
        return new EnumMap(keyType);
    }

    private static Type classBound(Type type) {
        return type instanceof ParameterizedType ? typeArgument(type, 0) : Object.class;
    }

    private static String text(JsonNode node) {
        if (!node.isString()) {
            throw new CodecException("Not a string: " + node);
        }
        return node.stringValue();
    }

    private static void requireArray(JsonNode node) {
        if (!node.isArray()) {
            throw new CodecException("Not an array: " + node);
        }
    }

    private static Map<TypeVariable<?>, Type> bindings(Type type) {
        if (!(type instanceof ParameterizedType parameterized)) {
            return Map.of();
        }
        val variables = raw(type).getTypeParameters();
        val arguments = parameterized.getActualTypeArguments();
        val bindings = new java.util.HashMap<TypeVariable<?>, Type>();
        for (int i = 0; i < variables.length; i++) {
            bindings.put(variables[i], arguments[i]);
        }
        return bindings;
    }

    private static Type substitute(Type type, Map<TypeVariable<?>, Type> bindings) {
        if (bindings.isEmpty()) {
            return type;
        }
        if (type instanceof TypeVariable<?> variable && bindings.containsKey(variable)) {
            return bindings.get(variable);
        }
        if (type instanceof ParameterizedType parameterized) {
            return new Parameterized(raw(parameterized), Arrays.stream(parameterized.getActualTypeArguments())
                    .map(argument -> substitute(argument, bindings))
                    .toList());
        }
        return type;
    }

    private record Parameterized(Class<?> rawType, List<Type> arguments) implements ParameterizedType {

        @Override
        public Type[] getActualTypeArguments() {
            return arguments.toArray(Type[]::new);
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
