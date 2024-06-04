package pl.jarekzegzula.contractorBilling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorBillingHoursRequest;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ContractorBillingControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @MockBean ContractorBillingService contractorBillingService;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  List<ContractorBilling> contractorBillings;

  @BeforeEach
  void setUp() {

    ArrayList<Contractor> contractors = new ArrayList<>();

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

    NewContractorBillingRequest newContractorBillingRequest =
        new NewContractorBillingRequest(1, 169.0, Year.of(2023), Month.MARCH);
    ContractorBilling contractorBilling1 =
        new ContractorBilling(newContractorBillingRequest, contractor1);
    contractorBilling1.setId(1);

    ContractorBilling contractorBilling2 = new ContractorBilling();
    contractorBilling2.setId(2);
    contractorBilling2.setContractor(contractors.get(1));
    contractorBilling2.setWorkedHours(152.0);
    contractorBilling2.setYear(Year.of(2023));
    contractorBilling2.setMonth(Month.MAY);

    contractorBillings = new ArrayList<>();
    contractorBillings.add(contractorBilling1);
    contractorBillings.add(contractorBilling2);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void getContractorBillings() throws Exception {

    given(this.contractorBillingService.findAll()).willReturn(this.contractorBillings);

    this.mockMvc
        .perform(get(this.baseUrl + "/contractor/billing").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(this.contractorBillings.size())))
        .andExpect(jsonPath("$.data.[0].id").value(1))
        .andExpect(jsonPath("$.data.[0].year").value(2023))
        .andExpect(jsonPath("$.data.[0].month").value("MARCH"));
  }

  @Test
  void getContractorBillingById() throws Exception {
    // given

    given(this.contractorBillingService.getContractorBillingById(1))
        .willReturn(this.contractorBillings.get(0));

    // When and then

    this.mockMvc
        .perform(get(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.year").value(2023))
        .andExpect(jsonPath("$.data.month").value("MARCH"));
  }

  @Test
  void getContractorBillingByIdNotFound() throws Exception {
    // given

    given(this.contractorBillingService.getContractorBillingById(1))
        .willThrow(new ObjectNotFoundException("contractor billing", 1));

    // When and then

    this.mockMvc
        .perform(get(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id 1"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void updateContractorWorkedHours() throws Exception {

    UpdateContractorBillingHoursRequest updateContractorBillingHoursRequest =
        new UpdateContractorBillingHoursRequest(140.0);

    Integer contractorBillingId = 1;

    String json = objectMapper.writeValueAsString(updateContractorBillingHoursRequest);

    doNothing()
        .when(contractorBillingService)
        .updateContractorBillingWorkedHours(
            updateContractorBillingHoursRequest, contractorBillingId);

    mockMvc
        .perform(
            put(
                    this.baseUrl + "/contractor/billing/hours/{contractorBillingId}",
                    contractorBillingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("Update success"));
  }

  @Test
  void addContractorBilling() throws Exception {
    NewContractorBillingRequest newContractorBillingRequest =
        new NewContractorBillingRequest(1, 150., Year.of(2023), Month.MARCH);

    String json = objectMapper.writeValueAsString(newContractorBillingRequest);

    mockMvc
        .perform(
            post(this.baseUrl + "/contractor/billing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Contractor billing added successfully"))
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  void deleteContractorBilling() throws Exception {

    mockMvc
        .perform(delete(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete success"));
  }

  @Test
  void deleteContractorBillingNotFound() throws Exception {
    Integer contractorBillingId = 10;

    doThrow(new ObjectNotFoundException("contractor billing", contractorBillingId))
        .when(this.contractorBillingService)
        .deleteContractorBillingById(10);

    mockMvc
        .perform(delete(this.baseUrl + "/contractor/billing/10").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message")
                .value("Could not find contractor billing with Id " + contractorBillingId));
  }

  @Test
  void getContractorBillingsMonthlyReport() throws Exception {
    Year year = Year.of(2023);
    Month month = Month.MARCH;

    List<ContractorBilling> contractorByYearAndMonth = new ArrayList<>();
    contractorByYearAndMonth.add(this.contractorBillings.get(0));
    List<ContractorBillingDTO> contractorByYearAndMonthDto =
        contractorByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();

    ContractorBillingReportByMonth contractorBillingReportByMonth =
        new ContractorBillingReportByMonth(contractorByYearAndMonthDto, year, month);

    given(contractorBillingService.getContractorBillingsMonthlyReport(Year.of(2023), Month.MARCH))
        .willReturn(contractorBillingReportByMonth);

    mockMvc
        .perform(
            get(this.baseUrl + "/contractor/billing/report")
                .param("year", String.valueOf(year.getValue()))
                .param("month", month.name()))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  void getContractorBillingsMonthlyReportNotFound() throws Exception {

    Year year = Year.of(2023);
    Month month = Month.MARCH;

    given(contractorBillingService.getContractorBillingsMonthlyReport(Year.of(2023), Month.MARCH))
        .willThrow(
            new ObjectNotFoundException("contractor billings", year.toString(), month.name()));

    mockMvc
        .perform(
            get(this.baseUrl + "/contractor/billing/report")
                .param("year", String.valueOf(year.getValue()))
                .param("month", month.name()))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(
            jsonPath("$.message")
                .value("Could not find contractor billings with given 2023 and MARCH"))
        .andExpect(jsonPath("$.data").isEmpty());
  }
}
