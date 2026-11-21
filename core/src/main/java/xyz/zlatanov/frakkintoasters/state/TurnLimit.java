package xyz.zlatanov.frakkintoasters.state;

/**
 * What a player may do only once a turn, kept by the turn until the next one starts.
 */
public enum TurnLimit {
    // The CAG Title Card's action while piloting: an unmanned viper, then another action.
    CAG_PILOT_ACTION,
    // Discarding a "Bait" or a "Dradis Contact" by choice draws a Mutiny Card, once a turn (rule A2.3.8.1).
    MUTINY_CARD_FROM_TREACHERY
}
