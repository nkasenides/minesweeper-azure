package servlets;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.QueryIterable;
import com.panickapps.response.ErrorResponse;
import com.panickapps.response.SuccessResponse;
import model.*;
import model.exception.InvalidCellReferenceException;
import model.response.GetStateResponse;
import model.response.MissingParameterResponse;
import util.APIUtils;
import util.CosmosUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class GetStateServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String sessionID = request.getParameter("sessionID");

        //3 - Required params:

        if (sessionID == null) {
            response.getWriter().write(new MissingParameterResponse("sessionID").toJSON());
            return;
        }

        //4 - Validate params:
        Session session = null;

        CosmosUtil.init();
        CosmosUtil.setCollectionID(CosmosUtil.SESSION_COLLECTION_ID);
        QueryIterable<Document> sessionsQuery = CosmosUtil.query("SELECT * FROM Session WHERE Session.sessionID='" + sessionID + "'");
        List<Document> sessionList = sessionsQuery.toList();
        if (sessionList.size() < 1) {
            response.getWriter().write(new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'").toJSON());
            return;
        }

        session = new Gson().fromJson(sessionList.get(0).toJson(), Session.class);

        if (session == null) {
            response.getWriter().write(new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'").toJSON());
            return;
        }

        Game game = null;

        CosmosUtil.setCollectionID(CosmosUtil.GAME_COLLECTION_ID);
        QueryIterable<Document> gamesQuery = CosmosUtil.query("SELECT Game.token, Game.gameState, Game.fullBoardState, Game.gameSpecification, Game.id FROM Game WHERE Game.token='" + session.getGameToken() + "'");
        List<Document> gameList = gamesQuery.toList();
        if (gameList.size() < 1) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + session.getGameToken() + "'").toJSON());
            return;
        }



        String gameJSON = gameList.get(0).toJson();
        System.out.println("RAW");
        System.out.println(gameJSON);
//
        game = new Gson().fromJson(gameJSON, Game.class);
//
        System.out.println("CONVERTED");
        System.out.println(new Gson().toJson(game));

        if (game == null) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + session.getGameToken() + "'").toJSON());
            return;
        }

        PartialStatePreference partialStatePreference = session.getPartialStatePreference();

        try {
            PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), session.getPositionRow(), session.getPositionCol(), game.getFullBoardState());
            response.getWriter().write(new GetStateResponse(partialBoardState, game.getGameState(), sessionID).toJSON());
        }
        //If failed to get the partial state, return error:
        catch (InvalidCellReferenceException e) {
            response.getWriter().write(new ErrorResponse("Error fetching partial state for session '" + sessionID + "'.", e.getMessage()).toJSON());
        }

    }
}
