package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Each basestar on the main game board launches raiders.
//
// @param raidersPerBasestar how many each basestar launches: 3 for the "launch raiders" icon (rule 11.5.2)
// @param icon               the "launch raiders" icon, which places a basestar on the Cylon Fleet board when there is
//                           none on the main game board. A card that launches raiders from each basestar, e.g. "Weapons
//                           Armed", does nothing then (rule 9.6)
public record LaunchRaidersEvent(int raidersPerBasestar, boolean icon) implements Event {

    public static final int RAIDERS_PER_ICON = 3;

    public LaunchRaidersEvent() {
        this(RAIDERS_PER_ICON, true);
    }

    public static LaunchRaidersEvent fromEachBasestar(int raiders) {
        return new LaunchRaidersEvent(raiders, false);
    }
}
