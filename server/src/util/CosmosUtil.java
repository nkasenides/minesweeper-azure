package util;

import com.microsoft.azure.documentdb.*;

public class CosmosUtil {

    private static DocumentClient client;
    public static final String DATABASE_NAME = "MinesweeperDB";
    public static final String DATABASE_ID = "iBgJAA==";

    private static String COLLECTION_NAME;
    private static String COLLECTION_ID;

    public static final String GAME_COLLECTION_NAME = "GameCollection";
    public static final String GAME_COLLECTION_ID = "iBgJAPwXMjU=";
    public static final String SESSION_COLLECTION_NAME = "SessionCollection";
    public static final String SESSION_COLLECTION_ID = "iBgJAMmHtT0=";

    /**
     * Initializes the DocumentClient and returns it.
     * @return DocumentClient.
     */
    public static DocumentClient init() {
        if (client != null) return client;
        else {
            client = new DocumentClient(
                    "https://minesweeper.documents.azure.com:443/",
                    "zosyn2c8p8QVPsrpRce9xsoNbAmjBD4fh3fqSexCn1Yg8HXrnaImrOJLozaSUqqCaP9t3WbxcVDMM3RuJD7qHA==",
                    new ConnectionPolicy(),
                    ConsistencyLevel.Session
            );
            return client;
        }
    }

    /**
     * Selects a collection given its ID.
     * @param collectionID
     */
    public static void setCollectionID(String collectionID) {
        COLLECTION_ID = collectionID;
    }

    public static void setCollectionName(String collectionName) {
        COLLECTION_NAME = collectionName;
    }

    /**
     * Creates a new document in the selected database and collection.
     * @param object The document.
     * @param autoIDGeneration Sets automatic ID generation.
     * @return Returns a String containing the ID of the object created.
     * @throws DocumentClientException
     */
    public static String createDocument(Resource object, boolean autoIDGeneration) throws DocumentClientException {
        ResourceResponse response = client.createDocument("/dbs/" + DATABASE_NAME + "/colls/" + COLLECTION_NAME, object, new RequestOptions(), autoIDGeneration);
        return response.getResource().getResourceId();
    }

    /**
     * Updates a document given the new data.
     * @param object The document to update.
     * @return Returns a String containing the ID of the document.
     * @throws DocumentClientException
     */
    public static String updateDocument(Resource object) throws DocumentClientException {
        ResourceResponse response = client.replaceDocument("/dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID + "/docs/" + object.getResourceId(), object,null);
        return response.getResource().getResourceId();
    }

    /**
     * Retrieves a list of documents based on a query.
     * @param query An SQL query.
     * @return Returns a QueryIterable, which can be iterated to get documents.
     */
    public static QueryIterable<Document> query(String query) {
        FeedResponse<Document> queryResults = client.queryDocuments(
                "/dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID,
                query,
                null
        );
        return queryResults.getQueryIterable();
    }

}
