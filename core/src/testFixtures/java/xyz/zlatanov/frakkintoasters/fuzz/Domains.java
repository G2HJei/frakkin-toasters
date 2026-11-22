package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ComponentRole;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.interrupt.RollAbilities;
import xyz.zlatanov.frakkintoasters.event.interrupt.UseRollAbilityEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The values a record component of a candidate answer is tried with, by its type and name: the player who is asked,
 * the cards he holds, the ships on the boards, every constant of an enum. Null is tried for every reference, because a
 * client may send it and the processor has to reject it.
 */
final class Domains {

    private static final List<ShipType> FIGHTERS        = List.of(ShipType.VIPER, ShipType.VIPER_MARK_VII,
            ShipType.ASSAULT_RAPTOR);
    private static final List<Object>   ALL_SKILL_CARDS = Stream.of(AllCardsProvider.politicsCards(),
                    AllCardsProvider.leadershipCards(), AllCardsProvider.tacticsCards(), AllCardsProvider.pilotingCards(),
                    AllCardsProvider.engineeringCards(), AllCardsProvider.treacheryCards())
            .flatMap(deck -> deck.cards().stream())
            .distinct()
            .map(card -> (Object) card)
            .toList();
    private static final List<Integer>  UNKNOWN_SHIPS   = List.of(-1, 0, 999);

    private Domains() {
    }

    /**
     * @param skillCardType the card the event plays, which narrows a {@code card} component to it
     */
    static List<Object> of(Asked asked, Class<?> record, RecordComponent component, SkillCardType skillCardType) {
        val type = component.getType();
        val name = component.getName();
        if (type == int.class || type == Integer.class) {
            return withNull(type, ints(asked, record, name));
        }
        if (type == boolean.class) {
            return List.of(false, true);
        }
        if (type == Boolean.class) {
            return Arrays.asList(null, false, true);
        }
        if (type == SkillCard.class) {
            return withNull(type, skillCards(asked, name, skillCardType));
        }
        if (type == ViperActivation.class) {
            return withNull(type, viperActivations(asked));
        }
        if (type == EventConstraint.class) {
            return Arrays.asList((Object) null); // submit replaces it with the decision's
        }
        if (type.isEnum()) {
            return withNull(type, constants(asked, record, name, type));
        }
        if (type == List.class || type == Set.class || type == Collection.class) {
            val lists = lists(asked, component);
            if (lists == null) {
                return gap(asked, record, component);
            }
            return type == Set.class
                    ? lists.stream().map(list -> (Object) new LinkedHashSet<>((List<?>) list)).toList()
                    : lists;
        }
        return gap(asked, record, component);
    }

    private static List<Object> ints(Asked asked, Class<?> record, String name) {
        val role = ComponentRole.of(name);
        if (role.isEmpty()) {
            asked.findings().accept(Finding.of(Finding.Kind.FUZZER, "no role for " + record.getSimpleName() + "." + name,
                    "No role for the int `" + record.getSimpleName() + "." + name + "`"));
            return IntStream.rangeClosed(0, asked.game().players().size()).boxed().map(i -> (Object) i).toList();
        }
        return switch (role.get()) {
            case PLAYER -> name.equals("playerNumber")
                    ? List.of(asked.playerNumber())
                    : IntStream.rangeClosed(1, asked.game().players().size()).boxed().map(i -> (Object) i).toList();
            case SHIP -> (name.toLowerCase().contains("centurion") ? centurionIds(asked) : shipIds(asked)).stream()
                    .map(id -> (Object) id).toList();
            case COUNT -> switch (name) {
                case "result" -> IntStream.rangeClosed(1, 8).boxed().map(i -> (Object) i).toList(); // a die's faces
                case "calculationsModifier" -> List.of(-1, 1);
                default -> IntStream.rangeClosed(-1, 8).boxed().map(i -> (Object) i).toList();
            };
            case SCOPE -> List.of(0);
        };
    }

