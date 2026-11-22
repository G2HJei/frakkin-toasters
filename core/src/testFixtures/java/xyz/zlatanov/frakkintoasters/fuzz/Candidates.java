package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.action.character.GlimpseTheFaceOfGodPlaceEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.ReorderTopQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SetTheAgendaChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.character.HumanDelusionEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.player.DiscardQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsIntoSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.TiebreakCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.fuzz.AnswerClasses.AnswerClass;
import xyz.zlatanov.frakkintoasters.operation.TitleOps;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;

/**
 * The candidate answers of one event class, in the order to try them. An answer with a shape (a discard of so many
 * cards, a draw of so many colors, an order) has its own source, written from its processor's rules; any other record
 * is built from the {@link Domains} of its components.
 */
final class Candidates {

    /**
     * @param enumerate up to how many candidates every one is tried
     * @param samples   how many random ones are tried when there are more
     */
    record Budget(int enumerate, int samples) {
    }

    static final Budget NORMAL     = new Budget(512, 64);
    static final Budget EXHAUSTIVE = new Budget(20_000, 20_000);

    private static final Map<Class<?>, BiFunction<Asked, Budget, List<? extends Event>>> SOURCES = Map.ofEntries(
            Map.entry(DrawSkillCardsEvent.class, Candidates::draws),
            Map.entry(DiscardSkillCardsEvent.class, Candidates::discards),
            Map.entry(DiscardQuorumCardsEvent.class, Candidates::quorumDiscards),
            Map.entry(PlayCardsBeforeSkillCheckEvent.class, (asked, budget) -> handPicks(asked, budget, 0).stream()
                    .map(cards -> new PlayCardsBeforeSkillCheckEvent(asked.playerNumber(), cards)).toList()),
            Map.entry(PlayCardsIntoSkillCheckEvent.class, (asked, budget) -> handPicks(asked, budget, 0).stream()
                    .map(cards -> new PlayCardsIntoSkillCheckEvent(asked.playerNumber(), cards)).toList()),
            Map.entry(HumanDelusionEvent.class, (asked, budget) -> handPicks(asked, budget, 1).stream()
                    .map(cards -> new HumanDelusionEvent(asked.playerNumber(), cards)).toList()),
            Map.entry(TiebreakCardsBeforeSkillCheckEvent.class, Candidates::tiebreaks),
            Map.entry(GlimpseTheFaceOfGodPlaceEvent.class, (asked, budget) -> {
                val hand = asked.player().skillCards().cards();
                return Combinations.arrangements(hand, Math.min(2, hand.size()), budget.samples(), asked.random()).stream()
                        .map(cards -> new GlimpseTheFaceOfGodPlaceEvent(asked.playerNumber(), cards)).toList();
            }),
            Map.entry(SetTheAgendaChoiceEvent.class, (asked, budget) -> {
                val hand = asked.game().presidentHand().cards();
                return Combinations.arrangements(hand, Math.min(2, hand.size()), budget.samples(), asked.random()).stream()
                        .map(cards -> new SetTheAgendaChoiceEvent(asked.playerNumber(), cards)).toList();
            }),
            Map.entry(ReorderTopQuorumCardsEvent.class, (asked, budget) -> {
                List<QuorumCard> deck = asked.game().decks().<QuorumCard>deck(DeckId.QUORUM).cards();
                val top = deck.subList(0, Math.min(5, deck.size()));
                val orders = new ArrayList<>(Combinations.permutations(top));
                Collections.shuffle(orders, asked.random());
                return orders.stream().map(order -> new ReorderTopQuorumCardsEvent(asked.playerNumber(), order)).toList();
            }),
            Map.entry(LaunchViperEvent.class, Candidates::launches));

    private static final List<ShipType> FIGHTERS = List.of(ShipType.VIPER, ShipType.VIPER_MARK_VII,
            ShipType.ASSAULT_RAPTOR);

    private Candidates() {
    }

    static List<? extends Event> of(Asked asked, AnswerClass answerClass, Budget budget) {
        val source = SOURCES.get(answerClass.type());
        return source != null ? source.apply(asked, budget) : reflective(asked, answerClass, budget);
    }

