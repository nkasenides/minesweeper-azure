package servlets;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.panickapps.response.ErrorResponse;
import model.Game;
import model.PartialStatePreference;
import model.Session;
import model.response.InvalidParameterResponse;
import model.response.JoinedGameResponse;
import model.response.MissingParameterResponse;
import model.response.UnknownFailureResponse;
import util.APIUtils;
import util.DynamoUtil;
import util.InputValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JoinGameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String gameToken = request.getParameter("gameToken");
        String playerName = request.getParameter("playerName");
        String partialStateWidthStr = request.getParameter("partialStateWidth");
        String partialStateHeightStr = request.getParameter("partialStateHeight");

        //3 - Required params:

        if (gameToken == null) {
            response.getWriter().write(new MissingParameterResponse("gameToken").toJSON());
            return;
        }

        if (playerName == null) {
            response.getWriter().write(new MissingParameterResponse("playerName").toJSON());
            return;
        }

        if (partialStateWidthStr == null) {
            response.getWriter().write(new MissingParameterResponse("partialStateWidth").toJSON());
            return;
        }

        if (partialStateHeightStr == null) {
            response.getWriter().write(new MissingParameterResponse("partialStateHeight").toJSON());
            return;
        }

        //4 - Validate params:
        int partialStateWidth;
        int partialStateHeight;

        try {
            partialStateWidth = Integer.parseInt(partialStateWidthStr);
            if (partialStateWidth < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'partialStateWidth' is invalid. Expected integer >= 5, found '" + partialStateWidthStr + "'.").toJSON());
            return;
        }

        try {
            partialStateHeight = Integer.parseInt(partialStateHeightStr);
            if (partialStateHeight < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'partialStateHeight' is invalid. Expected integer >=5, found '" + partialStateHeightStr + "'.").toJSON());
            return;
        }

        DynamoDBMapper mapper = DynamoUtil.getMapper();

        //Get the referenced game
        HashMap<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(gameToken));

        DynamoDBScanExpression gameScanExpression = new DynamoDBScanExpression()
                .withFilterExpression("begins_with(gameToken,:v1)")
                .withExpressionAttributeValues(eav);

        final List<Game> games =  mapper.scan(Game.class, gameScanExpression);

        Game game;

        try {
            game = games.get(0);
        }
        catch (IndexOutOfBoundsException e) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + gameToken + "'").toJSON());
            return;
        }

        if (game == null) {
            response.getWriter().write(new ErrorResponse("Game not found", "Could not find game with token '" + gameToken + "'").toJSON());
            return;
        }

        //check player name
        if (!InputValidator.validateStringAlNumOnly(playerName)) {
            response.getWriter().write(new ErrorResponse("Invalid player name", "The player name must contain alphanumeric characters only.").toJSON());
            return;
        }

        if (playerName.length() > 255 || playerName.length() < 5) {
            response.getWriter().write(new ErrorResponse("Invalid player name", "The player name must be between 5 and 255 characters long.").toJSON());
            return;
        }

        //Check if player is in game already
        eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(gameToken));

        DynamoDBScanExpression nameScanExpression = new DynamoDBScanExpression()
                .withFilterExpression("begins_with(gameToken,:v1)")
                .withExpressionAttributeValues(eav);

        final List<Session> sessionsOfTheGame =  mapper.scan(Session.class, nameScanExpression);
        int playersWithNameInGame = 0;
        for (Session s : sessionsOfTheGame) {
            if (s.getPlayerName().equals(playerName)) {
                playersWithNameInGame++;
            }
        }

        if (playersWithNameInGame > 0) {
            response.getWriter().write(new ErrorResponse("Player already in game", "The player with name " + playerName + " is already in this game.").toJSON());
            return;
        }

        //Check max player limit:
        if (sessionsOfTheGame.size() >= game.getGameSpecification().getMaxPlayers()) {
            response.getWriter().write(new ErrorResponse("Game full", "Game with token '" + gameToken + "' is already full (" + sessionsOfTheGame.size() + "/" + game.getGameSpecification().getMaxPlayers() + ").").toJSON());
            return;
        }

        if (partialStateWidth > game.getGameSpecification().getWidth()) {
            response.getWriter().write(new ErrorResponse("Invalid partial state width", "The partial state width cannot be more than " + game.getGameSpecification().getWidth() + ".").toJSON());
            return;
        }

        if (partialStateHeight > game.getGameSpecification().getHeight()) {
            response.getWriter().write(new ErrorResponse("Invalid partial state height", "The partial state height cannot be more than " + game.getGameSpecification().getHeight() + ".").toJSON());
            return;
        }

        //Create the session:
        final String sessionID = UUID.randomUUID().toString();
        final Session session = new Session(sessionID, new PartialStatePreference(partialStateWidth, partialStateHeight), playerName, gameToken, false);

        mapper.save(session);
        response.getWriter().write(new JoinedGameResponse(gameToken, session.getSessionID()).toJSON());

    }


}
