package xyz.zlatanov.frakkintoasters.state.deck;

import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

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

    /**
     * @return the type of the cards in the deck, so the cards of a fact about it can be read back
     */
    public Class<?> cardType() {
        return switch (this) {
            case CIVILIAN_SHIPS -> CivilianShip.class;
            case GALACTICA_DAMAGE -> GalacticaDamage.class;
            case PEGASUS_DAMAGE -> PegasusDamage.class;
            case BASESTAR_DAMAGE -> BasestarDamage.class;
            case DESTINATION -> DestinationCard.class;
            case POLITICS, LEADERSHIP, TACTICS, PILOTING, ENGINEERING, TREACHERY, DESTINY -> SkillCard.class;
            case QUORUM -> QuorumCard.class;
            case CRISIS -> CrisisCard.class;
            case SUPER_CRISIS -> SuperCrisisCard.class;
            case LOYALTY, LOYALTY_NOT_CYLON -> LoyaltyCard.class;
            case MUTINY -> MutinyCard.class;
            case MOTIVE -> MotiveCard.class;
        };
    }

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
