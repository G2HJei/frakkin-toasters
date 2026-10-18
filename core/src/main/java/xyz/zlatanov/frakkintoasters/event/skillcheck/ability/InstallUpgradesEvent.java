package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.Event;

public record InstallUpgradesEvent(boolean skillCheckPassed) implements Event {

}
