package xyz.zlatanov.frakkintoasters.event.player;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Collection;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.TurnLimit.MUTINY_CARD_FROM_TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DRADIS_CONTACT;

/**
 * "When a player chooses to discard this card, he must draw 1 Mutiny Card" (rule 10.2.1). Every discard counts as
 * chosen, also of a given number of cards from a hand that holds no more, except a random one, the whole hand (rule
 * A2.3.8.1) and a card played into a skill check. Every place where a player discards cards he picked asks it.
 */
@NoArgsConstructor(access = PRIVATE)
public final class TreacheryDiscards {

    private static final Set<SkillCardType> DRAW_MUTINY_CARD_WHEN_CHOSEN = Set.of(BAIT, DRADIS_CONTACT);

    public static Followup mutinyCardFor(GameView game, GameOps ops, int playerNumber, Collection<SkillCard> discarded) {
        // Revealed Cylons never draw Mutiny Cards (rule 5.2.3.4), so they don't use up the turn's draw either.
        val draws = discarded.stream().map(SkillCard::type).anyMatch(DRAW_MUTINY_CARD_WHEN_CHOSEN::contains)
                && !game.turn().hasUsed(playerNumber, MUTINY_CARD_FROM_TREACHERY)
                && game.player(playerNumber).isHuman();
        if (!draws) {
            return NONE;
        }
        // "A player cannot draw more than one Mutiny Card as a result of discarding these Treachery Cards in a single turn."
        ops.useTurnLimit(playerNumber, MUTINY_CARD_FROM_TREACHERY);
        return single(new DrawMutinyCardEvent(playerNumber));
    }
}
