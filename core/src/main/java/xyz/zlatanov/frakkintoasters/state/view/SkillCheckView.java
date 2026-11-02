package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.*;

import java.util.List;

public interface SkillCheckView {
    SkillCheckType type();

    /**
     * The player chosen by the location action that started this skill check (Administration, Admiral's Quarters,
     * Airlock), {@code null} otherwise.
     */
    Integer targetPlayerNumber();

    DeckView<SkillCard> cards();

    DeckView<SkillCard> matchingPile();

    DeckView<SkillCard> nonMatchingPile();

    boolean playedBeforeRevealed();

    List<SkillCard> playedBeforeSkillCheck();

    List<SkillCard> playedBeforeSkillCheck(int playerNumber);

    List<List<PlayedCard>> playedBeforeConflicts();

    List<SkillCardType> resolvedAbilities();

    int cardValue(SkillCard card);

    boolean emergencyDeclared();

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
