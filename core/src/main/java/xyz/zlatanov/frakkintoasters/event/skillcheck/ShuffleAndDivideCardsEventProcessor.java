package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

public class ShuffleAndDivideCardsEventProcessor extends EventProcessor<ShuffleAndDivideCardsEvent> {

    private static final List<SkillCardType> CARDS_WITH_SKILL_CHECK_ABILITY = List.of(
            //daybreak
            INSTALL_UPGRADES,
            ALL_HANDS_ON_DECK,
            DOGFIGHT,
            FORCE_THEIR_HAND,
            QUICK_THINKING,
            A_BETTER_MACHINE,
            BAIT,
            DRADIS_CONTACT,
            EXPLOIT_A_WEAKNESS,
            PERSONAL_VICES,
            VIOLENT_OUTBURSTS,

            //exodus
            ESTABLISH_NETWORK,
            IRON_WILL,
            PROTECT_THE_FLEET,
            RED_TAPE,
            TRUST_INSTINCTS
    );

    @Override
    public Followup process() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        return buildFollowup(cards);
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

    private Followup buildFollowup(List<SkillCard> cards) {
        val cardsWithAbilities = cards.stream()
                .filter(c -> CARDS_WITH_SKILL_CHECK_ABILITY.contains(c.type()))
                .toList();
        return cardsWithAbilities.isEmpty()
                ? Followup.NONE
                : Followup.single(new DetermineSkillCheckAbilitiesOrderEvent(game.currentPlayer(), cardsWithAbilities));
    }
}
