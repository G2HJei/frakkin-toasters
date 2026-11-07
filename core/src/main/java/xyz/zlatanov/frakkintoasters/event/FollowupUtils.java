package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ResolveCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;

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

    public static Event discard(int playerNumber, int count) {
        return new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, discardExactly(count));
    }

    //todo move to GameView
    public static int playerNumber(GameView game, Character character) {
        return game.players().stream()
                .filter(p -> character != null && p.character() == character)
                .mapToInt(PlayerView::number)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
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

    private static EventConstraint discardExactly(int count) {
        return switch (count) {
            case 1 -> DISCARD_EXACTLY_1;
            case 2 -> DISCARD_EXACTLY_2;
            case 3 -> DISCARD_EXACTLY_3;
            case 4 -> DISCARD_EXACTLY_4;
            case 5 -> DISCARD_EXACTLY_5;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    private static List<Event> addPlayerDecisions(GameView game, Class<? extends PlayerEvent> eventClass) {
        val playerEvents = new ArrayList<Event>();
        var decidingPlayer = game.currentPlayerNumber();
        do {
            decidingPlayer = decidingPlayer >= game.players().size() ? 1 : ++decidingPlayer;
            playerEvents.add(new PlayerDecisionEvent<>(decidingPlayer, eventClass));
        } while (decidingPlayer != game.currentPlayerNumber());
        return playerEvents;
    }
}
