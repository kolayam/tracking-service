package eu.nimble.service.tracking.impl.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import eu.nimble.service.tracking.imp.service.BlockchainService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.json.JsonObject;

/**
 * This controller should be implemented internally by company, who want to
 * connect NIMBLE platform for Tracking and Tracing. It basically provide three
 * APIs: 1. /simpleTracking Simple tracking for a product item with EPC code. It
 * returns a list of object events related to the product, sorted by eventTime
 * in descending order. 2. /masterData Get master data for given vocabulary
 * element by vocabulary type and vocabulary ID It returns description of a
 * single master data vocabulary element as a list of master data vocabulary
 * element. 3. /productionProcessTemplate Get process template for the given
 * product class ID. It returns a list of process steps from a single production
 * process template.
 * 
 * @author dqu
 *
 */
@CrossOrigin()
@RestController
public class TrackingController {

	private static Logger log = LoggerFactory.getLogger(TrackingController.class);

	/**
	 * URL of the EPCIS repository in company, from which the EPCIS events and
	 * master data as well as NIMBLE production process template can be retrieved.
	 */
	@Value("${spring.epcis.url}")
	private String epcisURL;
	
	@Autowired
	private BlockchainService blockchainService;

	@Autowired
	private RestTemplate restTemplate;

//    /**
//     * Simple tracking for a product item with EPC code. (using params in URL)
//     * Return a list of object events related to the product, sorted by eventTime in descending order
//     * 
//     * @param epc product item ID, e.g. urn:epc:id:sgtin:0614141.lindback.testproduct
//     * @return On success: JSON structure, which is a list of EPCIS object events.
//     * e.g. 
//     * [
//		 {
//		  "eventTimeZoneOffset": "-06:00",
//		  "bizStep": "urn:epcglobal:cbv:bizstep:entering_exiting",
//		  "recordTime": {"$date": 1524156074162},
//		  "readPoint": {"id": "urn:epc:id:sgln:readPoint.lindbacks.3"},
//		  "eventTime": {"$date": 1523414011116},
//		  "action": "OBSERVE",
//		  "bizLocation": {"id": "urn:epc:id:sgln:bizLocation.lindbacks.4"},
//		  "_id": {"$oid": "5ad8c6aabe0777000174179d"},
//		  "eventType": "ObjectEvent",
//		  "epcList": [{"epc": "urn:epc:id:sgtin:0614141.lindback.testproduct"}]
//		 }
//		]
//		
//		On Failure: An empty list will be returned when the product item is not found.
//     * @throws URISyntaxException 
//     */
//    @RequestMapping("/simpleTracking")
//    public ResponseEntity<?> simpleTracking(@RequestParam(required = true) String epc,  
//    		@RequestHeader(value="Authorization", required=true) String bearerToken) throws URISyntaxException {
//        String url = epcisURL + "Poll/SimpleEventQuery?MATCH_epc=" 
//        		+ epc + "&orderBy=eventTime&orderDirection=DESC&format=JSON";
//       
//
//        log.info("epc:" + epc);
//        log.info("URL:" + url);
//        
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", bearerToken);
//
//        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers) , String.class);
//        String result = response.getBody();
//
//        log.debug("RESPONSE " + result);
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
//        return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
//    }
//    
//    /**
//     * Get master data for given vocabulary element by vocabulary type and vocabulary ID. (using params in URL)
//     * @param type EPCIS Master Data Vocabulary Type, for example, urn:epcglobal:epcis:vtype:BusinessLocation
//     * @param id EPCIS Master Data Vocabulary Element ID, for example, urn:epc:id:sgln:bizLocation.lindbacks.1
//     * @return On Success: Return a list of master data vocabulary element, 
//     * 		when the given type and ID is unique, the list should contain only one element 
//     * e.g. 
//     * [{
//		 "attributes": {
//		  "lastUpdated": {"$date": 1523540863137},
//		  "urn:epcglobal:cbv:definition": ["Denotes a specific activity within a\n\t\t\t\t\t\t\tbusiness process where an object changes possession and/or\n\t\t\t\t\t\t\townership."],
//		  "urn:epcglobal:cbv:example": [
//		   "A wholesaler is assigned a lot of fish at a fish auction, verifies the quantity and acknowledges receipt.",
//		  ]
//		 },
//		 "id": "urn:epcglobal:cbv:bizstep:accepting",
//		 "type": "urn:epcglobal:epcis:vtype:BusinessStep"
//		}]
//		
//		On Failure: An empty list will be returned when the master data element vocabulary is not found.
//     */
//    @RequestMapping("/masterData")
//    public ResponseEntity<?> getMasterData(@RequestParam(required = false) String type, @RequestParam(required = true) String id,
//    		@RequestHeader(value="Authorization", required=true) String bearerToken) {
//    	
//    	String vocabularyElementID = id;
//    	String vocabularyElementType = type;
//    	
//    	String url = "";
//    	if(vocabularyElementType != null && !vocabularyElementType.isEmpty())
//    	{
//         url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name=" 
//        		+ vocabularyElementID + "&vocabularyName=" + vocabularyElementType + "&format=JSON";
//    	}
//    	else
//    	{
//         url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name=" 
//        		+ vocabularyElementID + "&format=JSON";
//    	}
//
//    	log.info("vocabularyElementID:" + vocabularyElementID);
//    	log.info("vocabularyElementType:" + vocabularyElementType);
//    	log.info("URL:" + url);
//    	
//		HttpHeaders headers = new HttpHeaders();
//		headers.set("Authorization", bearerToken);
//
//		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
//				String.class);
//		String result = response.getBody();
//		        
//        log.debug("RESPONSE " + result);
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
//        return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
//    }   

