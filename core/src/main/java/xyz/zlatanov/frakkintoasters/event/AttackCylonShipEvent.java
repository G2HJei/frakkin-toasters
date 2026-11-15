package xyz.zlatanov.frakkintoasters.event;

// A viper or an assault raptor attacks a raider, a heavy raider or a basestar in its space area, or Galactica does with
// "Weapons Control" (rule 11.6).
//
// @param humanFighterId the attacking fighter, null when Galactica attacks
public record AttackCylonShipEvent(Integer humanFighterId, int cylonShipId) implements Event {

    public static AttackCylonShipEvent byGalactica(int cylonShipId) {
        return new AttackCylonShipEvent(null, cylonShipId);
    }

    public static AttackCylonShipEvent byFighter(int humanFighterId, int cylonShipId) {
        return new AttackCylonShipEvent(humanFighterId, cylonShipId);
    }
}
