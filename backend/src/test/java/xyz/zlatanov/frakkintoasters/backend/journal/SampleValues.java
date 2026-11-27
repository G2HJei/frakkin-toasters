package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.TurnStarted;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.raw;
import static xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec.typeArgument;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

/**
 * It fails on a component type it doesn't know, so a new shape in an event or a fact has to be taught to the codec and
 * to this builder.
 */
final class SampleValues {

    private SampleValues() {
    }

    static Object of(Class<?> recordType) {
        return record(recordType);
    }

    private static Object record(Class<?> type) {
        val components = type.getRecordComponents();
        val values = new LinkedHashMap<String, Object>();
        // a list of cards needs the deck or pile beside it
        for (val component : components) {
            if (!ValueCodec.isWildcardList(component.getGenericType())) {
                values.put(component.getName(), value(component.getGenericType(), component));
            }
        }
        for (val component : components) {
            if (ValueCodec.isWildcardList(component.getGenericType())) {
                values.put(component.getName(), value(ValueCodec.componentType(component, values), component));
            }
        }
        try {
            val constructor = type.getDeclaredConstructor(Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new));
            constructor.setAccessible(true);
            return constructor.newInstance(Arrays.stream(components).map(c -> values.get(c.getName())).toArray());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Can't build a sample " + type.getSimpleName() + " from " + values, e);
        }
    }

    private static Object value(Type type, RecordComponent component) {
        val raw = raw(type);
        if (ValueCodec.isInt(raw)) {
            return 2;
        }
        if (raw == long.class || raw == Long.class) {
            return 3L;
        }
        if (raw == boolean.class || raw == Boolean.class) {
            return true;
        }
        if (raw == String.class) {
            return "sample";
        }
        if (raw.isEnum()) {
            val constants = raw.getEnumConstants();
            return constants[constants.length > 1 ? 1 : 0];
        }
        if (raw == Class.class) {
            val bound = raw(type instanceof ParameterizedType ? typeArgument(type, 0) : Object.class);
            if (bound.isAssignableFrom(ActionEvent.class)) {
                return ActionEvent.class;
            }
            throw new AssertionError("No sample class for " + type + " of " + component);
        }
        if (raw == List.class) {
            val element = value(typeArgument(type, 0), component);
            return List.of(element, element);
        }
        if (raw == java.util.Set.class) {
            val set = new LinkedHashSet<>();
            set.add(value(typeArgument(type, 0), component));
            return set;
        }
        if (raw == Map.class) {
            val map = new LinkedHashMap<>();
            map.put(value(typeArgument(type, 0), component), value(typeArgument(type, 1), component));
            return map;
        }
        if (raw == Event.class) {
            return new NoOpEvent(2);
        }
        if (raw == Followup.class) {
            return all(single(new NoOpEvent(1)), one(single(new ResolveCrisisCardEvent(CrisisCard.DETENTE)), Followup.NONE));
        }
        if (raw == Fact.class) {
            return new TurnStarted(4);
        }
        if (raw == InterruptWindow.class) {
            return new InterruptWindow.AfterViperAttacked(5);
        }
        if (raw.isRecord()) {
            return record(raw);
        }
        if (type instanceof TypeVariable<?> || type instanceof WildcardType) {
            throw new AssertionError("No sample for the type variable " + type + " of " + component);
        }
        throw new AssertionError("No sample for " + type.getTypeName() + " of " + component
                + ": teach the codec and this builder the new shape");
    }
}