    private static List<Object> skillCards(Asked asked, String name, SkillCardType skillCardType) {
        val hand = asked.player().skillCards().cards();
        if (skillCardType != null && name.equals("card")) {
            return hand.stream().filter(card -> card.type() == skillCardType).distinct().map(card -> (Object) card).toList();
        }
        val cards = new LinkedHashSet<Object>(hand);
        // Some abilities take a card of the check: "Quick Thinking", "Dogfight".
        val check = asked.game().activeSkillCheck();
        if (check != null) {
            cards.addAll(check.dividedCards());
            cards.addAll(check.cards().cards());
        }
        return List.copyOf(cards);
    }

    // The moves, attacks and escorts of each fighter in space, and every launch.
    private static List<Object> viperActivations(Asked asked) {
        val game = asked.game();
        val result = new ArrayList<Object>();
        game.boards().galactica().shipsInSpace(HumanFighterView.class).stream()
                .sorted(Ship.BY_ID)
                .forEach(fighter -> result.addAll(ActivateViperEventProcessor.activations(game, fighter, false)));
        for (val type : FIGHTERS) {
            for (val location : GalacticaBoardView.VIPER_LAUNCH_SPACES) {
                result.add(ViperActivation.launch(type, location));
            }
        }
        return result;
    }

    private static List<Object> constants(Asked asked, Class<?> record, String name, Class<?> type) {
        val player = asked.player();
        val game = asked.game();
        if (type == QuorumCard.class) {
            val cards = new LinkedHashSet<Object>(game.presidentHand().cards());
            cards.addAll(player.quorumCards().cards());
            return List.copyOf(cards);
        }
        if (type == MutinyCard.class) {
            return distinct(player.mutinyCards().cards());
        }
        if (type == LoyaltyCard.class) {
            return distinct(player.loyaltyCards().cards());
        }
        if (type == SuperCrisisCard.class) {
            return distinct(player.superCrisisCards().cards());
        }
        if (type == MotiveCard.class) {
            return distinct(player.motiveCards().cards());
        }
        if (type == CrisisCard.class) {
            return distinct(game.turn().crisisCards());
        }
        if (type == Character.class && name.equals("pilot")) {
            return player.character() == null ? List.of() : List.of(player.character());
        }
        if (type == CharacterAbility.class) {
            if (record == UseRollAbilityEvent.class) {
                return List.copyOf(RollAbilities.ABILITIES);
            }
            return player.character() == null ? List.of() : List.copyOf(CharacterAbility.of(player.character()));
        }
        return List.of(type.getEnumConstants());
    }

    // Lists of the elements a component holds: cards of the hand, ships of one place, constants of an enum.
    private static List<Object> lists(Asked asked, RecordComponent component) {
        val element = elementType(component.getGenericType());
        if (element == null) {
            return null;
        }
        val random = asked.random();
        val result = new ArrayList<Object>();
        result.add(List.of());
        if (element == SkillCard.class) {
            val hand = asked.player().skillCards().cards();
            // up to 8: "Impeachment" discards 5
            result.addAll(Combinations.subsets(hand, 1, Math.min(8, hand.size()), 48, random));
            return result;
        }
        if (element == Integer.class) {
            if (ComponentRole.of(component.getName()).filter(ComponentRole.SHIP::equals).isEmpty()) {
                return null;
            }
            for (val group : shipGroups(asked).values()) {
                result.addAll(Combinations.subsets(group, 1, Math.min(3, group.size()), 8, random));
            }
            return result;
        }
        if (element == EventConstraint.class) {
            return result;
        }
        if (element.isEnum()) {
            val constants = constants(asked, null, component.getName(), element);
            result.addAll(Combinations.subsets(constants, 1, 1, 64, random));
            result.addAll(Combinations.arrangements(constants, 2, 48, random));
            return result;
        }
        return null;
    }

