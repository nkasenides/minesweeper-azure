package util;

import com.microsoft.azure.documentdb.*;

public class CosmosUtil {

    private static DocumentClient client;
    public static final String DATABASE_NAME = "MinesweeperDB";
    public static final String DATABASE_ID = "FFAkAA==";

    private static String COLLECTION_NAME;
    private static String COLLECTION_ID;

    public static final String GAME_COLLECTION_NAME = "GameCollection";
    public static final String GAME_COLLECTION_ID = "FFAkALEy29k=";
    public static final String SESSION_COLLECTION_NAME = "SessionCollection";
    public static final String SESSION_COLLECTION_ID = "FFAkAIf9BkA=";

    /**
     * Initializes the DocumentClient and returns it.
     * @return DocumentClient.
     */
    public static DocumentClient init() {
        if (client != null) return client;
        else {
            client = new DocumentClient(
                    "https://minesweeperdb.documents.azure.com:443/",
                    "DxmJ7fkC4QTS7XdxuH2j5WchGoRnIDo1XwQb2f5xM7XfJ6dTec0MQL2CnqXWFHul96MoXkaWt0lXCj6tOZn7EQ==",
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
    public static String createDocument(Object object, boolean autoIDGeneration) throws DocumentClientException {
        ResourceResponse response = client.createDocument("/dbs/" + DATABASE_NAME + "/colls/" + COLLECTION_NAME, object, new RequestOptions(), autoIDGeneration);
        return response.getResource().getResourceId();
    }

    /**
     * Updates a document given the new data.
     * @param object The document to update.
     * @return Returns a String containing the ID of the document.
     * @throws DocumentClientException
     */
    public static String updateDocument(Object object, String resourceID) throws DocumentClientException {
        ResourceResponse<Document> documentResourceResponse = client.replaceDocument("/dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID + "/docs/" + resourceID, object, null);
        return documentResourceResponse.getResource().getResourceId();
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
