package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ComponentRole;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.interrupt.RollAbilities;
import xyz.zlatanov.frakkintoasters.event.interrupt.UseRollAbilityEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Null is tried for every reference, because many answers mean something by it (no discard, the card's other
 * instruction, no unmanned viper).
 */
final class Domains {

    static final List<ShipType> FIGHTERS = List.of(ShipType.VIPER, ShipType.VIPER_MARK_VII, ShipType.ASSAULT_RAPTOR);

    private Domains() {
    }

    static List<Object> of(Asked asked, Class<?> record, RecordComponent component, SkillCardType skillCardType) {
        val type = component.getType();
        val name = component.getName();
        if (record == LaunchViperEvent.class) {
            val launch = launch(asked, name);
            if (launch != null) {
                return launch;
            }
        }
        if (type == int.class || type == Integer.class) {
            val ints = ints(asked, name);
            return ints == null ? null : withNull(type, ints);
        }
        if (type == boolean.class) {
            return List.of(false, true);
        }
        if (type == Boolean.class) {
            return withNull(type, List.of(false, true));
        }
        if (type == SkillCard.class) {
            return withNull(type, skillCards(asked, name, skillCardType));
        }
        if (type == ViperActivation.class) {
            return withNull(type, viperActivations(asked));
        }
        if (type == EventConstraint.class) {
            return withNull(type, List.of()); // submit replaces it with the decision's
        }
        if (type.isEnum()) {
            return withNull(type, constants(asked, record, name, type));
        }
        return null;
    }

    private static List<Object> ints(Asked asked, String name) {
        val role = ComponentRole.of(name);
        if (role.isEmpty()) {
            return null;
        }
        return switch (role.get()) {
            case PLAYER -> name.equals("playerNumber")
                    ? List.of(asked.playerNumber())
                    : IntStream.rangeClosed(1, asked.game().players().size()).boxed().map(i -> (Object) i).toList();
            case SHIP -> shipIds(asked, name).stream().map(id -> (Object) id).toList();
            case COUNT -> switch (name) {
                case "result" -> IntStream.rangeClosed(1, 8).boxed().map(i -> (Object) i).toList(); // a die's faces
                case "calculationsModifier" -> List.of(-1, 1);
                default -> IntStream.rangeClosed(-1, 8).boxed().map(i -> (Object) i).toList();
            };
            case SCOPE -> List.of(0);
        };
    }

    // A card of the player's hand, or one that lies face up in the skill check: "Quick Thinking", "Dogfight".
    private static List<Object> skillCards(Asked asked, String name, SkillCardType skillCardType) {
        val hand = asked.player().skillCards().cards();
        if (skillCardType != null && name.equals("card")) {
            return hand.stream().filter(card -> card.type() == skillCardType).distinct().map(card -> (Object) card).toList();
        }
        val cards = new LinkedHashSet<Object>(hand);
        val check = asked.game().activeSkillCheck();
        if (check != null && check.cardsDivided()) {
            cards.addAll(check.dividedCards());
            cards.addAll(check.cards().cards());
            cards.addAll(check.matchingPile().cards());
            cards.addAll(check.nonMatchingPile().cards());
        }
        return List.copyOf(cards);
    }

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

    // A launch takes a fighter to a launch tube, piloted by its player or unmanned.
    private static List<Object> launch(Asked asked, String name) {
        return switch (name) {
            case "shipType" -> withNull(ShipType.class, List.copyOf(FIGHTERS));
            case "location" -> withNull(Object.class, List.copyOf(GalacticaBoardView.VIPER_LAUNCH_SPACES));
            case "pilot" -> withNull(Character.class, asked.player().character() == null
                    ? List.of() : List.of(asked.player().character()));
            case "unmannedViperId" -> withNull(Integer.class, asked.game().boards().galactica()
                    .shipsInSpace(HumanFighterView.class).stream()
                    .filter(fighter -> fighter.pilot() == null)
                    .sorted(Ship.BY_ID)
                    .map(fighter -> (Object) fighter.id())
                    .toList());
            default -> null;
        };
    }

