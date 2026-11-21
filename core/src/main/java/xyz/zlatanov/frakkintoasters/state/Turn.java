package xyz.zlatanov.frakkintoasters.state;

import xyz.zlatanov.frakkintoasters.state.character.Character;
import lombok.Getter;
import lombok.val;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;
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
    private       boolean                   returnsToCrisisStep    = false;
    private       boolean                   cylonShipsHeld         = false;
    private       DieRoll                   dieRoll;
    @Getter(NONE) //todo review excessive @Getter annotations
    private       InterruptWindow           interruptWindow;
    @Getter(NONE)
    private final List<PlayedInterruptCard> windowCards            = new ArrayList<>();
    @Getter(NONE)
    private final List<UsedRollAbility>     windowAbilities        = new ArrayList<>();
    @Getter(NONE)
    private final List<SkillCardType>       resolvedInterruptCards = new ArrayList<>();
    @Getter(NONE)
    private final Map<Location, Integer>    raidersToMiss          = new EnumMap<>(Location.class);
    @Getter(NONE)
    private final List<SkillCardType>       playedSkillCards       = new ArrayList<>();
    @Getter(NONE)
    private final Set<Resource>             lossReductions         = EnumSet.noneOf(Resource.class);
    @Getter(NONE)
    private final Map<Integer, Set<TurnLimit>> usedTurnLimits      = new TreeMap<>();
    @Getter(NONE)
    private final Map<Integer, Set<CharacterAbility>> usedAbilities = new TreeMap<>();
    private final Map<Integer, Set<CharacterAbility>> triggeredAbilities = new TreeMap<>();
    private final Map<Title, Character>     borrowedTitles         = new EnumMap<>(Title.class);
    @Getter(NONE)
    private       Integer                   remoteActionPlayerNumber;
    @Getter(NONE)
    private       Location                  remoteActionLocation;
    @Getter(NONE)
    private       Location                  startLocation;
    @Getter(NONE)
    private final Set<Integer>              sentToBrigAtEndOfTurn  = new TreeSet<>();
    @Getter(NONE)
    private       Integer                   actionCostPaidBy;

    @Mutates
    public void start(int number) {
        this.number = number;
        endedEarly = false;
        returnsToCrisisStep = false;
        dieRoll = null;
        resolvedInterruptCards.clear();
        raidersToMiss.clear();
        playedSkillCards.clear();
        lossReductions.clear();
        usedTurnLimits.clear();
        usedAbilities.clear();
        startLocation = null;
        sentToBrigAtEndOfTurn.clear();
        actionCostPaidBy = null;
        triggeredAbilities.clear();
        borrowedTitles.clear();
        endRemoteAction();
        cylonShipsHeld = false;
    }

    @Override
    public Map<Integer, Set<TurnLimit>> usedTurnLimits() {
        val copy = new TreeMap<Integer, Set<TurnLimit>>();
        usedTurnLimits.forEach((player, limits) -> copy.put(player, EnumSet.copyOf(limits)));
        return copy;
    }

    @Mutates
    public void useTurnLimit(int playerNumber, TurnLimit limit) {
        if (!usedTurnLimits.computeIfAbsent(playerNumber, ignored -> EnumSet.noneOf(TurnLimit.class)).add(limit)) {
            throw new FrakCallTheAdmiralException("Used this turn already: " + limit);
        }
    }

    @Override
    public Map<Integer, Set<CharacterAbility>> usedAbilities() {
        val copy = new TreeMap<Integer, Set<CharacterAbility>>();
        usedAbilities.forEach((player, abilities) -> copy.put(player, EnumSet.copyOf(abilities)));
        return copy;
    }

    // An ability may be used again in the same turn unless it says "once per turn": its processor checks that.
    @Mutates
    public void useAbility(int playerNumber, CharacterAbility ability) {
        usedAbilities.computeIfAbsent(playerNumber, ignored -> EnumSet.noneOf(CharacterAbility.class)).add(ability);
    }

    @Override
    public Map<Integer, Set<CharacterAbility>> triggeredAbilities() {
        val copy = new TreeMap<Integer, Set<CharacterAbility>>();
        triggeredAbilities.forEach((player, abilities) -> copy.put(player, EnumSet.copyOf(abilities)));
        return copy;
    }

    @Mutates
    public void triggerAbility(int playerNumber, CharacterAbility ability) {
        triggeredAbilities.computeIfAbsent(playerNumber, ignored -> EnumSet.noneOf(CharacterAbility.class)).add(ability);
    }

    @Mutates
    public void clearAbilityTrigger(int playerNumber, CharacterAbility ability) {
        val abilities = triggeredAbilities.get(playerNumber);
        if (abilities != null) {
            abilities.remove(ability);
            if (abilities.isEmpty()) {
                triggeredAbilities.remove(playerNumber);
            }
        }
    }

    @Override
    public Map<Title, Character> borrowedTitles() {
        return new EnumMap<>(borrowedTitles);
    }

    @Mutates
    public void borrowTitle(Title title, Character previousHolder) {
        borrowedTitles.put(title, previousHolder);
    }

    @Override
    public Optional<Location> startLocation() {
        return Optional.ofNullable(startLocation);
    }

    @Mutates
    public void noteStartLocation(Location location) {
        startLocation = location;
    }

    @Override
    public boolean actionCostPaid(int playerNumber) {
        return actionCostPaidBy != null && actionCostPaidBy == playerNumber;
    }

    @Mutates
    public void payActionCost(Integer playerNumber) {
        actionCostPaidBy = playerNumber;
    }

    @Override
    public Set<Integer> sentToBrigAtEndOfTurn() {
        return new TreeSet<>(sentToBrigAtEndOfTurn);
    }

    @Mutates
    public void sendToBrigAtEndOfTurn(int playerNumber) {
        sentToBrigAtEndOfTurn.add(playerNumber);
    }

    @Override
    public Optional<Location> remoteActionLocation(int playerNumber) {
        return remoteActionPlayerNumber != null && remoteActionPlayerNumber == playerNumber
                ? Optional.of(remoteActionLocation)
                : Optional.empty();
    }

    @Mutates
    public void allowRemoteAction(int playerNumber, Location location) {
        remoteActionPlayerNumber = playerNumber;
        remoteActionLocation = location;
    }

    @Mutates
    public void endRemoteAction() {
        remoteActionPlayerNumber = null;
        remoteActionLocation = null;
    }

    @Mutates
    public void holdCylonShips() {
        cylonShipsHeld = true;
    }

    @Override
    public List<SkillCardType> playedSkillCards() {
        return List.copyOf(playedSkillCards);
    }

    @Mutates
    public void playSkillCard(SkillCardType type) {
        playedSkillCards.add(type);
    }

    @Override
    public Set<Resource> lossReductions() {
        return lossReductions.isEmpty() ? Set.of() : EnumSet.copyOf(lossReductions);
    }

    @Mutates
    public void addLossReduction(Resource resource) {
        lossReductions.add(resource);
    }

    @Mutates
    public void useLossReduction(Resource resource) {
        if (!lossReductions.remove(resource)) {
            throw new FrakCallTheAdmiralException("No loss reduction for " + resource);
        }
    }

    @Mutates
    public void endEarly() {
        endedEarly = true;
    }

    @Mutates
    public void returnsToCrisisStep(boolean returnsToCrisisStep) {
        this.returnsToCrisisStep = returnsToCrisisStep;
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
    public List<UsedRollAbility> windowAbilities() {
        return List.copyOf(windowAbilities);
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
    public void useRollAbility(UsedRollAbility ability) {
        windowAbilities.add(ability);
    }

    // It resolves, or another reroll in the window comes first: either way it leaves the window.
    @Mutates
    public void removeRollAbility(UsedRollAbility ability) {
        if (!windowAbilities.remove(ability)) {
            throw new FrakCallTheAdmiralException("Not in the window: " + ability);
        }
    }

    @Mutates
    public void closeInterruptWindow() {
        if (!windowCards.isEmpty() || !windowAbilities.isEmpty()) {
            throw new FrakCallTheAdmiralException("Cards or abilities are left in the window: " + windowCards
                    + windowAbilities);
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
