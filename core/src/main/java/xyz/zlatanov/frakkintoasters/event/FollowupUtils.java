package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.MakeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PoliticalProwessEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.RevealCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.discardExactly;
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

    /**
     * @return what follows the start of a skill check, see {@code skill-check.drawio}. Before a skill check triggered by
     * a location, each player may play "Political Prowess" to decide it without making it
     */
    public static Followup beforeSkillCheckFollowups(GameView game) {
        val followups = new ArrayList<Followup>();
        if (game.activeSkillCheck().type().triggeredByLocation()) {
            followups.addAll(playOrPassDecisions(game, PoliticalProwessEvent.class));
        }
        followups.add(single(new MakeSkillCheckEvent()));
        return all(followups.toArray(Followup[]::new));
    }

    /**
     * Each player may play cards before the skill check, then they are revealed together.
     */
    public static Followup playCardsBeforeSkillCheck(GameView game) {
        return all(
                eachPlayerFromTheLeft(game, PlayCardsBeforeSkillCheckEvent.class),
                single(new RevealCardsBeforeSkillCheckEvent()));
    }

    /**
     * @return a decision for each player, starting with the player to the left of the current player and ending with
     * the current player, as players play Skill Cards into a skill check (rule 10.3.1)
     */
    public static Followup eachPlayerFromTheLeft(GameView game, Class<? extends PlayerEvent> eventClass) {
        val fromTheCurrent = playersFrom(game, game.turn().currentPlayerNumber());
        return all(Stream.concat(fromTheCurrent.stream().skip(1), Stream.of(fromTheCurrent.getFirst()))
                .map(player -> new PlayerDecisionEvent<>(player.number(), eventClass))
                .toArray(Event[]::new));
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

    public static Followup skillCheckAbilityFollowup(PlayerView player, SkillCardType type) {
        return switch (type) {
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
}
