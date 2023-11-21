package pl.jarekzegzula.contractorBilling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import pl.jarekzegzula.requests.NewContractorBillingRequest;
import pl.jarekzegzula.requests.UpdateContractorHoursRequest;
import pl.jarekzegzula.system.StatusCode;

import java.time.Month;
import java.time.Year;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for ContractorBilling API endpoints")
@Tag("integration")
public class ContractorBillingControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setup() throws Exception {

        ResultActions resultActions = this.mockMvc
                .perform(post(this.baseUrl + "/users/login")
                        .with(httpBasic("user", "1234")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token");

    }

    @Test
    @DisplayName("Check findAllContractorBillings (GET)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllContractorBillingsSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    @DisplayName("Check findContractorBillingById (GET)")
    void testFindContractorBillingByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/1")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.workedHours").value(160.0))
                .andExpect(jsonPath("$.data.year").value("2023"))
                .andExpect(jsonPath("$.data.month").value("MARCH"))
                .andExpect(jsonPath("$.data.payment").value("1391.3"));
    }

    @Test
    @DisplayName("Check findContractorBillingById with non-existent id (GET)")
    void testFindContractorByIdNotFound() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/10")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id 10"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check addContractorBilling with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddContractorBillingSuccess() throws Exception {
        NewContractorBillingRequest newContractorBillingRequest1 = new NewContractorBillingRequest(
                1,
                160.0,
                Year.of(2023),
                Month.FEBRUARY);

        String jsonRequest = objectMapper.writeValueAsString(newContractorBillingRequest1);

        this.mockMvc.perform(post(this.baseUrl + "/contractor/billing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Contractor billing added successfully"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.workedHours").value("160.0"));
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(5)));

    }

    @Test
    @DisplayName("Check addContractorBilling with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddContractorBillingErrorWithInvalidInput() throws Exception {
        NewContractorBillingRequest newContractorBillingRequest1 = new NewContractorBillingRequest(
                null,
                null,
                null,
                null);

        String jsonRequest = objectMapper.writeValueAsString(newContractorBillingRequest1);

        this.mockMvc.perform(post(this.baseUrl + "/contractor/billing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.id").value("must not be null"))
                .andExpect(jsonPath("$.data.workedHours").value("must not be null"))
                .andExpect(jsonPath("$.data.year").value("must not be null"))
                .andExpect(jsonPath("$.data.month").value("must not be null"));
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));

    }

    @Test
    @DisplayName("Check updateContractorBillingWorkingHours with valid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorBillingWorkedHoursSuccess() throws Exception {
        UpdateContractorHoursRequest updateContractorHoursRequest = new UpdateContractorHoursRequest(144.0);

        String json = objectMapper.writeValueAsString(updateContractorHoursRequest);


        this.mockMvc.perform(put(this.baseUrl + "/contractor/billing/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"));

        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/1")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.workedHours").value(144.0));

    }

    @Test
    @DisplayName("Check updateContractorBillingWorkedHours with non-existent id (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorBillingWorkedHoursErrorWithNonExistentId() throws Exception {

        UpdateContractorHoursRequest updateContractorHoursRequest = new UpdateContractorHoursRequest(144.0);

        String json = objectMapper.writeValueAsString(updateContractorHoursRequest);


        this.mockMvc.perform(put(this.baseUrl + "/contractor/billing/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id 10"));


    }

    @Test
    @DisplayName("Check updateContractorBillingWorkedHours with invalid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorBillingWorkedHoursErrorWithInvalidInput() throws Exception {
        UpdateContractorHoursRequest updateContractorHoursRequest = new UpdateContractorHoursRequest(null);

        String json = objectMapper.writeValueAsString(updateContractorHoursRequest);


        this.mockMvc.perform(put(this.baseUrl + "/contractor/billing/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.workedHours").value("must not be null"));


    }

    @Test
    @DisplayName("Check updateContractorBillingWorkedHours with the same salary value (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorBillingWorkedHoursErrorWithTheSameValue() throws Exception {
        UpdateContractorHoursRequest updateContractorHoursRequest = new UpdateContractorHoursRequest(160.0);

        String json = objectMapper.writeValueAsString(updateContractorHoursRequest);

        this.mockMvc.perform(put(this.baseUrl + "/contractor/billing/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("the given data to be changed is the same or less than zero"));


    }

    @Test
    @DisplayName("Check deleteContractorBilling with valid input (DELETE)")
    void testDeleteContractorBillingSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id 1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteArtifact with non-existent id (DELETE)")
    void testDeleteArtifactErrorWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/contractor/billing/100").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Contractor Billing with Id 100"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check getContractorsWorkedHoursInGivenYearMonth with valid input (GET)")
    void testGetContractorsWorkedHoursInGivenYearMonthSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                        .param("year", "2023")
                        .param("month", "MARCH")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.yearMonth").value("2023-03"))
                .andExpect(jsonPath("$.data.workingHours").value( 184.0))
                .andExpect(jsonPath("$.data.contractorBillings", Matchers.hasSize(3)));


    }

    @Test
    @DisplayName("Check getContractorsWorkedHoursInGivenYearMonth with non existent billings (GET)")
    void testGetContractorsWorkedHoursInGivenYearMonthWithNonExistentBillings() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                        .param("year", "2024")
                        .param("month", "DECEMBER")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billings with given 2024 and DECEMBER"))
                .andExpect(jsonPath("$.data").isEmpty());


    }

}
