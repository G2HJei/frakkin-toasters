package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.character.Character;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.skill.SkillCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.character.Character.CHIEF_GALEN_TYROL;

@Getter
@Accessors(fluent = true)
public class Player {

    private int             number;
    private Character       character;
    private List<SkillCard> skillCards      = new ArrayList<>();
    private boolean         hasMiracleToken = true;

    public Player(int number) {
        this.number = number;
    }

    public Player selectCharacter(Character selection) {
        assert character == null; //todo use asserts in core to avoid throwing FrakCallTheAdmiralException(s) everywhere
        character = selection;
        return this;
    }

    public void gainSkillCards(SkillCard... cardsToAdd) {
        skillCards.addAll(Arrays.asList(cardsToAdd));
    }

    public int handLimit() {
        return character == CHIEF_GALEN_TYROL ? 8 : 10;
    }

    public void exhaustMiracleToken() {
        if (!hasMiracleToken) {
            throw new FrakCallTheAdmiralException();
        }
        hasMiracleToken = false;
    }

    public void gainMiracleToken() {
        hasMiracleToken = true;
    }

    public boolean hasMiracleToken() {
        return hasMiracleToken;
    }
}