    private static Class<?> elementType(Type type) {
        if (type instanceof ParameterizedType parameterized
                && parameterized.getActualTypeArguments()[0] instanceof Class<?> element) {
            return element;
        }
        return null;
    }

    private static List<Integer> shipIds(Asked asked) {
        return shipGroups(asked).values().stream().flatMap(List::stream).distinct().sorted().toList();
    }

    private static List<Integer> centurionIds(Asked asked) {
        return asked.game().boards().galactica().boardingPartyTrack().keySet().stream()
                .map(centurion -> centurion.id())
                .sorted()
                .toList();
    }

    // The ships by where they are, because a card that takes several ships takes them from one space area or pile, and
    // by their type in space, for one that takes them from any area ("Authorization of Brutal Force").
    private static Map<String, List<Integer>> shipGroups(Asked asked) {
        val boards = asked.game().boards();
        val galactica = boards.galactica();
        val groups = new TreeMap<String, List<Integer>>();
        galactica.shipsInSpace().forEach((ship, location) -> {
            groups.computeIfAbsent(location.name(), key -> new ArrayList<>()).add(ship.id());
            groups.computeIfAbsent("TYPE_" + ship.getClass().getSimpleName(), key -> new ArrayList<>()).add(ship.id());
        });
        groups.put("BOARDING_PARTY", new ArrayList<>(centurionIds(asked)));
        boards.cylonFleet().shipsInSpace().forEach((ship, location) ->
                groups.computeIfAbsent("FLEET_" + location.name(), key -> new ArrayList<>()).add(ship.id()));
        groups.put("RESERVES", ids(galactica.reserves()));
        groups.put("DAMAGED", ids(galactica.damagedShips()));
        groups.put("SUPPLY", ids(galactica.supply()));
        groups.values().forEach(group -> group.sort(Integer::compare));
        groups.values().removeIf(List::isEmpty);
        return groups;
    }

    private static List<Integer> ids(Collection<? extends Ship> ships) {
        return new ArrayList<>(ships.stream().map(Ship::id).sorted().toList());
    }

    private static List<Object> distinct(List<?> values) {
        return values.stream().distinct().map(value -> (Object) value).toList();
    }

    private static List<Object> withNull(Class<?> type, List<Object> values) {
        if (type.isPrimitive()) {
            return values;
        }
        return Stream.concat(Stream.of((Object) null), values.stream()).toList();
    }

    /**
     * Values a client could send that the agent never would: any card of the decks, players and ships that aren't
     * there, nulls. A type without values gets null alone; the normal domains report it.
     */
    static List<Object> wide(Asked asked, RecordComponent component) {
        return wide(asked, component.getName(), component.getType(), component.getGenericType(), 0);
    }

    private static List<Object> wide(Asked asked, String name, Class<?> type, Type generic, int depth) {
        if (type == int.class || type == Integer.class) {
            return withNull(type, wideInts(asked, name));
        }
        if (type == long.class || type == Long.class) {
            return withNull(type, List.of(-1L, 0L, 1L, 999L));
        }
        if (type == boolean.class) {
            return List.of(false, true);
        }
        if (type == Boolean.class) {
            return Arrays.asList(null, false, true);
        }
        if (type == SkillCard.class) {
            return withNull(type, ALL_SKILL_CARDS);
        }
        if (type.isEnum()) {
            return withNull(type, List.of(type.getEnumConstants()));
        }
        if (type == List.class || type == Set.class || type == Collection.class) {
            val element = elementType(generic);
            if (element == null) {
                return Arrays.asList((Object) null);
            }
            val lists = wideLists(wide(asked, name, element, element, depth + 1), asked.random());
            return withNull(type, type == Set.class
                    ? lists.stream().map(list -> (Object) new LinkedHashSet<>((List<?>) list)).toList()
                    : lists);
        }
        if (type == Map.class) {
            return withNull(type, wideMaps(generic, asked.random()));
        }
        if (type.isRecord() && depth == 0) {
            val records = new ArrayList<>(type == ViperActivation.class ? viperActivations(asked) : List.of());
            records.addAll(wideRecords(asked, type));
            return withNull(type, records);
        }
        return Arrays.asList((Object) null);
    }

