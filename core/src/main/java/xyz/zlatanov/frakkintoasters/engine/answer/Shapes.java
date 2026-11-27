package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ComponentRole;
import xyz.zlatanov.frakkintoasters.event.action.character.GlimpseTheFaceOfGodPlaceEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.InfluentialEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.character.ReorderTopQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SetTheAgendaChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.ResignationEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.PlayedCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The search judges which sizes are legal, so a source says nothing of the rules but where to look and, where a rule
 * reads the elements, in which order to try them.
 */
final class Shapes {

    private static final int MAX_DRAW  = 5;
    private static final int MAX_ENUMS = 2; // "Treatment"'s 2 colors, "Launch Reserves"' 2 fighters

    sealed interface Source {
    }

    record Values(List<Object> values) implements Source {
    }

    record Elements(List<?> from, boolean ordered, int minStrength) implements Source {
    }

    private Shapes() {
    }

    static int pickedComponent(Class<?> type) {
        val components = type.getRecordComponents();
        var picked = -1;
        for (int i = 0; i < components.length; i++) {
            if (isCollection(components[i].getType())) {
                if (picked >= 0) {
                    return -2;
                }
                picked = i;
            }
        }
        if (picked < 0 && type == MoveEvent.class) {
            for (int i = 0; i < components.length; i++) {
                if (components[i].getName().equals("discardCard")) {
                    return i;
                }
            }
        }
        return picked;
    }

    static boolean isCollection(Class<?> type) {
        return type == List.class || type == Set.class || type == Collection.class || type == Map.class;
    }

    static List<Source> of(Asked asked, Class<?> type, RecordComponent component) {
        if (!isCollection(component.getType())) {
            // a move's discard: any card of the hand, or none when the move stays on its ship
            return List.of(new Values(nullValue()), new Elements(asked.player().skillCards().cards(), false, 0));
        }
        if (component.getType() == Map.class) {
            return elementType(component, 0) == SkillCardColor.class
                    ? List.of(new Values(colorSelections(drawCount(asked))))
                    : null;
        }
        val element = elementType(component, 0);
        if (element == SkillCard.class) {
            return List.of(skillCards(asked, type));
        }
        if (element == QuorumCard.class) {
            return List.of(quorumCards(asked, type));
        }
        if (element == Integer.class && ComponentRole.of(component.getName()).filter(ComponentRole.SHIP::equals).isPresent()) {
            return Domains.shipGroups(asked, component.getName()).values().stream()
                    .distinct()
                    .<Source>map(group -> new Elements(group, false, 0))
                    .toList();
        }
        if (element == PlayedCard.class) {
            val check = asked.game().activeSkillCheck();
            return List.of(new Values(check == null
                    ? List.of(List.of())
                    : oneOfEach(check.playedBeforeConflicts()).stream().map(pick -> (Object) pick).toList()));
        }
        if (element != null && element.isEnum()) {
            val constants = Domains.constants(asked, type, component.getName(), element);
            return List.of(new Values(multisets(constants, MAX_ENUMS)));
        }
        return null;
    }

    private static Source skillCards(Asked asked, Class<?> type) {
        val hand = asked.player().skillCards().cards();
        if (type == GlimpseTheFaceOfGodPlaceEvent.class) {
            return new Elements(hand, true, 0);
        }
        if (type == PlayCardsBeforeSkillCheckEvent.class) {
            // Each card once, and a Reckless or "Restore Order" card last, so a size has a witness with only one.
            val playable = hand.stream()
                    .filter(card -> card.type().playableBeforeSkillCheck())
                    .distinct()
                    .sorted(Comparator.comparing((SkillCard card) -> card.type().recklessOrRestoreOrder()))
                    .toList();
            return new Elements(playable, false, 0);
        }
        if (type == RevealPersonalGoalEvent.class) {
            // "Selfish": the strongest cards first, so the first ones of each size reach the strength if any do.
            val strongest = hand.stream().sorted(Comparator.comparingInt(SkillCard::value).reversed()).toList();
            return new Elements(strongest, false, RevealPersonalGoalEventProcessor.SELFISH_STRENGTH);
        }
        return new Elements(hand, false, 0);
    }

