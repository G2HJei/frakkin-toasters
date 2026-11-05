package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.HashSet;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DRADIS_CONTACT;

public class DiscardSkillCardsEventProcessor extends EventProcessor<DiscardSkillCardsEvent> {

    // "When a player chooses to discard this card, he draws 1 Mutiny Card."
    private static final Set<SkillCardType> DRAW_MUTINY_CARD_WHEN_CHOSEN = Set.of(BAIT, DRADIS_CONTACT);

    @Override
    protected boolean isValid() {
        return cardsInHand() && meetsDiscardLimit();
    }

    @Override
    public Followup process() {
        val cards = event.cards();
        if (cards.isEmpty()) {
            return NONE;
        }
        ops.player(player.number()).discard(cards.toArray(SkillCard[]::new));
        return drawsMutinyCard() ? single(new DrawMutinyCardEvent(player.number())) : NONE;
    }

    private boolean cardsInHand() {
        val cards = event.cards();
        return new HashSet<>(cards).size() == cards.size()
                && new HashSet<>(player.skillCards().cards()).containsAll(cards);
    }

    private boolean meetsDiscardLimit() {
        return switch (event.discardLimit()) {
            case null -> true;
            case DISCARD_EXACTLY_1 -> discardsExactly(1);
            case DISCARD_EXACTLY_2 -> discardsExactly(2);
            case DISCARD_EXACTLY_3 -> discardsExactly(3);
            case DISCARD_EXACTLY_4 -> discardsExactly(4);
            case DISCARD_EXACTLY_5 -> discardsExactly(5);
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    // A player with fewer Skill Cards than that discards all of them.
    private boolean discardsExactly(int count) {
        return event.cards().size() == Math.min(count, player.skillCards().size());
    }

    // Discarding a given number of cards is always a choice, even when the player has no other cards (rule A2.3.8.1).
    // Discarding both cards at once draws only 1 Mutiny Card.
    // todo the player draws only 1 Mutiny Card per turn this way, track it once there is per-turn state
    private boolean drawsMutinyCard() {
        return event.cards().stream()
                .map(SkillCard::type)
                .anyMatch(DRAW_MUTINY_CARD_WHEN_CHOSEN::contains);
    }
}
