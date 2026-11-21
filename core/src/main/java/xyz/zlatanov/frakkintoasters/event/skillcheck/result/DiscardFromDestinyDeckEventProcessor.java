package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class DiscardFromDestinyDeckEventProcessor extends EventProcessor<DiscardFromDestinyDeckEvent> {

    private static final int                  DISCARDED = 2;
    private static final Comparator<SkillCard> BY_CARD  = Comparator.comparing(SkillCard::type).thenComparing(SkillCard::value);

    /**
     * @return a discard for every distinct pair of the Destiny cards, or the whole deck when it holds fewer. The player
     * looked at the deck, so the cards are his options: only he sees them, and the browser doesn't know the deck
     */
    public static List<DiscardFromDestinyDeckEvent> options(GameView game, int playerNumber) {
        val cards = game.decks().destiny().cards();
        if (cards.size() <= DISCARDED) {
            return List.of(new DiscardFromDestinyDeckEvent(playerNumber, cards));
        }
        // Equal cards are copies, so a pair of them is one option.
        val pairs = new LinkedHashSet<List<SkillCard>>();
        for (int i = 0; i < cards.size(); i++) {
            for (int j = i + 1; j < cards.size(); j++) {
                pairs.add(new ArrayList<>(List.of(cards.get(i), cards.get(j))).stream().sorted(BY_CARD).toList());
            }
        }
        return pairs.stream().map(pair -> new DiscardFromDestinyDeckEvent(playerNumber, pair)).toList();
    }

    // Exactly 2 cards, or the whole deck if it has fewer. Equal cards are copies, so each takes one out of the deck.
    @Override
    protected boolean isValid() {
        val destiny = game.decks().destiny();
        val remaining = new ArrayList<>(destiny.cards());
        return event.cards() != null
                && event.cards().size() == Math.min(DISCARDED, destiny.size())
                && event.cards().stream().allMatch(remaining::remove);
    }

    @Override
    public Followup process() {
        ops.skillCheck().discardFromDestinyDeck(event.cards());
        return Followup.NONE;
    }
}
