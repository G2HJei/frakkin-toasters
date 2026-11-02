package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;

// Resolves the pass, partial pass or fail result of the active skill check, unless Change of Plans replaced the pass
// result or Iron Will prevented the fail result.
public record ResolveSkillCheckResultEvent() implements Event {
}
