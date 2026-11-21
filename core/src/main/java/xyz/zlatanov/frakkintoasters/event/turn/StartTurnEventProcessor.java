package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.ManipulativeEventProcessor;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EXPERT_PILOT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STIM_JUNKIE;

public class StartTurnEventProcessor extends EventProcessor<StartTurnEvent> {

    @Override
    public Followup process() {
        ops.startTurn();
        placeStrandedHelo();
        startPiloting();
        noteStartLocation();
        // Ellen Tigh's "Manipulative", at the start of her turn
        // Saul Tigh's "Alcoholic", at the start of every turn
        return allOf(CharacterPenalties.turnStarted(game), ManipulativeEventProcessor.offer(game), all(
                new ReceiveSkillsStepEvent(),
                new MovementStepEvent(),
                new ActionStepEvent(),
                new CrisisStepEvent(),
                new ActivateCylonShipsStepEvent(),
                new PrepareForJumpStepEvent(),
                new DiscardStepEvent(),
                new EndTurnEvent()));
    }

    // Expert Pilot: starting her turn in a viper, she has 2 actions in her Action step. The use records it.
    private void startPiloting() {
        val current = game.currentPlayer();
        if (current.hasAbility(EXPERT_PILOT)
                && game.boards().galactica().pilotedFighter(current.character()).isPresent()) {
            ops.player(current.number()).useAbility(EXPERT_PILOT);
        }
    }

    // Stim Junkie compares where Kat ends her Action step with where she starts her turn.
    private void noteStartLocation() {
        val current = game.currentPlayer();
        if (current.hasAbility(STIM_JUNKIE)) {
            game.locate(current.character()).ifPresent(ops::noteStartLocation);
        }
    }

    // Stranded: "At the start of your 2nd turn, place your character on the Hangar Deck location." His own turns,
    // because the turn number counts everyone's. An execution restarts the count, so a Helo selected then is Stranded
    // for his player's next turn (rule A2.1.1.10).
    private void placeStrandedHelo() {
        val current = game.currentPlayer();
        if (current.character() == KARL_HELO_AGATHON
                && game.locate(KARL_HELO_AGATHON).isEmpty()
                && current.turns() == 2) {
            ops.player(current.number()).moveTo(HANGAR_DECK);
        }
    }
}
