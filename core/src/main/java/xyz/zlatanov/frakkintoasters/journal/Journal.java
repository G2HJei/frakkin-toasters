package xyz.zlatanov.frakkintoasters.journal;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Append-only record of what the game ops did.
 */
public class Journal implements JournalWriter {

    public static final int ROOT_SCOPE = 0;

    private final List<JournalEntry> entries     = new ArrayList<>();
    private final Deque<Integer>     openScopes  = new ArrayDeque<>();
    private       int                lastScopeId = ROOT_SCOPE;

    /**
     * Opens a scope nested in the innermost open one.
     *
     * @return the id of the new scope
     */
    public int openScope(Event event, int actor) {
        int scopeId = ++lastScopeId;
        entries.add(new ScopeOpened(scopeId, currentScope(), event, actor));
        openScopes.push(scopeId);
        return scopeId;
    }

    /**
     * Closes the innermost open scope.
     */
    public void closeScope() {
        if (openScopes.isEmpty()) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new ScopeClosed(openScopes.pop()));
    }

    @Override
    public void record(Fact fact) {
        entries.add(new FactRecorded(currentScope(), fact));
    }

    /**
     * @return the innermost open scope, or {@link #ROOT_SCOPE}
     */
    public int currentScope() {
        return openScopes.isEmpty() ? ROOT_SCOPE : openScopes.peek();
    }

    public List<JournalEntry> entries() {
        return List.copyOf(entries);
    }

    /**
     * @return all facts in the order they were recorded
     */
    public List<Fact> facts() {
        return entries.stream()
                .filter(FactRecorded.class::isInstance)
                .map(e -> ((FactRecorded) e).fact())
                .toList();
    }

    /**
     * @return the facts recorded directly under the scope, not those of nested scopes
     */
    public List<Fact> facts(int scopeId) {
        return entries.stream()
                .filter(FactRecorded.class::isInstance)
                .map(FactRecorded.class::cast)
                .filter(e -> e.scopeId() == scopeId)
                .map(FactRecorded::fact)
                .toList();
    }
}
