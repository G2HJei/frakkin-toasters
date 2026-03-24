package xyz.zlatanov.frakkintoasters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.Character.CHIEF_GALEN_TYROL;

public class Player {

    private final Character character;
    private final List<SkillCard> skillCards = new ArrayList<>();

    public Player(Character character) {
        this.character = character;
    }

    public void addSkillCards(SkillCard... skillCard) {
        skillCards.addAll(Arrays.stream(skillCard).toList());
    }

    public List<SkillCard> skillCards() {
        return skillCards.stream().toList();
    }

    public int handLimit() {
        return character == CHIEF_GALEN_TYROL ? 8 : 10;
    }
}
