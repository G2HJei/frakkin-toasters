package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ResolveCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEventProcessor.canBeChosenForSickbay;

@NoArgsConstructor(access = PRIVATE)
public class FollowupUtils {

    /**
     * Like {@link Followup#all(Followup...)}, but skips the parts without effect, e.g. when nobody is in a location.
     */
    public static Followup allOf(Followup... followups) {
        return all(Arrays.stream(followups)
                .filter(followup -> !NONE.equals(followup))
                .toArray(Followup[]::new));
    }

    public static Event lose(Resource resource, int amount) {
        return new LoseResourceEvent(resource, amount);
    }

    public static Event gain(Resource resource, int amount) {
        return new GainResourceEvent(resource, amount);
    }

    /**
     * @return every player, clockwise from the given one. The current player token passes to the left (rule 8.1), so
     * clockwise is the order of the player numbers
     */
    public static List<PlayerView> playersFrom(GameView game, int firstPlayerNumber) {
        val count = game.players().size();
        return IntStream.range(0, count)
                .<PlayerView>mapToObj(i -> game.player((firstPlayerNumber - 1 + i) % count + 1))
                .toList();
    }

    public static Event discard(int playerNumber, int count) {
        return new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, discardExactly(count));
    }

    /**
     * @return the current player's choice of a character to send to "Sickbay", out of those he can choose (rule
     * 9.3.2), {@link Followup#NONE} when there is nobody to choose
     */
    public static Followup chooseCharacterForSickbay(GameView game) {
        return one(playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .filter(player -> canBeChosenForSickbay(game, player.number()))
                .map(player -> new SendToSickbayEvent(player.number()))
                .toArray(Event[]::new));
    }

    public static Followup beforeSkillCheckFollowups(GameView game) {
        val followupEvents = new ArrayList<Event>();
        followupEvents.addAll(addPlayerDecisions(game, PlayCardsBeforeSkillCheckEvent.class));
        followupEvents.add(new ResolveCardsBeforeSkillCheckEvent());
        return all(followupEvents.toArray(Event[]::new));
    }

    /**
     * Each player chooses to either play the event (e.g. a "Declare Emergency" card) or do nothing.
     */
    public static List<Followup> playOrPassDecisions(GameView game, Class<? extends PlayerEvent> eventClass) {
        return game.players().stream()
                .map(PlayerView::number)
                .map(n -> one(new PlayerDecisionEvent<>(n, eventClass), new NoOpEvent(n)))
                .toList();
    }

    public static Followup skillCheckAbilityFollowup(PlayerView player, SkillCard skillCard) {
        return switch (skillCard.type()) {
            case DOGFIGHT -> single(new PlayerDecisionEvent<>(player.number(), DogfightEvent.class));
            case FORCE_THEIR_HAND -> player.isHuman()
                    ? single(new PlayerDecisionEvent<>(player.number(), ForceTheirHandEvent.class))
                    : Followup.NONE;
            case QUICK_THINKING -> single(new PlayerDecisionEvent<>(player.number(), QuickThinkingEvent.class));
            case A_BETTER_MACHINE -> single(new ABetterMachineEvent());
            case BAIT -> single(new BaitEvent());
            case DRADIS_CONTACT -> single(new DradisContactEvent());
            case EXPLOIT_A_WEAKNESS -> single(new PlayerDecisionEvent<>(player.number(), ExploitAWeaknessEvent.class));
            case PERSONAL_VICES -> single(new PersonalVicesEvent());
            case VIOLENT_OUTBURSTS -> single(new ViolentOutburstsEvent());
            case PROTECT_THE_FLEET -> single(new PlayerDecisionEvent<>(player.number(), ProtectTheFleetEvent.class));
            case RED_TAPE -> single(new RedTapeEvent());
            case TRUST_INSTINCTS -> single(new TrustInstinctsEvent());
            case INSTALL_UPGRADES,
                 IRON_WILL,
                 ALL_HANDS_ON_DECK,
                 ESTABLISH_NETWORK -> Followup.NONE;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    private static List<Event> addPlayerDecisions(GameView game, Class<? extends PlayerEvent> eventClass) {
        val playerEvents = new ArrayList<Event>();
        var decidingPlayer = game.turn().currentPlayerNumber();
        do {
            decidingPlayer = decidingPlayer >= game.players().size() ? 1 : ++decidingPlayer;
            playerEvents.add(new PlayerDecisionEvent<>(decidingPlayer, eventClass));
        } while (decidingPlayer != game.turn().currentPlayerNumber());
        return playerEvents;
    }
}
