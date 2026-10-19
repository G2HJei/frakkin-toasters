package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.SkillCheckView;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ESTABLISH_NETWORK;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
public class SkillCheck implements SkillCheckView {

    private final SkillCheckType                type;
    private final Map<Integer, List<SkillCard>> playedBeforeSkillCheck = new LinkedHashMap<>();
    private final Deck<SkillCard>               cards                  = new Deck<>();
    private final Deck<SkillCard>               matchingPile           = new Deck<>();
    private final Deck<SkillCard>               nonMatchingPile        = new Deck<>();
    private final List<SkillCardType>           resolvedAbilities      = new ArrayList<>();

    //todo handle already resolved mechanics and removing future events when skill check ability card is removed separately
    public SkillCheck remove(SkillCard card, Integer playerNumber) {
        if (Optional.ofNullable(playedBeforeSkillCheck.get(playerNumber))
                .map(cards -> cards.contains(card))
                .orElse(false)) {
            playedBeforeSkillCheck.get(playerNumber).remove(card);
        } else if (matchingPile.cards().contains(card)) {
            matchingPile.remove(card);
        } else if (nonMatchingPile.cards().contains(card)) {
            nonMatchingPile.remove(card);
        } else {
            throw new FrakCallTheAdmiralException();
        }
        return this;
    }

    public void playedBefore(int playerNumber, List<SkillCard> cards) {
        playedBeforeSkillCheck.put(playerNumber, new ArrayList<>(cards));
    }

    @Override
    public List<SkillCard> playedBeforeSkillCheck(int playerNumber) {
        return playedBeforeSkillCheck.getOrDefault(playerNumber, List.of());
    }

    public void tieBrokenBefore(int playerNumber, SkillCard card) {
        playedBeforeSkillCheck.get(playerNumber).remove(card);
    }

    public SkillCheck resolved(SkillCardType cardType) {
        assert !resolvedAbilities.contains(cardType) : "Skill check abilities can be resolved once per skill check";
        resolvedAbilities.add(cardType);
        return this;
    }

    @Override
    public List<SkillCardType> resolvedAbilities() {
        return Collections.unmodifiableList(resolvedAbilities);
    }

    @Override
    public int cardValue(SkillCard card) {
        assert nonMatchingPile.cards().contains(card) || matchingPile.cards().contains(card) : "Card not in skill check";
        val faceValue = card.value();
        val valueDoubled = card.type().color() == ENGINEERING &&
                resolvedAbilities.contains(ESTABLISH_NETWORK);
        val valueMultiplier = valueDoubled ? 2 : 1;
        return faceValue * valueMultiplier;
    }

}