    // Every way to draw the count the constraint names over the 6 colors, then fewer: the processor takes fewer only
    // when no other card fits.
    private static List<DrawSkillCardsEvent> draws(Asked asked, Budget budget) {
        val count = drawCount(asked);
        val result = new ArrayList<DrawSkillCardsEvent>();
        for (int size = count; size >= 0; size--) {
            val sized = new ArrayList<DrawSkillCardsEvent>();
            for (val counts : Combinations.multisets(SkillCardColor.values().length, size)) {
                val selection = new EnumMap<SkillCardColor, Integer>(SkillCardColor.class);
                for (int color = 0; color < counts.length; color++) {
                    if (counts[color] > 0) {
                        selection.put(SkillCardColor.values()[color], counts[color]);
                    }
                }
                sized.add(new DrawSkillCardsEvent(asked.playerNumber(), selection, null));
            }
            Collections.shuffle(sized, asked.random());
            result.addAll(sized);
        }
        return result.size() > budget.enumerate() ? result.subList(0, budget.enumerate()) : result;
    }

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
                case DRAW_EXACTLY_5 -> {
                    return 5;
                }
                default -> {
                }
            }
        }
        return 5;
    }

    // The size the constraint asks for, from the hand as it is now: the Discard step asks everyone at once.
    private static List<DiscardSkillCardsEvent> discards(Asked asked, Budget budget) {
        val hand = asked.player().skillCards().cards();
        val size = discardSize(asked.decision().eventConstraints(), hand.size(), asked.player().handLimit());
        val picks = size < 0
                ? Combinations.subsets(hand, 0, hand.size(), budget.samples(), asked.random())
                : Combinations.subsets(hand, size, budget.enumerate(), asked.random());
        return picks.stream().map(cards -> new DiscardSkillCardsEvent(asked.playerNumber(), cards, null)).toList();
    }

    private static int discardSize(List<EventConstraint> constraints, int hand, int handLimit) {
        for (val constraint : constraints) {
            switch (constraint) {
                case DISCARD_EXACTLY_1 -> {
                    return Math.min(1, hand);
                }
                case DISCARD_EXACTLY_2 -> {
                    return Math.min(2, hand);
                }
                case DISCARD_EXACTLY_3 -> {
                    return Math.min(3, hand);
                }
                case DISCARD_EXACTLY_4 -> {
                    return Math.min(4, hand);
                }
                case DISCARD_EXACTLY_5 -> {
                    return Math.min(5, hand);
                }
                case DISCARD_DOWN_TO_HAND_LIMIT -> {
                    return Math.max(0, hand - handLimit);
                }
                case DISCARD_DOWN_TO_1 -> {
                    return Math.max(0, hand - 1);
                }
                case DISCARD_DOWN_TO_3 -> {
                    return Math.max(0, hand - 3);
                }
                default -> {
                }
            }
        }
        return -1;
    }

    private static List<DiscardQuorumCardsEvent> quorumDiscards(Asked asked, Budget budget) {
        val hand = asked.game().presidentHand().cards();
        val size = Math.max(0, hand.size() - TitleOps.QUORUM_HAND_LIMIT);
        return Combinations.subsets(hand, size, budget.enumerate(), asked.random()).stream()
                .map(cards -> new DiscardQuorumCardsEvent(asked.playerNumber(), cards, null))
                .toList();
    }

    // None, and picks of the hand of up to 4 cards.
    private static List<List<SkillCard>> handPicks(Asked asked, Budget budget, int minSize) {
        val hand = asked.player().skillCards().cards();
        val picks = new ArrayList<List<SkillCard>>();
        if (minSize == 0) {
            picks.add(List.of());
        }
        picks.addAll(Combinations.subsets(hand, Math.max(1, minSize), Math.min(4, hand.size()), budget.samples(),
                asked.random()));
        Collections.shuffle(picks, asked.random());
        return picks;
    }

    // One card of each group of equal cards played before the check.
    private static List<TiebreakCardsBeforeSkillCheckEvent> tiebreaks(Asked asked, Budget budget) {
        val check = asked.game().activeSkillCheck();
        if (check == null) {
            return List.of(new TiebreakCardsBeforeSkillCheckEvent(List.of()));
        }
        val picks = new ArrayList<>(Combinations.oneOfEach(check.playedBeforeConflicts()));
        Collections.shuffle(picks, asked.random());
        return picks.stream().map(TiebreakCardsBeforeSkillCheckEvent::new).toList();
    }

    private static List<LaunchViperEvent> launches(Asked asked, Budget budget) {
        val player = asked.player();
        val unmanned = Stream.concat(Stream.of((Integer) null), asked.game().boards().galactica()
                        .shipsInSpace(HumanFighterView.class).stream()
                        .filter(fighter -> fighter.pilot() == null)
                        .sorted(Ship.BY_ID)
                        .map(Ship::id))
                .toList();
        val pilots = Arrays.asList(player.character(), (Character) null);
        val types = Stream.concat(Stream.of((ShipType) null), FIGHTERS.stream()).toList();
        val locations = Stream.concat(Stream.of((Location) null), GalacticaBoardView.VIPER_LAUNCH_SPACES.stream()).toList();
        val result = new ArrayList<LaunchViperEvent>();
        for (val type : types) {
            for (val location : locations) {
                for (val pilot : pilots) {
                    for (val viperId : unmanned) {
                        result.add(new LaunchViperEvent(asked.playerNumber(), type, location, pilot, viperId, false));
                    }
                }
            }
        }
        Collections.shuffle(result, asked.random());
        return result;
    }

    /**
     * @return one event of the class built from the wide domains, an input a client could send but the agent never
     * would; {@code null} when the record refuses every set of values it is tried with
     */
    static Event hostile(Asked asked, AnswerClass answerClass) {
        val type = answerClass.type();
        val components = type.getRecordComponents();
        val domains = Arrays.stream(components).map(component -> Domains.wide(asked, component)).toList();
        val constructor = constructor(type, components);
        for (int attempt = 0; attempt < 16; attempt++) {
            val args = domains.stream().map(domain -> domain.get(asked.random().nextInt(domain.size()))).toArray();
            val event = construct(constructor, args);
            if (event != null) {
                return event;
            }
        }
        return null;
    }

    // Every tuple of the components' values when there are few, otherwise random ones.
    private static List<Event> reflective(Asked asked, AnswerClass answerClass, Budget budget) {
        val type = answerClass.type();
        val components = type.getRecordComponents();
        val domains = new ArrayList<List<Object>>();
        long tuples = 1;
        for (val component : components) {
            val domain = Domains.of(asked, type, component, answerClass.skillCardType());
            if (domain.isEmpty()) {
                return List.of();
            }
            domains.add(domain);
            tuples = Math.min(Long.MAX_VALUE / 64, tuples * domain.size());
        }
        val indices = new ArrayList<Long>();
        if (tuples <= budget.enumerate()) {
            for (long i = 0; i < tuples; i++) {
                indices.add(i);
            }
            Collections.shuffle(indices, asked.random());
        } else {
            for (int i = 0; i < budget.samples(); i++) {
                indices.add(Math.floorMod(asked.random().nextLong(), tuples));
            }
        }
        val constructor = constructor(type, components);
        val result = new ArrayList<Event>();
        for (val index : indices) {
            val args = new Object[components.length];
            var rest = index;
            for (int c = 0; c < components.length; c++) {
                val domain = domains.get(c);
                args[c] = domain.get((int) (rest % domain.size()));
                rest /= domain.size();
            }
            val event = construct(constructor, args);
            if (event != null) {
                result.add(event);
            }
        }
        return result;
    }

    private static java.lang.reflect.Constructor<?> constructor(Class<?> type, RecordComponent[] components) {
        try {
            return type.getDeclaredConstructor(Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No canonical constructor for " + type.getSimpleName(), e);
        }
    }

    // A constructor that refuses the values is a candidate the codec couldn't build either: it is skipped.
    private static Event construct(java.lang.reflect.Constructor<?> constructor, Object[] args) {
        try {
            return (Event) constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
