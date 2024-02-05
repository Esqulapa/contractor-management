package pl.jarekzegzula.contractor;

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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;
import pl.jarekzegzula.requests.updateRequest.*;
import pl.jarekzegzula.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@DisplayName("Integration tests for Contractor API endpoints")
@Tag("integration")
public class ContractorControllerIntegrationTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  String token;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  @BeforeEach
  void setup() throws Exception {

    ResultActions resultActions =
        this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("user", "1234")));
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    this.token = "Bearer " + json.getJSONObject("data").getString("token");
  }

  @Test
  @DisplayName("Check findAllContractors (GET)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllContractorsSuccess() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  @DisplayName("Check findContractorById (GET)")
  void testFindContractorByIdSuccess() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/2")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").value("2"))
        .andExpect(jsonPath("$.data.firstName").value("Arnold"))
        .andExpect(jsonPath("$.data.lastName").value("Bakon"));
  }

  @Test
  @DisplayName("Check findContractorById with non-existent id (GET)")
  void testFindContractorByIdNotFound() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/12")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor with Id 12"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check addContractor with valid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddContractorSuccess() throws Exception {
    NewContractorRequest newContractorRequest =
        new NewContractorRequest("Artur", "Testowy", 3, 50.0, 168, true, 1.5, 80.0);
    String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

    this.mockMvc
        .perform(
            post(this.baseUrl + "/contractor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Contractor added successfully"))
        .andExpect(jsonPath("$.data.firstName").value("Artur"))
        .andExpect(jsonPath("$.data.lastName").value("Testowy"))
        .andExpect(jsonPath("$.data.contractType").value(3))
        .andExpect(jsonPath("$.data.hourlyRate").value(50.0))
        .andExpect(jsonPath("$.data.overtimeMultiplier").value(1.5))
        .andExpect(jsonPath("$.data.contractorHourPrice").value(80.0));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
  }

  @Test
  @DisplayName("Check addContractor with invalid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddContractorErrorWithInvalidInput() throws Exception {
    NewContractorRequest newContractorRequest =
        new NewContractorRequest("", "", null, null, null, null, null, null);

    String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

    this.mockMvc
        .perform(
            post(this.baseUrl + "/contractor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.firstName").value("must not be empty"))
        .andExpect(jsonPath("$.data.lastName").value("must not be empty"))
        .andExpect(jsonPath("$.data.contractType").value("must not be null"));
    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  @DisplayName("Check updateContractorHourlyRate with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourlyRateSuccess() throws Exception {
    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(50.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.hourlyRate").value(50));
  }

  @Test
  @DisplayName("Check updateContractorHourlyRate with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourlyRateErrorWithNonExistentId() throws Exception {
    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(50.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorHourlyRate with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourlyRateErrorWithInvalidInput() throws Exception {
    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(-1.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message").value("Hourly rate remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorHourlyRate with the same salary value (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourlyRateSameValue() throws Exception {
    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(36.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message").value("Hourly rate remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check deleteContractor with valid input (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteContractorSuccess() throws Exception {
    this.mockMvc
        .perform(
            delete(this.baseUrl + "/contractor/1")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"));
    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/1")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor with Id 1"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteArtifact with non-existent id (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteArtifactErrorWithNonExistentId() throws Exception {
    this.mockMvc
        .perform(
            delete(this.baseUrl + "/contractor/100")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor with Id 100"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check updateContractorOvertimeMultiplier with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeMultiplierSuccess() throws Exception {

    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(2.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.overtimeMultiplier").value(2.0));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeMultiplier with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeMultiplierErrorWithNonExistentId() throws Exception {

    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(2.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeMultiplier with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeMultiplierErrorWithInvalidInput() throws Exception {

    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(-1.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Contractor overtime multiplier remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeMultiplier with the same salary value (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeMultiplierErrorWithTheSameValue() throws Exception {
    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(1.5);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Contractor overtime multiplier remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorHourPrice with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourPriceSuccess() throws Exception {

    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(60.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.contractorHourPrice").value(60.0));
  }

  @Test
  @DisplayName("Check updateContractorHourPrice with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourPriceErrorWithNonExistentId() throws Exception {

    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(30.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorHourPrice with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourPriceErrorWithInvalidInput() throws Exception {

    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(-1.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value("Contractor hour price remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorHourPrice with the same salary value (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorHourPriceErrorWithTheSameValue() throws Exception {

    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(50.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value("Contractor hour price remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorMonthlyHourLimit with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorMonthlyHourLimitSuccess() throws Exception {

    UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(170);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-limit/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.monthlyHourLimit").value(170.0));
  }

  @Test
  @DisplayName("Check updateContractorMonthlyHourLimit with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorMonthlyHourLimitErrorWithNonExistentId() throws Exception {

    UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(170);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-limit/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorMonthlyHourLimit with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorMonthlyHourLimitErrorWithInvalidInput() throws Exception {

    UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(-1);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-limit/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value("Contractor hour limit remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorMonthlyHourLimit with the same salary value (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorMonthlyHourLimitErrorWithTheSameValue() throws Exception {

    UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(168);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-limit/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message")
                .value("Contractor hour limit remains the same or the value is incorrect"));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeValue with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeValueSuccess() throws Exception {

    UpdateIsContractorOvertimePaid updateRequest = new UpdateIsContractorOvertimePaid(true);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/overtime/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.isOvertimePaid").value(true));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeValue with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeValueErrorWithNonExistentId() throws Exception {

    UpdateIsContractorOvertimePaid updateRequest = new UpdateIsContractorOvertimePaid(false);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/overtime/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeValue with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorOvertimeValueErrorWithInvalidInput() throws Exception {

    UpdateIsContractorOvertimePaid updateRequest = new UpdateIsContractorOvertimePaid(null);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/overtime/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
  }

  /////////////

  @Test
  @DisplayName("Check updateContractorContractType with valid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorContractTypeSuccess() throws Exception {

    UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(1);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(
            jsonPath("$.data.contractType")
                .value(ContractType.fromValue(updateRequest.contractType()).toString()));
  }

  @Test
  @DisplayName("Check updateContractorContractType with non-existent id (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorContractTypeErrorWithNonExistentId() throws Exception {

    UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(1);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  @DisplayName("Check updateContractorContractType with invalid input (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorContractTypeErrorWithInvalidInput() throws Exception {

    UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(10);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Invalid ContractType value: 10"));
  }

  @Test
  @DisplayName("Check updateContractorOvertimeValue with the same salary value (PUT)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateContractorContractTypeErrorWithTheSameValue() throws Exception {

    UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(2);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 2;

    this.mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Given Contract type is the same"));
  }
}
