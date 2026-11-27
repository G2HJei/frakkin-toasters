package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.optional;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;

/**
 * The powers of the "You Are a Cylon" cards, used as the card is revealed outside the "Brig" (rule 6.1). Every option
 * names the revealer, who chooses. All of them are "may", but "Draw Treachery Cards".
 */
@NoArgsConstructor(access = PRIVATE)
public final class CylonRevealPowers {

    public static Followup power(GameView game, int revealer, LoyaltyCard card) {
        return switch (card) {
            case CYLON_CAN_PLACE_CENTURION -> optional(revealer, new CylonPowerPlaceCenturionEvent(revealer));
            case CYLON_DAMAGE_GALACTICA -> game.decks().deck(DeckId.GALACTICA_DAMAGE).isEmpty()
                    ? NONE
                    : optional(revealer, new CylonPowerDamageGalacticaEvent(revealer));
            case CYLON_DECREASE_JUMP_TRACK -> optional(revealer, new CylonPowerDecreaseJumpTrackEvent(revealer));
            case CYLON_DRAW_TREACHERY_CARDS -> single(new CylonPowerDrawTreacheryEvent(revealer));
            case CYLON_REDUCE_MORALE -> optional(revealer, new CylonPowerReduceMoraleEvent(revealer));
            case CYLON_SEND_TO_BRIG -> targets(game, revealer,
                    target -> CylonPowerSendToBrigEventProcessor.canTarget(game, target),
                    target -> new CylonPowerSendToBrigEvent(revealer, target));
            case CYLON_SEND_TO_SICKBAY -> targets(game, revealer,
                    target -> CylonPowerSendToSickbayEventProcessor.canTarget(game, target),
                    target -> new CylonPowerSendToSickbayEvent(revealer, target));
            default -> throw new FrakCallTheAdmiralException(card + " has no reveal power");
        };
    }

    public static boolean isOnGalactica(GameView game, PlayerView player) {
        return game.locate(player.character()).filter(LocationsArea.GALACTICA.locations()::contains).isPresent();
    }

    private static Followup targets(GameView game, int revealer, IntPredicate canTarget, IntFunction<Event> event) {
        val options = new ArrayList<Event>();
        playersFrom(game, revealer).stream()
                .map(PlayerView::number)
                .filter(canTarget::test)
                .forEach(target -> options.add(event.apply(target)));
        if (options.isEmpty()) {
            return NONE;
        }
        options.add(new NoOpEvent(revealer));
        return one(options.toArray(Event[]::new));
    }
}
