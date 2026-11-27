package xyz.zlatanov.frakkintoasters.event.player;

// How a player has to discard Skill Cards, for the abilities that make him discard fewer ("Deceitful", "For Love").
public enum DiscardKind {
    CHOSEN,
    RANDOM,
    HAND // his whole hand, the count isn't used
}
