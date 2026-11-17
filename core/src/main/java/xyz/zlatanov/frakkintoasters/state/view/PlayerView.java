package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Set;

public interface PlayerView {

    int number();

    Character character();

    int handLimit();

    DeckView<SkillCard> skillCards();

    DeckView<MotiveCard> motiveCards();

    DeckView<LoyaltyCard> loyaltyCards();

    DeckView<MutinyCard> mutinyCards();

    DeckView<SuperCrisisCard> superCrisisCards();

    boolean hasMiracleToken();

    boolean isInfiltrating();

    boolean isHuman();

    boolean isRevealedCylon();

    /**
     * Has the "You Are the Mutineer" Loyalty Card (rule 5.2).
     */
    boolean isMutineer();

    /**
     * Counts the turns this player has started with his character, the one under way included. An execution starts it
     * again for the new character. {@link TurnView#number()} counts everyone's.
     */
    int turns();

    /**
     * The civilian ships this player looked at, which only he knows. A ship that goes back into the deck is forgotten.
     */
    Set<Integer> revealedCivilianShipIds();

    /**
     * The Loyalty Cards of other players that this player looked at, which only he knows.
     */
    List<SeenLoyaltyCard> seenLoyaltyCards();
}
