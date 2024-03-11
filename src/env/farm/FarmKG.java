package farm;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class FarmKG extends Artifact {

    private static final String USERNAME = "was-students";
    private static final String PASSWORD = "knowledge-representation-is-fun";

    private String repoLocation;

    private static String PREFIXES = "PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>\n"+
    "PREFIX hmas: <https://purl.org/hmas/>\n" +
    "PREFIX td: <https://www.w3.org/2019/wot/td#>\n";
    

    public void init(String repoLocation) {
        this.repoLocation = repoLocation;
    }

    @OPERATION
    public void queryFarm(OpFeedbackParam<String> farm){
        // the variable where we will store the result to be returned to the agent
        String farmValue = null;

        // sets your variable name for the farm to be queried
        //String farmVariableName = "farm";

        String farmVariableName = "farm-c3cecd1b-d37e-4ba5-8fe4-03defcf96f03";


        String queryStr = PREFIXES + "SELECT ?farm WHERE {\n" +
        "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#> {\n" +
        " bind <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#" + farmVariableName + "> a was:Farm.\n" +
        "}\n" +
        "}";

        //String queryStr = PREFIXES + "SELECT ?" + farmVariableName + " WHERE { ?" + farmVariableName + " a was:Farm. }";

        /* Example SPARQL query 
         * PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>
         * PREFIX hmas: <https://purl.org/hmas/>
         * PREFIX td: <https://www.w3.org/2019/wot/td#>
         * SELECT ?farm WHERE { ?farm a was:Farm. }
         */

        // executes query
        JsonArray farmBindings = executeQuery(queryStr);
        
        /* Example JSON result
         * [{"farm":
         *  {
         *   "type":"uri",
         *   "value":"https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-danai#farm-17c04810-567a-4236-b310-611bb4fd2a8c"
         *  }
         * }]
         */

        // handles result as JSON object
        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();
        JsonObject farmBinding = firstBinding.getAsJsonObject(farmVariableName);
        farmValue = farmBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        farm.set(farmValue);
    }

    @OPERATION 
    public void queryThing(String farm, String offeredAffordance, OpFeedbackParam<String> thingDescription) {
        // the variable where we will store the result to be returned to the agent
        String tdValue = null; 

        // sets your variable name for the farm to be queried
        String tdVariableName = "td";

        // constructs query
        String queryStr = PREFIXES + "SELECT ?" + tdVariableName + " WHERE {\n" + 
            "<" + farm + "> hmas:contains ?thing.\n" +
            "?thing td:hasActionAffordance ?aff.\n" +
            "?thing hmas:hasProfile ?" + tdVariableName + ".\n" +
            "?aff a <" + offeredAffordance +">.} LIMIT 1";
        
        /* Example SPARQL query 
         * PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>
         * PREFIX hmas: <https://purl.org/hmas/>
         * PREFIX td: <https://www.w3.org/2019/wot/td#>
         * SELECT ?td WHERE {
         *   <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-danai#farm-17c04810-567a-4236-b310-611bb4fd2a8c> hmas:contains ?thing.
         *   ?thing td:hasActionAffordance ?aff.
         *   ?thing hmas:hasProfile ?td.
         *   ?aff a <https://was-course.interactions.ics.unisg.ch/farm-ontology#ReadSoilMoistureAffordance>.
         * } LIMIT 1
         */

        // executes query
        JsonArray tdBindings = executeQuery(queryStr);

        /* Example JSON result
         * [{"td":
         *  {
         *   "type":"uri",
         *   "value":"https://raw.githubusercontent.com/Interactions-HSG/example-tds/was/tds/tractor1.ttl"
         *  }
         * }]
         */
        
        // handles result as JSON object
        JsonObject firstBinding = tdBindings.get(0).getAsJsonObject();
        JsonObject tdBinding = firstBinding.getAsJsonObject(tdVariableName);
        tdValue = tdBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        thingDescription.set(tdValue);
    }

    @OPERATION
    public void queryFarmSections(String farm, OpFeedbackParam<Object[]> sections) {
        // the variable where we will store the result to be returned to the agent
        Object[] sectionsValue = new Object[]{ "fakeSection1", "fakeSection2", "fakeSection3", "fakeSection4" };

        String queryString = PREFIXES + "SELECT ?LandSection WHERE {\n" +
            "  GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#> {\n" +
            "    BIND (<" + farm + "> AS ?farm)\n" +
            "    ?farm a was:Farm.\n" +
            "    ?farm hmas:contains ?LandSection.\n" +
            "    ?LandSection a was:LandSection.\n" +
            "  }\n" +
            "}";

        JsonArray sectionBindings = executeQuery(queryString);

        String[] sectionArray = new String[sectionBindings.size()];

        for (int i = 0; i < sectionBindings.size(); i++) {
        JsonObject binding = sectionBindings.get(i).getAsJsonObject();
        JsonObject landSection = binding.getAsJsonObject("LandSection");
        String sectionValue = landSection.getAsJsonPrimitive("value").getAsString();

        sectionArray[i] = sectionValue;
        }

        sectionsValue = sectionArray;

        // sets the value of interest to the OpFeedbackParam
        sections.set(sectionsValue);
    }

    @OPERATION
    public void querySectionCoordinates(String section, OpFeedbackParam<Object[]> coordinates) {
        // the variable where we will store the result to be returned to the agent
        Object[] coordinatesValue = new Object[]{ 0, 0, 1, 1 };

    String queryString = PREFIXES + "SELECT ?Coordinates WHERE {\n" +
        "  GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#> {\n" +
        "    BIND (<" + section + "> AS ?section)\n" +
        "    ?section hmas:contains ?coordinatesEntity.\n" +
        "    ?coordinatesEntity td:hasCoordinates ?Coordinates\n" +
        "  }\n" +
        "}";

    JsonArray coordinatesBindings = executeQuery(queryString);

    if (coordinatesBindings.size() > 0) {
        JsonObject binding = coordinatesBindings.get(0).getAsJsonObject();
        String coordinate = binding.getAsJsonPrimitive("Coordinates").getAsString();
        
        String[] coordinateParts = coordinate.split(",");
        if (coordinateParts.length == 4) { 
            for (int i = 0; i < coordinateParts.length; i++) {
                try {
                    coordinatesValue[i] = Double.parseDouble(coordinateParts[i]);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

        // sets the value of interest to the OpFeedbackParam
        coordinates.set(coordinatesValue);
    }

    @OPERATION 
    public void queryCropOfSection(String section, OpFeedbackParam<String> crop) {

        String queryString = PREFIXES + "SELECT ?CropType WHERE {\n" +
        "  GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#> {\n" +
        "    <" + section + "> was:hasCropType ?Crop.\n" +
        "    ?Crop rdf:type ?CropType.\n" +
        "  }\n" +
        "}";

        JsonArray cropBindings = executeQuery(queryString);

        String cropValue = null;

        if (cropBindings.size() > 0) {
            JsonObject firstBinding = cropBindings.get(0).getAsJsonObject();
            JsonObject cropTypeBinding = firstBinding.getAsJsonObject("CropType");
            cropValue = cropTypeBinding.getAsJsonPrimitive("value").getAsString();
        } else {
        // Handle case where no crop is found; you might want to set a default value or handle it differently
        cropValue = "No crop found";
    }
        // the variable where we will store the result to be returned to the agent
        // String cropValue = "fakeCrop";

        // sets the value of interest to the OpFeedbackParam
        crop.set(cropValue);
    }

    @OPERATION
    public void queryRequiredMoisture(String crop, OpFeedbackParam<Integer> level) {
        // the variable where we will store the result to be returned to the agent
        Integer moistureLevelValue = 120;

        String queryString = PREFIXES + "SELECT ?RequiredSoilMoisture WHERE {\n" +
        "  GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-sandra#> {\n" +
        "    <" + crop + "> was:hasCropType/td:hasrequiredSoilMoisture ?RequiredSoilMoisture.\n" +
        "  }\n" +
        "}";

        JsonArray moistureBindings = executeQuery(queryString);
            // Check if query returned any results
        if (moistureBindings.size() > 0) {
            JsonObject firstBinding = moistureBindings.get(0).getAsJsonObject();
            String moistureStr = firstBinding.getAsJsonPrimitive("RequiredSoilMoisture").getAsString();

            try {
            moistureLevelValue = Integer.parseInt(moistureStr);
            } catch (NumberFormatException e) {
            System.err.println("Error parsing moisture level from query result: " + e.getMessage());
        }
    }
        // sets the value of interest to the OpFeedbackParam
        level.set(moistureLevelValue);
    }

    private JsonArray executeQuery(String queryStr) {
        String queryUrl = this.repoLocation + "?query=" +  URLEncoder.encode(queryStr, StandardCharsets.UTF_8);
        
        try {
            URI uri = new URI(queryUrl);
            String authString = USERNAME + ":" + PASSWORD;
            byte[] authBytes = authString.getBytes(StandardCharsets.UTF_8);
            String encodedAuth = Base64.getEncoder().encodeToString(authBytes);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Basic " + encodedAuth)
                .header("Accept", "application/sparql-results+json")
                .GET()
                .build();
             
            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP error code : " + response.statusCode());
                }
                String responseBody = response.body();
                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                JsonArray bindingsArray = jsonObject.getAsJsonObject("results").getAsJsonArray("bindings");
                return bindingsArray;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new JsonArray();
    }
}
