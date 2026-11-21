package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.function.IntFunction;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;

public class FinishCylonRevealEventProcessor extends EventProcessor<FinishCylonRevealEvent> {

    @Override
    protected boolean isValid() {
        return player.isRevealedCylon();
    }

    @Override
    public Followup process() {
        val playerNumber = player.number();
        // 3. "Resurrect": a viper he pilots goes back to the "Reserves".
        ops.player(playerNumber).sendTo(RESURRECTION_SHIP);
        // 4. "Receive Super Crisis", unless he was executed.
        val superCrisisDeck = game.decks().deck(DeckId.SUPER_CRISIS);
        if (event.drawsSuperCrisisCard() && superCrisisDeck.size() + superCrisisDeck.discardSize() > 0) {
            ops.player(playerNumber).drawSuperCrisisCard();
        }
        // 7. "End Turn", if it is his. An execution has ended it already.
        if (playerNumber == game.turn().currentPlayerNumber() && !game.turn().endedEarly()) {
            ops.endTurnEarly();
        }
        // 5. From 7 distance on, he keeps his other Loyalty Cards.
        val giveCards = player.loyaltyCards().cards().stream().anyMatch(card -> card != MUTINEER)
                && FinishSleeperAgentPhaseEventProcessor.canHandOffLoyaltyCards(game)
                ? toAHuman(human -> new GiveLoyaltyCardsEvent(playerNumber, human))
                : NONE;
        // 6. The Mutineer card goes to a human whatever the distance.
        val passMutineer = player.isMutineer()
                ? toAHuman(human -> new PassMutineerCardEvent(playerNumber, human))
                : NONE;
        // 2. "Lose Titles" comes first, so a human who gets the Mutineer card passes on the titles he got.
        return allOf(single(new GiveTitlesToNextInLineEvent(playerNumber)), giveCards, passMutineer);
    }

    private Followup toAHuman(IntFunction<Event> event) {
        return one(playersFrom(game, player.number()).stream()
                .filter(PlayerView::isHuman)
                .map(human -> event.apply(human.number()))
                .toArray(Event[]::new));
    }
}
