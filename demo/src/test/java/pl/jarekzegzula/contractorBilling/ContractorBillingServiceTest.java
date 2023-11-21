package pl.jarekzegzula.contractorBilling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractor.ContractorRepository;
import pl.jarekzegzula.contractorBilling.Dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.NewContractorBillingRequest;
import pl.jarekzegzula.requests.UpdateContractorHoursRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.SameHoursOrLessThanZeroException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static pl.jarekzegzula.contractorBilling.ContractorBillingService.calculatePayment;
import static pl.jarekzegzula.contractorBilling.ContractorBillingService.countWorkingHoursWithoutWeekendsInMonth;
import static pl.jarekzegzula.system.Constants.*;

@ExtendWith(MockitoExtension.class)
class ContractorBillingServiceTest {

    @Mock
    private ContractorBillingRepository contractorBillingRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @InjectMocks
    private ContractorBillingService contractorBillingService;

    List<ContractorBilling> contractorBillings;

    List<Contractor> contractors;

    @BeforeEach
    void setUp() {
        Contractor testContractor1 = new Contractor();
        testContractor1.setId(1);
        testContractor1.setFirstName("Ferdynand");
        testContractor1.setLastName("Testowy");
        testContractor1.setSalary(1000.0);

        Contractor testContractor2 = new Contractor();
        testContractor2.setId(2);
        testContractor2.setFirstName("Ryszard");
        testContractor2.setLastName("Tester");
        testContractor2.setSalary(1500.0);

        contractors = new ArrayList<>();
        contractors.add(testContractor1);
        contractors.add(testContractor2);


        ContractorBilling contractorBilling1 = new ContractorBilling();
        contractorBilling1.setId(1);
        contractorBilling1.setContractor(contractors.get(0));
        contractorBilling1.setWorkedHours(160.0);
        contractorBilling1.setYear(Year.of(2023));
        contractorBilling1.setMonth(Month.MARCH);


        ContractorBilling contractorBilling2 = new ContractorBilling();
        contractorBilling1.setId(2);
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
    void testFindAll() {
        given(contractorBillingRepository.findAll()).willReturn(contractorBillings);

        List<ContractorBilling> result = contractorBillingService.findAll();

        assertEquals(result.size(), 2);
    }

    @Test
    void testFindContractorBillingById() {
        //given

        ContractorBilling contractorBilling = new ContractorBilling();
        contractorBilling.setId(1);
        contractorBilling.setContractor(contractors.get(0));
        contractorBilling.setWorkedHours(160.0);
        contractorBilling.setYear(Year.of(2023));
        contractorBilling.setMonth(Month.MARCH);

        given(contractorBillingRepository.findById(1)).willReturn(Optional.of(contractorBilling));

        //When
        ContractorBilling contractorBillingById = contractorBillingService.getContractorBillingById(1);

        //Then
        assertThat(contractorBillingById.getContractor()).isEqualTo(contractorBilling.getContractor());
        assertThat(contractorBillingById.getWorkedHours()).isEqualTo(contractorBilling.getWorkedHours());
        assertThat(contractorBillingById.getYear()).isEqualTo(contractorBilling.getYear());
        assertThat(contractorBillingById.getMonth()).isEqualTo(contractorBilling.getMonth());


    }

    @Test
    void testFindContractorBillingByIdNotFound() {

        // Given

        given(this.contractorBillingRepository.findById(1)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> this.contractorBillingService.getContractorBillingById(1));

        // Then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find contractor billing with Id 1");
        verify(this.contractorBillingRepository, times(1)).findById(any(Integer.class));
    }

    @Test
    void testAddNewContractorBilling() {
        //Given
        NewContractorBillingRequest request = new NewContractorBillingRequest(1, 150.0, Year.of(2023), Month.MARCH);

        Contractor testContractor1 = new Contractor();
        testContractor1.setId(1);
        testContractor1.setFirstName("Ferdynand");
        testContractor1.setLastName("Testowy");
        testContractor1.setSalary(1000.0);

        ContractorBilling contractorBilling = new ContractorBilling();
        contractorBilling.setContractor(testContractor1);
        contractorBilling.setWorkedHours(request.workedHours());
        contractorBilling.setYear(request.year());
        contractorBilling.setMonth(request.month());
        Double hoursInMonth = countWorkingHoursWithoutWeekendsInMonth(request.year(), request.month());
        contractorBilling.setPayment(
                calculatePayment(
                        hoursInMonth,
                        request.workedHours(),
                        testContractor1.getSalary())
        );

        given(contractorRepository.findById(1)).willReturn(Optional.of(testContractor1));
        given(contractorBillingRepository.existsByContractor_IdAndYearAndMonth(1, Year.of(2023), Month.MARCH))
                .willReturn(false);
        given(contractorBillingRepository.save(Mockito.any(ContractorBilling.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        //When

        ContractorBilling contractorBilling1 = contractorBillingService.addNewContractorBilling(request);

        //Then

        assertThat(contractorBilling1).isEqualTo(contractorBilling);
        assertNotNull(contractorBilling1);
        assertEquals(1, contractorBilling1.getContractor().getId());
        assertEquals(150.0, contractorBilling1.getWorkedHours());
        assertEquals(Year.of(2023), contractorBilling1.getYear());
        assertEquals(Month.MARCH, contractorBilling1.getMonth());
        assertNotNull(contractorBilling1.getPayment());

        verify(contractorRepository, Mockito.times(1)).findById(1);
        verify(contractorBillingRepository, Mockito.times(1)).existsByContractor_IdAndYearAndMonth(1, Year.of(2023), Month.MARCH);
        verify(contractorBillingRepository, Mockito.times(1)).save(any(ContractorBilling.class));

    }

    @Test
    void testAddNewContractorBillingAlreadyExists() {
        //Given
        NewContractorBillingRequest request = new NewContractorBillingRequest(1, 150.0, Year.of(2023), Month.MARCH);

        given(contractorRepository.findById(anyInt())).willReturn(Optional.of(new Contractor()));

        given(contractorBillingRepository.existsByContractor_IdAndYearAndMonth(anyInt(), any(Year.class), any(Month.class))).willReturn(true);

        //When and Then

        Throwable thrown = assertThrows(ContractorAlreadyExistInGivenTimeException.class, () -> contractorBillingService.addNewContractorBilling(request));

        assertThat(thrown).isInstanceOf(ContractorAlreadyExistInGivenTimeException.class).hasMessage("Contractor billing at given date already exist");
    }

    @Test
    void testUpdateContractorHours() {
        //Given
        UpdateContractorHoursRequest updateRequest = new UpdateContractorHoursRequest(150.0);

        Integer id = 1;

        ContractorBilling existingContractorBilling = new ContractorBilling();
        existingContractorBilling.setId(id);
        existingContractorBilling.setWorkedHours(100.0);

        given(contractorBillingRepository.findById(id)).willReturn(Optional.of(existingContractorBilling));

        //When

        contractorBillingService.updateContractorBillingWorkedHours(updateRequest, id);

        //Then

        assertEquals(updateRequest.workedHours(), existingContractorBilling.getWorkedHours(), 0.01);
    }

    @Test
    void testUpdateContractorHoursErrorSameHoursOrLessThanZeroException() {
        //Given
        Integer id = 1;
        UpdateContractorHoursRequest updateRequest = new UpdateContractorHoursRequest(150.0);
        ContractorBilling existingContractorBilling = new ContractorBilling();
        existingContractorBilling.setId(id);
        existingContractorBilling.setWorkedHours(150.0);

        given(contractorBillingRepository.findById(id)).willReturn(Optional.of(existingContractorBilling));

        //When and Then
        Throwable sameHoursOrLessThanZeroException = assertThrows(SameHoursOrLessThanZeroException.class, () -> contractorBillingService.updateContractorBillingWorkedHours(updateRequest, id));

        assertThat(sameHoursOrLessThanZeroException).isInstanceOf(SameHoursOrLessThanZeroException.class).hasMessage("the given data to be changed is the same or less than zero");

    }

    @Test
    void testDeleteContractorById() {
        given(this.contractorBillingRepository.findById(1)).willReturn(Optional.of(new ContractorBilling()));

        this.contractorBillingService.deleteContractorBillingById(1);

        verify(this.contractorBillingRepository, times(1)).deleteById(1);

    }



    @Test
    public void testDeleteContractorBillingByIdNotFound() {
        // Given
        Integer id = 1;

        given(contractorBillingRepository.findById(id)).willReturn(Optional.empty());
        //When
        Throwable error = assertThrows(ObjectNotFoundException.class, () -> contractorBillingService.deleteContractorBillingById(id));

        // When
        assertThat(error).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find Contractor Billing with Id " + id);
        verify(contractorBillingRepository, times(1)).findById(id);
    }

    @Test
    void getContractorWorkedHoursInGivenYearMonthTestSuccess() throws Exception {
        //Given

        Year year = Year.of(2023);
        Month month = Month.MARCH;

        List<ContractorBilling> contractorByYearAndMonth = new ArrayList<>();
        contractorByYearAndMonth.add(contractorBillings.get(0));
        List<ContractorBillingDTO> contractorBillingDTOS = contractorByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();

        ContractorBillingReportByMonth contractorBillingReportByMonth =
                new ContractorBillingReportByMonth(YearMonth.of(2023
                        , 3)
                        , countWorkingHoursWithoutWeekendsInMonth(year, month)
                        , contractorBillingDTOS);

        given(contractorBillingRepository.findByYearAndMonth(any(), any()))
                .willReturn(Optional.of(contractorByYearAndMonth));
        //When
        ContractorBillingReportByMonth contractorsWorkedHoursInGivenMonth = contractorBillingService.getContractorsWorkedHoursInGivenMonth(year, month);

        //Then
        assertThat(contractorsWorkedHoursInGivenMonth).isEqualTo(contractorBillingReportByMonth);


    }

    @Test
    void getContractorWorkedHoursInGivenYearMonthTestNotFound() throws Exception {
        //Given
        Year year = Year.of(2023);
        Month month = Month.MARCH;

        given(contractorBillingRepository.findByYearAndMonth(any(),any())).willReturn(Optional.empty());


        //When
        Throwable error = assertThrows(ObjectNotFoundException.class, () -> contractorBillingService.getContractorsWorkedHoursInGivenMonth(year,month));

        //Then
        assertThat(error).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find contractor billings with given " + year + " and " + month);
        verify(contractorBillingRepository, times(1)).findByYearAndMonth(year,month);


    }


    @Test
    public void testCalculatePayment_ZeroHours() {
        BigDecimal payment = calculatePayment(40.0, 0.0, 1500.0);
        assertEquals(new BigDecimal(ZERO_HOURS).setScale(2, RoundingMode.HALF_UP), payment);
    }

    @Test
    public void testCalculatePayment_LessThanFullTime() {
        BigDecimal payment = calculatePayment(40.0, 35.0, 1500.0);
        double expectedPayment = 35.0 * (1500.0 / 40.0);
        assertEquals(new BigDecimal(expectedPayment).setScale(2, RoundingMode.HALF_UP), payment);
    }

    @Test
    public void testCalculatePayment_FullTime() {
        BigDecimal payment = calculatePayment(40.0, 40.0, 1500.0);
        double expectedPayment = 40.0 * (1500.0 / 40.0);
        assertEquals(new BigDecimal(expectedPayment).setScale(2, RoundingMode.HALF_UP), payment);
    }

    @Test
    public void testCalculatePayment_Overtime() {
        BigDecimal payment = calculatePayment(40.0, 45.0, 1500.0);
        double fullTimePayment = 40.0 * (1500.0 / 40.0);
        double overtimePayment = (45.0 - 40.0) * (OVER_TIME_MULTIPLIER * 1500.0 / 40.0);
        double expectedPayment = fullTimePayment + overtimePayment;
        assertEquals(new BigDecimal(expectedPayment).setScale(2, RoundingMode.HALF_UP), payment);
    }

}

