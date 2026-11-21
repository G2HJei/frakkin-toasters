package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.DelusionalIntuitionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.character.DelusionalIntuitionEvent;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class GasCloudEventProcessor extends EventProcessor<GasCloudEvent> {

    private static final int CARDS = 3;

    @Override
    protected boolean isValid() {
        return game.playerNumber(game.admiral()) == event.playerNumber();
    }

    @Override
    public Followup process() {
        // The cards are in no pile until the Admiral places each one. Fewer are drawn when the deck has fewer left.
        val cards = new ArrayList<CrisisCard>();
        for (int i = 0; i < CARDS && game.decks().crisisCardsLeft() > 0; i++) {
            cards.add(ops.player(event.playerNumber()).drawCrisisCard());
        }
        ops.player(event.playerNumber()).see(DeckId.CRISIS, cards);
        // An Admiral with "Delusional Intuition" drew Crisis Cards: a Skill Card of any type, once for the draw.
        val intuition = !cards.isEmpty() && DelusionalIntuitionEventProcessor.canUse(game, player)
                ? single(new DelusionalIntuitionEvent(event.playerNumber()))
                : NONE;
        return allOf(intuition, single(new GasCloudPlaceEvent(event.playerNumber(), cards)));
    }
}
