package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.Followup.AllOf;
import xyz.zlatanov.frakkintoasters.event.Followup.None;
import xyz.zlatanov.frakkintoasters.event.Followup.OneOf;
import xyz.zlatanov.frakkintoasters.event.Followup.Single;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class Agenda {

    public record Frame(Followup followup, int parentScope) {
    }

    private final Deque<Frame> frames = new ArrayDeque<>();


    public void push(Followup followup, int parentScope) {
        switch (followup) {
            case None ignored -> {
            }
            case AllOf all -> all.followups().reversed().forEach(child -> push(child, parentScope));
            case Single single -> frames.push(new Frame(single, parentScope));
            case OneOf oneOf -> frames.push(new Frame(oneOf, parentScope));
        }
    }

    public Optional<Frame> top() {
        return Optional.ofNullable(frames.peek());
    }

    public Frame take() {
        if (frames.isEmpty() || !(frames.peek().followup() instanceof Single)) {
            throw new FrakCallTheAdmiralException("No event on top of the agenda");
        }
        return frames.pop();
    }

    public void select(Followup option) {
        if (!(frames.peek() instanceof Frame(OneOf oneOf, int parentScope))
                || !oneOf.options().contains(option)) {
            throw new FrakCallTheAdmiralException("Not an option of the choice on top of the agenda: " + option);
        }
        frames.pop();
        push(option, parentScope);
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }
}
