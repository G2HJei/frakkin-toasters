package xyz.zlatanov.frakkintoasters.event;

// The attack on a viper, after the die roll:
// Viper: 5-7 = damaged, 8 = destroyed
// Viper mark VII: 6-7 = damaged, 8 = destroyed (rule 11.3.1.3)
// Assault raptor: 7-8 = destroyed (rule 11.3.2.3)
public record AttackViperResultEvent(int humanFighterId) implements Event {
}
