package eu.nimble.service.tracking.impl;

import eu.nimble.service.tracking.TrackingApplication;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TrackingApplication.class)
public class TransformationEventControllerTest {

    @Value("${test.base-url}")
    private String baseUrl;

    private String getBaseUrl() {
        return baseUrl + "/Service";
    }

    @Value("${test.accessToken}")
    private String accessToken;

    String testTransformationEventData = "{\n" +
            "  \"epcis\": {\n" +
            "    \"EPCISBody\": {\n" +
            "      \"EventList\": [\n" +
            "          {\n" +
            "          \"TransformationEvent\": {\n" +
            "            \"eventTime\": 1523500411116,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"inputEPCList\": [\n" +
            "              {\n" +
            "                \"epc\": \"TEST-1\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"outputEPCList\": [\n" +
            "              {\n" +
            "                \"epc\": \"TEST-2\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"bizStep\": \"urn:epcglobal:cbv:bizstep:installing\",\n" +
            "            \"readPoint\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:readPoint.PodComp.3\"\n" +
            "            },\n" +
            "            \"bizLocation\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:bizLocation.PodComp.4\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"TransformationEvent\": {\n" +
            "            \"eventTime\": 1523500411116,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"inputEPCList\": [\n" +
            "              {\n" +
            "                \"epc\": \"TEST-2\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"outputEPCList\": [\n" +
            "              {\n" +
            "                \"epc\": \"TEST-3\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"bizStep\": \"urn:epcglobal:cbv:bizstep:installing\",\n" +
            "            \"readPoint\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:readPoint.PodComp.3\"\n" +
            "            },\n" +
            "            \"bizLocation\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:bizLocation.PodComp.4\"\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    public void jsonEventCapture() {
        this.postRestAPITest(this.getBaseUrl() + "/JSONEventCapture", "application/json", testTransformationEventData);
    }

    private void postRestAPITest(String url, String contentType, String entity) {
        try {
            HttpPost httpRequest = new HttpPost(url);
            httpRequest.addHeader("Authorization", accessToken);
            httpRequest.setHeader("Content-Type", contentType);
            StringEntity xmlEntity = new StringEntity(entity);
            httpRequest.setEntity(xmlEntity );
            HttpResponse httpresponse = HttpClientBuilder.create().build().execute(httpRequest);
            Assert.assertThat(
                    httpresponse.getStatusLine().getStatusCode(),
                    IsEqual.equalTo(HttpStatus.SC_OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
