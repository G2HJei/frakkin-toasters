package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.discardDownTo;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.HAND;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;

/**
 * The Skill Card discards a player is made to do by a card or an ability: not a cost he chooses to pay, nor the discard
 * down to his hand limit. Sharon "Athena" Agathon's "For Love" can make them smaller ({@link ForLoveEventProcessor}).
 */
@NoArgsConstructor(access = PRIVATE)
public final class ForcedDiscards {

    /**
     * @return the discard, through a {@link ForcedDiscardEvent} in games where another player has "For Love", so she is
     * asked when the discard happens. Games without her are left alone
     */
    public static Event discard(GameView game, int playerNumber, DiscardKind kind, int count) {
        val discard = CharacterPenalties.discardKind(game, playerNumber, kind); // "Headstrong"
        return ForLoveEventProcessor.mayBeOffered(game, playerNumber)
                ? new ForcedDiscardEvent(playerNumber, discard, count, false)
                : plain(playerNumber, discard, count);
    }

    public static Event discard(GameView game, int playerNumber, int count) {
        return discard(game, playerNumber, CHOSEN, count);
    }

    public static Event discardRandom(GameView game, int playerNumber, int count) {
        return discard(game, playerNumber, RANDOM, count);
    }

    public static Event plain(int playerNumber, DiscardKind kind, int count) {
        return switch (kind) {
            case CHOSEN -> FollowupUtils.discard(playerNumber, count);
            case RANDOM -> new DiscardRandomSkillCardsEvent(playerNumber, count);
            case HAND -> new DiscardAllSkillCardsEvent(playerNumber);
        };
    }

    /**
     * @return the discard 1 card smaller, counted from the hand he holds now: nothing when that leaves none, and a whole
     * hand becomes "keep 1". What is left is still forced, so another ability may make it smaller again
     */
    public static Followup oneFewer(GameView game, int playerNumber, DiscardKind kind, int count) {
        int smaller = cardsTaken(game, playerNumber, kind, count) - 1;
        if (smaller <= 0) {
            return NONE;
        }
        if (kind == HAND) {
            return single(new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, discardDownTo(1)));
        }
        return single(discard(game, playerNumber, kind, smaller));
    }

    static int cardsTaken(GameView game, int playerNumber, DiscardKind kind, int count) {
        int hand = game.player(playerNumber).skillCards().size();
        return kind == HAND ? hand : Math.min(count, hand);
    }
}
