package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

public record ResolveCapricaCrisisCardEvent(int playerNumber,
                                            CrisisCard crisisCardToPlay,
                                            CrisisCard crisisCardToPutAtBottom) implements PlayerEvent {
}
