package xyz.zlatanov.frakkintoasters.event;

// The attack on a Cylon ship, after the die roll:
// Raider: 3-8 = destroyed
// Heavy raider: 7-8 = destroyed
// Basestar: with a viper 8 = damaged, with an assault raptor 7-8 = damaged, with Galactica 5-8 = damaged
// "Best of the Best" replaces the attack: "roll a die and destroy that many raiders in the viper's space area".
public record AttackCylonShipResultEvent(Integer humanFighterId, int cylonShipId) implements Event {
}
