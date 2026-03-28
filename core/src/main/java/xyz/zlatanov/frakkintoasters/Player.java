package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.skill.SkillCardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.Character.CHIEF_GALEN_TYROL;

public class Player {

    private final Character           character;
    private final List<SkillCardType> skillCardTypes  = new ArrayList<>();
    private       boolean             hasMiracleToken = true;

    public Player(Character character) {
        this.character = character;
    }

    public void addSkillCards(SkillCardType... skillCardType) {
        skillCardTypes.addAll(Arrays.stream(skillCardType).toList());
    }

    public List<SkillCardType> skillCards() {
        return skillCardTypes.stream().toList();
    }

    public int handLimit() {
        return character == CHIEF_GALEN_TYROL ? 8 : 10;
    }

    public void exhaustMiracleToken() {
        hasMiracleToken = false;
    }

    public void gainMiracleToken() {
        hasMiracleToken = true;
    }

    public boolean hasMiracleToken() {
        return hasMiracleToken;
    }
}
