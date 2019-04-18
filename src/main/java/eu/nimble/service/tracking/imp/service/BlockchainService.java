package eu.nimble.service.tracking.imp.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;

import eu.nimble.service.tracking.impl.controller.TrackingController;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@CrossOrigin()
@Service
public class BlockchainService {
	private static Logger log = LoggerFactory.getLogger(TrackingController.class);

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${spring.blockchain-service.url}")
	public String blockchainURL;
	
	/**
	 * Verify tracking event in Blockchain
	 * @param jsonEventArray an string representation of JSON array of tracking events
	 * @return true, all tracking events found in Blockchain; false, otherwise
	 */
	public boolean verifyTrackingEvents(String jsonEventArray)
	{
		boolean verified = true;
		JSONArray eventList = new JSONArray(jsonEventArray);
		
		 for (int i = 0 ; i < eventList.length(); i++) {
		        JSONObject obj = eventList.getJSONObject(i);

		        if(!this.verifyTrackingEvent(obj))
		        {
		        	verified = false;
		        }
		    }
		
		
		return verified;
	}
	
	/**
	 * Verify a single tracking event in Blockchain
	 * @param jsonEventObj single tracking event in json
	 * @return true, found in Blockchain; false, otherwise
	 */
	public boolean verifyTrackingEvent(JSONObject jsonEventObj) 
	{
		String completeHash = this.generateUnifiedHashCode(jsonEventObj);
		
		boolean found  = this.verifyHashCode(completeHash);
		
		return found;
	}

	
	/**
	 * Generate complete hash for a given Json event object
	 * @param jsonObj
	 * @return
	 */
	private String generateUnifiedHashCode(JSONObject jsonObj)
	{
		JSONObject unifiedJsonFields = this.unifyJsonObjFields(jsonObj);
		String unifiedJsonStr = this.unifyJsonStringForHash(unifiedJsonFields);
		String completeHash = DigestUtils.sha256Hex(unifiedJsonStr);
		
		System.out.print("unifiedJSONStr: " + unifiedJsonStr + "\n");
		System.out.print("completeHash: " + completeHash + "\n");
		return completeHash;
	}

	/**
	 * Transform a given JSON Event back to the format when the event is sent to the Blockchain. 
	 * Because, the hash code stored in the blockchain was calculated base on that format. 
	 * Without a unified JSON format i.e. fields, it is not possible to compare hash code for verification. 
	 * @param jsonObj 
	 * @return JSON Event with a unified JSON fields
	 */
	private JSONObject unifyJsonObjFields(JSONObject jsonObj)
	{
		// Shallow copy
		JSONObject unifiedJsonObj = new JSONObject(jsonObj, JSONObject.getNames(jsonObj));
		
		// When company calculate hash, or send event data to Block-chain and local data storage,
		// the fields "_id", "userPartyID" are not included.
		List<String> removableFields =  Arrays.asList(new String[] { "_id", "userPartyID", "recordTime" });;

		for(String removableField : removableFields)
		{
			unifiedJsonObj.remove(removableField);
		}
		
		return unifiedJsonObj;
	}
	
	/**
	 * Unify string representation of a JSON Event Object, in order to have same hash code for identical JSON objects.
	 * 
	 * It can often happen, that identical JSON objects are presented as different strings. It will lead to different hash codes.
	 * Because, for example, 1) key locations is different 2) value is different sorted in array 
	 * 
	 * In order to avoid this problem, this method will do following to have a unified string representation:
	 * 1) Flatten key value pairs
	 * 2) Remove the generated list numbers in the flattened key
	 * 3) Each key value pair is connected as a string
	 * 4) Sort the key value string
	 * @param jsonObj JSON Event Object.
	 * @return unified string representation.
	 */
	private String unifyJsonStringForHash(JSONObject jsonObj) 
	{		
		Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonObj.toString());
		
		// Remove numbers from key e.g. "[0]" from "a.d[0]", because the number could be different each time during JSON flatten
		String regexListNr = "\\[\\d+\\]";
		List<String> flattenKeyValPairList = flattenJson.entrySet().stream().
				map(entry -> entry.getKey().replaceAll(regexListNr, "") + "=" + entry.getValue()).
				collect(Collectors.toList());
		
		List<String> sortedFlattenKeyValPairList = flattenKeyValPairList.stream().sorted().collect(Collectors.toList());
		
		return String.join(",", sortedFlattenKeyValPairList);
	}
	
	
	
	/**
	 * Verify hash code in Blockchain
	 * @param eventHash
	 * @return true, hash code is found; false, otherwise
	 */
	private boolean verifyHashCode(String eventHash)
	{
		boolean found = false;
		
		String url = blockchainURL;		

		HttpHeaders headers = new HttpHeaders();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("hash", eventHash)
				.queryParam("type", "rfid");
		HttpEntity<?> entity = new HttpEntity<>(headers);
		
		try {
			HttpEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity,
					String.class);
			
			 JSONObject jsonResponse = new JSONObject(response.getBody());
			 boolean successFlag = jsonResponse.getBoolean("success");
		     String successMsg = jsonResponse.getString("message");
		     
		     if(successFlag && successMsg.endsWith("found successfully"))
		     {
		    	 found = true;
		     }
		        
		} catch (HttpStatusCodeException e) {
			log.error("Received error during call blockchain services: " + e.getResponseBodyAsString());
		}

		return found;
	}
	
	private String getFileWithUtil(String fileName) throws IOException {
		String result = "";

		ClassLoader classLoader = getClass().getClassLoader();
		result = IOUtils.toString(classLoader.getResourceAsStream(fileName));

		return result;
	}
	
	
//	public static void main(String[] args) throws Exception {
//		String filename = "testFile/testJson.json";
//
//		BlockchainService bcSrv = new BlockchainService();
//		String content = bcSrv.getFileWithUtil(filename);
//		
//		boolean success = bcSrv.verifyTrackingEvents(content);
//		
//		System.out.print("verification successful: " + success);
//	}
	
}
