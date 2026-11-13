package xyz.zlatanov.frakkintoasters.state.skill;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.SkillCheckView;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ESTABLISH_NETWORK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
public class SkillCheck implements SkillCheckView {

    private final SkillCheckType                type;
    private       Integer                       targetPlayerNumber;
    private final Map<Integer, List<SkillCard>> playedBeforeSkillCheck = new LinkedHashMap<>();
    private       boolean                       playedBeforeRevealed   = false;
    private final Deck<SkillCard>               cards                  = new Deck<>();
    private final Deck<SkillCard>               matchingPile           = new Deck<>();
    private final Deck<SkillCard>               nonMatchingPile        = new Deck<>();
    private final List<SkillCardType>           resolvedAbilities      = new ArrayList<>();
    private       boolean                       emergencyDeclared      = false;
    private       Integer                       totalStrength;
    private       SkillCheckResult              result;
    private       boolean                       passResultReplaced     = false; //by change of plans
    private       boolean                       failResultPrevented    = false; //by iron will

    public Deck<SkillCard> pile(SkillCheckPile pile) {
        return switch (pile) {
            case CARDS -> cards;
            case MATCHING -> matchingPile;
            case NON_MATCHING -> nonMatchingPile;
        };
    }

    @Override
    public List<SkillCard> dividedCards() {
        return Stream.concat(matchingPile.cards().stream(), nonMatchingPile.cards().stream()).toList();
    }

    @Mutates
    public SkillCheck targetPlayerNumber(int targetPlayerNumber) {
        assert this.targetPlayerNumber == null : "Skill check target already chosen";
        this.targetPlayerNumber = targetPlayerNumber;
        return this;
    }

    //todo handle already resolved mechanics and removing future events when skill check ability card is removed separately
    @Mutates
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

    @Mutates
    public SkillCheck playedBefore(int playerNumber, List<SkillCard> cards) {
        playedBeforeSkillCheck.put(playerNumber, new ArrayList<>(cards));
        return this;
    }

    @Mutates
    public void revealPlayedBefore() {
        playedBeforeRevealed = true;
    }

    @Override
    public boolean playedBeforeRevealed() {
        return playedBeforeRevealed;
    }

    @Override
    public List<SkillCard> playedBeforeSkillCheck() {
        return playedBeforeSkillCheck.values()
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public List<SkillCard> playedBeforeSkillCheck(int playerNumber) {
        return List.copyOf(playedBeforeSkillCheck.getOrDefault(playerNumber, List.of()));
    }

    @Override
    public List<List<PlayedCard>> playedBeforeConflicts() {
        val playedCards = playedBeforeSkillCheck.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(c -> new PlayedCard(e.getKey(), c)))
                .toList();
        val recklessAndRestoreOrderCards = playedCards.stream()
                .filter(c -> c.skillCard().type().recklessOrRestoreOrder())
                .toList();
        val sameTypeCards = playedCards.stream()
                .filter(c -> !c.skillCard().type().recklessOrRestoreOrder())
                .collect(groupingBy(c -> c.skillCard().type()))
                .values();
        return Stream.concat(Stream.of(recklessAndRestoreOrderCards), sameTypeCards.stream())
                .filter(group -> group.size() > 1)
                .toList();
    }

    @Mutates
    public SkillCheck resolved(SkillCardType cardType) {
        assert !resolvedAbilities.contains(cardType) : "Skill check abilities can be resolved once per skill check";
        resolvedAbilities.add(cardType);
        return this;
    }

    @Override
    public List<SkillCardType> resolvedAbilities() {
        return List.copyOf(resolvedAbilities);
    }

    @Mutates
    public void declareEmergency() {
        assert !emergencyDeclared : "Limit of 1 Declare Emergency per skill check";
        emergencyDeclared = true;
    }

    @Mutates
    public SkillCheck totalStrength(int totalStrength) {
        assert this.totalStrength == null : "Skill check strength already totaled";
        this.totalStrength = totalStrength;
        return this;
    }

    @Mutates
    public SkillCheck result(SkillCheckResult result) {
        assert this.result == null : "Skill check result already determined";
        this.result = result;
        return this;
    }

    @Mutates
    public SkillCheck replacePassResult() {
        assert result == PASS && !passResultReplaced : "Only a pending pass result can be replaced";
        passResultReplaced = true;
        return this;
    }

    @Mutates
    public SkillCheck preventFailResult() {
        assert result == FAIL && !failResultPrevented : "Only a pending fail result can be prevented";
        failResultPrevented = true;
        return this;
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
