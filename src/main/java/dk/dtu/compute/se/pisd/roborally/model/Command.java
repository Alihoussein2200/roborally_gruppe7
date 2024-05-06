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
package dk.dtu.compute.se.pisd.roborally.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public enum Command {

    // This is a very simplistic way of realizing different commands.

    FORWARD_1("Forward       1",0),
    RIGHT("Turn Right",1),
    LEFT("Turn Left",2),
    FORWARD_2("Forward      2",3),
    FORWARD_3("Forward      3",4),
    U_TURN("U-turn",5),
    BACKWARD("Backward      1",6),
    /**
     * Command to repeat THE previous command.
     */
    AGAIN("Again",7),
    OPTION_LEFT_RIGHT("Left OR Right",8, LEFT, RIGHT);

    /**
     * The display name of the command.
     */
    final public String displayName;

    /**
     * The numerical value associated with the command.
     */
    final public int value;


    /**
     * The list of options associated with the command.
     */
    final private List<Command> options;

    /**
     * Constructs a Command with the provided display name, value, and options.
     *
     * @param displayName The display name of the command.
     * @param value The numerical value associated with the command.
     * @param options The list of options associated with the command.
     */
    Command(String displayName, int value, Command... options) {
        this.displayName = displayName;
        this.value = value;
        this.options = Collections.unmodifiableList(Arrays.asList(options));

    }



    public boolean isInteractive() {
        return !options.isEmpty();
    }



    public List<Command> getOptions() {
        return options;
    }

}
