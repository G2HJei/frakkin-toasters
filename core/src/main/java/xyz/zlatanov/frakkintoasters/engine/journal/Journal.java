package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;
import java.util.List;

/**
 * Append-only record of what the game ops did. At most one scope is open at a time: a followup runs after its parent's
 * scope has closed, so the parent is passed in explicitly.
 */
@NoArgsConstructor
public class Journal {

    public static final int ROOT_SCOPE = 0;

    private final List<JournalEntry> entries     = new ArrayList<>();
    private       int                openScope   = ROOT_SCOPE;
    private       int                lastScopeId = ROOT_SCOPE;

    /**
     * A journal that goes on from the entries, e.g. to restore a game. New scopes get the next ids.
     */
    public Journal(List<JournalEntry> entries) {
        this.entries.addAll(entries);
        lastScopeId = entries.stream()
                .filter(ScopeOpened.class::isInstance)
                .mapToInt(JournalEntry::scopeId)
                .max()
                .orElse(ROOT_SCOPE);
        val lastScopeClosed = entries.stream().anyMatch(e -> e instanceof ScopeClosed && e.scopeId() == lastScopeId);
        if (lastScopeId != ROOT_SCOPE && !lastScopeClosed) {
            throw new IllegalArgumentException("The entries end inside scope " + lastScopeId);
        }
    }

    /**
     * @param parentId the scope of the event that queued this one, {@link #ROOT_SCOPE} if none
     * @return the id of the new scope
     */
    public int openScope(Event event, int actor, int parentId) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        openScope = ++lastScopeId;
        entries.add(new ScopeOpened(openScope, parentId, event, actor));
        return openScope;
    }

    /**
     * @param followup what the event queued
     */
    public void closeScope(Followup followup) {
        if (openScope == ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new ScopeClosed(openScope, followup));
        openScope = ROOT_SCOPE;
    }

    public void record(Fact fact) {
        entries.add(new FactRecorded(currentScope(), fact));
    }

    /**
     * Records the answer to a {@link Followup.OneOf}. A choice is made between events, so no scope may be open.
     *
     * @param scopeId the scope of the event that queued the {@code OneOf}
     * @param option  the picked option
     */
    public void recordChoice(int scopeId, int chooser, Followup option) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new ChoiceMade(scopeId, chooser, option));
    }

    /**
     * @return the open scope, or {@link #ROOT_SCOPE}
     */
    public int currentScope() {
        return openScope;
    }

    public List<JournalEntry> entries() {
        return List.copyOf(entries);
    }

    public int size() {
        return entries.size();
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
     * @return the facts recorded under the scope
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
