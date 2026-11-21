package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.HashSet;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ChooseDestinationEventProcessor extends EventProcessor<ChooseDestinationEvent> {

    // The options name the cards drawn, so only their shape is checked: who chooses was decided by the draw.
    @Override
    protected boolean isValid() {
        val others = event.others();
        return event.chosen() != null
                && others != null && !others.isEmpty() && others.size() <= 2
                && others.stream().allMatch(Objects::nonNull)
                && !others.contains(event.chosen())
                && new HashSet<>(others).size() == others.size();
    }

    @Override
    public Followup process() {
        event.others().forEach(ops.galactica()::returnDestinationCardToBottom);
        return single(new ResolveDestinationCardEvent(event.chosen()));
    }
}
