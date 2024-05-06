package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a space on the game board in the RoboRally game.
 * Each space can contain a player, walls, and field actions.
 * Notifies observers when its state changes.
 */
public class Space extends Subject {

    public final Board board;
    public final int x;
    public final int y;
    private Player player;
    private List<Heading> walls = new ArrayList<>();
    private List<FieldAction> actions = new ArrayList<>();

    /**
     * Constructs a Space object with the provided parameters.
     *
     * @param board The board this space belongs to.
     * @param x The x-coordinate of the space.
     * @param y The y-coordinate of the space.
     */
    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
        actions = getActions();
        walls = getWalls();
    }

    /**
     *
     * @return The player on the space, or null if empty.
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * Sets the player on the space.
     * @param player The player to set on the space.
     */
    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }

    /**
     * Retrieves the list of wall directions present on the space.
     * @return The list of wall directions.
     */
    public List<Heading> getWalls() {
        return walls;
    }


    /**
     * Retrieves the list of field actions associated with the space.
     * @return The list of field actions.
     */
    public List<FieldAction> getActions() {
        return actions;
    }

    void playerChanged() {
        notifyChange();
    }
}
