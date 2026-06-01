package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

public record DetermineNextPresidentEvent() implements SelectNextOfficialEventProcessor, Event {

    @Override
    public List<Character> lineOfSuccession(Game game) {
        return List.of(
                LAURA_ROSLIN,
                GAIUS_BALTAR,
                LEE_ADAMA,
                TOM_ZAREK,
                ROMO_LAMPKIN,
                TORY_FOSTER,
                ELLEN_TIGH,
                LEE_APOLLO_ADAMA,
                TOM_ZAREK_ALT,
                FELIX_GAETA,
                WILLIAM_ADAMA,
                KARL_HELO_AGATHON,
                CHIEF_GALEN_TYROL,
                GAIUS_BALTAR_ALT,
                CALLANDRA_CALLY_TYROL,
                SHERMAN_DOC_COTTLE,
                HELENA_CAIN,
                ANASTASIA_DEE_DUALLA,
                LOUIS_HOSHI,
                KARL_HELO_AGATHON_ALT,
                SHARON_BOOMER_VALERII,
                SAUL_TIGH,
                BRENDAN_HOTDOG_COSTANZA,
                SAMUEL_T_ANDERS,
                KARA_STARBUCK_THRACE,
                LOUANNE_KAT_KATRAINE);
    }

    @Override
    public Followup apply(Game game) {
        if (firstPresident(game)) {
            val quorumCard = game.decks().quorum().draw();
            game.presidentHand().add(quorumCard);
        }
        game.president(calcNextInLine(game));
        return Followup.NONE;
    }

    private boolean firstPresident(Game game) {
        return game.president() == null;
    }
}
