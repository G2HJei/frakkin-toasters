package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.TurnLimit;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TurnView {

    /**
     * Counts player turns. It is 0 during setup.
     */
    int number();

    GameStep step();

    int currentPlayerNumber();

    /**
     * The player who is taking an action or moving right now. It is the current player, except when someone else acts
     * during that player's turn (e.g. the target of an "Executive Order" card). It does not change who the "current
     * player" is for game effects, e.g. skill checks.
     */
    int actingPlayerNumber();

    /**
     * The Crisis Cards resolved this turn, in the order they were played. They stay with the turn until it ends, when
     * they are discarded (rule 8.8).
     */
    List<CrisisCard> crisisCards();

    /**
     * The turn ended before its steps did, e.g. when the current player was executed (rule 7.1). The steps left don't
     * happen, except the Discard step: the hand limit applies at the end of every turn.
     */
    boolean endedEarly();

    /**
     * "After the Activate Cylon Ships step, return to the Resolve Crisis step" ("Fulfiller of Prophecy"): the turn has
     * another Crisis step and another Activate Cylon Ships step after this one.
     */
    boolean returnsToCrisisStep();

    /**
     * The latest die roll of the turn, {@code null} before the first. The window before a roll starts it, and its result
     * event reads it.
     */
    DieRollView dieRoll();

    /**
     * The interrupt window that is open, while its players answer.
     */
    Optional<InterruptWindow> interruptWindow();

    /**
     * The cards played into the open window. They stay face down until the window closes.
     */
    List<PlayedInterruptCard> windowCards();

    /**
     * The character abilities used in the open window on its die roll, secret until the window closes.
     */
    List<UsedRollAbility> windowAbilities();

    /**
     * The interrupt cards that took effect this turn, for "Major Victory", which is limited to 1 a turn.
     */
    List<SkillCardType> resolvedInterruptCards();

    /**
     * "Run Interference": how many of the raiders activating in a space area still miss, during a raider activation.
     */
    Map<Location, Integer> raidersToMiss();

    /**
     * The Skill Cards played this turn for their "Action:" or "Movement:", for the cards limited per turn, e.g. "Executive
     * Order".
     */
    List<SkillCardType> playedSkillCards();

    /**
     * "Preventative Policy": the resources whose next loss this turn is 1 smaller.
     */
    Set<Resource> lossReductions();

    /**
     * "Negotiation": no Cylon ship launches or activates for the rest of the turn.
     */
    boolean cylonShipsHeld();

    /**
     * What each player did this turn of what is limited to once a turn, e.g. the CAG's action while piloting.
     */
    Map<Integer, Set<TurnLimit>> usedTurnLimits();

    default boolean hasUsed(int playerNumber, TurnLimit limit) {
        return usedTurnLimits().getOrDefault(playerNumber, Set.of()).contains(limit);
    }

    /**
     * The character abilities each player used this turn, for the ones limited to once a turn.
     */
    Map<Integer, Set<CharacterAbility>> usedAbilities();

    default boolean hasUsed(int playerNumber, CharacterAbility ability) {
        return usedAbilities().getOrDefault(playerNumber, Set.of()).contains(ability);
    }

    /**
     * The abilities something just triggered and that wait to be offered, e.g. "Memento" after population was lost. The
     * engine asks for them after every event ({@code AbilityTriggers}).
     */
    Map<Integer, Set<CharacterAbility>> triggeredAbilities();

    /**
     * The titles the current player took for his turn ("Manipulative"), with the character who held them before.
     */
    Map<Title, Character> borrowedTitles();

    /**
     * @return the location whose action the player may take from wherever he is right now, e.g. through Louis Hoshi's
     * "Organized". Only that location's action then
     */
    Optional<Location> remoteActionLocation(int playerNumber);

    /**
     * Where the current player was at the start of his turn, a location or a space area. It is noted only when an
     * ability asks for it, Louanne "Kat" Katraine's "Stim Junkie".
     */
    Optional<Location> startLocation();

    /**
     * The players who go to the "Brig" at the end of the turn, e.g. by Romo Lampkin's "Kleptomania".
     */
    Set<Integer> sentToBrigAtEndOfTurn();

    /**
     * Whether the player paid the cost an ability puts on the action he is taking, e.g. the 2 Skill Cards of Laura
     * Roslin's "Terminal Illness". The action then follows.
     */
    boolean actionCostPaid(int playerNumber);
}
