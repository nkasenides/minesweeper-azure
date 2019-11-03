import com.google.gson.Gson;
import com.microsoft.azure.documentdb.*;

import java.util.UUID;

public class Main {



    public static void main(String[] args) {

        //Connect to CosmosDB:
        DocumentClient client = new DocumentClient(
                "https://minesweeperdb.documents.azure.com:443/",
                "DxmJ7fkC4QTS7XdxuH2j5WchGoRnIDo1XwQb2f5xM7XfJ6dTec0MQL2CnqXWFHul96MoXkaWt0lXCj6tOZn7EQ==",
                new ConnectionPolicy(),
                ConsistencyLevel.Session
        );

        final String DATABASE_NAME = "MinesweeperDB";
        final String DATABASE_ID = "iBgJAA==";
        final String COLLECTION_NAME = "GameCollection";
        final String COLLECTION_ID = "iBgJAI1++j0=";

//        //Create a database:
//        Database database = new Database();
//        database.setId(DATABASE_NAME);
//
//        try {
//            client.createDatabase(database, null);
//        } catch (DocumentClientException e) {
//            e.printStackTrace();
//        }
//
//        //Create a document collection:
//        DocumentCollection collectionInfo = new DocumentCollection();
//        collectionInfo.setId("GameCollection");
//        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.setOfferThroughput(400);
//        try {
//            client.createCollection("/dbs/" + DATABASE_NAME, collectionInfo, requestOptions);
//        } catch (DocumentClientException e) {
//            e.printStackTrace();
//        }


        //Create documents:

        Person person = new Person(UUID.randomUUID().toString(), "Nicos", "Kasenides", 26);
        try {
            client.createDocument("/dbs/" + DATABASE_NAME + "/colls/" + COLLECTION_NAME, person, new RequestOptions(), true);
        } catch (DocumentClientException e) {
            e.printStackTrace();
        }

        //Create document using GSON:
//        Person person = new Person(UUID.randomUUID().toString(),"John", "Smith", 50);
////        System.out.println(person.getResourceId());
//        final String personJSON = new Gson().toJson(person);
//        Document personDocument = new Document(personJSON);
//        try {
//            ResourceResponse<Document> document = client.createDocument("/dbs/" + DATABASE_NAME + "/colls/" + COLLECTION_NAME, personDocument, new RequestOptions(), true);
//            System.out.println(document.getResource().getResourceId());
//        } catch (DocumentClientException e) {
//            e.printStackTrace();
//        }
//
//        // Update a property
//        person.setAge(24);
//        try {
//            client.replaceDocument("/dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID + "/docs/" + "liEGANCA0rQBAAAAAAAAAA==",person,null);
//        } catch (DocumentClientException e) {
//            e.printStackTrace();
//        }

        //Query documents:
//        FeedResponse<Document> queryResults = client.queryDocuments("/dbs/" + DATABASE_NAME + "/colls/" + COLLECTION_NAME,
//                "SELECT * FROM Person",
//                null);
//
//        System.out.println("Running SQL query...");
//        for (Document p : queryResults.getQueryIterable()) {
//            System.out.println(String.format("\tRead %s", p));
//        }

    }

}
