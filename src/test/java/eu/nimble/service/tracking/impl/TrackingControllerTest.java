package eu.nimble.service.tracking.impl;

import org.junit.After;
import org.junit.Before;

import eu.nimble.service.tracking.impl.controller.TrackingController;

//@RunWith(SpringRunner.class)
//@WebMvcTest(value = TrackingController.class, secure = false)
public class TrackingControllerTest {

	//@Autowired
	//private MockMvc mockMvc;
	
	//@Autowired 
    //private TestRestTemplate  restTemplate;
    
    private TrackingController trackingController;

    String exampleJson = "{\"name\":\"OK\"}";
    
    @Before
    public void setUp() {
    	trackingController = new TrackingController();
    	
    }
    /*
    @Test
    public void testSimpleTracking() throws Exception {
    	Mockito.when(restTemplate.getForObject(Mockito.anyString(), Matchers.any(Class.class))).thenReturn(exampleJson);
    	
    	RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/simpleTracking/testID").accept(MediaType.APPLICATION_JSON);
    	
    	MvcResult result = mockMvc.perform(requestBuilder).andReturn();
    	
    	JSONAssert.assertEquals(exampleJson, result.getResponse()
				.getContentAsString(), false);
    }  
    
    @Test
    public void testGetMasterDataByID() {
        ResponseEntity<?> ret = null;
		
		ret = trackingController.getMasterDataByID(
					"urn:epc:id:sgln:bizLocation.lindbacks.1");
	
		assertNotNull(ret);
        assertEquals(HttpStatus.OK, ret.getStatusCode());
    }   
    
    @Test
    public void testGetMasterDataByTypeAndID() {
        ResponseEntity<?> ret = null;
		
		ret = trackingController.getMasterDataByTypeAndID("urn:epcglobal:epcis:vtype:BusinessLocation",
					"urn:epc:id:sgln:bizLocation.lindbacks.1");
	
		assertNotNull(ret);
        assertEquals(HttpStatus.OK, ret.getStatusCode());
    }    

    @Test
    public void testGetBusinessProcess() {
        ResponseEntity<?> ret = null;
		try {
			ret = trackingController.getBusinessProcess("lindback");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(ret);
        assertEquals(HttpStatus.OK, ret.getStatusCode());

    }

*/

    @After
    public void tearDown() {
    }


}
