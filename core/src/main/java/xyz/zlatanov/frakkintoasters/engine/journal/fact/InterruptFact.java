package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;

/**
 * Facts about the interrupt windows of the turn and the effects of the cards played into them that outlast the window.
 * Moving a card from a hand or into a discard pile is a separate fact.
 */
public sealed interface InterruptFact extends Fact {

    record InterruptWindowOpened(InterruptWindow window) implements InterruptFact {
    }

    record InterruptCardPlayed(PlayedInterruptCard card) implements InterruptFact {
    }

    // Another card of the window stands instead (rule 13.3), so this one goes back to the hand.
    record InterruptCardReturned(PlayedInterruptCard card) implements InterruptFact {
    }

    record InterruptCardResolved(PlayedInterruptCard card) implements InterruptFact {
    }

    record InterruptWindowClosed() implements InterruptFact {
    }

    // A character ability used in the window, secret until it closes.
    record RollAbilityUsed(UsedRollAbility ability) implements InterruptFact {
    }

    // Another reroll of the window comes first, so the ability isn't used and stays available.
    record RollAbilityWithdrawn(UsedRollAbility ability) implements InterruptFact {
    }

    record RollAbilityResolved(UsedRollAbility ability) implements InterruptFact {
    }

    // "Run Interference": the next raiders activating in the space area miss.
    record RaidersWillMiss(Location area, int count) implements InterruptFact {
    }

    record RaiderMissed(Location area) implements InterruptFact {
    }
}
