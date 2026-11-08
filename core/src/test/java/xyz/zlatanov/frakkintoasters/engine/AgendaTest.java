package xyz.zlatanov.frakkintoasters.engine;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.Agenda.Frame;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class AgendaTest {

    private final Agenda agenda = new Agenda();

    @Test
    void anEmptyAgendaHasNothingOnTop() {
        assertTrue(agenda.isEmpty());
        assertEquals(Optional.empty(), agenda.top());
        assertThrows(FrakCallTheAdmiralException.class, agenda::take);
    }

    @Test
    void noneAddsNothing() {
        agenda.push(NONE, 1);
        assertTrue(agenda.isEmpty());
    }

    @Test
    void allOfRunsItsChildrenInOrder() {
        agenda.push(all(event(1), event(2), event(3)), 7);

        assertEquals(List.of(new Frame(event(1), 7), new Frame(event(2), 7), new Frame(event(3), 7)), takeAll());
    }

    @Test
    void nestedAllOfsAreFlattened() {
        agenda.push(all(event(1), all(event(2), all(event(3), NONE)), event(4)), 7);

        assertEquals(List.of(event(1), event(2), event(3), event(4)), takeAll().stream().map(Frame::followup).toList());
    }

    @Test
    void whatAnEventQueuesRunsBeforeTheRestOfItsParentsFollowups() {
        agenda.push(all(event(1), event(2)), 7);
        agenda.take();
        agenda.push(all(event(3), event(4)), 8);

        assertEquals(List.of(new Frame(event(3), 8), new Frame(event(4), 8), new Frame(event(2), 7)), takeAll());
    }

    @Test
    void aChoiceWaitsOnTopAsOneFrame() {
        Followup choice = one(event(2), event(3));
        agenda.push(all(single(new NoOpEvent(1)), choice), 7);
        agenda.take();

        assertEquals(Optional.of(new Frame(choice, 7)), agenda.top());
        assertThrows(FrakCallTheAdmiralException.class, agenda::take);
    }

    @Test
    void selectingAnOptionPutsItInPlaceOfTheChoice() {
        Followup option = all(event(2), event(3));
        agenda.push(all(one(event(1), option), event(4)), 7);

        agenda.select(option);

        assertEquals(List.of(new Frame(event(2), 7), new Frame(event(3), 7), new Frame(event(4), 7)), takeAll());
    }

    @Test
    void onlyAnOptionOfTheChoiceOnTopCanBeSelected() {
        agenda.push(one(event(1), event(2)), 7);
        assertThrows(FrakCallTheAdmiralException.class, () -> agenda.select(event(3)));

        agenda.select(event(1));
        assertThrows(FrakCallTheAdmiralException.class, () -> agenda.select(event(1)));
        assertEquals(Optional.of(new Frame(event(1), 7)), agenda.top());
    }

    private static Followup event(int n) {
        return single(new NoOpEvent(n));
    }

    private List<Frame> takeAll() {
        List<Frame> taken = new ArrayList<>();
        while (!agenda.isEmpty()) {
            taken.add(agenda.take());
        }
        return taken;
    }
}
