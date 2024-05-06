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

 import dk.dtu.compute.se.pisd.roborally.model.Heading;
 import dk.dtu.compute.se.pisd.roborally.model.Player;
 import dk.dtu.compute.se.pisd.roborally.model.Space;
 import org.jetbrains.annotations.NotNull;

 /**
  * ...
  *
  * @author Ekkart Kindler, ekki@dtu.dk
  *
  */
public class ConveyorBelt extends FieldAction {

    private Heading heading; // Retningen, som transportbåndet peger i

    private boolean doublecb; // Angiver om det er et dobbelt transportbånd

     /**
      * Gets whether it is a double conveyor belt.
      *
      * @return true if it is a double conveyor belt, false otherwise
      */
    public boolean getDoublecb() {
        return doublecb;
    }

     /**
      * Gets the direction in which the conveyor belt points.
      *
      * @return the heading of the conveyor belt
      */
    public Heading getHeading() {
        return heading;
    }

     /**
      * Sets the direction in which the conveyor belt points.
      *
      * @param heading the heading to set
      */
    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    /**
     * Denne metode udfører ConveyorBelt-handlingen ved at flytte den nuværende spiller
     * på transportbåndet til den næste plads. Hvis den næste plads er optaget af en
     * anden spiller, forsøger metoden at skubbe den næste spiller.
     *
     * @param gameController Spilcontrolleren for det pågældende spil.
     * @param space          Pladsen, hvor handlingen skal udføres.
     * @return true, hvis handlingen lykkedes, ellers false.
     */



     /**
      * Performs the conveyor belt action by moving the current player on the conveyor belt
      * to the next space. If the next space is occupied by another player, the method attempts
      * to push the next player.
      *
      * @param gameController the game controller for the game
      * @param space          the space where the action is performed
      * @return true if the action was successfully executed, false otherwise
      */
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player currentPlayer = space.getPlayer();
        ConveyorBelt conveyorBelt = (ConveyorBelt) space.getActions().get(0);
        Heading currentHeading = conveyorBelt.getHeading();
        if (currentPlayer != null) { // Tjekker om der er en spiller på pladsen
            Space nextSpace = gameController.board.getNeighbour(space, currentHeading); // Finder den næste plads i retningen af transportbåndet
            Player nextPlayer = nextSpace.getPlayer(); // Spilleren på den næste plads

            if (nextPlayer == null) { // Hvis den næste plads er tom

                gameController.pushRobot(currentPlayer, currentHeading); // Flytter den aktuelle spiller til den næste plads
                return true;
            } else {

                if (gameController.board.getPlayerNumber(currentPlayer) < gameController.board.getPlayerNumber(nextPlayer)) { // Kontrollerer spillernes nummer i spillerlisten for at undgå gentagelser

                    if (!nextSpace.getActions().isEmpty()) {

                        if (nextSpace.getActions().get(0).getClass() == ConveyorBelt.class) { // Hvis der er endnu et transportbånd på den næste plads

                            nextPlayer.setActivated(true); // Markerer den næste spiller som aktiveret for at undgå gentagelser

                            doAction(gameController, nextSpace); // Rekursivt kald af metoden

                            gameController.pushRobot(currentPlayer, currentHeading); // Flytter den nuværende spiller til den næste plads
                        } else {

                            gameController.pushRobot(currentPlayer, currentHeading); // Flytter den nuværende spiller til den næste plads
                        }
                    } else {

                        gameController.pushRobot(currentPlayer, currentHeading); // Flytter den nuværende spiller til den næste plads
                    }
                    return true;
                } else {

                    if (nextPlayer.getSpace() == nextPlayer.getPrevSpace()) { // Hvis den næste spiller er tilbage på sit forrige plads

                        gameController.pushRobot(currentPlayer, currentHeading); // Flytter den nuværende spiller til den næste plads
                        return true;
                    } else {

                        nextPlayer.setSpace(nextPlayer.getPrevSpace()); // Flytter den næste spiller tilbage til sin forrige plads
                        return false;
                    }
                }
            }
        }
        return false;
    }
 }