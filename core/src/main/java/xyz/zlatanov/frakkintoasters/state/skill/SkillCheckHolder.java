package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
public class SkillCheckHolder {

    private final SkillCheck         type;
    private final Deck<SkillCard>    cards             = new Deck<>();
    private final List<SkillCard>    matchingPile      = new ArrayList<>();
    private final List<SkillCard>    nonMatchingPile   = new ArrayList<>();
    private final Set<SkillCardType> resolvedAbilities = EnumSet.noneOf(SkillCardType.class);

    //todo handle already resolved mechanics and removing future events when skill check ability card is removed separately
    public SkillCheckHolder remove(SkillCard card) {
        val removedFromMatching = matchingPile.remove(card);
        val removedFromNonMatching = nonMatchingPile.remove(card);
        assert removedFromMatching || removedFromNonMatching;
        return this;
    }
}
