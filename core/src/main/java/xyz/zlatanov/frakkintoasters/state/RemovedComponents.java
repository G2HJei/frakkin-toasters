package xyz.zlatanov.frakkintoasters.state;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;
import java.util.List;

public class RemovedComponents {
    private final List<Object> items = new ArrayList<>();

    @Mutates
    public void add(Object e) {
        items.add(e);
    }

    @Mutates
    public void remove(Object e) {
        if (!items.remove(e)) {
            throw new FrakCallTheAdmiralException("Not removed from the game: " + e);
        }
    }

    public List<Object> items() {
        return List.copyOf(items);
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }
}
