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
package dk.dtu.compute.se.pisd.roborally.dal;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
class Repository implements IRepository {

	private static final String GAME_GAMEID = "gameID";

	private static final String GAME_NAME = "name";

	private static final String GAME_CURRENTPLAYER = "currentPlayer";

	private static final String GAME_PHASE = "phase";

	private static final String GAME_STEP = "step";

	private static final String GAME_BOARD_NAME = "boardName";

	private static final String PLAYER_PLAYERID = "playerID";

	private static final String PLAYER_NAME = "name";

	private static final String PLAYER_COLOUR = "colour";

	private static final String PLAYER_GAMEID = "gameID";

	private static final String PLAYER_POSITION_X = "positionX";

	private static final String PLAYER_POSITION_Y = "positionY";

	private static final String PLAYER_HEADING = "heading";


	private static final String PLAYER_CHECKPOINT = "checkpoint";
	private static final String CARD_IN_HANDS_CARDNO = "CardNo";
	private static final String CARD_IN_HANDS_CARDVALUE = "CardValue";
	private static final String CARD_IN_REGISTERS_REGISTERNO = "RegisterNo";
	private static final String CARD_IN_REGISTERS_CARDVALUE = "CardValue";


	private final Connector connector;

	Repository(Connector connector){
		this.connector = connector;
	}

