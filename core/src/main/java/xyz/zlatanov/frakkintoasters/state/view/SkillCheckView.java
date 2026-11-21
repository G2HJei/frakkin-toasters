package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.*;

import java.util.List;

public interface SkillCheckView {
    SkillCheckType type();

    /**
     * The player the location action that started this skill check is about: the character chosen at the "Admiral's
     * Quarters" or the "Airlock", or the one in the "Brig". {@code null} for other skill checks.
     */
    Integer targetPlayerNumber();

    /**
     * The revealed Cylon who played the Super Crisis Card of this skill check at "Caprica", e.g. for "Human Prisoner".
     * {@code null} for other skill checks.
     */
    Integer playedBy();

    DeckView<SkillCard> cards();

    DeckView<SkillCard> matchingPile();

    DeckView<SkillCard> nonMatchingPile();

    /**
     * The cards of the matching pile, then those of the non-matching pile, once the cards are shuffled and divided.
     */
    List<SkillCard> dividedCards();

    boolean playedBeforeRevealed();

    List<SkillCard> playedBeforeSkillCheck();

    List<SkillCard> playedBeforeSkillCheck(int playerNumber);

    List<List<PlayedCard>> playedBeforeConflicts();

    /**
     * "Investigative Committee": the cards are played into this check face up, except the Destiny cards. It was played
     * before the check, and those cards stay with it until it ends.
     */
    default boolean investigated() {
        return playedBeforeRevealed() && playedBeforeSkillCheck().stream()
                .anyMatch(card -> card.type() == SkillCardType.INVESTIGATIVE_COMMITTEE);
    }

    /**
     * The cards the player put into an investigated check, which everyone sees.
     */
    List<SkillCard> shownCards(int playerNumber);

    List<SkillCardType> resolvedAbilities();

    int cardValue(SkillCard card);

    boolean emergencyDeclared();

    /**
     * @return what the cards in play changed the difficulty by, e.g. "Assign Arbitrator" or "Accept Prophecy"
     */
    int difficultyAdjustment();

    /**
     * @return whether the Politics Cards in the check count positive, after "Assign Chief of Staff"
     */
    boolean politicsCountPositive();

    /**
     * @return whether the cards were shuffled and divided into their piles. Before that, a card put into the check goes
     * face down onto the pile of cards
     */
    boolean cardsDivided();

    /**
     * @return the color whose cards count as strength 0 in the check after "Blind Devotion", {@code null} when none
     */
    SkillCardColor zeroStrengthColor();

    /**
     * The matching pile strength minus the non-matching pile strength, {@code null} until the strength is totaled.
     */
    Integer totalStrength();

    SkillCheckResult result();

    /**
     * Change of Plans was played, so the pass result is not resolved. The skill check still counts as passed.
     */
    boolean passResultReplaced();

    /**
     * Iron Will was resolved and the total strength was within 4 of the difficulty, so the fail result is not resolved.
     * The skill check still counts as failed.
     */
    boolean failResultPrevented();
}
