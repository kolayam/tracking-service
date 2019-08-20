package eu.nimble.service.tracking.impl.controller;

import io.swagger.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Api(tags = {"Tracking Delay Analysis Controller"})
@RestController
public class TrackingDelayAnalysisController {

    @Value("${spring.epcis.url}")
    private String epcisURL;

    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation(value = "Get delay for the given EPC list",
            notes = "Get delay for the given EPC list", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "epc list is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/getEPCListTimeDelay")
    public ResponseEntity<?> getEPCListTimeDelay(@ApiParam(value = "EPC List", required = true) @RequestParam("epcList") String[] epcList,
             @ApiParam(value = "Production Process Template", required = true) @RequestParam("inputDocument") String inputDocument,
             @ApiParam(value = "The Bearer token provided by the identity service", required = true)
             @RequestHeader(value = "Authorization", required = true)  String bearerToken) {

        JSONObject jsonObject = new JSONObject(inputDocument);
        JSONArray jsonTemplateArray = jsonObject.getJSONArray("productionProcessTemplate");

        JSONArray responseObject = new JSONArray();
        for(String item: epcList) {
            responseObject.put(getEPCItemDelay(item, bearerToken, jsonTemplateArray));
        }

        return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get delay for the given EPC item",
            notes = "Get delay for the given EPC item", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "epc item is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/getEPCTimeDelay")
    public ResponseEntity<?> getEPCTimeDelay(@ApiParam(value = "EPC Item", required = true) @RequestParam("item") String item,
             @ApiParam(value = "Production Process Template", required = true) @RequestBody String inputDocument,
             @ApiParam(value = "The Bearer token provided by the identity service", required = true)
             @RequestHeader(value = "Authorization", required = true)  String bearerToken) {

        JSONObject jsonObject = new JSONObject(inputDocument);
        JSONArray jsonTemplateArray = jsonObject.getJSONArray("productionProcessTemplate");
        return new ResponseEntity<>(getEPCItemDelay(item, bearerToken, jsonTemplateArray).toString(), HttpStatus.OK);
    }

    private JSONObject getEPCItemDelay(String item, String bearerToken, JSONArray jsonTemplateArray) {

        JSONArray eventDataList = getJsonEPCList(item, bearerToken);
        Integer totalDuration = 0;
        Integer epcItemTotalDuration = 0;
        JSONObject lastEPCItem = new JSONObject();
        Integer nextEventId = 0;

        for (int i = 0; i < jsonTemplateArray.length(); i++) {
            boolean epcItemTotalCalculated = true;
            if(i== 0 || nextEventId != null) {
                JSONObject templateObject = jsonTemplateArray.getJSONObject(nextEventId);
                for (int j = 0; j < eventDataList.length(); j++) {
                    JSONObject epcDocument = eventDataList.getJSONObject(j);
                    if(!epcDocument.has("used")) {
                        if(templateObject.getString("bizStep").equals(epcDocument.getString("bizStep")) &&
                                templateObject.getString("bizLocation").equals(epcDocument.getJSONObject("bizLocation").getString("id")) &&
                                templateObject.getString("readPoint").equals(epcDocument.getJSONObject("readPoint").getString("id"))) {

                            epcDocument.put("used", "true");
                            Long eventTime = epcDocument.getJSONObject("eventTime").getLong("$date");

                            if(templateObject.getString("hasNext").isEmpty()) {
                                if(lastEPCItem.length() != 0 ) {
                                    Long lastEPCItemEventTime = lastEPCItem.getJSONObject("eventTime").getLong("$date");
                                    epcItemTotalDuration += getDateTimeDifference(eventTime, lastEPCItemEventTime);
                                } else{
                                    epcItemTotalDuration += getDateTimeDifference(System.currentTimeMillis(), eventTime);
                                }
                                nextEventId = null;
                                epcItemTotalCalculated = false;
                            } else {
                                if(lastEPCItem.length() != 0 ) {
                                    Long lastEPCItemEventTime = lastEPCItem.getJSONObject("eventTime").getLong("$date");
                                    epcItemTotalDuration += getDateTimeDifference(eventTime, lastEPCItemEventTime);
                                    epcItemTotalCalculated = false;
                                }
                                nextEventId = Integer.parseInt(templateObject.getString("hasNext"));
                                lastEPCItem = epcDocument;
                            }
                        }
                    }

                    if(lastEPCItem.length() != 0 && eventDataList.length() == j +1) {
                        Long lastEPCItemEventTime = lastEPCItem.getJSONObject("eventTime").getLong("$date");
                        epcItemTotalDuration += getDateTimeDifference(System.currentTimeMillis(), lastEPCItemEventTime);
                        nextEventId = null;
                        epcItemTotalCalculated = false;
                    }
                }
            }

            JSONObject jsonTemplateObject = jsonTemplateArray.getJSONObject(i);
            totalDuration += Integer.parseInt(jsonTemplateObject.getString("durationToNext"));
            if(epcItemTotalCalculated) {
                epcItemTotalDuration += Integer.parseInt(jsonTemplateObject.getString("durationToNext"));
            }
        }

        Integer delay = epcItemTotalDuration - totalDuration ;
        JSONObject responseObject = new JSONObject();
        responseObject.put("unit", "H");
        responseObject.put("delay", delay);
        responseObject.put("epc", item);
        return responseObject;
    }

    private int getDateTimeDifference(Long time1, Long time2) {
        long milliseconds = time1 - time2;
        int seconds = (int) milliseconds / 1000;
        int hours = seconds / 3600;
		/*int minutes = (seconds % 3600) / 60;
		seconds = (seconds % 3600) % 60;*/
        return hours;
    }

    private JSONArray getJsonEPCList(String item, String bearerToken) {
        // define url
        String url = epcisURL.trim();
        if(!url.endsWith("/"))
        {
            url = url + "/";
        }
        url = url + "Poll/SimpleEventQuery?MATCH_epc=" + item
                + "&orderBy=eventTime&orderDirection=DESC&format=JSON";

        // get the event data list from the epcis application
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);
        JSONArray eventDataList = new JSONArray(response.getBody());
        return eventDataList;
    }
}