	/**
	 * This method creates the game by making a connection to the database and inserting all relevant information into the different tables.
	 * When it's done it closes the connection again.
	 * @Author Ekkart Kindler
	 * @param game
	 */
	@Override
	public boolean createGameInDB(Board game) {
		if (game.getGameId() == null) {
			Connection connection = connector.getConnection();
			System.out.println("Create game in database");
			try {
				connection.setAutoCommit(false);
				PreparedStatement ps = getInsertGameStatementRGK();
				ps.setString(1, game.getName()); // instead of name
				ps.setNull(2, Types.TINYINT); // game.getPlayerNumber(game.getCurrentPlayer())); is inserted after players!
				ps.setInt(3, game.getPhase().ordinal());
				ps.setInt(4, game.getStep());
				// If you have a foreign key constraint for current players,
				// the check would need to be temporarily disabled, since
				// MySQL does not have a per transaction validation, but
				// validates on a per row basis.
				// Statement statement = connection.createStatement();
				// statement.execute("SET foreign_key_checks = 0");

				int affectedRows = ps.executeUpdate();
				ResultSet generatedKeys = ps.getGeneratedKeys();
				if (affectedRows == 1 && generatedKeys.next()) {
					game.setGameId(generatedKeys.getInt(1));
				}
				generatedKeys.close();
				// Enable foreign key constraint check again:
				// statement.execute("SET foreign_key_checks = 1");
				// statement.close();
				createPlayersInDB(game);
				createPlayersHandCardsInDB(game);
				createPlayersRegisterCardsInDB(game);
				// since current player is a foreign key, it can oly be
				// inserted after the players are created, since MySQL does
				// not have a per transaction validation, but validates on
				// a per row basis.
				ps = getSelectGameStatementU();
				ps.setInt(1, game.getGameId());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
					rs.updateRow();
				} else {
					System.out.println("Error occurred during creation of game in database.");
				}
				rs.updateString(GAME_BOARD_NAME, game.boardName);
				rs.updateRow();
				rs.close();
				connection.commit();
				connection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("DB error");

				try {
					connection.rollback();
					connection.setAutoCommit(true);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			System.err.println("Game cannot be created in DB, since it has a game id already!");
		}
		return false;
	}

	/**
	 * This method updates the game by making a connection to the database and updating all relevant information in the different tables.
	 * When it's done it closes the connection again.
	 * @Author Ekkart Kindler
	 * @param game
	 * @return
	 */
	@Override
	public boolean updateGameInDB(Board game) {
		assert game.getGameId() != null;
		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);
			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, game.getGameId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
				rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
				rs.updateInt(GAME_STEP, game.getStep());
				rs.updateString(GAME_BOARD_NAME, game.boardName);
				rs.updateRow();
			} else {
				System.out.println("Error during game update in database.");
			}
			rs.close();
			updatePlayersInDB(game);
			updatePlayersHandCardsInDB(game);
			updatePlayersRegisterCardsInDB(game);
			connection.commit();
			connection.setAutoCommit(true);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Some DB error");
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * This method loads the game by making a connection to the database and selecting all relevant information in the different tables.
	 * When it's done it closes the connection again.
	 * @Author Ekkart Kindler
	 */
	@Override
	public Board loadGameFromDB(int id) {
		Board game;
		try {
			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();
			int playerNo = -1;
			if (rs.next()) {

				// int width = AppController.BOARD_WIDTH;
				// int height = AppController.BOARD_HEIGHT;
				// game = new Board(width,height);

				game = LoadBoard.loadBoard(rs.getString(GAME_BOARD_NAME));
				if (game == null) {
					return null;
				}
				playerNo = rs.getInt(GAME_CURRENTPLAYER);
				game.setPhase(Phase.values()[rs.getInt(GAME_PHASE)]);
				game.setStep(rs.getInt(GAME_STEP));
			} else {
				System.out.println("Error during load of game in database.");
				return null;
			}
			rs.close();
			game.setGameId(id);
			loadPlayersFromDB(game);
			loadCardsInPlayersHandFromDB(game);
			loadCardsInPlayersRegisterFromDB(game);
			if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
				game.setCurrentPlayer(game.getPlayer(playerNo));
			} else {
				System.out.println("Error during load of game in database.");
				return null;
			}
			return game;
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Some DB error");
		}
		return null;
	}

	/**
	 * Gets all the games saved in the database
	 * @Author Ekkart Kindler
	 * @return a list of all the games
	 */
	@Override
	public List<GameInDB> getGames() {
		// TODO when there many games in the DB, fetching all available games
		//      from the DB is a bit extreme; eventually there should a
		//      methods that can filter the returned games in order to
		//      reduce the number of the returned games.
		List<GameInDB> result = new ArrayList<>();
		try {
			PreparedStatement ps = getSelectGameIdsStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(GAME_GAMEID);
				String name = rs.getString(GAME_NAME);
				result.add(new GameInDB(id,name));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Insert information regarding the players in a game into the database table "Player".
	 * @Author Ekkart Kindler
	 * @param game
	 * @throws SQLException
	 */
	private void createPlayersInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			rs.moveToInsertRow();
			rs.updateInt(PLAYER_GAMEID, game.getGameId());
			rs.updateInt(PLAYER_PLAYERID, i);
			rs.updateString(PLAYER_NAME, player.getName());
			rs.updateString(PLAYER_COLOUR, player.getColor());
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(PLAYER_CHECKPOINT, player.getCheckpointTokens());
			rs.insertRow();
		}
		rs.close();
	}

	/**
	 * Inserts the cards on each players hand in a game into the database.
	 * @param game
	 * @throws SQLException
	 */
	private void createPlayersHandCardsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersHandStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			for(int j=0; j<8; j++) {
				rs.moveToInsertRow();
				rs.updateInt(PLAYER_GAMEID, game.getGameId());
				rs.updateInt(PLAYER_PLAYERID, i);
				rs.updateInt(CARD_IN_HANDS_CARDNO, j);
				if(game.getPlayer(i).getCardField(j).getCard() != null) {
					rs.updateInt(CARD_IN_HANDS_CARDVALUE, game.getPlayer(i).getCardField(j).getCard().command.value);
				}
				else {
					// rs.updateNull(CARD_IN_HANDS_CARDVALUE);
					rs.updateInt(CARD_IN_HANDS_CARDVALUE, -1);
				}
				rs.insertRow();
			}
		}
		rs.close();
	}

	/**
	 * Inserts the card in each players registers in the database.
	 * @param game
	 * @throws SQLException
	 */
	private void createPlayersRegisterCardsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersRegisterStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			for(int j=0; j<5; j++) {
				rs.moveToInsertRow();
				rs.updateInt(PLAYER_GAMEID, game.getGameId());
				rs.updateInt(PLAYER_PLAYERID, i);
				rs.updateInt(CARD_IN_REGISTERS_REGISTERNO, j);
				if(game.getPlayer(i).getProgramField(j).getCard() != null) {
					rs.updateInt(CARD_IN_REGISTERS_CARDVALUE, game.getPlayer(i).getProgramField(j).getCard().command.value);
				}
				else{
					rs.updateInt(CARD_IN_REGISTERS_CARDVALUE, -1);
				}
				rs.insertRow();
			}
		}
		rs.close();
	}

	/**
	 * Inserts each players deck/card pile in the database.
	 * @param game
	 * @throws SQLException
	 */


	/**
	 * Inserts each players discard pile in the database.
	 * @param game
	 * @throws SQLException
	 */


	/**
	 * Load information regarding the players in a game from the database.
	 * @Author Ekkart Kindler
	 * @param game
	 * @throws SQLException
	 */
	private void loadPlayersFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersASCStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			if (i++ == playerId) {
				String name = rs.getString(PLAYER_NAME);
				String colour = rs.getString(PLAYER_COLOUR);
				Player player = new Player(game, colour ,name);
				game.addPlayer(player);
				int x = rs.getInt(PLAYER_POSITION_X);
				int y = rs.getInt(PLAYER_POSITION_Y);
				player.setSpace(game.getSpace(x,y));
				int heading = rs.getInt(PLAYER_HEADING);
				player.setHeading(Heading.values()[heading]);
				player.setCheckpointTokens(rs.getInt(PLAYER_CHECKPOINT));
			} else {
				System.err.println("Game in DB does not have a player with id " + i +"!");
			}
		}
		rs.close();
	}

	/**
	 * Load each players cards into their hand from a saved game in the database.
	 * @param game
	 * @throws SQLException

	 */
	private void loadCardsInPlayersHandFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersHandStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		Command[] commands = Command.values();
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			int cardValue = rs.getInt(CARD_IN_HANDS_CARDVALUE)-1;
			int cardNumber = rs.getInt(CARD_IN_HANDS_CARDNO);
			if(cardValue < 0){

			}
			else {
				game.getPlayer(playerId).getCardField(cardNumber).setCard(new CommandCard(commands[cardValue]));
			}
		}
		rs.close();
	}

	/**
	 * Load each players cards into their registers from a saved game in the database.
	 * @param game
	 * @throws SQLException
	 */
	private void loadCardsInPlayersRegisterFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersRegisterStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		Command[] commands = Command.values();
		CommandCardField field;

		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			Player player = game.getPlayer(playerId);
			int cardValue = rs.getInt(CARD_IN_REGISTERS_CARDVALUE)-1;
			int registerNumber = rs.getInt(CARD_IN_REGISTERS_REGISTERNO);
			field = player.getCardField(registerNumber);
			if(cardValue < 0){}
			else {
				Command card = Command.values()[rs.getInt(CARD_IN_REGISTERS_CARDVALUE)];
				field.setCard(new CommandCard(card));
				game.getPlayer(playerId).getProgramField(registerNumber).setCard(new CommandCard(commands[cardValue]));

			}
		}
		rs.close();
	}






	/**
	 * Updates the information about each player in the database for the specific game.
	 * @Author Ekkart Kindler
	 * @param game
	 * @throws SQLException
	 */
	private void updatePlayersInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			Player player = game.getPlayer(playerId);
			// rs.updateString(PLAYER_NAME, player.getName()); // not needed: player's names does not change
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(PLAYER_CHECKPOINT, player.getCheckpointTokens());
			rs.updateRow();
		}
		rs.close();
	}

	/**
	 * Updates the information about each player cards in their hand, in the database for the specific game.
	 * @param game
	 * @throws SQLException
	 */
	private void updatePlayersHandCardsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersHandStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		Player player;
		for(int i = 0; i<game.getPlayersNumber(); i++) {
			if (rs.next()) {
				int playerId = rs.getInt(PLAYER_PLAYERID);
				player = game.getPlayer(playerId);
				for (int j = 0; j < 8; j++) {
					//rs.updateInt(CARDINPLAYERSHAND_CARDNO, j);
					if(player.getCardField(j).getCard() != null) {
						rs.updateInt(CARD_IN_HANDS_CARDVALUE, player.getCardField(j).getCard().command.value);
					}
					else{
						rs.updateInt(CARD_IN_HANDS_CARDVALUE, -1);
					}
					rs.updateRow();
				}
			}
		}
		rs.close();
	}

	/**
	 * Updates the information about each player cards in their registers, in the database for the specific game.
	 * @param game
	 * @throws SQLException
	 */
	private void updatePlayersRegisterCardsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardInPlayersRegisterStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		Player player;
		for(int i = 0; i<game.getPlayersNumber(); i++) {
			if (rs.next()) {
				int playerId = rs.getInt(PLAYER_PLAYERID);
				player = game.getPlayer(playerId);
				for (int j = 0; j < 5; j++) {
					//rs.updateInt(CARDINPLAYERSREGISTER_REGISTERNO, j);
					if(player.getCardField(j).getCard() != null) {
						rs.updateInt(CARD_IN_REGISTERS_CARDVALUE, player.getCardField(j).getCard().command.value);
					}
					else{
						rs.updateInt(CARD_IN_REGISTERS_CARDVALUE, -1);
					}
					rs.updateRow();
				}
			}
		}
		rs.close();
	}


	/**
	 * The prepared statement to send to the database in SQL to insert into the Game table
	 * @Author Ekkart Kindler
	 */
	private static final String SQL_INSERT_GAME =
			"INSERT INTO Game(name, currentPlayer, phase, step) VALUES (?, ?, ?, ?)";
	private PreparedStatement insert_game_stmt = null;

	private PreparedStatement getInsertGameStatementRGK() {
		if (insert_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				insert_game_stmt = connection.prepareStatement(
						SQL_INSERT_GAME,
						Statement.RETURN_GENERATED_KEYS);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return insert_game_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the Game table that matches the gameID
	 * @Author Ekkart Kindler
	 */
	private static final String SQL_SELECT_GAME =
			"SELECT * FROM Game WHERE gameID = ?";

	private PreparedStatement select_game_stmt = null;

	private PreparedStatement getSelectGameStatementU() {
		if (select_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_game_stmt = connection.prepareStatement(
						SQL_SELECT_GAME,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_game_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the Player table that matches the gameID
	 * @Author Ekkart Kindler
	 */
	private static final String SQL_SELECT_PLAYERS =
			"SELECT * FROM Player WHERE gameID = ?";

	private PreparedStatement select_players_stmt = null;

	private PreparedStatement getSelectPlayersStatementU() {
		if (select_players_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_players_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_stmt;
	}
	/**
	 * The prepared statement to send to the database in SQL that selects everything in the CardInPlayersHand table that matches the gameID
	 */
	private static final String SQL_SELECT_CARDINPLAYERSHAND =
			"SELECT * FROM CardInPlayersHand WHERE gameID = ?";

	private PreparedStatement select_cardInPlayersHand_stmt = null;

	private PreparedStatement getSelectCardInPlayersHandStatementU() {
		if (select_cardInPlayersHand_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cardInPlayersHand_stmt = connection.prepareStatement(
						SQL_SELECT_CARDINPLAYERSHAND,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cardInPlayersHand_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the CardInPlayersRegister table that matches the gameID
	 */
	private static final String SQL_SELECT_CARDINPLAYERSREGISTER =
			"SELECT * FROM CardInPlayersRegister WHERE gameID = ?";

	private PreparedStatement select_cardInPlayersRegister_stmt = null;

	private PreparedStatement getSelectCardInPlayersRegisterStatementU() {
		if (select_cardInPlayersRegister_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cardInPlayersRegister_stmt = connection.prepareStatement(
						SQL_SELECT_CARDINPLAYERSREGISTER,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cardInPlayersRegister_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the CardInPlayersCardPile that matches the gameID
	 */
	private static final String SQL_SELECT_CARDINPLAYERSCARDPILE =
			"SELECT * FROM CardInPlayersCardPile WHERE gameID = ?";

	private PreparedStatement select_cardInPlayersCardPile_stmt = null;

	private PreparedStatement getSelectCardInPlayersCardPileStatementU() {
		if (select_cardInPlayersCardPile_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cardInPlayersCardPile_stmt = connection.prepareStatement(
						SQL_SELECT_CARDINPLAYERSCARDPILE,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cardInPlayersCardPile_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the CardInPlayersDiscardPile that matches the gameID
	 */
	private static final String SQL_SELECT_CARDINPLAYERSDISCARDPILE =
			"SELECT * FROM CardInPlayersDiscardPile WHERE gameID = ?";

	private PreparedStatement select_cardInPlayersDiscardPile_stmt = null;

	private PreparedStatement getSelectCardInPlayersDiscardPileStatementU() {
		if (select_cardInPlayersDiscardPile_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cardInPlayersDiscardPile_stmt = connection.prepareStatement(
						SQL_SELECT_CARDINPLAYERSDISCARDPILE,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cardInPlayersDiscardPile_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects everything in the Players table that matches the gameID and sort it in ascending order
	 * @Author Ekkart Kindler
	 */
	private static final String SQL_SELECT_PLAYERS_ASC =
			"SELECT * FROM Player WHERE gameID = ? ORDER BY playerID ASC";

	private PreparedStatement select_players_asc_stmt = null;

	private PreparedStatement getSelectPlayersASCStatement() {
		if (select_players_asc_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				// This statement does not need to be updatable
				select_players_asc_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS_ASC);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_asc_stmt;
	}

	/**
	 * The prepared statement to send to the database in SQL that selects the gameIDs and names from the Game table
	 * @Author Ekkart Kindler
	 */
	private static final String SQL_SELECT_GAMES =
			"SELECT gameID, name FROM Game";

	private PreparedStatement select_games_stmt = null;

	private PreparedStatement getSelectGameIdsStatement() {
		if (select_games_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_games_stmt = connection.prepareStatement(
						SQL_SELECT_GAMES);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_games_stmt;
	}


}