package pl.jarekzegzula.contractor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorOvertimeMultiplier;
import pl.jarekzegzula.requests.UpdateContractorPrice;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.ValueUnchangedException;

import java.math.BigDecimal;
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
        NewContractorRequest newContractor1 = new NewContractorRequest(
                "Marian", "Paździoch", 1600.0,1.5,2000.0);

        NewContractorRequest newContractor2 = new NewContractorRequest(
                "Ryszard", "Peja", 3000.0,1.5,4000.0);

        NewContractorRequest newContractor3 = new NewContractorRequest(
                "Ferdynand", "Kiepski ", 1500.0,1.5,2000.0);

        Contractor contractor1 = this.contractorService.addNewContractor(newContractor1);
        Contractor contractor2 = this.contractorService.addNewContractor(newContractor2);
        Contractor contractor3 = this.contractorService.addNewContractor(newContractor3);

        this.contractors = new ArrayList<>();
        this.contractors.add(contractor1);
        this.contractors.add(contractor2);
        this.contractors.add(contractor3);


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
        testContractor.setSalary(1000.0);
        testContractor.setOvertimeMultiplier(1.5);
        testContractor.setContractorPrice(2000.0);

        given(this.contractorRepository.findById(1)).willReturn(Optional.of(testContractor));

        // When
        Contractor returnedContractor = this.contractorService.getContractorById(1);

        // Then
        assertThat(returnedContractor.getId()).isEqualTo(testContractor.getId());
        assertThat(returnedContractor.getFirstName()).isEqualTo(testContractor.getFirstName());
        assertThat(returnedContractor.getLastName()).isEqualTo(testContractor.getLastName());
        assertThat(returnedContractor.getSalary()).isEqualTo(testContractor.getSalary());
        assertThat(returnedContractor.getContractorPrice()).isEqualTo(testContractor.getContractorPrice());
        assertThat(returnedContractor.getOvertimeMultiplier()).isEqualTo(testContractor.getOvertimeMultiplier());
        verify(this.contractorRepository, times(1)).findById(1);
    }

    @Test
    void testFindContractorByIdNotFound() {

        // Given

        given(this.contractorRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(()->{ Contractor contractor = this.contractorService.getContractorById(1);
        });

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
        assertThat(allContractors.size()).isEqualTo(this.contractors.size());
        assertEquals(allContractors,this.contractors);
        verify(this.contractorRepository,times(1)).findAll();
    }



    @Test
    void testAddNewContractorSuccess() {

        //Given
        NewContractorRequest newContractorRequest = new NewContractorRequest(
                "Artur", "Testowy", 1600.0,1.5,2000.0);

        Contractor testContractor = new Contractor();
        testContractor.setFirstName(newContractorRequest.firstName());
        testContractor.setLastName(newContractorRequest.lastName());
        testContractor.setSalary(newContractorRequest.salary());
        testContractor.setOvertimeMultiplier(1.5);
        testContractor.setContractorPrice(2000.0);


        given(this.contractorRepository.save(testContractor)).willReturn(testContractor);

        //When
        Contractor savedContractor = this.contractorService.addNewContractor(newContractorRequest);

        //Then

        assertThat(savedContractor.getFirstName()).isEqualTo(testContractor.getFirstName());
        assertThat(savedContractor.getLastName()).isEqualTo(testContractor.getLastName());
        assertThat(savedContractor.getSalary()).isEqualTo(testContractor.getSalary());
        assertThat(savedContractor.getContractorPrice()).isEqualTo(testContractor.getContractorPrice());
        assertThat(savedContractor.getOvertimeMultiplier()).isEqualTo(testContractor.getOvertimeMultiplier());

        verify(this.contractorRepository,times(1)).save(testContractor);


    }

    @Test
    public void testAddNewContractorWhenContractorExists() {
        //Given
        NewContractorRequest request = new NewContractorRequest("John", "Doe", 1000.0,1.5,2000.0);

        //When
        given(contractorRepository.existsByFirstNameAndLastName(request.firstName(), request.lastName())).willReturn(true);

        //Then
        assertThrows(ContractorAlreadyExistInGivenTimeException.class, () -> contractorService.addNewContractor(request));
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
    void updateContractorSalary() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setSalary(1000.0);

        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(2000.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateContractorSalary(updateRequest, id);

        // Then
        assertEquals(updateRequest.salary(), existingContractor.getSalary(), 0.01);

    }
    @Test
    public void testUpdateContractorSalaryUnchanged() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setSalary(1000.0);

        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(1000.0);
        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorSalary(updateRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorSalaryInvalidValue() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setSalary(1000.0);

        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(-500.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorSalary(updateRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }
    @Test
    void updateContractorPrice() {
        // Given

        Integer id = 1;

        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractorPrice(1000.0);

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(2000.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        contractorService.updateContractorPrice(updateRequest, id);

        // Then
        assertEquals(updateRequest.price(), existingContractor.getContractorPrice(), 0.01);

    }
    @Test
    public void testUpdateContractorPriceUnchanged() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractorPrice(1000.0);

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(1000.0);
        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorPrice(updateRequest, id));

        //Then
        verify(this.contractorRepository,times(1)).findById(1);
    }
    @Test
    public void testUpdateContractorPriceInvalidValue() {
        // Given
        Integer id = 1;
        Contractor existingContractor = new Contractor();
        existingContractor.setId(id);
        existingContractor.setContractorPrice(1000.0);

        UpdateContractorPrice updateRequest = new UpdateContractorPrice(-500.0);

        given(contractorRepository.findById(id)).willReturn(Optional.of(existingContractor));

        // When
        assertThrows(ValueUnchangedException.class, () -> contractorService.updateContractorPrice(updateRequest, id));

        //Then
        verify(contractorRepository,times(1)).findById(1);
    }

    @Test
    void updateContractorOvertimeMultiplier() {
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

}