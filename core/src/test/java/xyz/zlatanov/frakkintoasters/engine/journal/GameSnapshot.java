package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Board;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;

import java.util.*;

/**
 * Everything that can differ between two games, as plain values that compare with {@code equals} (the components don't:
 * a Viper equals by id only, a Basestar ignores its damage, only Deck has equals). Used to check that a replayed game
 * equals the live one. A failed {@code assertEquals} prints both maps, so the differing entry is easy to find.
 */
public record GameSnapshot(Map<String, Object> values) {

    public static GameSnapshot of(Game game) {
        val s = new TreeMap<String, Object>();
        s.put("objective", game.objective());
        s.put("outcome", game.outcome());
        s.put("step", game.turn().step());
        s.put("turn", game.turn().number());
        s.put("currentPlayer", game.turn().currentPlayerNumber());
        s.put("actingPlayer", game.turn().actingPlayerNumber());
        s.put("turnCrisisCards", new ArrayList<>(game.turn().crisisCards()));
        s.put("turnEndedEarly", game.turn().endedEarly());
        s.put("president", game.president());
        s.put("admiral", game.admiral());
        s.put("cag", game.cag());
        s.put("nukes", game.nukes());
        s.put("presidentHand", deck(game.presidentHand()));
        for (val id : DeckId.values()) {
            s.put("deck." + id, deck(game.decks().deck(id)));
        }
        for (val player : game.players()) {
            val prefix = "player" + player.number() + ".";
            s.put(prefix + "character", player.character());
            for (val pile : PlayerPile.values()) {
                s.put(prefix + pile, deck(player.pile(pile)));
            }
            s.put(prefix + "miracleToken", player.hasMiracleToken());
            s.put(prefix + "infiltrating", player.isInfiltrating());
            s.put(prefix + "revealedCivilianShips", new ArrayList<>(player.revealedCivilianShipIds()));
            s.put(prefix + "turns", player.turns());
        }
        galactica(s, game.boards().galactica());
        val pegasus = game.boards().pegasus();
        s.put("pegasus.characters", new TreeMap<>(characters(pegasus)));
        s.put("pegasus.damagedLocations", sorted(pegasus.damagedLocations()));
        s.put("pegasus.engineRoomActivated", pegasus.engineRoomActivated());
        val fleet = game.boards().cylonFleet();
        s.put("fleet.pursuit", fleet.pursuitTrack());
        s.put("fleet.characters", new TreeMap<>(characters(fleet)));
        s.put("fleet.ships", shipsIn(fleet.shipsInSpace()));
        s.put("fleet.damagedLocations", sorted(fleet.damagedLocations()));
        val supply = game.cylonShips();
        s.put("supply.basestars", supply.basestars().stream().map(GameSnapshot::describe).toList());
        s.put("supply.raiders", supply.raiders().stream().map(GameSnapshot::describe).toList());
        s.put("supply.heavyRaiders", supply.heavyRaiders().stream().map(GameSnapshot::describe).toList());
        s.put("supply.centurions", supply.centurions());
        s.put("removed", game.removedComponents().items().stream()
                .map(item -> item instanceof Ship ship ? describe(ship) : String.valueOf(item)).toList());
        s.put("skillCheck", skillCheck(game.activeSkillCheck()));
        return new GameSnapshot(s);
    }

    private static void galactica(Map<String, Object> s, GalacticaBoard board) {
        s.put("galactica.fuel", board.fuel());
        s.put("galactica.food", board.food());
        s.put("galactica.morale", board.morale());
        s.put("galactica.population", board.population());
        s.put("galactica.jumpPreparation", board.jumpPreparation());
        s.put("galactica.colonialOneDestroyed", board.colonialOneDestroyed());
        s.put("galactica.characters", new TreeMap<>(characters(board)));
        s.put("galactica.damagedLocations", sorted(board.damagedLocations()));
        s.put("galactica.reserves", board.reserves().stream().map(GameSnapshot::describe).sorted().toList());
        s.put("galactica.damagedShips", board.damagedShips().stream().map(GameSnapshot::describe).sorted().toList());
        s.put("galactica.ships", shipsIn(board.shipsInSpace()));
        s.put("galactica.boardingParty", new LinkedHashMap<>(board.boardingPartyTrack()));
    }

    private static Map<String, Object> deck(Deck<?> deck) {
        val result = new TreeMap<String, Object>();
        result.put("cards", new ArrayList<>(deck.cards()));
        result.put("discarded", new ArrayList<>(deck.discardedCards()));
        result.put("revealed", new ArrayList<>(deck.revealedCards()));
        return result;
    }

    private static Object skillCheck(SkillCheck check) {
        if (check == null) {
            return null;
        }
        val result = new TreeMap<String, Object>();
        result.put("type", check.type());
        result.put("target", check.targetPlayerNumber());
        result.put("playedBefore", new TreeMap<>(playedBefore(check)));
        result.put("playedBeforeRevealed", check.playedBeforeRevealed());
        for (val pile : SkillCheckPile.values()) {
            result.put("pile." + pile, deck(check.pile(pile)));
        }
        result.put("resolvedAbilities", new ArrayList<>(check.resolvedAbilities()));
        result.put("emergencyDeclared", check.emergencyDeclared());
        result.put("totalStrength", check.totalStrength());
        result.put("result", check.result());
        result.put("passResultReplaced", check.passResultReplaced());
        result.put("failResultPrevented", check.failResultPrevented());
        return result;
    }

    private static Map<Integer, Object> playedBefore(SkillCheck check) {
        val result = new HashMap<Integer, Object>();
        for (int player = 1; player <= 8; player++) {
            val cards = check.playedBeforeSkillCheck(player);
            if (!cards.isEmpty()) {
                result.put(player, new ArrayList<>(cards));
            }
        }
        return result;
    }

    private static Map<String, Object> characters(Board board) {
        val result = new HashMap<String, Object>();
        board.characters().forEach((character, location) -> result.put(character.name(), location));
        return result;
    }

    private static Map<String, Object> shipsIn(Map<Ship, ?> ships) {
        val result = new TreeMap<String, Object>();
        ships.forEach((ship, location) -> result.put(describe(ship), location));
        return result;
    }

    private static List<String> sorted(Collection<?> values) {
        return values.stream().map(Object::toString).sorted().toList();
    }

    /**
     * A ship with the parts that its own equals ignores: the pilot of a fighter and the damage of a basestar.
     */
    private static String describe(Ship ship) {
        val base = ship.getClass().getSimpleName() + "#" + ship.id();
        if (ship instanceof HumanFighter fighter) {
            return base + " pilot=" + fighter.pilot();
        }
        if (ship instanceof Basestar basestar) {
            return base + " damage=" + basestar.damage();
        }
        return base;
    }
}
