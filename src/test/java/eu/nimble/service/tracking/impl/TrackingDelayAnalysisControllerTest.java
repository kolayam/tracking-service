package eu.nimble.service.tracking.impl;

import eu.nimble.service.tracking.TrackingApplication;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TrackingApplication.class)
public class TrackingDelayAnalysisControllerTest {

    @Value("${nimble.platformHost}")
    private String baseUrl;

    @Value("${test.accessToken}")
    private String accessToken;

    @Value("${spring.epcis.url}")
    private String epcisURL;

    private String getBaseUrl() {
        String url = epcisURL.trim();
        if(!url.endsWith("/"))
        {
            url = url + "/";
        }
        return url + "/Service";
    }

    String inputDocument = "{\n" +
            "  \"productClass\": \"lindbacks_test\",\n" +
            "  \"productionProcessTemplate\": [\n" +
            "    {\n" +
            "      \"id\": \"1\",\n" +
            "      \"hasPrev\": \"\",\n" +
            "      \"readPoint\": \"urn:epc:id:sgln:readPoint.PodComp.1\",\n" +
            "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.PodComp.2\",\n" +
            "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:other\",\n" +
            "      \"durationToNext\": \"1\",\n" +
            "      \"hasNext\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2\",\n" +
            "      \"hasPrev\": \"1\",\n" +
            "      \"readPoint\": \"urn:epc:id:sgln:readPoint.lindbacks.2\",\n" +
            "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.lindbacks.3\",\n" +
            "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:installing\",\n" +
            "      \"durationToNext\": \"1\",\n" +
            "      \"hasNext\": \"3\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"3\",\n" +
            "      \"hasPrev\": \"2\",\n" +
            "      \"readPoint\": \"urn:epc:id:sgln:readPoint.lindbacks.3\",\n" +
            "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.lindbacks.4\",\n" +
            "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:entering_exiting\",\n" +
            "      \"durationToNext\": \"4\",\n" +
            "      \"hasNext\": \"4\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"4\",\n" +
            "      \"hasPrev\": \"3\",\n" +
            "      \"readPoint\": \"urn:epc:id:sgln:readPoint.lindbacks.4\",\n" +
            "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.lindbacks.5\",\n" +
            "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:shipping\",\n" +
            "      \"durationToNext\": \"1\",\n" +
            "      \"hasNext\": \"\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    String testJsonEventData = "{\n" +
            "  \"epcis\": {\n" +
            "    \"EPCISBody\": {\n" +
            "      \"EventList\": [\n" +
            "        {\n" +
            "          \"ObjectEvent\": {\n" +
            "            \"eventTime\": 1564660166383,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"epcList\": [\n" +
            "              {\n" +
            "                \"epc\": \"LB-3377-3-A1201\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"action\": \"OBSERVE\",\n" +
            "            \"bizStep\": \"urn:epcglobal:cbv:bizstep:other\",\n" +
            "            \"readPoint\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:readPoint.PodComp.1\"\n" +
            "            },\n" +
            "            \"bizLocation\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:bizLocation.PodComp.2\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"ObjectEvent\": {\n" +
            "            \"eventTime\": 1564660166394,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"epcList\": [\n" +
            "              {\n" +
            "                \"epc\": \"LB-3377-3-A1201\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"action\": \"OBSERVE\",\n" +
            "            \"bizStep\": \"urn:epcglobal:cbv:bizstep:installing\",\n" +
            "            \"readPoint\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:readPoint.PodComp.2\"\n" +
            "            },\n" +
            "            \"bizLocation\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:bizLocation.PodComp.3\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"ObjectEvent\": {\n" +
            "            \"eventTime\": 1564660166406,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"epcList\": [\n" +
            "              {\n" +
            "                \"epc\": \"LB-3377-3-A1201\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"action\": \"OBSERVE\",\n" +
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
            "          \"ObjectEvent\": {\n" +
            "            \"eventTime\": 1564660166421,\n" +
            "            \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "            \"epcList\": [\n" +
            "              {\n" +
            "                \"epc\": \"LB-3377-3-A1201\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"action\": \"OBSERVE\",\n" +
            "            \"bizStep\": \"urn:epcglobal:cbv:bizstep:entering_exiting\",\n" +
            "            \"readPoint\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:readPoint.PodComp.4\"\n" +
            "            },\n" +
            "            \"bizLocation\": {\n" +
            "              \"id\": \"urn:epc:id:sgln:bizLocation.PodComp.5\"\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    public void getEPCTimeDelay() {
        this.postRestAPITest(this.getBaseUrl() + "/JSONEventCapture", "application/json", testJsonEventData);
        String epcItem = "LB-3377-3-A1201";
        this.postRestAPIWithParameterTest(baseUrl + "/getEPCTimeDelay", epcItem, inputDocument, false);
    }

    @Test
    public void getEPCListTimeDelay() {
        String epcList = "LB-3377-3-A1201, LB-3377-3-A1201, LB-3377-3-A1201";
        this.postRestAPIWithParameterTest(baseUrl + "/getEPCListTimeDelay", epcList, inputDocument, true);
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

    private void postRestAPIWithParameterTest(String url, String parameter, String entity, boolean list) {
        try {
            HttpPost httpRequest = new HttpPost(url);
            httpRequest.addHeader("Authorization", accessToken);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

            if(list) {
                postParameters.add(new BasicNameValuePair("epcList", parameter));
            } else {
                postParameters.add(new BasicNameValuePair("item", parameter));
            }

            postParameters.add(new BasicNameValuePair("inputDocument", entity));
            httpRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            HttpResponse httpresponse = HttpClientBuilder.create().build().execute(httpRequest);
            Assert.assertThat(
                    httpresponse.getStatusLine().getStatusCode(),
                    IsEqual.equalTo(HttpStatus.SC_OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
