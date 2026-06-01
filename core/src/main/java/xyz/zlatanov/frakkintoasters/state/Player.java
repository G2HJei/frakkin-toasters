package xyz.zlatanov.frakkintoasters.state;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

@Getter
@Accessors(fluent = true)
public class Player {

    private       Character             character;
    private final Deck<SkillCard>       skillCards       = new Deck<>();
    private final Deck<MotiveCard>      motiveCards      = new Deck<>();
    private final Deck<LoyaltyCard>     loyaltyCards     = new Deck<>();
    private final Deck<MutinyCard>      mutinyCards      = new Deck<>();
    private final Deck<SuperCrisisCard> superCrisisCards = new Deck<>();
    private       boolean               hasMiracleToken  = true;
    private       boolean               isInfiltrating   = false;

    public Player selectCharacter(Character selection) {
        assert character == null; //todo use asserts in core to avoid throwing FrakCallTheAdmiralException(s) everywhere
        character = selection;
        return this;
    }

    public void gainSkillCards(SkillCard... cardsToAdd) {
        skillCards.addOnTop(Arrays.asList(cardsToAdd));
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

    public boolean isHuman() {
        val isCylonLeader = character.type() == CYLON_LEADER;
        val hasNotRevealedCylonLoyalty = loyaltyCards.revealedCards()
                .stream()
                .noneMatch(LoyaltyCard::isCylon);
        return isCylonLeader && isInfiltrating
                || hasNotRevealedCylonLoyalty;
    }

    public void infiltrateGalactica() {
        assert character.type() == CYLON_LEADER;
        isInfiltrating = true;
    }

    public void endInfiltration() {
        assert character.type() == CYLON_LEADER;
        isInfiltrating = false;
    }
}
