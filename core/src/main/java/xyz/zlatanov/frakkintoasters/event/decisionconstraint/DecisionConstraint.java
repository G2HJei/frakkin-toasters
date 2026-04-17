package xyz.zlatanov.frakkintoasters.event.decisionconstraint;

import xyz.zlatanov.frakkintoasters.event.Event;

public interface DecisionConstraint<T extends Event> {

    boolean validConstraint(T event);
}
