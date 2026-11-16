package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Once the fleet has traveled at least four distance, each player (including revealed Cylon players, but not Cylon
// Leaders) is dealt one card from the top of the Loyalty deck. Cylon Leaders get two additional Motive cards." (rule 5.5)
// Characters with an extra Loyalty Card get two, unless they are revealed Cylons (rule 6.1.2.2).
public record SleeperAgentPhaseEvent() implements Event {
}
