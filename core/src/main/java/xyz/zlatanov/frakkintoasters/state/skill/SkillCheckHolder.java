package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
public class SkillCheckHolder {

    private final SkillCheck      type;
    private final Deck<SkillCard> cards = new Deck<>();
}
