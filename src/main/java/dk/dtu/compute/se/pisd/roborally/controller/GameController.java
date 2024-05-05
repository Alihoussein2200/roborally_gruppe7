/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

public class GameController {

    protected boolean winnerFound = false;
    protected AppController appController;
    final public Board board;
    public GameController(@NotNull Board board) {
        this.board = board;
        this.appController = appController;
    }

    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        if (!board.getIsFirstTurnOfLoadedGame()) {
            for (int i = 0; i < board.getNumberOfPlayers(); i++) {
                this.DealNewCardsToPlayer(board.getPlayer(i));
            }
        } else {
            board.setIsFirstTurnOfLoadedGame(false);
        }
    }


    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    public void DealNewCardsToPlayer(Player player) {
        if (player != null) {
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setCard(null);
                field.setVisible(true);
            }
            for (int j = 0; j < Player.NO_CARDS; j++) {
                CommandCardField field = player.getCardField(j);
                field.setCard(generateRandomCommandCard());
                field.setVisible(true);
            }
        }
    }

    /**
     * Finish programming phase and transition to activation phase.
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
    }

    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getNumberOfPlayers(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * Starts round wiht stepmode set to true
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * Continues the execution of the programs of all players.
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }


    /**
     * Executes the next step of the programs of all players.
     */
    protected void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null && !card.command.isInteractive()) {
                    Command command = card.command;
                    executeCommand(currentPlayer, command);
                    if (isPreviousCardInteractive()) {
                        return;
                    }
                } else if (card != null) {
                    board.setPhase(Phase.PLAYER_INTERACTION);
                    return;
                }
                setNextPlayerToCurrent(currentPlayer, step);
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    /**
     * executes the given command for the current player.
     *
     * @param player
     * @param command
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {

            switch (command) {
                case FORWARD_1 -> this.movePlayer(player, 1, false);
                case FORWARD_2 -> this.movePlayer(player, 2, false);
                case FORWARD_3 -> this.movePlayer(player, 3, false);
                case RIGHT -> this.turnRight(player);
                case LEFT -> this.turnLeft(player);
                case U_TURN -> this.makeUTurn(player);
                case BACKWARD -> this.movePlayer(player, 1, true);
                case AGAIN -> this.playPrevCardAgain(player, board.getStep());
            }
        }
    }


    // TODO: V2

    /**
     * Moves a player on the board. The player can move forward or backward based on the MoveBackwards parameter.
     * The player will continue to move until they have moved the specified number of spaces, or until they fall in a pit or off the board.
     * If the player face a wall, the movement will stop.
     * If the target space is occupied by another player, the current player will attempt to push the other player.
     *
     * @param player        The player to be moved.
     * @param SpacesToMove  The number of spaces the player should move.
     * @param MoveBackwards If true, the player moves in the opposite direction of their current heading.
     */
    public void movePlayer(@NotNull Player player, int SpacesToMove, boolean MoveBackwards) {
        boolean again = false;
        do {
            Space space = player.getSpace();
            player.setPrevSpace(player.getSpace());
            if (player != null && player.board == board && space != null) {
                Heading heading = player.getHeading();
                if (MoveBackwards) {
                    heading = heading.next();
                    heading = heading.next();
                }
                Space target = board.getNeighbour(space, heading);
                if (willHitWall(space, target, heading)) {
                    return ;
                }
                if (target != null && target.getPlayer() == null) {
                    target.setPlayer(player);
                } else {
                    if (pushRobot(target.getPlayer(), heading)) {
                        target.setPlayer(player);
                    }
                }
                again = checkPit(target);
            }
            SpacesToMove--;
        } while (SpacesToMove > 0 && !again);
    }

    /**
     * Attempts to push a player to a neighbouring space in a specified direction.
     * If the target space is occupied by another player, the method will recursively attempt to push that player as well.
     * The method also checks for collisions with walls and pits.
     *
     * @param player  The player to be pushed.
     * @param heading The direction in which the player should be pushed.
     * @return Returns true if the player was successfully pushed and false if the push was obstructed by a wall.
     */
    public boolean pushRobot(@NotNull Player player, Heading heading) {
        Space space = player.getSpace();
        if (player.board == board && space != null) {
            Space target = board.getNeighbour(space, heading);
            if (willHitWall(space, target, heading)) {
                return false;
            }
            if (target != null && target.getPlayer() == null) {
                player.setPrevSpace(player.getSpace());
                target.setPlayer(player);
                checkPit(target);
                return true;
            } else {
                if (target != null && pushRobot(target.getPlayer(), heading)) {
                    player.setPrevSpace(player.getSpace());
                    target.setPlayer(player);
                    checkPit(target);
                    return true;
                }
            }
        }
        return false;
    }

    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    /**
     * Turns the player around
     *
     * @param player subject to be turned
     */
    public void makeUTurn(@NotNull Player player) {
        if (player.board == board) {
            player.setHeading(player.getHeading().prev());
            player.setHeading(player.getHeading().prev());
        }
    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Plays the previous card again if it exists and is not an interactive command.
     * If the previous card is an interactive command or if there is no previous card, sets the phase to PLAYER_INTERACTION.
     *
     * @param currentPlayer The player who is currently taking their turn.
     * @param currentStep   The current step in the player's program.
     */
    public void playPrevCardAgain(@NotNull Player currentPlayer, int currentStep) {
        int prevStep = currentStep - 1;
        if (prevStep >= 0) {
            Command command = board.getCurrentPlayer().getProgramField(prevStep).getCard().command;
            if (command == Command.AGAIN && prevStep >= 1) {
                playPrevCardAgain(currentPlayer, prevStep);
            } else if (command != Command.AGAIN && !command.isInteractive()) {
                executeCommand(currentPlayer, command);
            } else {
                board.setPhase(Phase.PLAYER_INTERACTION);
            }
        } else {
            board.setPhase(Phase.PLAYER_INTERACTION);
        }
    }


    /**
     * Execute the option chosen, for interactive cards
     *
     * @param player  current player
     * @param command command to be executed
     */
    public void executeOptionAndContinue(@NotNull Player player, Command command) {
        executeCommand(player, command);
        board.setPhase(Phase.ACTIVATION);
        setNextPlayerToCurrent(player, board.getStep());
        if (!board.isStepMode()) {
            continuePrograms();
        }
    }

    /**
     * Sets the next player in the turn order as the current player.
     *
     * @param currentPlayer The player who is currently taking their turn.
     * @param currentStep   The current step in the player's program.
     */
    protected void setNextPlayerToCurrent(Player currentPlayer, int currentStep) {
        int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
        if (nextPlayerNumber < board.getNumberOfPlayers()) {
            board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));

        } else {
            currentStep++;
            activateFieldActions();
            if (currentStep < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(currentStep);
                board.setStep(currentStep);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                startProgrammingPhase();
            }
        }
    }

    /**
     * Activates the actions for each player on the board.
     * This includes executing field actions, handling special cases like conveyor belts,
     * and checking for game conditions like checkpoints, pits, and the winner.
     */
    public void activateFieldActions() {
        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player currentPlayer = board.getPlayer(i);
            currentPlayer.setActivated(false);
            currentPlayer.setPrevSpace(currentPlayer.getSpace());
        }

        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player currentPlayer = board.getPlayer(i);
            Space space = currentPlayer.getSpace();
            if (!currentPlayer.getActivated()) {
                for (FieldAction fieldAction : space.getActions()) {
                    fieldAction.doAction(this, space);
                    if (fieldAction instanceof ConveyorBelt) {
                        DoubleConveyorBelt(fieldAction, space);
                    }
                }
            }
        }

        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player currentPlayer = board.getPlayer(i);
            Space space = currentPlayer.getSpace();
            for (FieldAction fieldAction : space.getActions()) {
                if (fieldAction instanceof Checkpoint || fieldAction instanceof Pit) {
                    fieldAction.doAction(this, space);
                }
            }
        }

        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            checkWinner(board.getPlayer(i));
        }
    }



    private boolean willHitWall(Space spaceFrom, Space spaceTo, Heading direction) {
        return (spaceFrom.getWalls().contains(direction) || spaceTo.getWalls().contains(direction.next().next()));
    }

    /**
     * Checks if the most recent non-"AGAIN" command card in the current player's program is a interactive card.
     * @return true if the most recent non-"AGAIN" command card is interactive, false otherwise.
     */
    private boolean isPreviousCardInteractive() {
        Player currentPlayer = this.board.getCurrentPlayer();
        int step = this.board.getStep();

        CommandCard card = currentPlayer.getProgramField(step).getCard();

        if (card != null && card.command == Command.AGAIN) {
            for (int i = step - 1; i >= 0; i--) {
                CommandCard prevCard = currentPlayer.getProgramField(i).getCard();
                if (prevCard.command != Command.AGAIN) {
                    return prevCard.command.isInteractive();
                }
            }
        }

        return false;
    }


    /**
     * Checks if the given player has won the game by comparing their checkpoint tokens with the total checkpoints.
     * If the player has won, the game is reset.
     *
     * @param player The player to check for winning condition.
     */
    private void checkWinner(Player player) {
        if (player.getCheckpointTokens() == board.checkpoints) {
            winnerFound = true;
        }
        if (winnerFound) {
            appController.GameIsDone(player);
        }
    }

    /**
     * Checks if the player on the given space has fallen into a pit or off the map.
     * If the player has fallen into a pit or off the map, the player's cards are cleared,
     * and the player is moved to a reboot or start space.
     *
     * @param space The space to check for a pit or off-map condition.
     * @return True if the player has fallen into a pit or off the map, false otherwise.
     */
    private boolean checkPit(Space space) {
        Player player = space.getPlayer(); // Get the player on the space
        if (player != null) { // Check if there is a player on the space
            if (!space.getActions().isEmpty() && space.getActions().get(0).getClass() == Pit.class) { // Check if there is a pit action on the space
                space.getActions().get(0).doAction(this, space); // Perform pit action
                return true; // Return true to indicate that the player has fallen into a pit
            } else if (OutOfMap(player.getPrevSpace(), space)) { // Check if the player is out of the map
                clearPlayersCards(player); // Clear the player's cards
                Space destinationSpace = rebootOrStart(player.getPrevSpace(), player); // Determine the destination space
                player.setSpace(destinationSpace); // Update the player's space
                return true; // Return true to indicate that the player has fallen off the map
            }
        }
        return false; // Return false if the player has not fallen into a pit or off the map
    }


    /**
     * Checks if the current space is out of the map based on the previous and current space.
     *
     * @param prevSpace    The previous space.
     * @param currentSpace The current space.
     * @return true if the current space is out of the map, otherwise false.
     */
    private boolean OutOfMap(Space prevSpace, Space currentSpace) {
        // Check if the current space is adjacent to the previous space in the x or y direction
        // Otherwise, the current space is out of the map
        if ((prevSpace.y == currentSpace.y && (prevSpace.x == currentSpace.x + 1 || prevSpace.x == currentSpace.x - 1)) ||
                (prevSpace.x == currentSpace.x && (prevSpace.y == currentSpace.y + 1 || prevSpace.y == currentSpace.y - 1))) {
            return false; // If the current space is adjacent to the previous space, it is not out of the map
        } else return prevSpace.x != currentSpace.x || prevSpace.y != currentSpace.y; // If the current space is the same as the previous space, it is not out of the map
    }


    /**
     * Clears the players next programming cards.
     *
     * @param player
     */
    public void clearPlayersCards(Player player) {
        for (int i = board.getStep() + 1; i < 5; i++) {
            player.getProgramField(i).setCard(null);
        }
    }



    /**
     * Flytter en spiller til det næste felt baseret på det aktuelle felt og dens retning.
     *
     * @param space Det aktuelle felt, hvor spilleren befinder sig.
     * @param heading Retningen, som spilleren bevæger sig i.
     */




    /**
     * Vælger enten genstartfeltet eller startfeltet afhængigt af spillerens position på brættet.
     *
     * @param space Det nuværende felt, hvor spilleren befinder sig.
     * @param player Spilleren, hvis startfelt skal returneres, hvis det er relevant.
     * @return Startfeltet for spilleren, hvis spilleren er på et felt med lav x-koordinat, ellers genstartfeltet.
     */
    public Space rebootOrStart(Space space, Player player){
        if(space.x < 3){
            return player.getStartSpace();
        }
        else{
            return getRebootSpace();
        }
    }

    /**
     * Finder det første felt med en genstartshandling.
     *
     * @return Feltet med en genstartshandling, hvis der findes en, ellers null.
     */
    private Space getRebootSpace() {
        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                Space space = board.getSpace(i, j);
                if (!space.getActions().isEmpty() && space.getActions().get(0) instanceof Reboot) {
                    return space;
                }
            }
        }
        return null;
    }




    /**
     * Håndterer dobbelt conveyorbåndaktion på et givet felt og det næste felt.
     *
     * @param fieldAction Den første conveyorbåndaktion.
     * @param space       Feltet, hvor handlingen skal udføres.
     */
    private void DoubleConveyorBelt(FieldAction fieldAction, Space space) {
        ConveyorBelt conveyorBelt = (ConveyorBelt) fieldAction;
        if (conveyorBelt.getDoublecb()) {
            Space newSpace = board.getNeighbour(space, conveyorBelt.getHeading());
            if (!newSpace.getActions().isEmpty()) {
                FieldAction nextAction = newSpace.getActions().get(0);
                if (nextAction instanceof ConveyorBelt) {
                    ConveyorBelt secondConveyor = (ConveyorBelt) nextAction;
                    if (secondConveyor.getDoublecb()) {
                        secondConveyor.doAction(this, newSpace);
                    }
                }
            }
        }
    }

}
