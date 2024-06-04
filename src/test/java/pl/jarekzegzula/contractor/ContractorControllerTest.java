package pl.jarekzegzula.contractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorContractTypeRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorOvertimeMultiplier;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorHourPriceRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorHourlyRateRequest;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Turn off spring security
class ContractorControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @MockBean ContractorService contractorService;
  @Mock ContractorRepository contractorRepository;

  List<Contractor> contractors;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  @BeforeEach
  void setup() {

    this.contractors = new ArrayList<>();

    Contractor contractor1 = new Contractor();
    contractor1.setId(1);
    contractor1.setFirstName("Marian");
    contractor1.setLastName("Paździoch");
    contractor1.setContractType(ContractType.CONTRACT_B2B);
    contractor1.setMonthlyEarnings(59.53 * 168);
    contractor1.setHourlyRate(59.53);
    contractor1.setMonthlyHourLimit(168);
    contractor1.setIsOvertimePaid(true);
    contractor1.setOvertimeMultiplier(1.5);
    contractor1.setContractorHourPrice(80.0);
    contractors.add(contractor1);

    Contractor contractor2 = new Contractor();
    contractor2.setId(2);
    contractor2.setFirstName("Ryszard");
    contractor2.setLastName("Peja");
    contractor2.setContractType(ContractType.CONTRACT_B2B);
    contractor2.setMonthlyEarnings(59.53 * 168);
    contractor2.setHourlyRate(59.53);
    contractor2.setMonthlyHourLimit(168);
    contractor2.setIsOvertimePaid(true);
    contractor2.setOvertimeMultiplier(1.5);
    contractor2.setContractorHourPrice(80.0);
    contractors.add(contractor2);

    Contractor contractor3 = new Contractor();
    contractor3.setId(3);
    contractor3.setFirstName("Zbigniew");
    contractor3.setLastName("Cebula");
    contractor3.setContractType(ContractType.CONTRACT_OF_MANDATE);
    contractor3.setMonthlyEarnings(59.53 * 168);
    contractor3.setHourlyRate(59.53);
    contractor3.setMonthlyHourLimit(168);
    contractor3.setIsOvertimePaid(true);
    contractor3.setOvertimeMultiplier(1.5);
    contractor3.setContractorHourPrice(80.0);
    contractors.add(contractor3);

    Contractor contractor4 = new Contractor();
    contractor4.setId(4);
    contractor4.setFirstName("Bogdan");
    contractor4.setLastName("Baryła");
    contractor4.setContractType(ContractType.CONTRACT_OF_MANDATE);
    contractor4.setMonthlyEarnings(59.53 * 168);
    contractor4.setHourlyRate(59.53);
    contractor4.setMonthlyHourLimit(168);
    contractor4.setIsOvertimePaid(true);
    contractor4.setOvertimeMultiplier(1.5);
    contractor4.setContractorHourPrice(80.0);
    contractors.add(contractor4);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void testGetAllContractors() throws Exception {
    // given
    given(contractorService.getContractors()).willReturn(this.contractors);

    // When Then
    mockMvc
        .perform(get(this.baseUrl + "/contractor").contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(this.contractors.size())))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].firstName").value("Marian"));
  }

  @Test
  void testGetContractorByIdSuccess() throws Exception {
    // Given
    given(this.contractorService.getContractorById(1)).willReturn(this.contractors.get(0));

    // When and then
    this.mockMvc
        .perform(get(this.baseUrl + "/contractor/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.firstName").value("Marian"))
        .andExpect(jsonPath("$.data.lastName").value("Paździoch"));
  }

  @Test
  void testGetContractorByIdNotFound() throws Exception {
    // Given
    Integer id = 10;

    given(this.contractorService.getContractorById(id))
        .willThrow(new ObjectNotFoundException("contractor", id));

    // When and then
    this.mockMvc
        .perform(get(this.baseUrl + "/contractor/" + id).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + id))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAddContractor() throws Exception {

    // Given
    NewContractorRequest newContractorRequest =
        new NewContractorRequest("Artur", "Testowy", 3, 50.0, 168, true, 1.5, 80.0);

    String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

    // When and Then
    mockMvc
        .perform(
            post(this.baseUrl + "/contractor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Contractor added successfully"))
        .andExpect(jsonPath("$.data.firstName").value("Artur"))
        .andExpect(jsonPath("$.data.lastName").value("Testowy"))
        .andExpect(jsonPath("$.data.contractType").value("3"))
        .andExpect(jsonPath("$.data.hourlyRate").value(50.0))
        .andExpect(jsonPath("$.data.overtimeMultiplier").value(1.5))
        .andExpect(jsonPath("$.data.contractorHourPrice").value(80.0));
  }

  @Test
  void testUpdateContractorHourlyRateSuccess() throws Exception {

    // Given
    Contractor savedContractor = new Contractor();
    savedContractor.setId(1);
    savedContractor.setFirstName("Marian");
    savedContractor.setLastName("Paździoch");

    Integer contractorId = 1;

    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(1500.0);
    String json = objectMapper.writeValueAsString(updateRequest);

    doNothing()
        .when(contractorService)
        .updateContractorHourlyRateAndMonthlyEarnings(updateRequest, contractorId);

    // When and Then
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Update success"));
  }

  @Test
  void testUpdateContractorHourlyRateErrorWithNonExistentId() throws Exception {
    // Given
    UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(1500.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    doThrow(new ObjectNotFoundException("contractor", contractorId))
        .when(this.contractorService)
        .updateContractorHourlyRateAndMonthlyEarnings(updateRequest, contractorId);
    // When and Than
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hourly-rate/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  void testDeleteContractorSuccess() throws Exception {

    // Given
    Integer contractorId = 1;

    doNothing().when(contractorService).deleteContractorById(contractorId);

    // When and Then
    mockMvc
        .perform(
            delete(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Delete Success"));
  }

  @Test
  void testDeleteContractorNonExistentId() throws Exception {
    Integer contractorId = 10;

    doThrow(new ObjectNotFoundException("contractor", contractorId))
        .when(this.contractorService)
        .deleteContractorById(contractorId);

    mockMvc
        .perform(
            delete(this.baseUrl + "/contractor/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testUpdateContractorOvertimeMultiplier() throws Exception {

    // Given
    Contractor savedContractor = new Contractor();
    savedContractor.setId(1);
    savedContractor.setFirstName("Marian");
    savedContractor.setLastName("Paździoch");
    savedContractor.setOvertimeMultiplier(1.5);

    Integer contractorId = 1;

    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(2.0);
    String json = objectMapper.writeValueAsString(updateRequest);

    doNothing()
        .when(contractorService)
        .updateContractorOvertimeMultiplier(updateRequest, contractorId);

    // When and Then
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Update success"));
  }

  @Test
  void testUpdateContractorOvertimeMultiplierErrorWithNonExistentId() throws Exception {
    // Given
    UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(2.0);

    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    doThrow(new ObjectNotFoundException("contractor", contractorId))
        .when(this.contractorService)
        .updateContractorOvertimeMultiplier(updateRequest, contractorId);
    // When and Than
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/multiplier/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  void testUpdateContractorHourPriceSuccess() throws Exception {

    // Given
    Contractor savedContractor = new Contractor();
    savedContractor.setId(1);
    savedContractor.setFirstName("Marian");
    savedContractor.setLastName("Paździoch");
    savedContractor.setOvertimeMultiplier(1.5);

    Integer contractorId = 1;

    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(7000.0);
    String json = objectMapper.writeValueAsString(updateRequest);

    doNothing().when(contractorService).updateContractorHourPrice(updateRequest, contractorId);

    // When and Then
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Update success"));
  }

  @Test
  void testUpdateContractorPriceErrorWithNonExistentId() throws Exception {
    // Given
    UpdateContractorHourPriceRequest updateRequest = new UpdateContractorHourPriceRequest(7000.0);
    String json = objectMapper.writeValueAsString(updateRequest);

    Integer contractorId = 10;

    doThrow(new ObjectNotFoundException("contractor", contractorId))
        .when(this.contractorService)
        .updateContractorHourPrice(updateRequest, contractorId);
    // When and Than
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/hour-price/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }

  @Test
  void testUpdateContractorContractType() throws Exception {

    // Given
    Contractor savedContractor = new Contractor();
    savedContractor.setId(1);
    savedContractor.setFirstName("Marian");
    savedContractor.setLastName("Paździoch");
    savedContractor.setContractType(ContractType.CONTRACT_B2B);

    Integer contractorId = 1;

    UpdateContractorContractTypeRequest updateContractorContractTypeRequest =
        new UpdateContractorContractTypeRequest(1);

    String json = objectMapper.writeValueAsString(updateContractorContractTypeRequest);

    doNothing()
        .when(contractorService)
        .updateContractType(updateContractorContractTypeRequest, contractorId);

    // When and Then
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Update success"));
  }

  @Test
  void testUpdateContractorContractTypeWithNonExistentId() throws Exception {
    // Given
    UpdateContractorContractTypeRequest updateContractorContractTypeRequest =
        new UpdateContractorContractTypeRequest(1);
    String json = objectMapper.writeValueAsString(updateContractorContractTypeRequest);

    Integer contractorId = 10;

    doThrow(new ObjectNotFoundException("contractor", contractorId))
        .when(this.contractorService)
        .updateContractType(updateContractorContractTypeRequest, contractorId);
    // When and Than
    mockMvc
        .perform(
            put(this.baseUrl + "/contractor/contract-type/{contractorId}", contractorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
  }
}
