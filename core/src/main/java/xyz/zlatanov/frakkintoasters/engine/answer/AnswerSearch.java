package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.engine.answer.AnswerClasses.AnswerClass;
import xyz.zlatanov.frakkintoasters.engine.answer.Shapes.Elements;
import xyz.zlatanov.frakkintoasters.engine.answer.Shapes.Source;
import xyz.zlatanov.frakkintoasters.engine.answer.Shapes.Values;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

final class AnswerSearch {

    // Far above any class of the game, which has at most a few thousand candidates.
    private static final int MAX_TUPLES = 50_000;

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTORS = new ConcurrentHashMap<>();

    private final Asked                 asked;
    private final LegalAnswers.Listener listener;
    private final Set<Event>            events = new LinkedHashSet<>();
    private final List<ShapedAnswer>    shapes = new ArrayList<>();

    AnswerSearch(Asked asked, LegalAnswers.Listener listener) {
        this.asked = asked;
        this.listener = listener;
    }

    LegalAnswers run() {
        for (val answerClass : AnswerClasses.of(asked.decision().action())) {
            if (open(answerClass)) {
                search(answerClass);
            }
        }
        return new LegalAnswers(List.copyOf(events), withoutHeldShapes());
    }

    private boolean open(AnswerClass answerClass) {
        try {
            return answerClass.gate().open(asked);
        } catch (RuntimeException thrown) {
            // The game asks the same check before it offers the class, so this is the game's bug: let prepare judge.
            listener.crashed(answerClass.type(), null, thrown);
            return true;
        }
    }

