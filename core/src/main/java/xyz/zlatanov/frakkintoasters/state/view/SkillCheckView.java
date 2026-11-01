package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.*;

import java.util.List;

public interface SkillCheckView {
    SkillCheckType type();

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

    SkillCheckResult result();

    /**
     * Change of Plans was played, so the pass result is not resolved. The skill check still counts as passed.
     */
    boolean passResultReplaced();
}
