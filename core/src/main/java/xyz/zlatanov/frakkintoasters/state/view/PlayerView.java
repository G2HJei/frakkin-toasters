package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR_ALT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public interface PlayerView {

    int number();

    Character character();

    int handLimit();

    DeckView<SkillCard> skillCards();

    DeckView<MotiveCard> motiveCards();

    DeckView<LoyaltyCard> loyaltyCards();

    DeckView<MutinyCard> mutinyCards();

    DeckView<SuperCrisisCard> superCrisisCards();

    /**
     * @return the Quorum Cards the player holds outside the President's hand: only the one "Popular Influence" leaves
     * him to play or discard
     */
    DeckView<QuorumCard> quorumCards();

    /**
     * The miracle tokens on his character sheet: at most 1, at most 3 for Gaius Baltar (alt.) (rule 4.3.1)
     */
    int miracleTokens();

    default boolean hasMiracleToken() {
        return miracleTokens() > 0;
    }

    default int maxMiracleTokens() {
        return character() == GAIUS_BALTAR_ALT ? 3 : 1;
    }

    /**
     * @return whether he can gain a miracle token: below his maximum, and not a revealed Cylon (rule 4.3.2). A Cylon
     * Leader can, Infiltrating or not (rule 4.3.3)
     */
    default boolean canGainMiracleToken() {
        return character() != null && !isRevealedCylon() && miracleTokens() < maxMiracleTokens();
    }

    /**
     * @return whether his character prints the ability and it is in effect: a revealed Cylon's are ignored (rule
     * 6.1.2.2), a Cylon Leader's always apply (rule 6.2.1.2)
     */
    default boolean hasAbility(CharacterAbility ability) {
        return character() != null && character() == ability.character()
                && (character().type() == CYLON_LEADER
                || !isRevealedCylon());
    }

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
