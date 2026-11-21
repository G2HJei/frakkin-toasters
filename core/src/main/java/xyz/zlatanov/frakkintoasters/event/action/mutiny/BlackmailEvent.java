package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Blackmail (Mutiny Card)
// Action: take 3 random Skill Cards from the President. Discard it. Not as President, nor when he holds 2 or fewer.
public record BlackmailEvent(int playerNumber) implements MutinyCardEvent {
}
