package eu.nimble.service.tracking.impl.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
	private RestTemplate restTemplate;

    /**
     * Simple tracking for a product item with EPC code. (using params in URL)
     * Return a list of object events related to the product, sorted by eventTime in descending order
     * 
     * @param epc product item ID, e.g. urn:epc:id:sgtin:0614141.lindback.testproduct
     * @return On success: JSON structure, which is a list of EPCIS object events.
     * e.g. 
     * [
		 {
		  "eventTimeZoneOffset": "-06:00",
		  "bizStep": "urn:epcglobal:cbv:bizstep:entering_exiting",
		  "recordTime": {"$date": 1524156074162},
		  "readPoint": {"id": "urn:epc:id:sgln:readPoint.lindbacks.3"},
		  "eventTime": {"$date": 1523414011116},
		  "action": "OBSERVE",
		  "bizLocation": {"id": "urn:epc:id:sgln:bizLocation.lindbacks.4"},
		  "_id": {"$oid": "5ad8c6aabe0777000174179d"},
		  "eventType": "ObjectEvent",
		  "epcList": [{"epc": "urn:epc:id:sgtin:0614141.lindback.testproduct"}]
		 }
		]
		
		On Failure: An empty list will be returned when the product item is not found.
     * @throws URISyntaxException 
     */
    @RequestMapping("/simpleTracking")
    public ResponseEntity<?> simpleTracking(@RequestParam(required = true) String epc,  
    		@RequestHeader(value="Authorization", required=true) String bearerToken) throws URISyntaxException {
        String url = epcisURL + "Poll/SimpleEventQuery?MATCH_epc=" 
        		+ epc + "&orderBy=eventTime&orderDirection=DESC&format=JSON";
       

        log.info("epc:" + epc);
        log.info("URL:" + url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers) , String.class);
        String result = response.getBody();

        log.debug("RESPONSE " + result);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
    }
    
    /**
     * Get master data for given vocabulary element by vocabulary type and vocabulary ID. (using params in URL)
     * @param type EPCIS Master Data Vocabulary Type, for example, urn:epcglobal:epcis:vtype:BusinessLocation
     * @param id EPCIS Master Data Vocabulary Element ID, for example, urn:epc:id:sgln:bizLocation.lindbacks.1
     * @return On Success: Return a list of master data vocabulary element, 
     * 		when the given type and ID is unique, the list should contain only one element 
     * e.g. 
     * [{
		 "attributes": {
		  "lastUpdated": {"$date": 1523540863137},
		  "urn:epcglobal:cbv:definition": ["Denotes a specific activity within a\n\t\t\t\t\t\t\tbusiness process where an object changes possession and/or\n\t\t\t\t\t\t\townership."],
		  "urn:epcglobal:cbv:example": [
		   "A wholesaler is assigned a lot of fish at a fish auction, verifies the quantity and acknowledges receipt.",
		  ]
		 },
		 "id": "urn:epcglobal:cbv:bizstep:accepting",
		 "type": "urn:epcglobal:epcis:vtype:BusinessStep"
		}]
		
		On Failure: An empty list will be returned when the master data element vocabulary is not found.
     */
    @RequestMapping("/masterData")
    public ResponseEntity<?> getMasterData(@RequestParam(required = false) String type, @RequestParam(required = true) String id,
    		@RequestHeader(value="Authorization", required=true) String bearerToken) {
    	
    	String vocabularyElementID = id;
    	String vocabularyElementType = type;
    	
    	String url = "";
    	if(vocabularyElementType != null && !vocabularyElementType.isEmpty())
    	{
         url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name=" 
        		+ vocabularyElementID + "&vocabularyName=" + vocabularyElementType + "&format=JSON";
    	}
    	else
    	{
         url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name=" 
        		+ vocabularyElementID + "&format=JSON";
    	}

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
	@RequestMapping("/productionProcessTemplate/{productClass}")
	public ResponseEntity<?> getProductionProcessTemplate(@PathVariable String productClass, 
			@RequestHeader(value="Authorization", required=true) String bearerToken) throws IOException {
		String url = epcisURL + "GetProductionProcessTemplate/" + productClass;

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
	@RequestMapping("/simpleTracking/{itemID:.+}")
	public ResponseEntity<?> simpleTrackingWithID(@PathVariable String itemID,
			@RequestHeader(value="Authorization", required=true) String bearerToken) {

		String url = epcisURL + "Poll/SimpleEventQuery?MATCH_epc=" + itemID
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
	@RequestMapping("/masterData/id/{vocabularyElementID:.+}")
	public ResponseEntity<?> getMasterDataByID(@PathVariable String vocabularyElementID,
			@RequestHeader(value="Authorization", required=true) String bearerToken) {

		String url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name="
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
	@RequestMapping("/masterData/type/{vocabularyElementType}/id/{vocabularyElementID:.+}")
	public ResponseEntity<?> getMasterDataByTypeAndID(@PathVariable String vocabularyElementType,
			@PathVariable String vocabularyElementID,
			@RequestHeader(value="Authorization", required=true) String bearerToken) {

		String url = epcisURL + "Poll/SimpleMasterDataQuery?includeAttributes=true&includeChildren=true&EQ_name="
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

}
