package xyz.zlatanov.frakkintoasters.state;

import java.util.ArrayList;
import java.util.List;

public class RemovedComponents {
    private final List<Object> items = new ArrayList<>();

    public void add(Object e) {
        items.add(e);
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }
}
