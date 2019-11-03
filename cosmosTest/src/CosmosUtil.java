import com.microsoft.azure.documentdb.*;

public class CosmosUtil {

    private static DocumentClient client;
    private static String DATABASE_NAME;
    private static String COLLECTION_NAME;
    private static String DATABASE_ID;
    private static String COLLECTION_ID;

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
     * Creates a database with a given name.
     * @param databaseName The name of the database.
     * @return Returns a String containing the ID of the database created.
     * @throws DocumentClientException
     */
    public static String createDatabase(String databaseName) throws DocumentClientException {
        Database database = new Database();
        database.setId(DATABASE_NAME);
        ResourceResponse<Database> response = client.createDatabase(database, null);
        DATABASE_NAME = databaseName;
        DATABASE_ID = response.getResource().getResourceId();
        return DATABASE_ID;
    }

    /**
     * Creates a new collection in the selected database.
     * @param databaseName The name of the database to create the collection into.
     * @param collectionName The name of the collection to create.
     * @return Returns a String containing the ID of the collection created.
     * @throws DocumentClientException
     */
    public static String createCollection(String databaseName, String collectionName) throws DocumentClientException {
        DocumentCollection collectionInfo = new DocumentCollection();
        collectionInfo.setId(COLLECTION_NAME);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setOfferThroughput(400);
        ResourceResponse<DocumentCollection> response = client.createCollection("/dbs/" + databaseName, collectionInfo, requestOptions);
        COLLECTION_NAME = collectionName;
        COLLECTION_ID = response.getResource().getResourceId();
        return COLLECTION_ID;
    }

    /**
     * Selects a database give its ID.
     * @param databaseID
     */
    public static void selectDatabase(String databaseID) {
        DATABASE_ID = databaseID;
    }

    /**
     * Selects a collection given its ID.
     * @param collectionID
     */
    public static void selectCollection(String collectionID) {
        COLLECTION_ID = collectionID;
    }

    /**
     * Creates a new document in the selected database and collection.
     * @param object The document.
     * @param autoIDGeneration Sets automatic ID generation.
     * @return Returns a String containing the ID of the object created.
     * @throws DocumentClientException
     */
    public static String createDocument(Resource object, boolean autoIDGeneration) throws DocumentClientException {
        ResourceResponse response = client.createDocument("/dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID, object, new RequestOptions(), autoIDGeneration);
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
