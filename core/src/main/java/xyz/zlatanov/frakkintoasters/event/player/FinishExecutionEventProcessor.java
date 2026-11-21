package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.jump.ObjectiveInstructionsEventProcessor;
import xyz.zlatanov.frakkintoasters.event.loyalty.DealLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;

public class FinishExecutionEventProcessor extends EventProcessor<FinishExecutionEvent> {

    @Override
    protected boolean isValid() {
        return player.character() != null && game.removedComponents().contains(event.executedCharacter());
    }

    @Override
    public Followup process() {
        // "The player adds one card from the "You Are Not a Cylon" deck to the Loyalty deck, shuffles the Loyalty deck
        // thoroughly, and draws one new Loyalty Card"
        ops.loyaltyDeck().addNotACylon();
        val followups = new ArrayList<Event>();
        if (player.isMutineer()) {
            // The new character "immediately receives the "You Are the Mutineer" Card faceup" (rule 7.1), and "anytime a
            // human player receives" it, he draws one Mutiny Card (rule 5.2.1). He has no title to give away.
            followups.add(new DrawMutinyCardEvent(player.number()));
        }
        followups.add(new DealLoyaltyCardsEvent(player.number(), loyaltyCardsToDeal(), false));
        if (player.character() == SHARON_BOOMER_VALERII && sleeperAgentPhaseDone()) {
            // "If selected after the Sleeper Agent phase, she is immediately placed in the "Brig"" (rule A2.1.1.15)
            followups.add(new SendToBrigEvent(player.number()));
        }
        // "If the Admiral, President or CAG is executed, the title will change hands after the new character is
        // selected", to the highest in line, the new character included (rules 4.4, 7.3).
        val executed = event.executedCharacter();
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

    // One card, and the additional ones for the characters that have them (rules 7.1, A2.1.1.6 and A2.1.1.15). Before the
    // Sleeper Agent phase the player of an executed Sharon "Boomer" Valerii draws one more, a new Gaius Baltar adds a card
    // to the deck and draws one more, and a new Boomer only adds a card. After it a new Boomer draws one more.
    private int loyaltyCardsToDeal() {
        var cards = 1;
        if (sleeperAgentPhaseDone()) {
            return player.character() == SHARON_BOOMER_VALERII ? cards + 1 : cards;
        }
        if (event.executedCharacter() == SHARON_BOOMER_VALERII) {
            cards++;
        }
        if (player.character() == GAIUS_BALTAR) {
            ops.loyaltyDeck().addNotACylon();
            cards++;
        } else if (player.character() == SHARON_BOOMER_VALERII) {
            ops.loyaltyDeck().addNotACylon();
        }
        return cards;
    }

    private boolean sleeperAgentPhaseDone() {
        return game.distanceTraveled() >= ObjectiveInstructionsEventProcessor.SLEEPER_AGENT_DISTANCE;
    }
}
