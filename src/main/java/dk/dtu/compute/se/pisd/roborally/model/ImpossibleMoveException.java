package dk.dtu.compute.se.pisd.roborally.model;

/**
 * An exception to be called, if a move is not possible
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class ImpossibleMoveException extends Exception{
    private final Player player;
    private final Space space;
    private final Heading heading;

    public ImpossibleMoveException(Player player, Space space, Heading heading)
    {
        super("Move impossible");
        this.player = player;
        this.space = space;
        this.heading = heading;
    }
}
