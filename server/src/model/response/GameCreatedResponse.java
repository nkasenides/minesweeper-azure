package model.response;

import com.google.gson.JsonObject;
import com.panickapps.response.SuccessResponse;

public class GameCreatedResponse extends SuccessResponse {

    public GameCreatedResponse(String gameToken) {
        super("Game created", "Game created successfully.");
        JsonObject data = new JsonObject();
        data.addProperty("gameToken", gameToken);
        setData(data);
    }

}
