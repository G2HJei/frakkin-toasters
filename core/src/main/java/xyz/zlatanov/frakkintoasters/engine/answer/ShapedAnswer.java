package xyz.zlatanov.frakkintoasters.engine.answer;

import xyz.zlatanov.frakkintoasters.event.Event;

/**
 * An answer with a part to pick, such as the cards of a discard: too many ways to list each one.
 *
 * @param example the answer with the pick made one legal way, as {@code submit} takes it
 * @param pick    the component to fill, and what it may hold
 */
public record ShapedAnswer(Event example, Pick pick) {
}
