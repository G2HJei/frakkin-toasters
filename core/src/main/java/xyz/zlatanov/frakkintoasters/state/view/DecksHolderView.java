package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

public interface DecksHolderView {

    /**
     * @return how many Skill Cards of the color a player can still draw: the draw pile and the discard pile, which is
     * shuffled in when the draw pile runs out. With none left, nobody can draw that color (rule 8.2.2.5)
     */
    int skillCardsLeft(SkillCardColor color);

    int civilianShipsLeft();
}
