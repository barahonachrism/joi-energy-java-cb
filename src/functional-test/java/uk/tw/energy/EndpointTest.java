package uk.tw.energy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.MeterReadings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
class EndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldStoreReadings() throws JsonProcessingException {
        try {
            MeterReadings meterReadings = new MeterReadingsBuilder().generateElectricityReadings().build();
            HttpEntity<String> entity = getStringHttpEntity(meterReadings);

            ResponseEntity<String> response = restTemplate.postForEntity("/readings/store", entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        } catch (NoSuchAlgorithmException e) {
            assertTrue(false, "Not exists secure random algorithm");
        }
        
    }

    @Test
    void givenMeterIdShouldReturnAMeterReadingAssociatedWithMeterId() throws JsonProcessingException {
        String smartMeterId = "bob";
        populateMeterReadingsForMeter(smartMeterId);

        ResponseEntity<String> response = restTemplate.getForEntity("/readings/read/" + smartMeterId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldCalculateAllPrices() throws JsonProcessingException {
        String smartMeterId = "bob";
        populateMeterReadingsForMeter(smartMeterId);

        ResponseEntity<String> response = restTemplate.getForEntity("/price-plans/compare-all/" + smartMeterId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenMeterIdAndLimitShouldReturnRecommendedCheapestPricePlans() throws JsonProcessingException {
        String smartMeterId = "bob";
        populateMeterReadingsForMeter(smartMeterId);

        ResponseEntity<String> response =
                restTemplate.getForEntity("/price-plans/recommend/" + smartMeterId + "?limit=2", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpEntity<String> getStringHttpEntity(Object object) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonMeterData = mapper.writeValueAsString(object);
        return (HttpEntity<String>) new HttpEntity(jsonMeterData, headers);
    }

    private void populateMeterReadingsForMeter(String smartMeterId) throws JsonProcessingException {
        try {
            MeterReadings readings = new MeterReadingsBuilder().setSmartMeterId(smartMeterId)
                    .generateElectricityReadings(20)
                    .build();
            HttpEntity<String> entity = getStringHttpEntity(readings);
            restTemplate.postForEntity("/readings/store", entity, String.class);
        } catch (NoSuchAlgorithmException e) {
            assertTrue(false, "Not exists secure random algorithm");
        }

        
    }
}
