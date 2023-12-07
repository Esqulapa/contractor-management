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
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorOvertimeMultiplier;
import pl.jarekzegzula.requests.UpdateContractorPrice;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
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
    @DisplayName("Check findAllContractors (GET)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllContractorsSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Check findContractorById (GET)")
    void testFindContractorByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/2")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value("2"))
                .andExpect(jsonPath("$.data.firstName").value("Arnold"))
                .andExpect(jsonPath("$.data.lastName").value("Bakon"))
                .andExpect(jsonPath("$.data.salary").value(10000.0));
    }

    @Test
    @DisplayName("Check findContractorById with non-existent id (GET)")
    void testFindContractorByIdNotFound() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/contractor/12")
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
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
                new NewContractorRequest("Marek", "Marucha", 1600.0,1.5,2000.0);

        String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

        this.mockMvc.perform(post(this.baseUrl + "/contractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Contractor added successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Marek"))
                .andExpect(jsonPath("$.data.lastName").value("Marucha"))
                .andExpect(jsonPath("$.data.salary").value(1600.0));
        this.mockMvc.perform(get(this.baseUrl + "/contractor").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
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
                new NewContractorRequest("", "", null,null,null);

        String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

        this.mockMvc.perform(post(this.baseUrl + "/contractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.firstName").value("must not be empty"))
                .andExpect(jsonPath("$.data.lastName").value("must not be empty"))
                .andExpect(jsonPath("$.data.salary").value("must not be null"));
        this.mockMvc.perform(get(this.baseUrl + "/contractor").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));

    }
    @Test
    @DisplayName("Check updateContractorSalary with valid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorSalarySuccess() throws Exception {
        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(2500.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/salary/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"));

    this.mockMvc.perform(get(this.baseUrl + "/contractor/{contractorId}",contractorId).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.salary").value(2500.0));

    }

    @Test
    @DisplayName("Check updateContractorSalary with non-existent id (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorSalaryErrorWithNonExistentId() throws Exception {
        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(1500.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 10;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/salary/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId));


    }

    @Test
    @DisplayName("Check updateContractorSalary with invalid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorSalaryErrorWithInvalidInput() throws Exception {
        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(-1.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/salary/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Salary remains the same or the value is incorrect"));


    }

    @Test
    @DisplayName("Check updateContractorSalary with the same salary value (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorSalaryErrorWithTheSameValue() throws Exception {
        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(10000.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/salary/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Salary remains the same or the value is incorrect"));


    }
    @Test
    @DisplayName("Check deleteContractor with valid input (DELETE)")
    void testDeleteContractorSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/contractor/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
        this.mockMvc.perform(get(this.baseUrl + "/contractor/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id 1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteArtifact with non-existent id (DELETE)")
    void testDeleteArtifactErrorWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/contractor/100").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
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

        this.mockMvc.perform(put(this.baseUrl + "/contractor/multiplier/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"));

        this.mockMvc.perform(get(this.baseUrl + "/contractor/{contractorId}",contractorId)
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
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

        this.mockMvc.perform(put(this.baseUrl + "/contractor/multiplier/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId));


    }

    @Test
    @DisplayName("Check updateContractorOvertimeMultiplier with invalid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorOvertimeMultiplierErrorWithInvalidInput() throws Exception {

        UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(-1.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/multiplier/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message")
                        .value("Contractor overtime multiplier remains the same or the value is incorrect"));


    }

    @Test
    @DisplayName("Check updateContractorOvertimeMultiplier with the same salary value (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorOvertimeMultiplierErrorWithTheSameValue() throws Exception {
        UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(1.5);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/multiplier/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message")
                        .value("Contractor overtime multiplier remains the same or the value is incorrect"));


    }


    //Update price

    @Test
    @DisplayName("Check updateContractorPrice with valid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorPriceSuccess() throws Exception {

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(17000.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/price/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"));

        this.mockMvc.perform(get(this.baseUrl + "/contractor/{contractorId}",contractorId)
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.contractorPrice").value(17000.0));

    }

    @Test
    @DisplayName("Check updateContractorPrice with non-existent id (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorPriceErrorWithNonExistentId() throws Exception {

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(17000.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 10;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/price/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId));


    }

    @Test
    @DisplayName("Check updateContractorPrice with invalid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorPriceErrorWithInvalidInput() throws Exception {

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(-1.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/price/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message")
                        .value("Contractor price remains the same or the value is incorrect"));


    }

    @Test
    @DisplayName("Check updateContractorPrice with the same salary value (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateContractorPriceErrorWithTheSameValue() throws Exception {

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(15000.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 2;

        this.mockMvc.perform(put(this.baseUrl + "/contractor/price/{contractorId}",contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message")
                        .value("Contractor price remains the same or the value is incorrect"));


    }



}