    private void search(AnswerClass answerClass) {
        val type = answerClass.type();
        val components = type.getRecordComponents();
        val picked = Shapes.pickedComponent(type);
        if (picked == -2) {
            listener.unsupported(type, "", "more than one list");
            return;
        }
        val domains = new ArrayList<List<Object>>();
        for (int i = 0; i < components.length; i++) {
            if (i == picked) {
                domains.add(Collections.singletonList(null)); // filled per source
                continue;
            }
            val domain = Domains.of(asked, type, components[i], answerClass.skillCardType());
            if (domain == null) {
                listener.unsupported(type, components[i].getName(), "no values for " + components[i].getGenericType());
                return;
            }
            if (domain.isEmpty()) {
                return;
            }
            domains.add(domain);
        }
        List<Source> sources = List.of();
        if (picked >= 0) {
            sources = Shapes.of(asked, type, components[picked]);
            if (sources == null) {
                listener.unsupported(type, components[picked].getName(),
                        "no source for " + components[picked].getGenericType());
                return;
            }
        }
        val constructor = constructor(type);
        val indices = new int[components.length];
        var tuples = 0;
        do {
            if (++tuples > MAX_TUPLES) {
                listener.unsupported(type, "", "more than " + MAX_TUPLES + " candidates");
                return;
            }
            val args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                args[i] = domains.get(i).get(indices[i]);
            }
            if (picked < 0) {
                add(judge(type, constructor, args));
            } else {
                for (val source : sources) {
                    fill(type, constructor, args, picked, components[picked], source);
                }
            }
        } while (next(indices, domains));
    }

    private void fill(Class<?> type, Constructor<?> constructor, Object[] args, int picked, RecordComponent component,
                      Source source) {
        switch (source) {
            case Values values -> values.values().forEach(value -> {
                args[picked] = value;
                add(judge(type, constructor, args));
            });
            case Elements elements -> pick(type, constructor, args, picked, component, elements);
        }
    }

    // The legal sizes, each found by a witness: size 0 is a concrete answer, each run of legal sizes a shape.
    private void pick(Class<?> type, Constructor<?> constructor, Object[] args, int picked, RecordComponent component,
                      Elements elements) {
        val from = elements.from();
        val single = !Shapes.isCollection(component.getType());
        val largest = single ? Math.min(1, from.size()) : from.size();
        val examples = new Event[largest + 1];
        for (int size = single ? 1 : 0; size <= largest; size++) {
            for (val positions : witnesses(from.size(), size)) {
                args[picked] = value(component, from, positions, single);
                val event = judge(type, constructor, args);
                if (event != null) {
                    examples[size] = event;
                    break;
                }
            }
        }
        if (!single && examples[0] != null) {
            events.add(examples[0]);
        }
        var size = 1;
        while (size <= largest) {
            if (examples[size] == null) {
                size++;
                continue;
            }
            var end = size;
            while (end < largest && examples[end + 1] != null) {
                end++;
            }
            shapes.add(new ShapedAnswer(examples[size], pickOf(component, elements, size, end)));
            size = end + 1;
        }
    }

    /**
     * A source puts its elements in the order that makes the first ones legal when any of that size are.
     */
    static List<List<Integer>> witnesses(int count, int size) {
        if (size == 0 || size == count) {
            return List.of(IntStream.range(0, size).boxed().toList());
        }
        val offsets = new LinkedHashSet<>(List.of(0, count - size));
        val result = new LinkedHashSet<List<Integer>>();
        for (val offset : offsets) {
            val positions = new TreeSet<Integer>();
            for (int i = 0; i < size; i++) {
                positions.add((offset + i) % count);
            }
            result.add(List.copyOf(positions));
        }
        return List.copyOf(result);
    }

    private static Object value(RecordComponent component, List<?> from, List<Integer> positions, boolean single) {
        if (single) {
            return from.get(positions.getFirst());
        }
        val picked = positions.stream().map(from::get).toList();
        return component.getType() == Set.class ? new LinkedHashSet<>(picked) : picked;
    }

    @SuppressWarnings("unchecked")
    private static Pick pickOf(RecordComponent component, Elements elements, int min, int max) {
        val element = Shapes.isCollection(component.getType()) ? Shapes.elementType(component, 0) : component.getType();
        val name = component.getName();
        if (element == SkillCard.class) {
            return new Pick.SkillCardPick(name, (List<SkillCard>) elements.from(), min, max, elements.ordered(),
                    elements.minStrength());
        }
        if (element == QuorumCard.class) {
            return new Pick.QuorumCardPick(name, (List<QuorumCard>) elements.from(), min, max, elements.ordered());
        }
        if (element == Integer.class) {
            return new Pick.ShipPick(name, (List<Integer>) elements.from(), min, max);
        }
        throw new IllegalStateException("No pick of " + component.getGenericType());
    }

    private Event judge(Class<?> type, Constructor<?> constructor, Object[] args) {
        final Event built;
        try {
            built = (Event) constructor.newInstance(args);
        } catch (InvocationTargetException refused) {
            return null; // the record refuses the values: the codec couldn't build it either
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't build a " + type.getSimpleName(), e);
        }
        try {
            final Event event = built instanceof ConstrainedEvent constrained
                    ? constrained.withConstraints(asked.decision().eventConstraints())
                    : built;
            if (event instanceof PlayerEvent playerEvent && playerEvent.playerNumber() != asked.playerNumber()) {
                return null;
            }
            ProcessorRegistry.create(event).prepare(asked.game(), event);
            return event;
        } catch (InvalidActionException rejected) {
            return null;
        } catch (RuntimeException thrown) {
            listener.crashed(type, built, thrown);
            return null;
        }
    }

    private void add(Event event) {
        if (event != null) {
            events.add(event);
        }
    }

    // A shape that another one holds, e.g. 3 raiders of one area beside 3 raiders of any area, says nothing more.
    private List<ShapedAnswer> withoutHeldShapes() {
        val kept = new ArrayList<ShapedAnswer>();
        for (int i = 0; i < shapes.size(); i++) {
            val shape = shapes.get(i);
            var held = false;
            for (int j = 0; j < shapes.size() && !held; j++) {
                val other = shapes.get(j);
                held = j != i && AnswerMatch.holds(other, shape) && (!AnswerMatch.holds(shape, other) || j < i);
            }
            if (!held) {
                kept.add(shape);
            }
        }
        return kept;
    }

    private static boolean next(int[] indices, List<List<Object>> domains) {
        for (int i = 0; i < indices.length; i++) {
            if (++indices[i] < domains.get(i).size()) {
                return true;
            }
            indices[i] = 0;
        }
        return false;
    }

    private static Constructor<?> constructor(Class<?> type) {
        return CONSTRUCTORS.computeIfAbsent(type, ignored -> {
            try {
                val constructor = type.getDeclaredConstructor(Arrays.stream(type.getRecordComponents())
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new));
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("No canonical constructor of " + type.getSimpleName(), e);
            }
        });
    }
}
