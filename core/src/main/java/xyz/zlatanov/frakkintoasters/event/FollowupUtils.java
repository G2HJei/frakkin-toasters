package xyz.zlatanov.frakkintoasters.event;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

@NoArgsConstructor(access = PRIVATE)
public class FollowupUtils {


    public static Followup beforeSkillCheckFollowups(GameView game) {
        val followupEvents = addPlayerDecisions(game, PlayCardsBeforeSkillCheckEvent.class);
        return all(followupEvents.toArray(Event[]::new));
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
        var decidingPlayer = game.currentPlayerNumber();
        do {
            decidingPlayer = decidingPlayer >= game.players().size() ? 1 : ++decidingPlayer;
            playerEvents.add(new PlayerDecisionEvent<>(decidingPlayer, eventClass));
        } while (decidingPlayer != game.currentPlayerNumber());
        return playerEvents;
    }
}
