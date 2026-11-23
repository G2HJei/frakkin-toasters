package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActions;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovements;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardPlays;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardPlays;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardPlays;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The event classes that may answer a decision, and for each the cheap check that says it can't be legal now, so the
 * search skips it. The check is the one the game asks before it offers the card, the ability or the location.
 */
final class AnswerClasses {

    private static final Map<Class<?>, List<AnswerClass>> BY_ACTION = new ConcurrentHashMap<>();

    /**
     * @param skillCardType the card a Skill Card's event plays, {@code null} for any other event
     */
    record AnswerClass(Class<? extends Event> type, Gate gate, SkillCardType skillCardType) {
    }

    @FunctionalInterface
    interface Gate {
        boolean open(Asked asked);
    }

    private AnswerClasses() {
    }

    static List<AnswerClass> of(Class<?> action) {
        return BY_ACTION.computeIfAbsent(action, AnswerClasses::find);
    }

    private static List<AnswerClass> find(Class<?> action) {
        return EventCatalog.types().stream()
                .filter(action::isAssignableFrom)
                .filter(Class::isRecord)
                .filter(type -> !Modifier.isAbstract(type.getModifiers()))
                .filter(ProcessorRegistry::hasProcessor)
                // Neither answers a decision: submit rejects a sandbox event and runs an anytime event ahead of it. The
                // anytime events are listed only on their own, as what a player may do at any pause.
                .filter(type -> !SandboxEvent.class.isAssignableFrom(type))
                .filter(type -> action == AnytimeEvent.class || !AnytimeEvent.class.isAssignableFrom(type))
                .filter(type -> type != PlayerDecisionEvent.class)
                .sorted(Comparator.comparing(Class::getName))
                .map(AnswerClasses::answerClass)
                .toList();
    }

    private static AnswerClass answerClass(Class<? extends Event> type) {
        if (SkillCardEvent.class.isAssignableFrom(type)) {
            val cardType = ((SkillCardEventProcessor<?>) processor(type)).type();
            return new AnswerClass(type, asked -> SkillCardPlays.canPlay(asked.game(), asked.player(), cardType), cardType);
        }
        if (QuorumCardEvent.class.isAssignableFrom(type)) {
            val card = ((QuorumCardEventProcessor<?>) processor(type)).card();
            return new AnswerClass(type, asked -> QuorumCardPlays.canPlay(asked.game(), asked.player(), card), null);
        }
        if (MutinyCardEvent.class.isAssignableFrom(type)) {
            val card = ((MutinyCardEventProcessor<?>) processor(type)).card();
            return new AnswerClass(type, asked -> MutinyCardPlays.canPlay(asked.game(), asked.player(), card), null);
        }
        if (CharacterActionEvent.class.isAssignableFrom(type)) {
            val ability = ((CharacterActionEventProcessor<?>) processor(type)).ability();
            return new AnswerClass(type, asked -> CharacterActions.canUse(asked.game(), asked.player(), ability), null);
        }
        if (CharacterMovementEvent.class.isAssignableFrom(type)) {
            val ability = ((CharacterMovementEventProcessor<?>) processor(type)).ability();
            return new AnswerClass(type, asked -> CharacterMovements.canUse(asked.game(), asked.player(), ability), null);
        }
        if (LocationEvent.class.isAssignableFrom(type)) {
            val location = location(type);
            return new AnswerClass(type, asked -> asked.has(EventConstraint.ANYWHERE_EVEN_IF_DAMAGED)
                    ? LocationEvent.canActIgnoringDamage(asked.game(), asked.player(), location)
                    : LocationEvent.canAct(asked.game(), asked.player(), location), null);
        }
        return new AnswerClass(type, asked -> true, null);
    }

    private static Object processor(Class<?> type) {
        try {
            return Class.forName(type.getName() + "Processor").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No processor for " + type.getSimpleName(), e);
        }
    }

    // A location's event names its location whatever its fields hold, so a record of default values tells it.
    private static Location location(Class<?> type) {
        try {
            val components = type.getRecordComponents();
            val constructor = type.getDeclaredConstructor(Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new));
            val event = (LocationEvent) constructor.newInstance(Arrays.stream(components)
                    .map(component -> defaultValue(component.getType()))
                    .toArray());
            return Objects.requireNonNull(event.location());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No location for " + type.getSimpleName(), e);
        }
    }

    static Object defaultValue(Class<?> type) {
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
}
