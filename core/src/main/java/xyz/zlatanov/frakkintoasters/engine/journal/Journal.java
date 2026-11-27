package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.EventInjected;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.OnlyAnswerTaken;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * At most one scope is open at a time: a followup runs after its parent's scope has closed, so the parent is passed in
 * explicitly.
 */
@NoArgsConstructor
public class Journal {

    public static final int ROOT_SCOPE = 0;

    private final List<JournalEntry> entries     = new ArrayList<>();
    private       int                openScope   = ROOT_SCOPE;
    private       int                lastScopeId = ROOT_SCOPE;

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

    public int openScope(Event event, int actor, int parentId) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        openScope = ++lastScopeId;
        entries.add(new ScopeOpened(openScope, parentId, event, actor));
        return openScope;
    }

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
     * A choice is made between events, so no scope may be open.
     */
    public void recordChoice(int scopeId, int chooser, Followup option) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new ChoiceMade(scopeId, chooser, option));
    }

    /**
     * It is injected between events, so no scope may be open.
     */
    public void recordInjection(Event event) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new EventInjected(event));
    }

    public void recordOnlyAnswer(int scopeId) {
        if (openScope != ROOT_SCOPE) {
            throw new FrakCallTheAdmiralException();
        }
        entries.add(new OnlyAnswerTaken(scopeId));
    }

    public int currentScope() {
        return openScope;
    }

    public Optional<ScopeOpened> scope(int scopeId) {
        // the scope asked for is usually one of the last
        for (int at = entries.size() - 1; at >= 0; at--) {
            if (entries.get(at) instanceof ScopeOpened opened && opened.scopeId() == scopeId) {
                return Optional.of(opened);
            }
        }
        return Optional.empty();
    }

    public List<JournalEntry> entries() {
        return List.copyOf(entries);
    }

    public int size() {
        return entries.size();
    }

    public List<Fact> facts() {
        return entries.stream()
                .filter(FactRecorded.class::isInstance)
                .map(e -> ((FactRecorded) e).fact())
                .toList();
    }

    public List<Fact> facts(int scopeId) {
        return entries.stream()
                .filter(FactRecorded.class::isInstance)
                .map(FactRecorded.class::cast)
                .filter(e -> e.scopeId() == scopeId)
                .map(FactRecorded::fact)
                .toList();
    }
}
