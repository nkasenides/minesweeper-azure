package servlets;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.QueryIterable;
import com.panickapps.response.ErrorResponse;
import com.panickapps.response.SuccessResponse;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import model.*;
import model.exception.InvalidCellReferenceException;
import model.response.GameMessage;
import model.response.MissingParameterResponse;
import model.response.PlayResponse;
import model.response.UnknownFailureResponse;
import util.APIUtils;
import util.CosmosUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayServlet extends HttpServlet {

    private AblyRealtime ably;
    private Channel channel;

    public PlayServlet() {
        try {
            ably = new AblyRealtime("rbXxOw.brAgMA:B8r0N2B-W2qCb3uj");
        } catch (AblyException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String sessionID = request.getParameter("sessionID");
        String moveStr = request.getParameter("move");
        String rowStr = request.getParameter("row");
        String colStr = request.getParameter("col");

        //3 - Required params:
        if (sessionID == null) {
            response.getWriter().write(new MissingParameterResponse("sessionID").toJSON());
            return;
        }

        if (moveStr == null) {
            response.getWriter().write(new MissingParameterResponse("move").toJSON());
            return;
        }

        if (rowStr == null) {
            response.getWriter().write(new MissingParameterResponse("row").toJSON());
            return;
        }

        if (colStr == null) {
            response.getWriter().write(new MissingParameterResponse("col").toJSON());
            return;
        }

        Session session = null;
        CosmosUtil.init();
        CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
        QueryIterable<Document> sessionsQuery = CosmosUtil.query("SELECT * FROM Session WHERE Session.sessionID='" + sessionID + "'");
        List<Document> sessionList = sessionsQuery.toList();
        if (sessionList.size() < 1) {
            response.getWriter().write(new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'").toJSON());
            return;
        }

        final Document sessionDocument = sessionList.get(0);
        session = new Gson().fromJson(sessionList.get(0).toJson(), Session.class);

        if (session == null) {
            response.getWriter().write(new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'").toJSON());
            return;
        }

        Game game = null;

        CosmosUtil.setCollectionID(CosmosUtil.GAME_COLLECTION_ID);
        QueryIterable<Document> gamesQuery = CosmosUtil.query("SELECT * FROM Game WHERE Game.token='" + session.getGameToken() + "'");
        List<Document> gameList = gamesQuery.toList();
        if (gameList.size() < 1) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + session.getGameToken() + "'").toJSON());
            return;
        }

        final Document gameDocument = gameList.get(0);
        game = new Gson().fromJson(gameList.get(0).toJson(), Game.class);

        if (game == null) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + session.getGameToken() + "'").toJSON());
            return;
        }

        System.out.println(new Gson().toJson(game));

        //Parse move:
        Move move;
        try {
            move = Move.valueOf(moveStr);
        } catch (IllegalArgumentException e) {
            response.getWriter().write(new ErrorResponse("Invalid move", "The move '" + moveStr + "' is not valid.").toJSON());
            return;
        }

        //Parse coordinates:
        int row;
        int col;

        try {
            row = Integer.parseInt(rowStr);
            if (row < 0 || row > game.getGameSpecification().getHeight()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new ErrorResponse("Invalid row", "The row '" + rowStr + "' is not valid. Rows must be within 0 and the game's height.").toJSON());
            return;
        }

        try {
            col = Integer.parseInt(colStr);
            if (col < 0 || col > game.getGameSpecification().getWidth()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new ErrorResponse("Invalid column", "The column'" + rowStr + "' is not valid. Rows must be within 0 and the game's width.").toJSON());
            return;
        }

        //Make game & session checks:
        if (session.isSpectator()) {
            response.getWriter().write(new ErrorResponse("Invalid operation", "You are registered to this game as a spectator and cannot perform any moves.").toJSON());
            return;
        }

        if (game.getGameState() == GameState.NOT_STARTED) {
            response.getWriter().write(new ErrorResponse("Invalid operation", "The game has not started yet!").toJSON());
            return;
        }

        final FullBoardState fullBoardState = game.getFullBoardState();

        //Make the move:
        switch (move) {

            case REVEAL:

                //Check if already revealed:
                if (fullBoardState.getCells()[row][col].getRevealState() != RevealState.COVERED) {
                    response.getWriter().write(new SuccessResponse("Cell already revealed", "The cell (" + row + "," + col + ") has already been revealed.").toJSON());
                    return;
                }

                //Reveal:
                RevealState revealState = game.reveal(row, col);
                switch (revealState) {
                    case REVEALED_0:
                    case REVEALED_1:
                    case REVEALED_2:
                    case REVEALED_3:
                    case REVEALED_4:
                    case REVEALED_5:
                    case REVEALED_6:
                    case REVEALED_7:
                    case REVEALED_8:
                        session.changePoints(10);
                        break;
                    case REVEALED_MINE:
                        session.changePoints(-5);
                        break;
                }

                //Check if game has ended:
                if (game.getGameState() == GameState.ENDED_LOST || game.getGameState() == GameState.ENDED_WON) {
                    game.revealAll();
                }

                //Save the game and session:
                try {
                    CosmosUtil.setCollectionID(CosmosUtil.GAME_COLLECTION_ID);
                    CosmosUtil.updateDocument(game, gameDocument.getResourceId());
                    CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
                    CosmosUtil.updateDocument(session, sessionDocument.getResourceId());
                } catch (Exception e) {
                    response.getWriter().write(new UnknownFailureResponse("Failed to update the game state." + e.getMessage()).toJSON());
                    return;
                }

                //Respond:
                publishStateToAllPlayers(game, row, col);
                response.getWriter().write(new PlayResponse(move, row, col, game.getGameState(), session.getPoints()).toJSON());
                return;

            case FLAG_UNFLAG:

                //Check if already revealed:
                if (fullBoardState.getCells()[row][col].getRevealState() != RevealState.COVERED && fullBoardState.getCells()[row][col].getRevealState() != RevealState.FLAGGED) {
                    response.getWriter().write(new SuccessResponse("Cell already revealed", "The cell (" + row + "," + col + ") has already been revealed.").toJSON());
                    return;
                }

                //Flag/Unflag the cell:
                game.flag(row, col);

                //Save the game and session:
                try {
                    CosmosUtil.setCollectionID(CosmosUtil.GAME_COLLECTION_ID);
                    CosmosUtil.updateDocument(game, gameDocument.getResourceId());
                    CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
                    CosmosUtil.updateDocument(session, sessionDocument.getResourceId());
                } catch (Exception e) {
                    response.getWriter().write(new UnknownFailureResponse("Failed to update the game state." + e.getMessage()).toJSON());
                    return;
                }

                publishStateToAllPlayers(game, row, col);
                response.getWriter().write(new PlayResponse(move, row, col, game.getGameState(), session.getPoints()).toJSON());
                return;

            case SHIFT:

                final PartialStatePreference partialStatePreference = session.getPartialStatePreference();

                //Check if the new partialBoardState would be valid:
                try {
                    new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), row, col, game.getFullBoardState());
                } catch (InvalidCellReferenceException e) {
                    response.getWriter().write(new ErrorResponse("Invalid move", "The move shift to cell (" + row + ", " + col + ") is out of bounds.").toJSON());
                    return;
                }

                //Set the new position:
                session.setPositionCol(col);
                session.setPositionRow(row);

                //Save the session:
                try {
                    CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
                    CosmosUtil.updateDocument(session, sessionDocument.getResourceId());
                } catch (Exception e) {
                    response.getWriter().write(new UnknownFailureResponse("Failed to update the game state." + e.getMessage()).toJSON());
                    return;
                }

                publishStateToPlayer(game, session);
                response.getWriter().write(new PlayResponse(move, row, col, game.getGameState(), session.getPoints()).toJSON());
                return;
        }


    }

    private void publishStateToAllPlayers(final Game game, final int changedRow, final int changedCol) {
        CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
        QueryIterable<Document> querySessions = CosmosUtil.query("SELECT * FROM Session WHERE Session.gameToken='" + game.getToken() + "'");
        List<Document> sessionDocuments = querySessions.toList();
        final List<Session> allSessions = new ArrayList<>();
        for (Document d : sessionDocuments) {
            Session session = new Gson().fromJson(d.toJson(), Session.class);
            //Publish state only to the players who have an AoI including the changed cell:
            if (
                    changedRow >= session.getPositionRow() && changedRow < session.getPositionRow() + session.getPartialStatePreference().getHeight() &&
                            changedCol >= session.getPositionCol() && changedCol < session.getPositionCol() + session.getPartialStatePreference().getWidth()
            ) {
                allSessions.add(session);
            }
        }

        for (final Session session : allSessions) {
            publishStateToPlayer(game, session);
        }
    }

    //Publishes the state of the game to the player with the given session ID
    private void publishStateToPlayer(final Game game, final Session session) {
        try {
            final String channelName = "gameState-" + session.getSessionID();
            Channel channel = ably.channels.get(channelName);
            final GameState gameState = game.getGameState();
            final PartialBoardState partialState = new PartialBoardState(session.getPartialStatePreference().getWidth(), session.getPartialStatePreference().getHeight(), session.getPositionRow(), session.getPositionCol(), game.getFullBoardState());
            GameMessage message = new GameMessage(gameState, partialState);
            String json = message.toJson();
            channel.publish("state", json);
        } catch (AblyException | InvalidCellReferenceException e) {
            throw new RuntimeException(e);
        }
    }

}
