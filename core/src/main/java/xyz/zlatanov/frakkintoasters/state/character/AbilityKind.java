package xyz.zlatanov.frakkintoasters.state.character;

/**
 * When a character ability is used, as its sheet prints it.
 */
public enum AbilityKind {
    // "Action:", taken instead of another action
    ACTION,
    // "Movement:", used instead of moving (rule 8.3.5)
    MOVEMENT,
    // used when something happens, often "you may"
    TRIGGER,
    // always in effect
    PASSIVE,
    // the negative ability, always in effect
    NEGATIVE
}
