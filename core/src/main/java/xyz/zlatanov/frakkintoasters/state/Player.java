package xyz.zlatanov.frakkintoasters.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

@RequiredArgsConstructor
@Getter //todo replace getters with explicit methods for View interface usage to ensure no modification is possible
@Accessors(fluent = true)
public class Player implements PlayerView {

    private final int                   number;
    private       Character             character;
    private final Deck<SkillCard>       skillCards              = new Deck<>();
    private final Deck<MotiveCard>      motiveCards             = new Deck<>();
    private final Deck<LoyaltyCard>     loyaltyCards            = new Deck<>();
    private final Deck<MutinyCard>      mutinyCards             = new Deck<>();
    private final Deck<SuperCrisisCard> superCrisisCards        = new Deck<>();
    private       boolean               hasMiracleToken         = true;
    private       boolean               isInfiltrating          = false;
    private final Set<Integer>          revealedCivilianShipIds = new LinkedHashSet<>();
    private       int                   turns                   = 0;

    @SuppressWarnings("unchecked")
    public <T> Deck<T> pile(PlayerPile pile) {
        return (Deck<T>) switch (pile) {
            case SKILL -> skillCards;
            case MOTIVE -> motiveCards;
            case LOYALTY -> loyaltyCards;
            case MUTINY -> mutinyCards;
            case SUPER_CRISIS -> superCrisisCards;
        };
    }

    @Mutates
    public Player character(Character selection) {
        assert character == null; //todo use asserts in core to avoid throwing FrakCallTheAdmiralException(s) everywhere
        character = selection;
        return this;
    }

    @Mutates
    public void gainSkillCards(SkillCard... cardsToAdd) {
        skillCards.addOnTop(Arrays.asList(cardsToAdd));
    }

    @Mutates
    public void countTurn() {
        turns++;
    }

    @Override
    public int handLimit() {
        return character == CHIEF_GALEN_TYROL ? 8 : 10;
    }

    @Mutates
    public void exhaustMiracleToken() {
        if (!hasMiracleToken) {
            throw new FrakCallTheAdmiralException();
        }
        hasMiracleToken = false;
    }

    @Mutates
    public void gainMiracleToken() {
        hasMiracleToken = true;
    }

    @Override
    public boolean hasMiracleToken() {
        return hasMiracleToken;
    }

    @Override
    public boolean isHuman() {
        val isCylonLeader = character.type() == CYLON_LEADER;
        return isCylonLeader && isInfiltrating ||
                !isCylonLeader && !isRevealedCylon();
    }

    //todo review when a cylon leader is revealed cylon and when a human and updathe the methods if needed
    @Override
    public boolean isRevealedCylon() {
        return loyaltyCards.revealedCards()
                .stream()
                .anyMatch(LoyaltyCard::isCylon);
    }

    @Mutates
    public void infiltrateGalactica() {
        assert character.type() == CYLON_LEADER;
        isInfiltrating = true;
    }

    @Mutates
    public void endInfiltration() {
        assert character.type() == CYLON_LEADER;
        isInfiltrating = false;
    }
}
