package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;

// After everyone has been dealt (rule 5.5): each revealed Cylon looks at his facedown Loyalty Cards and gives them to a
// human player of his choice (rule 5.5.1), passing the "You Are the Mutineer" card on (rule 5.5.2.2), and the current
// player chooses who draws an additional card when nobody got the Mutineer card (rule 5.5.2.3).
public record FinishSleeperAgentPhaseEvent() implements Event {
}
