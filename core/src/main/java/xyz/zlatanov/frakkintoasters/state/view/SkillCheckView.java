package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;

public interface SkillCheckView {
    SkillCheckType type();

    DeckView<SkillCard> cards();

    DeckView<SkillCard> matchingPile();

    DeckView<SkillCard> nonMatchingPile();

    List<SkillCard> playedBeforeSkillCheck(int playerNumber);

    List<SkillCardType> resolvedAbilities();

    int cardValue(SkillCard card);
}
