package xyz.zlatanov.frakkintoasters.state;

import java.util.ArrayList;
import java.util.List;

public class RemovedComponents {
    private final List<Object> items = new ArrayList<>();

    @Mutates
    public void add(Object e) {
        items.add(e);
    }

    public List<Object> items() {
        return List.copyOf(items);
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }
}
