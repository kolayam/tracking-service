package eu.nimble.service.tracking.impl.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Api(tags = {"Aggregation Event Operation" })
@RestController
public class AggregationEventController {

    @Value("${spring.epcis.url}")
    private String epcisURL;

    private String getBaseUrl() {
        String url = epcisURL.trim();
        if(!url.endsWith("/"))
        {
            url = url + "/";
        }
        return url;
    }

    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation(value = "", notes = "", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "epc is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/getEpcAggregationOutput")
    public ResponseEntity<?> getEpcAggregationOutput(@RequestParam("itemID") String itemID,
         @RequestHeader(value = "Authorization", required = true)  String bearerToken) {

        JSONArray globalArray = new JSONArray();
        JSONArray aggregationEventList = getJsonEPCList(bearerToken);
        JSONObject traceTreeObject = new JSONObject();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("epc", itemID);

        getEntityObject(itemID, aggregationEventList, globalArray);
//        JSONObject entityObject = new JSONObject();
//        boolean findEpc = false;
//        for (int i = 0; i < aggregationEventList.length(); i++) {
//            JSONArray childEPCs = aggregationEventList.getJSONObject(i).getJSONArray("childEPCs");
//            for (int j = 0; j < childEPCs.length(); j++) {
//                String childEPC = childEPCs.getJSONObject(j).getString("epc");
//                if (itemID.equals(childEPC) && aggregationEventList.getJSONObject(i).getString("action").equals("ADD")) {
//                    findEpc = true;
//                    entityObject.put("epc", childEPC);
//                    entityObject.put("hasParent", aggregationEventList.getJSONObject(i).getString("parentID"));
//                    entityObject.put("startTime", aggregationEventList.getJSONObject(i).getJSONObject("eventTime").getLong("$date"));
//                }
//            }
//
//            if (aggregationEventList.getJSONObject(i).getString("action").equals("DELETE") && findEpc) {
//                entityObject.put("endTime", aggregationEventList.getJSONObject(i).getJSONObject("eventTime").getLong("$date"));
//            }
//        }
//
//        if(!findEpc) {
//            entityObject.put("epc", itemID);
//        }

//        jsonObject.put("Entity", entityObject);
        if(globalArray.length() == 0) {
            JSONObject object1 = new JSONObject();
            object1.put("Entity", jsonObject);
            globalArray.put(object1);
        }
        traceTreeObject.put("traceTree", jsonObject);
        return new ResponseEntity<>( traceTreeObject.toString(), HttpStatus.OK);
    }



    private void getEntityObject(String itemID, JSONArray aggregationEventList, JSONArray globalArray) {

        JSONObject obj = new JSONObject();
        boolean findEpc = false;
        for (int i = 0; i < aggregationEventList.length(); i++) {
            JSONArray childEPCs = aggregationEventList.getJSONObject(i).getJSONArray("childEPCs");
            JSONObject entityObject = new JSONObject();
            for (int j = 0; j < childEPCs.length(); j++) {
                String childEPC = childEPCs.getJSONObject(j).getString("epc");
                if (itemID.equals(childEPC) && aggregationEventList.getJSONObject(i).getString("action").equals("ADD")) {
                    findEpc = true;
                    entityObject.put("epc", childEPC);
                    entityObject.put("hasParent", aggregationEventList.getJSONObject(i).getString("parentID"));
                    entityObject.put("startTime", aggregationEventList.getJSONObject(i).getJSONObject("eventTime").getLong("$date"));
                }
            }

            if (aggregationEventList.getJSONObject(i).getString("action").equals("DELETE") && findEpc) {
                entityObject.put("endTime", aggregationEventList.getJSONObject(i).getJSONObject("eventTime").getLong("$date"));
            }

            obj.put("Entity", entityObject);
            if(entityObject.length() != 0) {
                globalArray.put(entityObject);
            }
        }

        if(obj.getJSONObject("Entity").has("hasParent")) {
            getEntityObject(obj.getJSONObject("Entity").getString("hasParent"), aggregationEventList, globalArray);
        }
    }



    private JSONArray getJsonEPCList(String bearerToken) {
        // define url
        String url = this.getBaseUrl();
        String eventType = "AggregationEvent";
        url = url + "/Poll/SimpleEventQuery?format=JSON&eventType=" + eventType;

        // get the event data list from the epcis application
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);
        JSONArray aggregationEventList = new JSONArray(response.getBody());
        return aggregationEventList;
    }
}
