package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Set;


public class DiscardSkillCardsEventProcessor extends EventProcessor<DiscardSkillCardsEvent> {

    @Override
    protected boolean isValid() {
        return holdsSkillCards(event.cards()) && meetsDiscardLimit();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.cards().toArray(SkillCard[]::new));
        // Discarding a given number of cards is always a choice, even when the player has no other cards (rule A2.3.8.1).
        return TreacheryDiscards.mutinyCardFor(game, ops, player.number(), event.cards());
    }

    private boolean meetsDiscardLimit() {
        return switch (event.discardLimit()) {
            case null -> true;
            case DISCARD_EXACTLY_1 -> discardsExactly(1);
            case DISCARD_EXACTLY_2 -> discardsExactly(2);
            case DISCARD_EXACTLY_3 -> discardsExactly(3);
            case DISCARD_EXACTLY_4 -> discardsExactly(4);
            case DISCARD_EXACTLY_5 -> discardsExactly(5);
            case DISCARD_DOWN_TO_HAND_LIMIT -> player.skillCards().size() - event.cards().size() == player.handLimit();
            case DISCARD_DOWN_TO_1 -> player.skillCards().size() - event.cards().size() == 1;
            case DISCARD_DOWN_TO_3 -> player.skillCards().size() - event.cards().size() == 3;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    // A player with fewer Skill Cards than that discards all of them.
    private boolean discardsExactly(int count) {
        return event.cards().size() == Math.min(count, player.skillCards().size());
    }
}
