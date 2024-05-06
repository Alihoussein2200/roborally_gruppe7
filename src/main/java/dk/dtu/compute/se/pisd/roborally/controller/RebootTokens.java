package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;


/**
 * Represents a reboot token field action on the game board.
 * Reboot tokens may influence game mechanics by interacting with player positions or states
 * when they land on or pass through a space with a reboot token.
 */
public class RebootTokens extends FieldAction {
    @Override
    public boolean doAction(GameController gameController, Space space) {
        return false;
    }
    private Heading heading;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }


}
