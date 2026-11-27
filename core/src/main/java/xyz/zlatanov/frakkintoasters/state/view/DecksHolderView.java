package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

public interface DecksHolderView {

    /**
     * @return how many Skill Cards of the color a player can still draw: the draw pile and the discard pile, which is
     * shuffled in when the draw pile runs out. With none left, nobody can draw that color (rule 8.2.2.5)
     */
    int skillCardsLeft(SkillCardColor color);

    int civilianShipsLeft();

    int loyaltyCardsLeft();

    DeckView<LoyaltyCard> loyalty();

    /**
     * @return the Crisis Cards in the draw pile and the discard pile, which is shuffled in when the draw pile runs out
     */
    int crisisCardsLeft();

    /**
     * @return the Quorum Cards in the draw pile and the discard pile, which is shuffled in when the draw pile runs out.
     * The others are in the President's hand, in play or out of the game
     */
    int quorumCardsLeft();

    DeckView<SkillCard> destiny();

    <T> DeckView<T> deck(DeckId id);
}
