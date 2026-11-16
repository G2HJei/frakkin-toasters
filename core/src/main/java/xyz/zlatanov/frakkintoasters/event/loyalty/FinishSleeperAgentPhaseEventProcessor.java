package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class FinishSleeperAgentPhaseEventProcessor extends EventProcessor<FinishSleeperAgentPhaseEvent> {

    /**
     * With 7 or more distance traveled, a Cylon who is revealed or executed keeps his excess Loyalty Cards (rule
     * 6.1.1). The Mutineer card is always passed on.
     */
    public static final int NO_HAND_OFF_DISTANCE = 7;

    public static boolean canHandOffLoyaltyCards(GameView game) {
        return game.distanceTraveled() < NO_HAND_OFF_DISTANCE;
    }

    @Override
    public Followup process() {
        val followups = new ArrayList<Followup>();
        for (val cylon : playersFrom(game, game.turn().currentPlayerNumber())) {
            // Cylon Leaders get Motive Cards, not Loyalty Cards.
            if (cylon.character().type() == CYLON_LEADER || !cylon.isRevealedCylon()) {
                continue;
            }
            if (cylon.loyaltyCards().cards().contains(MUTINEER)) {
                followups.add(toAHuman(cylon, human -> new PassMutineerCardEvent(cylon.number(), human, true)));
            }
            val hasCardsToGive = cylon.loyaltyCards().cards().stream().anyMatch(card -> card != MUTINEER);
            if (hasCardsToGive && canHandOffLoyaltyCards(game)) {
                followups.add(toAHuman(cylon, human -> new GiveLoyaltyCardsEvent(cylon.number(), human)));
            }
        }
        // "If the 'You Are the Mutineer' card was included in the Loyalty deck and was not revealed, the current player
        // chooses a human player to draw an additional card from the Loyalty deck."
        if (game.decks().loyalty().cards().contains(MUTINEER)) {
            val current = game.turn().currentPlayerNumber();
            followups.add(toAHuman(game.currentPlayer(), human -> new ChooseExtraLoyaltyCardEvent(current, human)));
        }
        return all(followups.toArray(Followup[]::new));
    }

    private Followup toAHuman(PlayerView chooser, java.util.function.IntFunction<Event> event) {
        return one(playersFrom(game, chooser.number()).stream()
                .filter(PlayerView::isHuman)
                .map(human -> event.apply(human.number()))
                .toArray(Event[]::new));
    }
}
