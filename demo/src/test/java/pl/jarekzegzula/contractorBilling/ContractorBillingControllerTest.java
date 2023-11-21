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
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractorBilling.Dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.NewContractorBillingRequest;
import pl.jarekzegzula.requests.UpdateContractorHoursRequest;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static pl.jarekzegzula.contractorBilling.ContractorBillingService.countWorkingHoursWithoutWeekendsInMonth;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ContractorBillingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ContractorBillingService contractorBillingService;

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
        contractor1.setSalary(1600.0);
        contractors.add(contractor1);

        Contractor contractor2 = new Contractor();
        contractor2.setId(2);
        contractor2.setFirstName("Ryszard");
        contractor2.setLastName("Peja");
        contractor2.setSalary(1300.0);
        contractors.add(contractor2);

        ContractorBilling contractorBilling1 = new ContractorBilling();
        contractorBilling1.setId(1);
        contractorBilling1.setContractor(contractors.get(0));
        contractorBilling1.setWorkedHours(160.0);
        contractorBilling1.setYear(Year.of(2023));
        contractorBilling1.setMonth(Month.MARCH);


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
    void tearDown() {
    }

    @Test
    void getContractorBillings() throws Exception {

        given(this.contractorBillingService.findAll()).willReturn(this.contractorBillings);

        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing").accept(MediaType.APPLICATION_JSON))
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
        //given

        given(this.contractorBillingService.getContractorBillingById(1)).willReturn(this.contractorBillings.get(0));

        //When and then

        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.year").value(2023))
                .andExpect(jsonPath("$.data.month").value("MARCH"));
    }
    @Test
    void getContractorBillingByIdNotFound() throws Exception {
        //given

        given(this.contractorBillingService.getContractorBillingById(1)).willThrow(new ObjectNotFoundException("contractor billing", 1));

        //When and then

        this.mockMvc.perform(get(this.baseUrl + "/contractor/billing/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id 1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getContractorsWorkedHoursInGivenYearMonth() throws Exception {
        Year year = Year.of(2023);
        Month month = Month.MARCH;

        List<ContractorBilling> contractorByYearAndMonth = new ArrayList<>();
        contractorByYearAndMonth.add(this.contractorBillings.get(0));
        List<ContractorBillingDTO> contractorByYearAndMonthDto = contractorByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();

        ContractorBillingReportByMonth contractorBillingReportByMonth =
                new ContractorBillingReportByMonth(YearMonth.of(2023
                        , 3)
                        , countWorkingHoursWithoutWeekendsInMonth(Year.of(2023), Month.MARCH)
                        , contractorByYearAndMonthDto);


        given(contractorBillingService.getContractorsWorkedHoursInGivenMonth(Year.of(2023),Month.MARCH))
                .willReturn(contractorBillingReportByMonth);

        mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                .param("year", String.valueOf(year.getValue()))
                .param("month", month.name()))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").exists());
    }
    @Test
    void getContractorsWorkedHoursInGivenYearMonthNotFound() throws Exception {

        Year year = Year.of(2023);
        Month month = Month.MARCH;


        given(contractorBillingService.getContractorsWorkedHoursInGivenMonth(Year.of(2023),Month.MARCH))
                .willThrow(new ObjectNotFoundException("contractor billings", year.toString(), month.name()));

        mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                        .param("year", String.valueOf(year.getValue()))
                        .param("month", month.name()))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billings with given 2023 and MARCH"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void updateContractorWorkedHours() throws Exception {

        UpdateContractorHoursRequest updateContractorHoursRequest = new UpdateContractorHoursRequest(140.0);

        Integer contractorBillingId = 1;

        String json = objectMapper.writeValueAsString(updateContractorHoursRequest);

        doNothing().when(contractorBillingService).updateContractorBillingWorkedHours(updateContractorHoursRequest,contractorBillingId);

        mockMvc.perform(put(this.baseUrl + "/contractor/billing/{contractorBillingId}", contractorBillingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Update success"));


    }

    @Test
    void addContractorBilling() throws Exception {
        NewContractorBillingRequest newContractorBillingRequest = new NewContractorBillingRequest(1,150.,Year.of(2023),Month.MARCH);

        String json = objectMapper.writeValueAsString(newContractorBillingRequest);

        mockMvc.perform(post(this.baseUrl + "/contractor/billing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Contractor billing added successfully"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void deleteContractorBilling() throws Exception {

        mockMvc.perform(delete(this.baseUrl + "/contractor/billing/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
    }

    @Test
    void deleteContractorBillingNotFound() throws Exception {
        Integer contractorBillingId = 10;

        doThrow(new ObjectNotFoundException("contractor billing", contractorBillingId))
                .when(this.contractorBillingService).deleteContractorBillingById(10);

        mockMvc.perform(delete(this.baseUrl + "/contractor/billing/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billing with Id " + contractorBillingId));
    }

    @Test
    void getContractorWorkedHoursInGivenYearMonthTestSuccess() throws Exception {
        //Given

        List<ContractorBilling> contractorByYearAndMonth = new ArrayList<>();
        contractorByYearAndMonth.add(contractorBillings.get(0));
        List<ContractorBillingDTO> contractorBillingDTOS = contractorByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();

        ContractorBillingReportByMonth contractorBillingReportByMonth =
                new ContractorBillingReportByMonth(YearMonth.of(2023
                        , 3)
                        , countWorkingHoursWithoutWeekendsInMonth(Year.of(2023), Month.MARCH)
                        , contractorBillingDTOS);

        given(contractorBillingService.getContractorsWorkedHoursInGivenMonth(Year.of(2023),Month.MARCH))
                .willReturn(contractorBillingReportByMonth);

        mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                        .param("year", "2023")
                        .param("month", "MARCH")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.yearMonth").value("2023-03"))
                .andExpect(jsonPath("$.data.workingHours").value( 184.0))
                .andExpect(jsonPath("$.data.contractorBillings[0].workedHours").value(160.0))
                .andExpect(jsonPath("$.data.contractorBillings[0].id").value( 1 ));
    }
    @Test
    void getContractorWorkedHoursInGivenYearMonthTestNotFound() throws Exception {

        doThrow(new ObjectNotFoundException("contractor billings", "2023", "MARCH"))
                .when(this.contractorBillingService).getContractorsWorkedHoursInGivenMonth(Year.of(2023),Month.MARCH);

        mockMvc.perform(get(this.baseUrl + "/contractor/billing/report")
                    .param("year", "2023")
                    .param("month", "MARCH")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor billings with given 2023 and MARCH" ));

    }

}