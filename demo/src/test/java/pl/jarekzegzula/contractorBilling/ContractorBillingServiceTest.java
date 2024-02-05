package pl.jarekzegzula.contractorBilling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jarekzegzula.calc.Calculator;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractor.ContractorRepository;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorBillingHoursRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.SameHoursOrLessThanZeroException;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContractorBillingServiceTest {

  @Mock private ContractorBillingRepository contractorBillingRepository;

  @Mock private ContractorRepository contractorRepository;

  @InjectMocks private ContractorBillingService contractorBillingService;

  List<ContractorBilling> contractorBillings;

  List<Contractor> contractors;

  @BeforeEach
  void setUp() {

    Contractor testContractor1 = new Contractor();
    testContractor1.setId(1);
    testContractor1.setFirstName("Ferdynand");
    testContractor1.setLastName("Testowy");
    testContractor1.setContractType(ContractType.CONTRACT_B2B);
    testContractor1.setMonthlyEarnings(59.53 * 168);
    testContractor1.setHourlyRate(59.53);
    testContractor1.setMonthlyHourLimit(168);
    testContractor1.setIsOvertimePaid(true);
    testContractor1.setOvertimeMultiplier(1.5);
    testContractor1.setContractorHourPrice(80.0);

    Contractor testContractor2 = new Contractor();
    testContractor2.setId(2);
    testContractor2.setFirstName("Marian");
    testContractor2.setLastName("Testowy");
    testContractor2.setContractType(ContractType.CONTRACT_OF_MANDATE);
    testContractor2.setMonthlyEarnings(59.53 * 150);
    testContractor2.setHourlyRate(40.0);
    testContractor2.setMonthlyHourLimit(150);
    testContractor2.setIsOvertimePaid(true);
    testContractor2.setOvertimeMultiplier(1.5);
    testContractor2.setContractorHourPrice(55.0);

    contractors = new ArrayList<>();
    contractors.add(testContractor1);
    contractors.add(testContractor2);

    ContractorBilling contractorBilling1 = new ContractorBilling();
    contractorBilling1.setId(1);
    contractorBilling1.setContractor(contractors.get(0));
    contractorBilling1.setWorkedHours(160.0);
    contractorBilling1.setYear(Year.of(2023));
    contractorBilling1.setMonth(Month.MARCH);
    contractorBilling1.setContractorRemuneration(
        Calculator.calculateContractorPayment(
            contractorBilling1.getWorkedHours(), contractorBilling1.getContractor()));
    contractorBilling1.setClientCharge(
        Calculator.calculateClientsChargeFromContractorHours(
            contractorBilling1.getWorkedHours(), contractorBilling1.getContractor()));
    contractorBilling1.setProfit(
        Calculator.calculateProfit(
            contractorBilling1.getClientCharge(), contractorBilling1.getContractorRemuneration()));

    ContractorBilling contractorBilling2 = new ContractorBilling();
    contractorBilling1.setId(2);
    contractorBilling2.setContractor(contractors.get(1));
    contractorBilling2.setWorkedHours(152.0);
    contractorBilling2.setYear(Year.of(2023));
    contractorBilling2.setMonth(Month.MAY);
    contractorBilling2.setContractorRemuneration(
        Calculator.calculateContractorPayment(
            contractorBilling2.getWorkedHours(), contractorBilling2.getContractor()));
    contractorBilling2.setClientCharge(
        Calculator.calculateClientsChargeFromContractorHours(
            contractorBilling2.getWorkedHours(), contractorBilling2.getContractor()));
    contractorBilling2.setProfit(
        Calculator.calculateProfit(
            contractorBilling2.getClientCharge(), contractorBilling2.getContractorRemuneration()));

    contractorBillings = new ArrayList<>();
    contractorBillings.add(contractorBilling1);
    contractorBillings.add(contractorBilling2);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void testFindAll() {
    given(contractorBillingRepository.findAll()).willReturn(contractorBillings);

    List<ContractorBilling> result = contractorBillingService.findAll();

    assertEquals(result.size(), 2);
  }

  @Test
  void testFindContractorBillingById() {
    // given

    ContractorBilling contractorBilling = new ContractorBilling();
    contractorBilling.setId(1);
    contractorBilling.setContractor(contractors.get(0));
    contractorBilling.setWorkedHours(160.0);
    contractorBilling.setYear(Year.of(2023));
    contractorBilling.setMonth(Month.MARCH);

    given(contractorBillingRepository.findById(1)).willReturn(Optional.of(contractorBilling));

    // When
    ContractorBilling contractorBillingById = contractorBillingService.getContractorBillingById(1);

    // Then
    assertThat(contractorBillingById.getContractor()).isEqualTo(contractorBilling.getContractor());
    assertThat(contractorBillingById.getWorkedHours())
        .isEqualTo(contractorBilling.getWorkedHours());
    assertThat(contractorBillingById.getYear()).isEqualTo(contractorBilling.getYear());
    assertThat(contractorBillingById.getMonth()).isEqualTo(contractorBilling.getMonth());
  }

  @Test
  void testFindContractorBillingByIdNotFound() {

    // Given

    given(this.contractorBillingRepository.findById(1)).willReturn(Optional.empty());

    // When
    Throwable thrown =
        catchThrowable(() -> this.contractorBillingService.getContractorBillingById(1));

    // Then
    assertThat(thrown)
        .isInstanceOf(ObjectNotFoundException.class)
        .hasMessage("Could not find contractor billing with Id 1");
    verify(this.contractorBillingRepository, times(1)).findById(any(Integer.class));
  }

  @Test
  void testAddNewContractorBilling() {
    // Given
    NewContractorBillingRequest request =
        new NewContractorBillingRequest(1, 150.0, Year.of(2023), Month.MARCH);

    Contractor testContractor1 = new Contractor();
    testContractor1.setId(1);
    testContractor1.setFirstName("Ferdynand");
    testContractor1.setLastName("Testowy");
    testContractor1.setContractType(ContractType.CONTRACT_B2B);
    testContractor1.setMonthlyEarnings(59.53 * 168);
    testContractor1.setHourlyRate(59.53);
    testContractor1.setMonthlyHourLimit(168);
    testContractor1.setIsOvertimePaid(true);
    testContractor1.setOvertimeMultiplier(1.5);
    testContractor1.setContractorHourPrice(80.0);

    ContractorBilling contractorBilling = new ContractorBilling();
    contractorBilling.setContractor(testContractor1);
    contractorBilling.setWorkedHours(request.workedHours());
    contractorBilling.setYear(request.year());
    contractorBilling.setMonth(request.month());
    contractorBilling.setContractorRemuneration(
        Calculator.calculatePaymentForB2BContract(request.workedHours(), testContractor1));
    contractorBilling.setClientCharge(
        Calculator.calculateClientsChargeFromContractorHours(
            request.workedHours(), testContractor1));
    contractorBilling.setProfit(
        Calculator.calculateProfit(
            contractorBilling.getClientCharge(), contractorBilling.getContractorRemuneration()));

    given(contractorRepository.findById(1)).willReturn(Optional.of(testContractor1));
    given(
            contractorBillingRepository.existsByContractorIdAndYearAndMonth(
                1, Year.of(2023), Month.MARCH))
        .willReturn(false);
    given(contractorBillingRepository.save(Mockito.any(ContractorBilling.class)))
        .willReturn(contractorBilling);

    // When

    ContractorBilling contractorBilling2 =
        contractorBillingService.addNewContractorBilling(request);

    // Then

    assertThat(contractorBilling2).isEqualTo(contractorBilling);
    assertNotNull(contractorBilling2);
    assertEquals(1, contractorBilling2.getContractor().getId());
    assertEquals(150.0, contractorBilling2.getWorkedHours());
    assertEquals(Year.of(2023), contractorBilling2.getYear());
    assertEquals(Month.MARCH, contractorBilling2.getMonth());
    assertNotNull(contractorBilling2.getContractorRemuneration());
    assertNotNull(contractorBilling2.getClientCharge());
    assertNotNull(contractorBilling2.getProfit());

    verify(contractorRepository, Mockito.times(1)).findById(1);
    verify(contractorBillingRepository, Mockito.times(1))
        .existsByContractorIdAndYearAndMonth(1, Year.of(2023), Month.MARCH);
    verify(contractorBillingRepository, Mockito.times(1)).save(any(ContractorBilling.class));

    System.out.println(contractorBilling2);
  }

  @Test
  void testAddNewContractorBillingAlreadyExists() {
    // Given
    NewContractorBillingRequest request =
        new NewContractorBillingRequest(1, 150.0, Year.of(2023), Month.MARCH);

    given(contractorRepository.findById(anyInt())).willReturn(Optional.of(new Contractor()));

    given(
            contractorBillingRepository.existsByContractorIdAndYearAndMonth(
                anyInt(), any(Year.class), any(Month.class)))
        .willReturn(true);

    // When and Then

    Throwable thrown =
        assertThrows(
            ContractorAlreadyExistInGivenTimeException.class,
            () -> contractorBillingService.addNewContractorBilling(request));

    assertThat(thrown)
        .isInstanceOf(ContractorAlreadyExistInGivenTimeException.class)
        .hasMessage("Contractor billing at given date already exists");
  }

  @Test
  void testUpdateContractorHours() {
    // Given
    UpdateContractorBillingHoursRequest updateRequest =
        new UpdateContractorBillingHoursRequest(150.0);

    Integer id = 1;

    ContractorBilling existingContractorBilling = new ContractorBilling();
    existingContractorBilling.setId(id);
    existingContractorBilling.setWorkedHours(100.0);

    given(contractorBillingRepository.findById(id))
        .willReturn(Optional.of(existingContractorBilling));

    // When

    contractorBillingService.updateContractorBillingWorkedHours(updateRequest, id);

    // Then

    assertEquals(updateRequest.workedHours(), existingContractorBilling.getWorkedHours(), 0.01);
  }

  @Test
  void testUpdateContractorHoursErrorSameHoursOrLessThanZeroException() {
    // Given
    Integer id = 1;
    UpdateContractorBillingHoursRequest updateRequest =
        new UpdateContractorBillingHoursRequest(150.0);
    ContractorBilling existingContractorBilling = new ContractorBilling();
    existingContractorBilling.setId(id);
    existingContractorBilling.setWorkedHours(150.0);

    given(contractorBillingRepository.findById(id))
        .willReturn(Optional.of(existingContractorBilling));

    // When and Then
    Throwable sameHoursOrLessThanZeroException =
        assertThrows(
            SameHoursOrLessThanZeroException.class,
            () -> contractorBillingService.updateContractorBillingWorkedHours(updateRequest, id));

    assertThat(sameHoursOrLessThanZeroException)
        .isInstanceOf(SameHoursOrLessThanZeroException.class)
        .hasMessage("The given data to be changed is the same or less than zero");
  }

  @Test
  void testDeleteContractorById() {
    given(this.contractorBillingRepository.findById(1))
        .willReturn(Optional.of(new ContractorBilling()));

    this.contractorBillingService.deleteContractorBillingById(1);

    verify(this.contractorBillingRepository, times(1)).deleteById(1);
  }

  @Test
  public void testDeleteContractorBillingByIdNotFound() {
    // Given
    Integer id = 1;

    given(contractorBillingRepository.findById(id)).willReturn(Optional.empty());
    // When
    Throwable error =
        assertThrows(
            ObjectNotFoundException.class,
            () -> contractorBillingService.deleteContractorBillingById(id));

    // When
    assertThat(error)
        .isInstanceOf(ObjectNotFoundException.class)
        .hasMessage("Could not find Contractor Billing with Id " + id);
    verify(contractorBillingRepository, times(1)).findById(id);
  }

  @Test
  void testGetContractorBillingMonthlyReportSuccess() {
    // Given

    Year year = Year.of(2023);
    Month month = Month.MARCH;

    List<ContractorBilling> contractorByYearAndMonth = new ArrayList<>();
    contractorByYearAndMonth.add(contractorBillings.get(0));
    List<ContractorBillingDTO> contractorBillingDTOS =
        contractorByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();

    ContractorBillingReportByMonth contractorBillingReportByMonth =
        new ContractorBillingReportByMonth(contractorBillingDTOS, year, month);

    given(contractorBillingRepository.findByYearAndMonth(any(), any()))
        .willReturn(contractorByYearAndMonth);
    // When
    ContractorBillingReportByMonth contractorsWorkedHoursInGivenMonth =
        contractorBillingService.getContractorBillingsMonthlyReport(year, month);

    // Then
    assertThat(contractorsWorkedHoursInGivenMonth).isEqualTo(contractorBillingReportByMonth);
  }

  @Test
  void testGetContractorBillingMonthlyReportNotFound() {
    // Given
    Year year = Year.of(2023);
    Month month = Month.MARCH;

    given(contractorBillingRepository.findByYearAndMonth(any(), any()))
        .willReturn(Collections.emptyList());

    // When
    Throwable error =
        assertThrows(
            ObjectNotFoundException.class,
            () -> contractorBillingService.getContractorBillingsMonthlyReport(year, month));

    // Then
    assertThat(error)
        .isInstanceOf(ObjectNotFoundException.class)
        .hasMessage("Could not find contractor billings with given " + year + " and " + month);
    verify(contractorBillingRepository, times(1)).findByYearAndMonth(year, month);
  }

  @Test
  public void testCalculateProfit() {
    // given
    BigDecimal num1 = BigDecimal.valueOf(1000);
    BigDecimal num2 = BigDecimal.valueOf(100);
    // when

    BigDecimal bigDecimal = Calculator.calculateProfit(num1, num2);
    // then

    assertThat(bigDecimal).isEqualTo(BigDecimal.valueOf(900));
  }
}