    static List<Object> constants(Asked asked, Class<?> record, String name, Class<?> type) {
        val player = asked.player();
        val game = asked.game();
        if (type == QuorumCard.class) {
            val cards = new LinkedHashSet<Object>();
            if (asked.isPresident()) {
                cards.addAll(game.presidentHand().cards());
            }
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

    static List<Integer> shipIds(Asked asked, String name) {
        return shipGroups(asked, name).values().stream().flatMap(List::stream).distinct().sorted().toList();
    }

    /**
     * By where the ships are and by their kind there, because a list of ships comes from one place (a space area, the
     * "Reserves") and often of one kind.
     */
    static Map<String, List<Integer>> shipGroups(Asked asked, String name) {
        val kind = kindOf(name);
        val boards = asked.game().boards();
        val galactica = boards.galactica();
        val groups = new TreeMap<String, List<Object>>();
        galactica.shipsInSpace().forEach((ship, location) -> {
            val kindName = ship.getClass().getSimpleName();
            groups.computeIfAbsent("SPACE_" + location.name(), key -> new ArrayList<>()).add(ship);
            groups.computeIfAbsent("SPACE_" + location.name() + "_" + kindName, key -> new ArrayList<>()).add(ship);
            groups.computeIfAbsent("SPACE_" + kindName, key -> new ArrayList<>()).add(ship);
        });
        groups.put("BOARDING_PARTY", new ArrayList<>(galactica.boardingPartyTrack().keySet()));
        boards.cylonFleet().shipsInSpace().forEach((ship, location) ->
                groups.computeIfAbsent("FLEET_" + location.name(), key -> new ArrayList<>()).add(ship));
        byKind(groups, "RESERVES", galactica.reserves());
        byKind(groups, "DAMAGED", galactica.damagedShips());
        byKind(groups, "SUPPLY", galactica.supply());
        val result = new TreeMap<String, List<Integer>>();
        groups.forEach((place, ships) -> {
            val ids = ships.stream().filter(kind).map(Domains::id).sorted().toList();
            if (!ids.isEmpty()) {
                result.put(place, ids);
            }
        });
        return result;
    }

    private static Predicate<Object> kindOf(String name) {
        val lower = name.toLowerCase();
        if (lower.contains("centurion")) {
            return Centurion.class::isInstance;
        }
        if (lower.contains("civilian")) {
            return CivilianShip.class::isInstance;
        }
        if (lower.contains("basestar")) {
            return Basestar.class::isInstance;
        }
        if (lower.contains("raider")) {
            return ship -> ship instanceof Raider || ship instanceof HeavyRaider;
        }
        if (lower.contains("cylonship")) {
            return ship -> ship instanceof CylonShip || ship instanceof Basestar;
        }
        if (lower.contains("viper") || lower.contains("fighter")) {
            return HumanFighterView.class::isInstance;
        }
        return ship -> true;
    }

    private static int id(Object ship) {
        return ship instanceof Centurion centurion ? centurion.id() : ((Ship) ship).id();
    }

    private static void byKind(Map<String, List<Object>> groups, String place, Collection<? extends Ship> ships) {
        ships.stream().sorted(Ship.BY_ID).forEach(ship -> {
            groups.computeIfAbsent(place, key -> new ArrayList<>()).add(ship);
            groups.computeIfAbsent(place + "_" + ship.getClass().getSimpleName(), key -> new ArrayList<>()).add(ship);
        });
    }

    private static List<Object> distinct(List<?> values) {
        return values.stream().distinct().map(value -> (Object) value).toList();
    }

    private static List<Object> withNull(Class<?> type, List<?> values) {
        if (type.isPrimitive()) {
            return List.copyOf(values);
        }
        val result = new ArrayList<Object>();
        result.add(null);
        result.addAll(values);
        return result;
    }
}
