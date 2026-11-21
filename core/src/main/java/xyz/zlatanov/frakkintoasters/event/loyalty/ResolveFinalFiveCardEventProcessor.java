package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscards;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class ResolveFinalFiveCardEventProcessor extends EventProcessor<ResolveFinalFiveCardEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.holderPlayerNumber()) && isPlayer(event.lookerPlayerNumber())
                && event.card() != null && event.card().isFinalFive();
    }

    // Revealed Cylons aren't affected by Final Five cards (rule 5.4.1.2), so the effects on a player skip them.
    @Override
    public Followup process() {
        val looker = event.lookerPlayerNumber();
        ops.player(event.holderPlayerNumber()).showFinalFiveCard(event.card());
        return switch (event.card()) {
            // the activation icons printed on the card, in their order
            case FINAL_FIVE_CYLON_SHIPS_ACTIVATE ->
                    all(new ActivateRaidersEvent(), new ActivateCylonBasestarsEvent(), new ActivateHeavyRaidersAndCenturionsEvent());
            case FINAL_FIVE_DAMAGE_GALACTICA_TWICE -> all(new DamageBattlestarEvent(), new DamageBattlestarEvent());
            case FINAL_FIVE_THEY_ARE_EXECUTED -> canBeExecuted(game, looker) ? single(new ExecuteEvent(looker)) : NONE;
            case FINAL_FIVE_YOU_ARE_SENT_TO_BRIG -> canBeSentToBrig(game, looker) ? single(new SendToBrigEvent(looker)) : NONE;
            case FINAL_FIVE_YOU_ARE_EXECUTED -> holderExecuted();
            default -> throw new FrakCallTheAdmiralException("Not a Final Five card: " + event.card());
        };
    }

    // The holder is executed and every other player discards 2 random Skill Cards. The card prints no reshuffle: it
    // leaves the game with the execution it causes, which then doesn't resolve it a second time.
    private Followup holderExecuted() {
        val holder = event.holderPlayerNumber();
        val effects = new ArrayList<Event>();
        if (canBeExecuted(game, holder)) {
            ops.player(holder).removeLoyaltyCardsFromGame(List.of(event.card()));
            effects.add(new ExecuteEvent(holder));
        }
        playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .filter(PlayerView::isHuman)
                .filter(other -> other.number() != holder)
                .forEach(other -> effects.add(ForcedDiscards.discardRandom(game, other.number(), 2)));
        return all(effects.toArray(Event[]::new));
    }
}
