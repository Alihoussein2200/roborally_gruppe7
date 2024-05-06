package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * Represents a reboot field action on the game board.
 * This action is associated with changing a player's direction based on a specified heading
 * when they land on or pass through a reboot space.
 *
 * NOT IMPLEMENTED FULLY
 */
public class Reboot extends FieldAction {

    private Heading heading;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        return false;
    }
}
