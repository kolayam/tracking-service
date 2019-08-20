package eu.nimble.service.tracking.impl.controller;

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

@RestController
public class TransformationEventController {
    @Value("${spring.epcis.url}")
    private String epcisURL;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/getEpcTransformationOutput")
    public ResponseEntity<?> getEpcTransformationOutput(@RequestParam("epc") String epc,
            @RequestHeader(value = "Authorization", required = true)  String bearerToken) {

        JSONArray transformationEventList = getJsonEPCList(bearerToken);
        JSONObject traceTreeObject = new JSONObject();

        JSONObject entityObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        while(true) {
            entityObject = getEntityObject(epc, transformationEventList);
            if(entityObject.length() == 0) {
                JSONObject object = new JSONObject();
                object.put("epc", epc);
                JSONObject object1 = new JSONObject();
                object1.put("Entity", object);
                jsonArray.put(object1);
               break;
            } else {
                jsonArray.put(entityObject);
                epc = entityObject.getJSONObject("Entity").getString("hasOutput");
            }
        }

        traceTreeObject.put("traceTree", jsonArray);
        return new ResponseEntity<>( traceTreeObject.toString(), HttpStatus.OK);
    }

    private JSONObject getEntityObject(String epc, JSONArray transformationEventList) {

        JSONObject obj = new JSONObject();
        for (int i = 0; i < transformationEventList.length(); i++) {
            String inputEPC = transformationEventList.getJSONObject(i).getJSONArray("inputEPCList")
                    .getJSONObject(0).getString("epc");
            JSONObject entityObject = new JSONObject();
            if(epc.equals(inputEPC)) {
                String outputEPC = transformationEventList.getJSONObject(i).getJSONArray("outputEPCList")
                        .getJSONObject(0).getString("epc");
                entityObject.put("epc", epc);
                entityObject.put("hasOutput", outputEPC);
                obj.put("Entity", entityObject);
            }
        }
        return obj;
    }

    private JSONArray getJsonEPCList( String bearerToken) {
        // define url
        String url = epcisURL.trim();
        if(!url.endsWith("/"))
        {
            url = url + "/";
        }
        String eventType = "TransformationEvent";
        url = url + "/Poll/SimpleEventQuery?format=JSON&eventType=" + eventType;

        // get the event data list from the epcis application
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);
        JSONArray transformationEventList = new JSONArray(response.getBody());
        return transformationEventList;
    }
}
