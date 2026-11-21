package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

public class DetermineNextAdmiralEventProcessor extends EventProcessor<DetermineNextAdmiralEvent> implements LineOfSuccessionSelector {

    @Override
    public List<Character> lineOfSuccession() {
        val lineOfSuccession = new ArrayList<>(List.of(
                HELENA_CAIN,
                WILLIAM_ADAMA,
                SAUL_TIGH,
                KARL_HELO_AGATHON,
                FELIX_GAETA,
                LOUIS_HOSHI,
                TOM_ZAREK_ALT,
                LEE_APOLLO_ADAMA,
                ANASTASIA_DEE_DUALLA,
                KARL_HELO_AGATHON_ALT,
                KARA_STARBUCK_THRACE,
                LOUANNE_KAT_KATRAINE,
                SHARON_BOOMER_VALERII,
                BRENDAN_HOTDOG_COSTANZA,
                SAMUEL_T_ANDERS,
                CHIEF_GALEN_TYROL,
                CALLANDRA_CALLY_TYROL,
                SHERMAN_DOC_COTTLE,
                LEE_ADAMA,
                TOM_ZAREK,
                ELLEN_TIGH,
                GAIUS_BALTAR_ALT,
                GAIUS_BALTAR,
                ROMO_LAMPKIN,
                TORY_FOSTER,
                LAURA_ROSLIN));
        return lineOfSuccession;
    }

    @Override
    public Followup process() {
        val brig = galacticaBoard.charactersIn(BRIG);
        // The highest in line outside the "Brig". When every human is in it, the highest in line there, until one of
        // them leaves it (rules 4.4.2.3 and 4.4.3.2).
        val next = Optional.ofNullable(nextInLine(game, event.excludedPlayerNumber(), character -> !brig.contains(character)))
                .orElseGet(() -> nextInLine(game, event.excludedPlayerNumber(), character -> true));
        ops.titles().assignAdmiral(next);
        return Followup.NONE;
    }

    @Override
    public Character noNextInLine() {
        return null;
    }
}
