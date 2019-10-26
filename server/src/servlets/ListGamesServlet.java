package servlets;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.QueryIterable;
import com.panickapps.response.SuccessResponse;
import model.Game;
import model.StatelessGame;
import model.response.ListGamesResponse;
import org.json.JSONException;
import util.APIUtils;
import util.CosmosUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListGamesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String startedOnlyStr = request.getParameter("startedOnly");

        boolean startedOnly = startedOnlyStr != null;

        APIUtils.initCosmosDB();
        CosmosUtil.setCollectionID(CosmosUtil.GAME_COLLECTION_ID);
        QueryIterable<Document> items = CosmosUtil.query("SELECT Game.token, Game.gameState, Game.gameSpecification FROM Game");

        ArrayList<StatelessGame> statelessGames = new ArrayList<>();

        for (Document p : items) {
            final String documentJSON = p.toJson();
            try {
                Game game = new Gson().fromJson(documentJSON, Game.class);
                StatelessGame sg = StatelessGame.fromGame(game);
                statelessGames.add(sg);
            } catch (JSONException e) {
                System.out.println("Could not parse object");
            }
        }

        if (statelessGames.size() < 1) {
            response.getWriter().write(new SuccessResponse("Games fetched", "No games found").toJSON());
        }
        else {
            response.getWriter().write(new ListGamesResponse(statelessGames).toJSON());
        }

    }

}
