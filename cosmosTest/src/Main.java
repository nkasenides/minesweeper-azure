import com.google.gson.Gson;
import com.microsoft.azure.documentdb.*;

import java.io.IOException;
import java.util.List;

public class Main {

    // Replace with your DocumentDB end point and master key.
    private static final String END_POINT = "nkasenides.documents.azure.com";
    private static final String MASTER_KEY = "aJxI2LqWOAWHME7KEpuEUjrZnHiPotZpICUBrJWKERay0EK5fqomVQFJ7huMY9Gj9h6v7jVdsRE1O8lwvhIvPA==";

    // Define an id for your database and collection
    private static final String DATABASE_ID = "minesweeperdb";
    private static final String COLLECTION_ID = "minesweepercollection";

    // We'll use Gson for POJO <=> JSON serialization for this sample.
    // Codehaus' Jackson is another great POJO <=> JSON serializer.
    private static Gson gson = new Gson();


    public static void main(String[] args) throws DocumentClientException,
            IOException {
        // Instantiate a DocumentClient w/ your DocumentDB Endpoint and AuthKey.
        DocumentClient documentClient = new DocumentClient(END_POINT,
                MASTER_KEY, ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Session);

        // Start from a clean state (delete database in case it already exists).
        try {
            documentClient.deleteDatabase("dbs/" + DATABASE_ID, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Define a new database using the id above.
        Database myDatabase = new Database();
        myDatabase.setId(DATABASE_ID);

        // Create a new database.
        myDatabase = documentClient.createDatabase(myDatabase, null)
                .getResource();


        System.out.println("Created a new database:");
        System.out.println(myDatabase.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Define a new collection using the id above.
        DocumentCollection myCollection = new DocumentCollection();
        myCollection.setId(COLLECTION_ID);

        // Set the provisioned throughput for this collection to be 1000 RUs.
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setOfferThroughput(1000);

        // Create a new collection.
        myCollection = documentClient.createCollection(
                "dbs/" + DATABASE_ID, myCollection, requestOptions)
                .getResource();

        System.out.println("Created a new collection:");
        System.out.println(myCollection.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Create an object, serialize it into JSON, and wrap it into a
        // document.
        SomePojo allenPojo = new SomePojo("123", "Allen Brewer", "allen [at] contoso.com");
        String allenJson = gson.toJson(allenPojo);
        Document allenDocument = new Document(allenJson);

        // Create the 1st document.
        allenDocument = documentClient.createDocument(
                "dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID, allenDocument, null, false)
                .getResource();

        System.out.println("Created 1st document:");
        System.out.println(allenDocument.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Create another object, serialize it into JSON, and wrap it into a
        // document.
        SomePojo lisaPojo = new SomePojo("456", "Lisa Andrews",
                "lisa [at] contoso.com");
        String somePojoJson = gson.toJson(lisaPojo);
        Document lisaDocument = new Document(somePojoJson);

        // Create the 2nd document.
        lisaDocument = documentClient.createDocument(
                "dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID, lisaDocument, null, false)
                .getResource();

        System.out.println("Created 2nd document:");
        System.out.println(lisaDocument.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Query documents
        List<Document> results = documentClient
                .queryDocuments(
                        "dbs/" + DATABASE_ID + "/colls/" + COLLECTION_ID,
                        "SELECT * FROM myCollection WHERE myCollection.email = 'allen [at] contoso.com'",
                        null).getQueryIterable().toList();

        System.out.println("Query document where e-mail address = 'allen [at] contoso.com':");
        System.out.println(results.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Replace Document Allen with Percy
        allenPojo = gson.fromJson(results.get(0).toString(), SomePojo.class);
        allenPojo.setName("Percy Bowman");
        allenPojo.setEmail("Percy Bowman [at] contoso.com");

        allenDocument = documentClient.replaceDocument(
                allenDocument.getSelfLink(),
                new Document(gson.toJson(allenPojo)), null)
                .getResource();

        System.out.println("Replaced Allen's document with Percy's contact information");
        System.out.println(allenDocument.toString());
        System.out.println("Press any key to continue..");
        System.in.read();

        // Delete Percy's Document
        documentClient.deleteDocument(allenDocument.getSelfLink(), null);

        System.out.println("Deleted Percy's document");
        System.out.println("Press any key to continue..");
        System.in.read();

        // Delete Database
        documentClient.deleteDatabase("dbs/" + DATABASE_ID, null);

        System.out.println("Deleted database");
        System.out.println("Press any key to continue..");
        System.in.read();

    }
}
