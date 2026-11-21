package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardAtBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardOnTopEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RAPTOR_PILOT;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

public class RaptorPilotEventProcessor extends EventProcessor<RaptorPilotEvent> {

    @Override
    protected boolean isValid() {
        return player.hasAbility(RAPTOR_PILOT) && (event.deck() == CRISIS || event.deck() == DESTINATION);
    }

    // Only he learns the cards, so they are a CardsSeen until they go back.
    @Override
    public Followup process() {
        int n = player.number();
        val playerOps = ops.player(n);
        playerOps.useAbility(RAPTOR_PILOT);
        if (event.deck() == CRISIS) {
            val cards = List.of(playerOps.drawCrisisCard(), playerOps.drawCrisisCard());
            playerOps.see(CRISIS, cards);
            return placeEach(cards, card -> new PlaceCrisisCardOnTopEvent(n, card),
                    card -> new PlaceCrisisCardOnBottomEvent(n, card));
        }
        val cards = List.of(playerOps.drawDestinationCard(), playerOps.drawDestinationCard());
        playerOps.see(DESTINATION, cards);
        return placeEach(cards, card -> new PlaceDestinationCardOnTopEvent(n, card),
                card -> new PlaceDestinationCardAtBottomEvent(n, card));
    }

    // He picks the card to put back first and where, then the other: the one put on top last is the top card.
    private static <T> Followup placeEach(List<T> cards, Function<T, Event> onTop, Function<T, Event> atBottom) {
        val options = new ArrayList<Followup>();
        for (int first = 0; first < cards.size(); first++) {
            val card = cards.get(first);
            val other = cards.get(1 - first);
            val placeOther = one(onTop.apply(other), atBottom.apply(other));
            options.add(all(single(onTop.apply(card)), placeOther));
            options.add(all(single(atBottom.apply(card)), placeOther));
        }
        return one(options.toArray(Followup[]::new));
    }
}
