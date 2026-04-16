package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

public record ResolveCapricaCrisisCardEvent(int playerNumber,
                                            CrisisCard crisisCardToPlay,
                                            CrisisCard crisisCardToPutAtBottom) implements PlayerEvent {
}
