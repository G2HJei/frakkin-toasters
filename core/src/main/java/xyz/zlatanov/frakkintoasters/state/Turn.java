package xyz.zlatanov.frakkintoasters.state;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.TurnView;

import java.util.*;

import static lombok.AccessLevel.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

@Getter
@Accessors(fluent = true)
public class Turn implements TurnView {
    @Setter(onMethod_ = @Mutates)
    private       int                       number                 = 0;
    @Setter(onMethod_ = @Mutates)
    private       GameStep                  step                   = SETUP;
    @Setter(onMethod_ = @Mutates)
    private       int                       currentPlayerNumber    = 1;
    @Getter(NONE)
    private       Integer                   actingPlayerNumber; // null means the current player is acting
    @Getter(NONE)
    private final List<CrisisCard>          crisisCards            = new ArrayList<>();
    private       boolean                   endedEarly             = false;
    private       DieRoll                   dieRoll;
    @Getter(NONE) //todo review excessive @Getter annotations
    private       InterruptWindow           interruptWindow;
    @Getter(NONE)
    private final List<PlayedInterruptCard> windowCards            = new ArrayList<>();
    @Getter(NONE)
    private final List<SkillCardType>       resolvedInterruptCards = new ArrayList<>();
    @Getter(NONE)
    private final Map<Location, Integer>    raidersToMiss          = new EnumMap<>(Location.class);

    @Mutates
    public void start(int number) {
        this.number = number;
        endedEarly = false;
        dieRoll = null;
        resolvedInterruptCards.clear();
        raidersToMiss.clear();
    }

    @Mutates
    public void endEarly() {
        endedEarly = true;
    }

    @Override
    public int actingPlayerNumber() {
        return actingPlayerNumber != null ? actingPlayerNumber : currentPlayerNumber;
    }

    /**
     * @param actingPlayerNumber the player acting out of turn, or {@code null} when the current player is acting
     */
    @Mutates
    public Turn actingPlayerNumber(Integer actingPlayerNumber) {
        this.actingPlayerNumber = actingPlayerNumber;
        return this;
    }

    @Override
    public List<CrisisCard> crisisCards() {
        return List.copyOf(crisisCards);
    }

    @Mutates
    public void playCrisisCard(CrisisCard card) {
        crisisCards.add(card);
    }

    @Mutates
    public void removeCrisisCards(List<CrisisCard> cards) {
        cards.forEach(crisisCards::remove);
    }

    @Mutates
    public void startDieRoll() {
        dieRoll = new DieRoll();
    }

    @Override
    public Optional<InterruptWindow> interruptWindow() {
        return Optional.ofNullable(interruptWindow);
    }

    @Override
    public List<PlayedInterruptCard> windowCards() {
        return List.copyOf(windowCards);
    }

    @Override
    public List<SkillCardType> resolvedInterruptCards() {
        return List.copyOf(resolvedInterruptCards);
    }

    @Override
    public Map<Location, Integer> raidersToMiss() {
        return raidersToMiss.isEmpty() ? Map.of() : new EnumMap<>(raidersToMiss);
    }

    @Mutates
    public void openInterruptWindow(InterruptWindow window) {
        if (interruptWindow != null) {
            throw new FrakCallTheAdmiralException("A window is open already: " + interruptWindow);
        }
        interruptWindow = window;
    }

    @Mutates
    public void playInterruptCard(PlayedInterruptCard card) {
        windowCards.add(card);
    }

    @Mutates
    public void returnInterruptCard(PlayedInterruptCard card) {
        removeWindowCard(card);
    }

    @Mutates
    public void resolveInterruptCard(PlayedInterruptCard card) {
        removeWindowCard(card);
        resolvedInterruptCards.add(card.card().type());
    }

    @Mutates
    public void closeInterruptWindow() {
        if (!windowCards.isEmpty()) {
            throw new FrakCallTheAdmiralException("Cards are left in the window: " + windowCards);
        }
        interruptWindow = null;
    }

    @Mutates
    public void raidersWillMiss(Location area, int count) {
        raidersToMiss.put(area, count);
    }

    @Mutates
    public void raiderMissed(Location area) {
        raidersToMiss.computeIfPresent(area, (ignored, count) -> count > 1 ? count - 1 : null);
    }

    private void removeWindowCard(PlayedInterruptCard card) {
        if (!windowCards.remove(card)) {
            throw new FrakCallTheAdmiralException("Not in the window: " + card);
        }
    }
}
