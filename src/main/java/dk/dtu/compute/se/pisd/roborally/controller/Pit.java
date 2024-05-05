package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Pit extends FieldAction {

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player currentPlayer = space.getPlayer();
        if (currentPlayer != null) {
            gameController.clearPlayersCards(currentPlayer);
            currentPlayer.setSpace(gameController.rebootOrStart(space, currentPlayer));
            return true;
        }
        return false;
    }
}
