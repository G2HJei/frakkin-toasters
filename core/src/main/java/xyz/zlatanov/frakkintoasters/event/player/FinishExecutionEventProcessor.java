package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class FinishExecutionEventProcessor extends EventProcessor<FinishExecutionEvent> {

    @Override
    protected boolean isValid() {
        return player.character() != null && game.removedComponents().contains(event.executedCharacter());
    }

    @Override
    public Followup process() {
        // "The player adds one card from the "You Are Not a Cylon" deck to the Loyalty deck, shuffles the Loyalty deck
        // thoroughly, and draws one new Loyalty Card"
        // todo Sharon "Boomer" Valerii's player draws a second one before the Sleeper Agent phase, which doesn't exist yet
        ops.loyaltyDeck().addNotACylon();
        ops.player(player.number()).drawLoyaltyCards(1);
        // "If the Admiral, President or CAG is executed, the title will change hands after the new character is
        // selected", to the highest in line, the new character included (rules 4.4, 7.3).
        val executed = event.executedCharacter();
        val followups = new ArrayList<Event>();
        if (game.president() == executed) {
            followups.add(new DetermineNextPresidentEvent());
        }
        if (game.admiral() == executed) {
            followups.add(new DetermineNextAdmiralEvent());
        }
        if (game.cag() == executed) {
            followups.add(new DetermineNextCagEvent());
        }
        return all(followups.toArray(Event[]::new));
    }
}
