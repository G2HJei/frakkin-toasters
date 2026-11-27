package xyz.zlatanov.frakkintoasters.engine.answer;

import xyz.zlatanov.frakkintoasters.event.Event;

/**
 * An answer with a part to pick, such as the cards of a discard: too many ways to list each one.
 */
public record ShapedAnswer(Event example, Pick pick) {
}
