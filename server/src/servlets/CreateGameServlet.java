package servlets;

import com.panickapps.response.ErrorResponse;
import model.*;
import model.response.GameCreatedResponse;
import model.response.InvalidParameterResponse;
import model.response.MissingParameterResponse;
import util.APIUtils;
import util.AuthUtils;
import util.CosmosUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CreateGameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        try {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String passwordStr = request.getParameter("password");
        String maxPlayersStr = request.getParameter("maxPlayers");
        String widthStr = request.getParameter("width");
        String heightStr = request.getParameter("height");
        String difficultyStr = request.getParameter("difficulty");

        //2 - Security:
        if (passwordStr == null) {
            response.getWriter().write(AuthUtils.MISING_PASSWORD_RESPONSE.toJSON());
            return;
        } else {
            if (!passwordStr.equals(AuthUtils.ADMIN_PASSWORD)) {
                response.getWriter().write(AuthUtils.SECURITY_RESPONSE.toJSON());
                return;
            }
        }

        //3 - Required params:

        if (maxPlayersStr == null) {
            response.getWriter().write(new MissingParameterResponse("maxPlayers").toJSON());
            return;
        }

        if (widthStr == null) {
            response.getWriter().write(new MissingParameterResponse("width").toJSON());
            return;
        }

        if (heightStr == null) {
            response.getWriter().write(new MissingParameterResponse("height").toJSON());
            return;
        }

        if (difficultyStr == null) {
            response.getWriter().write(new MissingParameterResponse("difficulty").toJSON());
            return;
        }

        //4 - Validate params:
        int maxPlayers = -1;
        int width = -1;
        int height = -1;
        Difficulty difficulty;

        try {
            maxPlayers = Integer.parseInt(maxPlayersStr);
            if (maxPlayers < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'maxPlayers' is invalid. Expected positive non-zero integer, found '" + maxPlayersStr + "'.").toJSON());
            return;
        }

        try {
            width = Integer.parseInt(widthStr);
            if (width < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'width' is invalid. Expected integer >=5, found '" + widthStr + "'.").toJSON());
            return;
        }

        try {
            height = Integer.parseInt(heightStr);
            if (height < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'height' is invalid. Expected integer >=5, found '" + heightStr + "'.").toJSON());
            return;
        }

        try {
            difficulty = Difficulty.valueOf(difficultyStr);
        } catch (IllegalArgumentException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'difficulty' is invalid: '" + difficultyStr + "'.").toJSON());
            return;
        }

        //5 - Process request:
        final String gameToken = UUID.randomUUID().toString().replace("-", "");
        final GameSpecification gameSpecification = new GameSpecification(maxPlayers, width, height, difficulty);
        final Game game = new Game(gameSpecification, gameToken);

        //Start the game. This would be done by the admin manually in an actual game
        game.setGameState(GameState.STARTED);

        try {
            APIUtils.initCosmosDB();
            CosmosUtil.setCollectionName(CosmosUtil.GAME_COLLECTION_NAME);
            CosmosUtil.createDocument(game, true);
            response.getWriter().write(new GameCreatedResponse(game.getToken()).toJSON());
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write(new ErrorResponse("Game not created", "Failed to create a game. " + e.getMessage()).toJSON());
        }


    }

}
