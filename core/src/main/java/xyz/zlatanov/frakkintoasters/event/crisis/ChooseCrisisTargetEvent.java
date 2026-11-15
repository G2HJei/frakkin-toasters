package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// The player a Crisis Card names picks the player its section is about, e.g. "The Admiral chooses another player to gain
// 1 miracle token." A null target is the answer where the card gives another instruction instead: "You must choose
// another player to receive the President title or the current player is executed."
public record ChooseCrisisTargetEvent(int playerNumber, CrisisCard card, Integer targetPlayerNumber) implements PlayerEvent {
}
