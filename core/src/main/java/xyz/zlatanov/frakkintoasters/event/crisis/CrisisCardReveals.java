package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.DelusionalIntuitionEvent;
import xyz.zlatanov.frakkintoasters.event.character.DelusionalIntuitionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.character.SecretDestinyEvent;
import xyz.zlatanov.frakkintoasters.event.character.SecretDestinyEventProcessor;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

/**
 * What happens between drawing a Crisis Card and resolving it: the abilities that act on a draw or on a revealed card.
 * The card is already in the turn ({@code GameOps.playCrisisCard}).
 */
@NoArgsConstructor(access = PRIVATE)
public final class CrisisCardReveals {

    /**
     * @return "Delusional Intuition" when the player who drew the card has it, then {@link #revealed}
     */
    public static Followup drawn(GameView game, CrisisCard card, int drawerPlayerNumber) {
        val intuition = DelusionalIntuitionEventProcessor.canUse(game, game.player(drawerPlayerNumber))
                ? single(new DelusionalIntuitionEvent(drawerPlayerNumber))
                : NONE;
        return allOf(intuition, revealed(game, card));
    }

    /**
     * @return the card's resolution, or "Secret Destiny" instead. The resolution names nobody, so the choice is the
     * player's who can use the ability
     */
    public static Followup revealed(GameView game, CrisisCard card) {
        val resolve = new ResolveCrisisCardEvent(card);
        return game.players().stream()
                .filter(player -> SecretDestinyEventProcessor.canUse(game, player))
                .findFirst()
                .map(starbuck -> one(new SecretDestinyEvent(starbuck.number(), card), resolve))
                .orElse(single(resolve));
    }
}
