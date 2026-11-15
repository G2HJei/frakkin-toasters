package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

public interface DieRollView {

    /**
     * @return the face of the die, {@code null} until it is rolled. A reroll replaces it
     */
    Integer value();

    /**
     * @return the Skill Cards that took effect on this roll, e.g. "Strategic Planning". A reroll keeps them
     */
    List<SkillCardType> cards();

    /**
     * @return what the cards add to the face
     */
    int modifier();

    /**
     * @return the face plus the calculationsModifier, treated as 1 to 8 after all of them (rule 13.1)
     */
    int result();
}