    private static Source quorumCards(Asked asked, Class<?> type) {
        val hand = asked.isPresident() ? asked.game().presidentHand().cards() : List.<QuorumCard>of();
        if (type == ReorderTopQuorumCardsEvent.class) {
            return new Elements(InfluentialEventProcessor.top(asked.game()), true, 0);
        }
        if (type == SetTheAgendaChoiceEvent.class) {
            // its player has looked at the President's hand
            return new Elements(asked.game().presidentHand().cards(), true, 0);
        }
        if (type == ResignationEvent.class) {
            // the hand that is left once "Resignation" is played
            val left = new ArrayList<>(hand);
            left.remove(QuorumCard.RESIGNATION);
            return new Elements(left, false, 0);
        }
        val cards = new ArrayList<>(hand);
        cards.addAll(asked.player().quorumCards().cards());
        return new Elements(cards, false, 0);
    }

    static Class<?> elementType(RecordComponent component, int index) {
        if (component.getGenericType() instanceof ParameterizedType parameterized
                && parameterized.getActualTypeArguments()[index] instanceof Class<?> element) {
            return element;
        }
        return null;
    }

    private static List<Object> nullValue() {
        val values = new ArrayList<Object>();
        values.add(null);
        return values;
    }

    // The count the decision's constraint names, 5 at most: the processor takes fewer only when no other card fits.
    private static int drawCount(Asked asked) {
        for (val constraint : asked.decision().eventConstraints()) {
            switch (constraint) {
                case DRAW_EXACTLY_1, DRAW_EXACTLY_1_OF_ANY_COLOR -> {
                    return 1;
                }
                case DRAW_EXACTLY_2, DRAW_EXACTLY_2_OF_ANY_COLOR -> {
                    return 2;
                }
                case DRAW_EXACTLY_3 -> {
                    return 3;
                }
                case DRAW_EXACTLY_4 -> {
                    return 4;
                }
                default -> {
                }
            }
        }
        return MAX_DRAW;
    }

    // Every selection of up to so many cards over the 6 colors, as the counts per color: 462 for 5 cards.
    private static List<Object> colorSelections(int count) {
        val colors = List.of(SkillCardColor.values());
        val result = new ArrayList<Object>();
        for (val multiset : multisets(colors.stream().map(color -> (Object) color).toList(), count)) {
            val selection = new EnumMap<SkillCardColor, Integer>(SkillCardColor.class);
            ((List<?>) multiset).forEach(color -> selection.merge((SkillCardColor) color, 1, Integer::sum));
            result.add(selection);
        }
        return result;
    }

    static List<Object> multisets(List<Object> values, int size) {
        val result = new ArrayList<Object>();
        collect(values.stream().filter(Objects::nonNull).toList(), size, 0, new ArrayList<>(), result);
        return result;
    }

    private static void collect(List<Object> values, int size, int from, List<Object> prefix, List<Object> result) {
        result.add(List.copyOf(prefix));
        if (prefix.size() == size) {
            return;
        }
        for (int i = from; i < values.size(); i++) {
            prefix.add(values.get(i));
            collect(values, size, i, prefix, result);
            prefix.removeLast();
        }
    }

    static <T> List<List<T>> oneOfEach(List<List<T>> groups) {
        List<List<T>> result = List.of(List.of());
        for (val group : groups) {
            val next = new ArrayList<List<T>>();
            for (val prefix : result) {
                for (val element : new LinkedHashSet<>(group)) {
                    val pick = new ArrayList<>(prefix);
                    pick.add(element);
                    next.add(List.copyOf(pick));
                }
            }
            result = next;
        }
        return result;
    }
}