    private static List<Object> wideInts(Asked asked, String name) {
        val players = asked.game().players().size();
        val role = ComponentRole.of(name);
        if (role.isEmpty()) {
            return IntStream.rangeClosed(-1, players + 1).boxed().map(i -> (Object) i).toList();
        }
        return switch (role.get()) {
            // the engine itself rejects an event that names another player than the one who submits it
            case PLAYER -> name.equals("playerNumber")
                    ? List.of(asked.playerNumber())
                    : IntStream.rangeClosed(0, players + 1).boxed().map(i -> (Object) i).toList();
            case SHIP -> Stream.of(shipIds(asked), centurionIds(asked), UNKNOWN_SHIPS)
                    .flatMap(List::stream)
                    .distinct()
                    .sorted()
                    .map(id -> (Object) id)
                    .toList();
            case COUNT -> Stream.concat(IntStream.rangeClosed(-2, 10).boxed(), Stream.of(99)).map(i -> (Object) i).toList();
            case SCOPE -> List.of(-1, 0, 1, 999);
        };
    }

    // None, single ones, pairs, a pair of the same value, and a null element, which a list read from JSON can hold.
    private static List<Object> wideLists(List<Object> elements, Random random) {
        val values = elements.stream().filter(Objects::nonNull).toList();
        val result = new ArrayList<Object>();
        result.add(List.of());
        result.add(Arrays.asList((Object) null));
        if (values.isEmpty()) {
            return result;
        }
        for (int i = 0; i < 6; i++) {
            result.add(List.of(values.get(random.nextInt(values.size()))));
            result.add(List.of(values.get(random.nextInt(values.size())), values.get(random.nextInt(values.size()))));
        }
        val twice = values.get(random.nextInt(values.size()));
        result.add(List.of(twice, twice));
        return result;
    }

    // Maps of an enum to counts, the shape of a draw's colors, in the enum's order.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> wideMaps(Type generic, Random random) {
        if (!(generic instanceof ParameterizedType parameterized)
                || !(parameterized.getActualTypeArguments()[0] instanceof Class<?> key) || !key.isEnum()) {
            return List.of();
        }
        val keys = key.getEnumConstants();
        val result = new ArrayList<Object>();
        result.add(new EnumMap(key));
        for (int i = 0; i < 6; i++) {
            val map = new EnumMap(key);
            for (int entries = 0; entries <= i % 2; entries++) {
                map.put(keys[random.nextInt(keys.length)], random.nextInt(13) - 2);
            }
            result.add(map);
        }
        return result;
    }

    // Records of the wide values of their components, e.g. a viper activation of a ship that isn't there.
    private static List<Object> wideRecords(Asked asked, Class<?> type) {
        val components = type.getRecordComponents();
        val domains = Arrays.stream(components)
                .map(component -> wide(asked, component.getName(), component.getType(), component.getGenericType(), 1))
                .toList();
        final Constructor<?> constructor;
        try {
            constructor = type.getDeclaredConstructor(Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            return List.of();
        }
        val result = new ArrayList<Object>();
        for (int i = 0; i < 8; i++) {
            val args = domains.stream().map(domain -> domain.get(asked.random().nextInt(domain.size()))).toArray();
            try {
                result.add(constructor.newInstance(args));
            } catch (InvocationTargetException refused) {
                // a compact constructor that refuses the values: the codec could not build it either
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    private static List<Object> gap(Asked asked, Class<?> record, RecordComponent component) {
        val name = record.getSimpleName() + "." + component.getName();
        asked.findings().accept(Finding.of(Finding.Kind.FUZZER, "no domain for " + name,
                "No candidate values for `" + name + "` (" + component.getGenericType().getTypeName() + ")"));
        return Arrays.asList((Object) null);
    }
}
