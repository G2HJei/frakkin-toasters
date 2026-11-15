package xyz.zlatanov.frakkintoasters.operation;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINY;

/**
 * Puts components where an engine test needs them and no op of the rules would put them. It sits in the ops' package,
 * because recording a fact is theirs alone.
 */
@NoArgsConstructor(access = PRIVATE)
public class TestOps {

    // The cards don't come out of their decks, as GiveSkillCardEvent's don't.
    public static void stackDestinyDeck(GameOps ops, List<SkillCard> topFirst) {
        ops.record(new CardsAddedOnTop(DESTINY, topFirst));
    }
}
