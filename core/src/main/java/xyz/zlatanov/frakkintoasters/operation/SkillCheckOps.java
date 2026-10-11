package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class SkillCheckOps extends AbstractOps {

    SkillCheckOps(Game game) {
        super(game);
    }

    public void start(SkillCheck skillCheck) {
        game.startSkillCheck(skillCheck);
    }

    public void addCards(List<SkillCard> cards) {
        game.activeSkillCheck().cards().addOnTop(cards);
    }

    public void addFromDestinyDeck(int count) { //todo always 2? check rules
        val destinyDeck = game.decks().destiny();
        val cards = destinyDeck.draw(count);
        if (destinyDeck.isEmpty()) {
            new DestinyDeckOps(game).create();
        }
        addCards(cards);
    }

    /**
     * Sorts the played cards and splits them into matching and non-matching piles.
     *
     * @return all cards, sorted by color, value and type
     */
    public List<SkillCard> divideCards() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        return List.copyOf(cards);
    }

    public void discard(SkillCard card) {
        game.activeSkillCheck().remove(card);
        game.decks().discard(card);
    }

    private List<SkillCard> drawAllAndSort() {
        val deck = game.activeSkillCheck().cards();
        val all = new ArrayList<>(deck.draw(deck.size()));
        all.sort(comparing((SkillCard card) -> card.type().color())
                .thenComparing(SkillCard::value, Comparator.reverseOrder())
                .thenComparing(SkillCard::type, Comparator.reverseOrder()));
        return all;
    }

    private void splitIntoPiles(List<SkillCard> sortedCards) {
        val skillCheck = game.activeSkillCheck();
        val matchingColors = skillCheck.type().skillCheckColors();
        for (val card : sortedCards) {
            val cardMatchesSkillCheck = matchingColors.contains(card.type().color());
            if (cardMatchesSkillCheck) {
                skillCheck.matchingPile().add(card);
            } else {
                skillCheck.nonMatchingPile().add(card);
            }
        }
    }
}
