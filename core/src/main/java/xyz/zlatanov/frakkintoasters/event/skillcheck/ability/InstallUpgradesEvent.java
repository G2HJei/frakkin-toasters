package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.Event;

// Install Upgrades - color: ENGINEERING, values: 0
// Skill Check: If this skill check passes, the current player draws 2 Engineering Cards. If it fails, he draws 1
// Engineering Card.
public record InstallUpgradesEvent(boolean skillCheckPassed) implements Event {

}
