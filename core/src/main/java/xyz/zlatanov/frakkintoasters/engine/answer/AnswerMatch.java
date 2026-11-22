package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Whether an event is one of the answers: equal component by component, with a list compared as a multiset, because
 * the search tries one order of each.
 */
final class AnswerMatch {

    private AnswerMatch() {
    }

    static boolean same(Event answer, Event event) {
        if (answer.getClass() != event.getClass()) {
            return false;
        }
        for (val component : answer.getClass().getRecordComponents()) {
            if (!equalValues(value(component, answer), value(component, event))) {
                return false;
            }
        }
        return true;
    }

    static boolean fits(ShapedAnswer shape, Event event) {
        val example = shape.example();
        if (example.getClass() != event.getClass()) {
            return false;
        }
        val pick = shape.pick();
        for (val component : example.getClass().getRecordComponents()) {
            val value = value(component, event);
            val fits = component.getName().equals(pick.component())
                    ? fitsPick(pick, value)
                    : equalValues(value(component, example), value);
            if (!fits) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return whether every filling of the inner shape fits the outer one
     */
    static boolean holds(ShapedAnswer outer, ShapedAnswer inner) {
        val outerPick = outer.pick();
        val innerPick = inner.pick();
        if (outer.example().getClass() != inner.example().getClass() || outerPick.getClass() != innerPick.getClass()
                || !outerPick.component().equals(innerPick.component()) || outerPick.ordered() != innerPick.ordered()
                || innerPick.min() < outerPick.min() || innerPick.max() > outerPick.max()) {
            return false;
        }
        if (outerPick instanceof Pick.SkillCardPick outerCards && innerPick instanceof Pick.SkillCardPick innerCards
                && innerCards.minStrength() < outerCards.minStrength()) {
            return false;
        }
        for (val component : outer.example().getClass().getRecordComponents()) {
            if (!component.getName().equals(outerPick.component())
                    && !equalValues(value(component, outer.example()), value(component, inner.example()))) {
                return false;
            }
        }
        val left = counts(outerPick.elements());
        for (val element : innerPick.elements()) {
            if (left.merge(element, -1L, Long::sum) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean fitsPick(Pick pick, Object value) {
        if (!(value instanceof Collection<?> picked)) {
            return pick.min() <= 1 && pick.max() >= 1 && value != null && pick.elements().contains(value);
        }
        if (picked.stream().anyMatch(Objects::isNull) || picked.size() < pick.min() || picked.size() > pick.max()) {
            return false;
        }
        val left = counts(pick.elements());
        for (val element : picked) {
            if (left.merge(element, -1L, Long::sum) < 0) {
                return false;
            }
        }
        return !(pick instanceof Pick.SkillCardPick cards) || cards.minStrength() == 0
                || picked.stream().mapToInt(card -> ((SkillCard) card).value()).sum() >= cards.minStrength();
    }

    private static boolean equalValues(Object a, Object b) {
        if (a instanceof Collection<?> left && b instanceof Collection<?> right) {
            return left.size() == right.size() && counts(left).equals(counts(right));
        }
        if (a instanceof Map<?, ?> left && b instanceof Map<?, ?> right) {
            return withoutZeros(left).equals(withoutZeros(right));
        }
        return Objects.equals(a, b);
    }

    // A draw's counts per color: a color at 0 draws nothing, as a color left out does.
    private static Map<?, ?> withoutZeros(Map<?, ?> counts) {
        val result = new HashMap<Object, Object>(counts);
        result.values().removeIf(value -> Integer.valueOf(0).equals(value));
        return result;
    }

    private static Map<Object, Long> counts(Collection<?> elements) {
        val counts = new HashMap<Object, Long>();
        elements.forEach(element -> counts.merge(element, 1L, Long::sum));
        return counts;
    }

    private static Object value(RecordComponent component, Event event) {
        try {
            val accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(event);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't read " + component, e);
        }
    }
}
