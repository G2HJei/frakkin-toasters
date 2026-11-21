package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_MISSION_SPECIALIST;

public class DrawDestinationCardsEventProcessor extends EventProcessor<DrawDestinationCardsEvent> {

    private static final int ADMIRAL_CARDS            = 2;
    private static final int MISSION_SPECIALIST_CARDS = 3;
    private static final int BLIND_JUMP_CARDS         = 1;

    // The Admiral draws 2 cards, or the Mission Specialist 3 instead, whose card is then used up. After a "Blind Jump"
    // the Admiral draws only 1, and the Mission Specialist's card stays for the next jump.
    @Override
    public Followup process() {
        if (event.blindJump()) {
            // One card leaves nothing to choose: the fleet goes there.
            val card = ops.galactica().drawDestinationCards(BLIND_JUMP_CARDS).getFirst();
            return single(new ResolveDestinationCardEvent(card));
        }
        val specialist = game.quorumCardHolder(ASSIGN_MISSION_SPECIALIST);
        val chooser = specialist.orElseGet(() -> game.playerNumber(game.admiral()));
        if (specialist.isPresent()) {
            ops.titles().discardFromPlay(ASSIGN_MISSION_SPECIALIST);
        }
        val cards = ops.galactica().drawDestinationCards(specialist.isPresent() ? MISSION_SPECIALIST_CARDS : ADMIRAL_CARDS);
        // The cards are in no pile until the choice, and only the chooser sees them: the others stay secret.
        ops.player(chooser).see(DeckId.DESTINATION, cards);
        return one(cards.stream()
                .map(chosen -> new ChooseDestinationEvent(chooser, chosen, others(cards, chosen)))
                .toArray(Event[]::new));
    }

    private static List<DestinationCard> others(List<DestinationCard> cards, DestinationCard chosen) {
        val others = new ArrayList<>(cards);
        others.remove(chosen);
        return others;
    }
}
