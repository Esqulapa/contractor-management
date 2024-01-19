package pl.jarekzegzula.contractor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;
import pl.jarekzegzula.requests.updateRequest.*;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.IllegalContractTypeArgument;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.ValueUnchangedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractorServiceTest {

    @Mock
    ContractorRepository contractorRepository;

    @InjectMocks
    ContractorService contractorService;

    List<Contractor> contractors;

    @BeforeEach
    void setUp(){
        Contractor testContractor = new Contractor();
        testContractor.setId(1);
        testContractor.setFirstName("Ferdynand");
        testContractor.setLastName("Testowy");
        testContractor.setContractType(ContractType.CONTRACT_B2B);
        testContractor.setMonthlyEarnings(10000.0);
        testContractor.setHourlyRate(59.53);
        testContractor.setMonthlyHourLimit(168);
        testContractor.setIsOvertimePaid(true);
        testContractor.setOvertimeMultiplier(1.5);
        testContractor.setContractorHourPrice(80.0);

        Contractor testContractor2 = new Contractor();
        testContractor.setId(2);
        testContractor.setFirstName("Zbyszej");
        testContractor.setLastName("Testowy");
        testContractor.setContractType(ContractType.CONTRACT_OF_EMPLOYMENT);
        testContractor.setMonthlyEarnings(10000.0);
        testContractor.setHourlyRate(59.53);
        testContractor.setMonthlyHourLimit(168);
        testContractor.setIsOvertimePaid(true);
        testContractor.setOvertimeMultiplier(1.5);
        testContractor.setContractorHourPrice(80.0);



        this.contractors = new ArrayList<>();
        this.contractors.add(testContractor);
        this.contractors.add(testContractor2);


    }

    @AfterEach
    void tearDown() {
    }
    @Test
    void testFindContractorByIdSuccess() {

        // Given
        Contractor testContractor = new Contractor();
        testContractor.setId(1);
        testContractor.setFirstName("Ferdynand");
        testContractor.setLastName("Testowy");
        testContractor.setContractType(ContractType.CONTRACT_B2B);
        testContractor.setMonthlyEarnings(10000.0);
        testContractor.setHourlyRate(59.53);
        testContractor.setMonthlyHourLimit(168);
        testContractor.setIsOvertimePaid(true);
        testContractor.setOvertimeMultiplier(1.5);
        testContractor.setContractorHourPrice(80.0);

        given(this.contractorRepository.findById(1)).willReturn(Optional.of(testContractor));

        // When
        Contractor returnedContractor = this.contractorService.getContractorById(1);

        // Then

        assertThat(returnedContractor.getId()).isEqualTo(testContractor.getId());
        assertThat(returnedContractor.getFirstName()).isEqualTo(testContractor.getFirstName());
        assertThat(returnedContractor.getLastName()).isEqualTo(testContractor.getLastName());
        assertThat(returnedContractor.getContractType()).isEqualTo(testContractor.getContractType());
        assertThat(returnedContractor.getMonthlyEarnings()).isEqualTo(testContractor.getMonthlyEarnings());
        assertThat(returnedContractor.getHourlyRate()).isEqualTo(testContractor.getHourlyRate());
        assertThat(returnedContractor.getMonthlyEarnings()).isEqualTo(testContractor.getMonthlyEarnings());
        assertThat(returnedContractor.getIsOvertimePaid()).isEqualTo(testContractor.getIsOvertimePaid());
        assertThat(returnedContractor.getOvertimeMultiplier()).isEqualTo(testContractor.getOvertimeMultiplier());
        assertThat(returnedContractor.getContractorHourPrice()).isEqualTo(testContractor.getContractorHourPrice());

        verify(this.contractorRepository, times(1)).findById(1);

    }

    @Test
    void testFindContractorByIdNotFound() {

        // Given

        given(this.contractorRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(()-> this.contractorService.getContractorById(1));

        // Then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                        .hasMessage("Could not find contractor with Id 1");
        verify(this.contractorRepository, times(1)).findById(Mockito.any(Integer.class));
    }


    @Test
    void testGetAllContractorsSuccess() {
        //Given
        given(this.contractorRepository.findAll()).willReturn(this.contractors);
        //When
        List<Contractor> allContractors = this.contractorService.getContractors();
        //Then
        System.out.println(contractors);
        assertThat(allContractors.size()).isEqualTo(this.contractors.size());
        assertEquals(allContractors,this.contractors);
        verify(this.contractorRepository,times(1)).findAll();
    }



    @Test
    void testAddNewContractorSuccess() {

        //Given
        NewContractorRequest newContractorRequest = new NewContractorRequest(
                "Artur", "Testowy",
                3, 50.0,168,
                true,1.5,80.0);

        Contractor testContractor = new Contractor();
        testContractor.setFirstName(newContractorRequest.firstName());
        testContractor.setLastName(newContractorRequest.lastName());
        testContractor.setOvertimeMultiplier(1.5);
        ContractType contractType = ContractType.fromValue(newContractorRequest.contractType());
        testContractor.setContractType(contractType);


        given(this.contractorRepository.save(any(Contractor.class))).willReturn(testContractor);

        //When
        Contractor savedContractor = this.contractorService.addNewContractor(newContractorRequest);

        //Then

        assertThat(savedContractor.getFirstName()).isEqualTo(testContractor.getFirstName());
        assertThat(savedContractor.getLastName()).isEqualTo(testContractor.getLastName());
        assertThat(savedContractor.getOvertimeMultiplier()).isEqualTo(testContractor.getOvertimeMultiplier());

        System.out.println(savedContractor);

        verify(this.contractorRepository,times(1)).save(any(Contractor.class));


    }

    @Test
    public void testAddNewContractorWhenContractorExists() {
        //Given
        NewContractorRequest newContractorRequest = new NewContractorRequest(
                "Artur", "Testowy",
                3, 50.0,168,
                true,1.5,80.0);


        //When
        given(contractorRepository.existsByFirstNameAndLastName(newContractorRequest.firstName(), newContractorRequest.lastName())).willReturn(true);

        //Then
        assertThrows(ContractorAlreadyExistInGivenTimeException.class, () -> contractorService.addNewContractor(newContractorRequest));
    }

    @Test
    void testDeleteContractorById() {


        given(this.contractorRepository.findById(12)).willReturn(Optional.of(new Contractor()));

        //When
        this.contractorService.deleteContractorById(12);

        //Then
        verify(this.contractorRepository,times(1)).deleteById(12);

    }
    @Test
    public void testDeleteContractorByIdWhenContractorNotFound() {
        // Given
        Integer id = 1;

        given(contractorRepository.findById(id)).willReturn(Optional.empty());
        //When
        Throwable error = assertThrows(ObjectNotFoundException.class, () -> contractorService.deleteContractorById(id));

        // When
        assertThat(error).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find contractor with Id " + id);
        verify(contractorRepository,times(1)).findById(1);
    }

    @Test
    void testUpdateContractorHourlyRateAndMonthlyEarnings() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);
        existingContractor.setMonthlyHourLimit(168);


        UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(60.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateContractorHourlyRateAndMonthlyEarnings(updateRequest, id);

        // Then
        assertEquals(updateRequest.hourlyRate(), existingContractor.getHourlyRate(), 0.01);

        System.out.println(existingContractor);

    }
    @Test
    public void testUpdateContractorHourlyRateAndMonthlyEarningsWithTheSameValue() {
        // Given
        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);
        existingContractor.setMonthlyHourLimit(168);

        UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(50.0);
        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorHourlyRateAndMonthlyEarnings(updateRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorHourlyRateAndMonthlyEarningsInvalidValue() {
        // Given
        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);

        UpdateContractorHourlyRateRequest updateRequest = new UpdateContractorHourlyRateRequest(-500.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorHourlyRateAndMonthlyEarnings(updateRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }
    @Test
    void testUpdateContractorHourPrice() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);
        existingContractor.setMonthlyHourLimit(168);
        existingContractor.setMonthlyEarnings(168*50.0);

        UpdateMonthlyHourLimitRequest updateMonthlyHourLimitRequest = new UpdateMonthlyHourLimitRequest(150);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateMonthlyHourLimitRequest, id);

        // Then
        assertEquals(updateMonthlyHourLimitRequest.hours(), existingContractor.getMonthlyHourLimit());
        assertEquals((updateMonthlyHourLimitRequest.hours()*existingContractor.getHourlyRate()),
                (existingContractor.getMonthlyHourLimit()*existingContractor.getHourlyRate()));

    }
    @Test
    public void testUpdateContractorHourPriceUnchanged() {
        // Given
        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);
        existingContractor.setMonthlyHourLimit(168);
        existingContractor.setMonthlyEarnings(168*50.0);

        UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(168);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorHourPriceInvalidValue() {
        // Given
        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setHourlyRate(50.0);
        existingContractor.setMonthlyHourLimit(168);
        existingContractor.setMonthlyEarnings(168*50.0);

        UpdateMonthlyHourLimitRequest updateRequest = new UpdateMonthlyHourLimitRequest(-1);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }

    @Test
    void testUpdateContractorOvertimeMultiplier() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setOvertimeMultiplier(1.50);

        UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(2.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateContractorOvertimeMultiplier(updateRequest, id);

        // Then
        assertEquals(updateRequest.multiplier(), existingContractor.getOvertimeMultiplier(), 0.01);

    }

    @Test
    public void testUpdateContractorOvertimeMultiplierUnchanged() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setOvertimeMultiplier(1.50);

        UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(1.5);
        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorOvertimeMultiplier(updateRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorOvertimeMultiplierInvalidValue() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setOvertimeMultiplier(1.50);

        UpdateContractorOvertimeMultiplier updateRequest = new UpdateContractorOvertimeMultiplier(-1.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorOvertimeMultiplier(updateRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }
    @Test
    void testUpdateContractorHourLimitSuccess() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setMonthlyHourLimit(150);
        existingContractor.setHourlyRate(40.0);

        UpdateMonthlyHourLimitRequest updateMonthlyHourLimitRequest = new UpdateMonthlyHourLimitRequest(168);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateMonthlyHourLimitRequest, id);

        // Then
        assertEquals(updateMonthlyHourLimitRequest.hours(), existingContractor.getMonthlyHourLimit());

    }

    @Test
    public void testUpdateContractorHourLimitUnchanged() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setMonthlyHourLimit(168);
        existingContractor.setHourlyRate(40.0);

        UpdateMonthlyHourLimitRequest updateMonthlyHourLimitRequest = new UpdateMonthlyHourLimitRequest(168);
        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateMonthlyHourLimitRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorHourLimitInvalidValue() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setMonthlyHourLimit(168);
        existingContractor.setHourlyRate(40.0);

        UpdateMonthlyHourLimitRequest updateMonthlyHourLimitRequest = new UpdateMonthlyHourLimitRequest(-168);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateMonthlyHourLimitRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }

    @Test
    void testUpdateContractorIsOvertimePaidValue() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setIsOvertimePaid(false);

        UpdateIsContractorOvertimePaid updateRequest = new UpdateIsContractorOvertimePaid(true);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateIsOvertimePaid(updateRequest, id);

        // Then
        assertEquals(updateRequest.value(), existingContractor.getIsOvertimePaid());

    }
    @Test
    void testUpdateContractorIsOvertimePaidValueUnchanged() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setIsOvertimePaid(false);

        UpdateIsContractorOvertimePaid updateRequest = new UpdateIsContractorOvertimePaid(false);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateIsOvertimePaid(updateRequest, id));

        // Then
        verify(this.contractorRepository,times(1)).findById(1);

    }

    @Test
    void testUpdateContractorContractTypeSuccess() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractType(ContractType.CONTRACT_OF_EMPLOYMENT);

        UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(3);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateContractType(updateRequest, id);

        // Then
        assertEquals(ContractType.fromValue(updateRequest.contractType()), existingContractor.getContractType());

    }
    @Test
    void testUpdateContractorWithTheSameContractType() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractType(ContractType.CONTRACT_OF_EMPLOYMENT);

        UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(1);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractType(updateRequest,id));


        // Then
        verify(this.contractorRepository,times(1)).findById(1);

    }

    @Test
    void testUpdateContractorContractTypeWithInvalidValue() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractType(ContractType.CONTRACT_OF_EMPLOYMENT);

        UpdateContractorContractTypeRequest updateRequest = new UpdateContractorContractTypeRequest(-5);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(IllegalContractTypeArgument.class, () -> contractorService.updateContractType(updateRequest,id));


        // Then
        verify(this.contractorRepository,times(1)).findById(1);

    }



}