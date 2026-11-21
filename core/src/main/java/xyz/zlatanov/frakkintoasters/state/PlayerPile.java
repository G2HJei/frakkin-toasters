package xyz.zlatanov.frakkintoasters.state;

import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public enum PlayerPile {
    SKILL,
    MOTIVE,
    LOYALTY,
    MUTINY,
    SUPER_CRISIS,
    QUORUM; // a Quorum Card outside the President's hand, which "Popular Influence" leaves its player to play

    /**
     * @return the type of the cards in the pile, so the cards of a fact about it can be read back
     */
    public Class<?> cardType() {
        return switch (this) {
            case SKILL -> SkillCard.class;
            case MOTIVE -> MotiveCard.class;
            case LOYALTY -> LoyaltyCard.class;
            case MUTINY -> MutinyCard.class;
            case SUPER_CRISIS -> SuperCrisisCard.class;
            case QUORUM -> QuorumCard.class;
        };
    }
}
