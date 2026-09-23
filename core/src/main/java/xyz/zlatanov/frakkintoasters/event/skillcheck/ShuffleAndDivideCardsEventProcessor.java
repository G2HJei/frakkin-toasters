package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class ShuffleAndDivideCardsEventProcessor extends EventProcessor<ShuffleAndDivideCardsEvent> {

    @Override
    public Followup process() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        val cardsWithAbility = getCardsWithSkillCheckAbility(cards);
        return Followup.NONE;
    }

    private List<SkillCard> getCardsWithSkillCheckAbility(List<SkillCard> cards) {
        return null;
    }

    private ArrayList<SkillCard> drawAllAndSort() {
        val deck = game.activeSkillCheck().cards();
        val all = new ArrayList<>(deck.draw(deck.size()));
        all.sort(comparing((SkillCard card) -> card.type().color())
                .thenComparing(SkillCard::value, Comparator.reverseOrder())
                .thenComparing(SkillCard::type, Comparator.reverseOrder()));
        return all;
    }

    private void splitIntoPiles(ArrayList<SkillCard> sortedCards) {
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