	/**
	 * Get process template for the given product class ID. (using Path Variable )
	 * 
	 * Product class means the product uploaded in the NIMBLE catalogue.
	 * 
	 * @param productClass product class ID in the NIMBLE catalogue.
	 * @return On Success: a list of process steps from a single production process
	 *         template e.g. [ { "id": "1", "hasPrev": "", "readPoint":
	 *         "urn:epc:id:sgln:readPoint.lindbacks.1", "bizLocation":
	 *         "urn:epc:id:sgln:bizLocation.lindbacks.2", "bizStep":
	 *         "urn:epcglobal:cbv:bizstep:other", "hasNext": "2" }, { "id": "2",
	 *         "hasPrev": "1", "readPoint": "urn:epc:id:sgln:readPoint.lindbacks.2",
	 *         "bizLocation": "urn:epc:id:sgln:bizLocation.lindbacks.3", "bizStep":
	 *         "urn:epcglobal:cbv:bizstep:installing", "hasNext": "3" } ]
	 * @throws IOException
	 */
	@ApiOperation(value = "Get production process template for the given product class", 
			notes = "Return production process template, which consists of a list of production process steps",
			tags = {"Production Process Template"}, produces = "application/json",
			response = eu.nimble.service.tracking.model.ProductionProcessStep.class, responseContainer="List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "On success, return a list of production process steps. Return a empty list '[]'',in case Production Process Template is not found"),
	@ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?")})
	@GetMapping("/productionProcessTemplate/{productClass}")
	public ResponseEntity<?> getProductionProcessTemplate(@ApiParam(value = "NIMBLE Product catergory ID from which process template will retrieve", required = true) @PathVariable(value = "productClass") String productClass, 
			@ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken) throws IOException {
		String url = epcisURL.trim();
		if(!url.endsWith("/"))
		{
			url = url + "/";
		}
		url = url + "/GetProductionProcessTemplate/" + productClass;


		log.info("productClass:" + productClass);
		log.info("URL:" + url);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", bearerToken);

		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		String result = response.getBody();

		log.debug("RESPONSE " + result);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
	}

	/**
	 * Simple tracking for a product item. (using Path Variable ) Return a list of
	 * object events related to the product, sorted by eventTime in descending order
	 * 
	 * @param epc product item ID, e.g.
	 *            urn:epc:id:sgtin:0614141.lindback.testproduct
	 * @return On success: JSON structure, which is a list of EPCIS object events.
	 *         e.g. [ { "eventTimeZoneOffset": "-06:00", "bizStep":
	 *         "urn:epcglobal:cbv:bizstep:entering_exiting", "recordTime": {"$date":
	 *         1524156074162}, "readPoint": {"id":
	 *         "urn:epc:id:sgln:readPoint.lindbacks.3"}, "eventTime": {"$date":
	 *         1523414011116}, "action": "OBSERVE", "bizLocation": {"id":
	 *         "urn:epc:id:sgln:bizLocation.lindbacks.4"}, "_id": {"$oid":
	 *         "5ad8c6aabe0777000174179d"}, "eventType": "ObjectEvent", "epcList":
	 *         [{"epc": "urn:epc:id:sgtin:0614141.lindback.testproduct"}] } ]
	 * 
	 *         On Failure: An empty list will be returned when the product item is
	 *         not found.
	 */
	@ApiOperation(value = "Track by Product ID", 
			notes = "Return a list of Object events related to the product, sorted by eventTime in descending order",
			tags = {"Event Data"}, produces = "application/json",
			responseContainer="List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "On success, return an array of EPCIS object events. Return a empty list '[]'',in case product with given EPC code is not found."),
	@ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?")})
	@GetMapping("/simpleTracking/{itemID:.+}")
	public ResponseEntity<?> simpleTrackingWithID(@ApiParam(value = " Product EPC code from which tracking information will retrieve", required = true) @PathVariable String itemID,
			@ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true)  String bearerToken) {
		String url = epcisURL.trim();
		if(!url.endsWith("/"))
		{
			url = url + "/";
		}
		url = url + "Poll/SimpleEventQuery?MATCH_epc=" + itemID
				+ "&orderBy=eventTime&orderDirection=DESC&format=JSON";

		log.info("epc:" + itemID);
		log.info("URL:" + url);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", bearerToken);

		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		String result = response.getBody();
	
		log.debug("RESPONSE " + result);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
	}


	@PostMapping("/getEPCTimeDelay")
	public ResponseEntity<?> getEPCTimeDelay(@RequestParam("item") String item,
			  @RequestBody String inputDocument,
			  @RequestHeader(value = "Authorization", required = true)  String bearerToken) {

		// Initialize variable for total and epcItem duration
        Integer totalDuration = 0;
        Integer epcItemTotalDuration = 0;

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
        String result = response.getBody();

        // take epcis list and make it json array
		JSONObject jsonObject = new JSONObject(inputDocument);
        JSONArray epcList = new JSONArray(response.getBody());


        JSONObject lastEPCItem = new JSONObject();

        // take the json template
		JSONArray jsonTemplateArray = jsonObject.getJSONArray("productionProcessTemplate");
		// initialize nextEventId
		Integer nextEventId = 0;
		for (int i = 0; i < jsonTemplateArray.length(); i++) {
			boolean epcItemTotalCalculated = true;
			if(i== 0 || nextEventId != null) {
                JSONObject templateObject = jsonTemplateArray.getJSONObject(nextEventId);
                for (int j = 0; j < epcList.length(); j++) {
                    JSONObject epcDocument = epcList.getJSONObject(j);
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

					if(lastEPCItem.length() != 0 && epcList.length() == j +1) {
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
		System.out.println(delay);
		System.out.println(epcItemTotalDuration);
		System.out.println(totalDuration);
		JSONObject responseObject = new JSONObject();
		responseObject.put("item",  item);
		responseObject.put("delay", delay);

		return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
	}

	private int getDateTimeDifference(Long time1, Long time2) {
		long milliseconds = time1 - time2;
		int seconds = (int) milliseconds / 1000;
		int hours = seconds / 3600;
		/*int minutes = (seconds % 3600) / 60;
		seconds = (seconds % 3600) % 60;*/
		return hours;
	}
	

	/**
	 * Get master data for given vocabulary element by vocabulary ID. (using Path
	 * Variable )
	 * 
	 * @param vocabularyElementID EPCIS Master Data Vocabulary Element ID, for
	 *                            example, urn:epc:id:sgln:bizLocation.lindbacks.1
	 * @return On Success: Return description of a single master data vocabulary
	 *         element e.g. [{ "attributes": { "lastUpdated": {"$date":
	 *         1523540863137}, "urn:epcglobal:cbv:definition": ["Denotes a specific
	 *         activity within a\n\t\t\t\t\t\t\tbusiness process where an object
	 *         changes possession and/or\n\t\t\t\t\t\t\townership."],
	 *         "urn:epcglobal:cbv:example": [ "A wholesaler is assigned a lot of
	 *         fish at a fish auction, verifies the quantity and acknowledges
	 *         receipt.", ] }, "id": "urn:epcglobal:cbv:bizstep:accepting", "type":
	 *         "urn:epcglobal:epcis:vtype:BusinessStep" }]
	 * 
	 *         On Failure: An empty list will be returned when the master data
	 *         element vocabulary is not found.
	 */
	@ApiOperation(value = "Find EPCIS Master Data vocabulary element ID", 
			notes = "Return a list of master data vocabulary element, when the given vocabulary element type and ID is unique, the list contains only one master data vocabulary element ",
			tags = {"Master Data"}, produces = "application/json",
			responseContainer="List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "On success, return an array of EPCIS object events. Return a empty list '[]'',in case product with given EPC code is not found."),
	@ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?")})
	@GetMapping("/masterData/id/{vocabularyElementID:.+}")
	public ResponseEntity<?> getMasterDataByID(@ApiParam(value = "EPCIS Master Data Vocabulary Element ID from which master data information will retrieve", required = true) @PathVariable String vocabularyElementID,
			@ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true)   String bearerToken) {
		String url = epcisURL.trim();
		if(!url.endsWith("/"))
		{
			url = url + "/";
		}
		url = url + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name="
				+ vocabularyElementID + "&format=JSON";

		log.info("vocabularyElementID:" + vocabularyElementID);
		log.info("URL:" + url);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", bearerToken);

		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		String result = response.getBody();

		log.debug("RESPONSE " + result);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
	}

	/**
	 * Get master data for given vocabulary element by vocabulary type and
	 * vocabulary ID. (usingPath Variable)
	 * 
	 * @param vocabularyElementType EPCIS Master Data Vocabulary Type, for example,
	 *                              urn:epcglobal:epcis:vtype:BusinessLocation
	 * @param vocabularyElementID   EPCIS Master Data Vocabulary Element ID, for
	 *                              example, urn:epc:id:sgln:bizLocation.lindbacks.1
	 * @return On Success: Return description of a single master data vocabulary
	 *         element e.g. [{ "attributes": { "lastUpdated": {"$date":
	 *         1523540863137}, "urn:epcglobal:cbv:definition": ["Denotes a specific
	 *         activity within a\n\t\t\t\t\t\t\tbusiness process where an object
	 *         changes possession and/or\n\t\t\t\t\t\t\townership."],
	 *         "urn:epcglobal:cbv:example": [ "A wholesaler is assigned a lot of
	 *         fish at a fish auction, verifies the quantity and acknowledges
	 *         receipt.", ] }, "id": "urn:epcglobal:cbv:bizstep:accepting", "type":
	 *         "urn:epcglobal:epcis:vtype:BusinessStep" }]
	 * 
	 *         On Failure: An empty list will be returned when the master data
	 *         element vocabulary is not found.
	 */
	@ApiOperation(value = "Find EPCIS Master Data vocabulary element by Type and ID", 
			notes = "Return a list of master data vocabulary element, when the given vocabulary element type and ID is unique, the list contains only one master data vocabulary element ",
			tags = {"Master Data"}, produces = "application/json",
			responseContainer="List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "On success, return an list of master data vocabulary element. Return a empty list '[]'', in case Master Data Vocabulary Element is not found"),
	@ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?")})
	@GetMapping("/masterData/type/{vocabularyElementType}/id/{vocabularyElementID:.+}")
	public ResponseEntity<?> getMasterDataByTypeAndID(@ApiParam(value = "EPCIS Master Data Vocabulary Type, for example, urn:epcglobal:epcis:vtype:BusinessLocation", required = true) @PathVariable String vocabularyElementType,
			@ApiParam(value = "EPCIS Master Data Vocabulary Element ID, for example, urn:epc:id:sgln:bizLocation.lindbacks.1", required = true) @PathVariable String vocabularyElementID,
			@ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value="Authorization", required=true) String bearerToken) {
		String url = epcisURL.trim();
		if(!url.endsWith("/"))
		{
			url = url + "/";
		}
		url = url + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name="
				+ vocabularyElementID + "&vocabularyName=" + vocabularyElementType + "&format=JSON";

		log.info("vocabularyElementID:" + vocabularyElementID);
		log.info("vocabularyElementType:" + vocabularyElementType);
		log.info("URL:" + url);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", bearerToken);

		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		String result = response.getBody();

		log.debug("RESPONSE " + result);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
	}
	
	/**
	 * Verify tracking event in Blockchain
	 * @param jsonEventArray an string representation of JSON array of tracking events
	 * @param bearerToken bearerToken of an NIMBLE account
	 * @return true, all tracking events found in Blockchain; false, otherwise
	 */
	@ApiOperation(value = "", notes = "Verify a list of EPCIS Events in JSON Array. An example list of EPCIS Event is: <br> <textarea disabled style=\"width:98%\" class=\"body-textarea\">" 
			+ " [\r\n" + 
			"  {\r\n" + 
			"    \"eventTimeZoneOffset\": \"-06:00\",\r\n" + 
			"    \"bizStep\": \"urn:epcglobal:cbv:bizstep:entering_exiting\",\r\n" + 
			"    \"recordTime\": {\r\n" + 
			"      \"$date\": 1524156074162\r\n" + 
			"    },\r\n" + 
			"    \"readPoint\": {\r\n" + 
			"      \"id\": \"urn:epc:id:sgln:readPoint.lindbacks.3\"\r\n" + 
			"    },\r\n" + 
			"    \"eventTime\": {\r\n" + 
			"      \"$date\": 1523414011116\r\n" + 
			"    },\r\n" + 
			"    \"action\": \"OBSERVE\",\r\n" + 
			"    \"bizLocation\": {\r\n" + 
			"      \"id\": \"urn:epc:id:sgln:bizLocation.lindbacks.4\"\r\n" + 
			"    },\r\n" + 
			"    \"_id\": {\r\n" + 
			"      \"$oid\": \"5ad8c6aabe0777000174179d\"\r\n" + 
			"    },\r\n" + 
			"    \"eventType\": \"ObjectEvent\",\r\n" + 
			"    \"epcList\": [\r\n" + 
			"      {\r\n" + 
			"        \"epc\": \"urn:epc:id:sgtin:0614141.lindback.testproduct\"\r\n" + 
			"      }\r\n" + 
			"    ]\r\n" + 
			"  }\r\n" + 
			"]"
			+ "</textarea> ", tags = {"Blockchain Verification"}, response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "On success, return true. False, otherwise"),
			@ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?")})
	@ApiImplicitParam(name = "jsonEventArray", value = "A JSON Array representing a list EPCIS Events, which require Blockchain verification.", dataType = "String", paramType = "body", required = true)
	@PostMapping(value = "/verifyEventsInBlockChain") 
	public ResponseEntity<Boolean> verifyTrackingEventsInBlockchain(@RequestBody String jsonEventArray, 
			@ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value="Authorization", required=true) String bearerToken)
	{
		boolean found = blockchainService.verifyTrackingEvents(jsonEventArray);
		
		return new ResponseEntity<Boolean>(found, HttpStatus.OK); 
	}

}
