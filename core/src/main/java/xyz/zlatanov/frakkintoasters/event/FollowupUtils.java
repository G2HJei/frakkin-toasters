package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayFromDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlaySkillsEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ResolveSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ShuffleAndDivideCardsEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

@NoArgsConstructor(access = PRIVATE)
public class FollowupUtils {


    public static Followup skillCheckFollowup(GameView game) {
        val followupEvents = new ArrayList<Event>();
        followupEvents.add(new PlayFromDestinyDeckEvent());
        addPlayerDecisions(game, followupEvents);
        followupEvents.add(new ShuffleAndDivideCardsEvent());
        followupEvents.add(new ResolveSkillCheckEvent());
        return all(followupEvents.toArray(Event[]::new));
    }

    public static Followup skillCheckAbilityFollowup(PlayerView player, SkillCard skillCard) {
        return switch (skillCard.type()) {
            case DOGFIGHT -> single(new PlayerDecisionEvent<>(player.number(), DogfightEvent.class));
            case FORCE_THEIR_HAND -> player.isHuman()
                    ? single(new PlayerDecisionEvent<>(player.number(), ForceTheirHandEvent.class))
                    : Followup.NONE;
            case QUICK_THINKING -> single(new PlayerDecisionEvent<>(player.number(), QuickThinkingEvent.class));
            case A_BETTER_MACHINE -> single(new ABetterMachineEvent(player.number()));
            case BAIT -> single(new BaitEvent());
            case DRADIS_CONTACT -> single(new DradisContactEvent());
            case EXPLOIT_A_WEAKNESS -> single(new PlayerDecisionEvent<>(player.number(), ExploitAWeaknessEvent.class));
            case PERSONAL_VICES -> single(new PersonalVicesEvent(player.number()));
            case VIOLENT_OUTBURSTS -> single(new ViolentOutburstsEvent(player.number()));
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

    private static void addPlayerDecisions(GameView game, ArrayList<Event> followupEvents) {
        var decidingPlayer = game.currentPlayer();
        do {
            decidingPlayer = decidingPlayer >= game.players().size() ? 1 : ++decidingPlayer;
            followupEvents.add(new PlayerDecisionEvent<>(decidingPlayer, PlaySkillsEvent.class));
        } while (decidingPlayer != game.currentPlayer());
    }
}
