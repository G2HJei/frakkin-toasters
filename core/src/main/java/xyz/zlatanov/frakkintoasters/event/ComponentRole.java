package xyz.zlatanov.frakkintoasters.event;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * What an int component of a record names, told by its name: a player, a ship, a journal scope or a count. The wire
 * schema gives it to the clients, and the random playouts pick a value by it.
 */
public enum ComponentRole {

    PLAYER, SHIP, SCOPE, COUNT;

    private static final Pattern SHIP_NAMES = Pattern.compile("(?i).*(shipId|fighterId|raiderId|basestarId|viperIds?|"
            + "centurionId|basestarShipId|raiderShipId|civilianShipId\\d?|shipIds|cylonShipId|escortCivilianShipId|"
            + "knownCivilianShips)");
    private static final Set<String> PLAYER_NAMES = Set.of("playerNumber", "targetPlayerNumber", "targetPlayer",
            "recipientPlayerNumber", "excludedPlayerNumber", "targetHumanPlayer", "ownerPlayerNumber", "otherPlayerNumber",
            "chooser", "actor", "player", "currentPlayer", "actingPlayer", "winnerPlayerNumbers", "winners",
            "waitingForPlayerNumber", "targetPlayers", "holderPlayerNumber", "administrationPlayerNumber",
            "drawerPlayerNumber", "lookerPlayerNumber", "discarderPlayerNumber");
    private static final Set<String> SCOPE_NAMES = Set.of("scopeId", "parentId");
    private static final Set<String> COUNT_NAMES = Set.of("count", "amount", "value", "modifier", "distanceBefore",
            "number", "turn", "turns", "size", "discardSize", "basestars", "raiders", "heavyRaiders", "centurions",
            "civilianShips", "totalStrength", "difficulty", "fuel", "food", "morale", "population", "nukes",
            "distanceTraveled", "playerCount", "calculationsModifier", "handLimit", "option", "fuelCost", "moraleCost",
            "populationCost", "result", "formatVersion", "activationsLeft", "delta", "adjustment", "raidersPerBasestar",
            "miracleTokens", "min", "max", "minStrength");

    /**
     * @return the role of an int component by its name, empty when the name can't be placed
     */
    public static Optional<ComponentRole> of(String name) {
        if (PLAYER_NAMES.contains(name)) {
            return Optional.of(PLAYER);
        }
        if (SCOPE_NAMES.contains(name)) {
            return Optional.of(SCOPE);
        }
        if (name.equals("id") || SHIP_NAMES.matcher(name).matches()) {
            return Optional.of(SHIP);
        }
        if (COUNT_NAMES.contains(name)) {
            return Optional.of(COUNT);
        }
        return Optional.empty();
    }
}
