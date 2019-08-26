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

    @Test
    public void getEPCListTimeDelay() {
        String epcList = "LB-3377-3-A1201, LB-3377-3-A1201, LB-3377-3-A1201";
        this.postRestAPIWithParameterTest(baseUrl + "/getEPCListTimeDelay", epcList, inputDocument, true);
    }

    @Test
    public void getEPCTimeDelay() {
        String epcItem = "LB-3377-3-A1201";
        this.postRestAPIWithParameterTest(baseUrl + "/getEPCTimeDelay", epcItem, inputDocument, false);
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
