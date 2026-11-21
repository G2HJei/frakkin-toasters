package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.InterruptFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsLost;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;

import java.util.List;

public class InterruptOps extends GameOps {

    InterruptOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public void openWindow(InterruptWindow window) {
        record(new InterruptWindowOpened(window));
    }

    public void play(PlayedInterruptCard card) {
        record(new CardsLost(card.playerNumber(), PlayerPile.SKILL, List.of(card.card())));
        record(new InterruptCardPlayed(card));
    }

    public void returnToHand(PlayedInterruptCard card) {
        record(new InterruptCardReturned(card));
        player(card.playerNumber()).receiveSkillCard(card.card());
    }

    /**
     * The card takes effect: it leaves the window and goes to the discard pile. The caller applies its effect.
     */
    public void resolve(PlayedInterruptCard card) {
        record(new InterruptCardResolved(card));
        discardToDeck(card.card());
    }

    public void useAbility(UsedRollAbility ability) {
        record(new RollAbilityUsed(ability));
    }

    public void withdraw(UsedRollAbility ability) {
        record(new RollAbilityWithdrawn(ability));
    }

    /**
     * The ability takes effect: it leaves the window, and its player uses it (a miracle spends its token). The caller
     * applies its effect.
     */
    public void resolve(UsedRollAbility ability) {
        record(new RollAbilityResolved(ability));
        player(ability.playerNumber()).useAbility(ability.ability());
        useAbilityOnDieRoll(ability.ability());
    }

    public void closeWindow() {
        record(new InterruptWindowClosed());
    }

    public void raidersWillMiss(Location area, int count) {
        record(new RaidersWillMiss(area, count));
    }

    public void raiderMissed(Location area) {
        record(new RaiderMissed(area));
    }
}
