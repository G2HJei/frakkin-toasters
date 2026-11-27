package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

public interface DieRollView {

    Integer value();

    /**
     * @return the Skill Cards that took effect on this roll, e.g. "Strategic Planning". A reroll keeps them
     */
    List<SkillCardType> cards();

    int modifier();

    /**
     * @return the character abilities used on this roll, e.g. a reroll by "FTL Operator". A reroll keeps them
     */
    List<CharacterAbility> abilities();

    /**
     * @return whether an ability set the face instead of the die ("Hotshot", "Longshot"), so there is no die to reroll
     */
    boolean replaced();

    /**
     * @return whether the face is the result, which nothing modifies or rerolls ("Longshot")
     */
    boolean fixed();

    /**
     * @return the face plus the modifier, treated as 1 to 8 after all of them (rule 13.1), or a fixed face as it is
     */
    int result();
}
