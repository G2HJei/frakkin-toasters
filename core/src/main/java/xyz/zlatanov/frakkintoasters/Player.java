package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;

@Getter
@Accessors(fluent = true)
public class Player {

    private       Character         character;
    private final List<SkillCard>   skillCards      = new ArrayList<>();
    private final List<MotiveCard>  motiveCards     = new ArrayList<>();
    private final List<LoyaltyCard> loyaltyCards    = new ArrayList<>();
    private       boolean           hasMiracleToken = true;

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
