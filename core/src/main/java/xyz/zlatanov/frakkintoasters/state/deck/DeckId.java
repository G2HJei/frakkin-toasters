package xyz.zlatanov.frakkintoasters.state.deck;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

/**
 * Names every deck of the game, so facts can refer to one.
 */
public enum DeckId {
    CIVILIAN_SHIPS,
    GALACTICA_DAMAGE,
    PEGASUS_DAMAGE,
    BASESTAR_DAMAGE,
    DESTINATION,
    POLITICS,
    LEADERSHIP,
    TACTICS,
    PILOTING,
    ENGINEERING,
    TREACHERY,
    DESTINY,
    QUORUM,
    CRISIS,
    SUPER_CRISIS,
    LOYALTY,
    LOYALTY_NOT_CYLON,
    MUTINY,
    MOTIVE;

    public static DeckId of(SkillCardColor color) {
        return switch (color) {
            case POLITICS -> POLITICS;
            case LEADERSHIP -> LEADERSHIP;
            case TACTICS -> TACTICS;
            case PILOTING -> PILOTING;
            case ENGINEERING -> ENGINEERING;
            case TREACHERY -> TREACHERY;
        };
    }
}
