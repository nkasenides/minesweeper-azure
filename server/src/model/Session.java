package model;

import com.microsoft.azure.documentdb.Resource;

import java.util.UUID;

public class Session {

    private String id;
    private String sessionID;
    private PartialStatePreference partialStatePreference;
    private String playerName;
    private String gameToken;
    private int positionCol;
    private int positionRow;
    private boolean spectator;
    private int points;

    public Session(String sessionID, PartialStatePreference partialStatePreference, String playerName, String gameToken, boolean spectator) {
        this.id = sessionID;
        this.sessionID = sessionID;
        this.partialStatePreference = partialStatePreference;
        this.playerName = playerName;
        this.gameToken = gameToken;
        this.positionCol = 0;
        this.positionRow = 0;
        this.spectator = spectator;
        this.points = 0;
    }

    public Session() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public void setPartialStatePreference(PartialStatePreference partialStatePreference) {
        this.partialStatePreference = partialStatePreference;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameToken() {
        return gameToken;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }

    public int getPositionCol() {
        return positionCol;
    }

    public void setPositionCol(int positionCol) {
        this.positionCol = positionCol;
    }

    public int getPositionRow() {
        return positionRow;
    }

    public void setPositionRow(int positionRow) {
        this.positionRow = positionRow;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void changePoints(int points) {
        this.points += points;
    }

}
