package pl.jarekzegzula.contractor;

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
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
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
@AutoConfigureMockMvc(addFilters = false) //Turn off spring security
class ContractorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ContractorService contractorService;

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
        contractor1.setSalary(1600.0);
        contractors.add(contractor1);

        Contractor contractor2 = new Contractor();
        contractor2.setId(2);
        contractor2.setFirstName("Ryszard");
        contractor2.setLastName("Peja");
        contractor2.setSalary(1300.0);
        contractors.add(contractor2);

        Contractor contractor3 = new Contractor();
        contractor3.setId(3);
        contractor3.setFirstName("Zbigniew");
        contractor3.setLastName("Cebula");
        contractor3.setSalary(1700.0);
        contractors.add(contractor3);

        Contractor contractor4 = new Contractor();
        contractor4.setId(4);
        contractor4.setFirstName("Bogdan");
        contractor4.setLastName("Baryła");
        contractor4.setSalary(1350.0);
        contractors.add(contractor4);


    }
    @AfterEach
    void tearDown() {
    }


    @Test
    void testGetAllContractors() throws Exception {
        //given
        given(contractorService.getContractors()).willReturn(this.contractors);

        //When Then
        mockMvc.perform(get(this.baseUrl + "/contractor").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.contractors.size())))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("Marian"))
                ;


    }

    @Test
    void testGetContractorByIdSuccess() throws Exception {
        // Given
        given(this.contractorService.getContractorById(1)).willReturn(this.contractors.get(0));

        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/contractor/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Marian"))
                .andExpect(jsonPath("$.data.lastName").value("Paździoch"))
                .andExpect(jsonPath("$.data.salary").value(1600.0));
    }

    @Test
    void testGetContractorByIdNotFound() throws Exception {
        // Given
        Integer id = 10;

        given(this.contractorService.getContractorById(id)).willThrow(new ObjectNotFoundException("contractor", id));

        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/contractor/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + id))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    void testAddContractor() throws Exception {
        // Given
        NewContractorRequest newContractorRequest = new NewContractorRequest("Marian", "Paździoch", 1600.0);

        String jsonRequest = objectMapper.writeValueAsString(newContractorRequest);

        Contractor savedContractor = new Contractor();
        savedContractor.setId(1);
        savedContractor.setFirstName("Marian");
        savedContractor.setLastName("Paździoch");
        savedContractor.setSalary(1600.0);

        given(this.contractorService.addNewContractor(newContractorRequest)).willReturn(savedContractor);
        //When and Then
        mockMvc.perform(post(this.baseUrl + "/contractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Contractor added successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Marian"));
    }

    @Test
    void testUpdateContractorSalary() throws Exception {

        //Given
        Contractor savedContractor = new Contractor();
        savedContractor.setId(1);
        savedContractor.setFirstName("Marian");
        savedContractor.setLastName("Paździoch");
        savedContractor.setSalary(1600.0);

        Integer contractorId = 1;

        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(1500.0);
        String json = objectMapper.writeValueAsString(updateRequest);


        doNothing().when(contractorService).updateContractorSalary(updateRequest, contractorId);

        //When and Then
        mockMvc.perform(put(this.baseUrl + "/contractor/{contractorId}", contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Update success"));
    }

    @Test
    void testUpdateContractorSalaryErrorWithNonExistentId() throws Exception {
        //Given
        UpdateContractorSalaryRequest updateRequest = new UpdateContractorSalaryRequest(1500.0);

        String json = objectMapper.writeValueAsString(updateRequest);

        Integer contractorId = 10;

        doThrow(new ObjectNotFoundException("contractor",contractorId)).when(this.contractorService).updateContractorSalary(updateRequest,contractorId);
        //When and Than
       mockMvc.perform(put(this.baseUrl + "/contractor/{contractorId}", contractorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId));
    }

    @Test
    void testDeleteContractorSuccess() throws Exception {

        //Given
        Integer contractorId = 1;

        doNothing().when(contractorService).deleteContractorById(contractorId);

        //When and Then
        mockMvc.perform(delete(this.baseUrl + "/contractor/{contractorId}", contractorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Delete Success"));
    }

    @Test
    void testDeleteContractorNonExistentId() throws Exception {
        Integer contractorId = 10;

        doThrow(new ObjectNotFoundException("contractor", contractorId)).when(this.contractorService).deleteContractorById(contractorId);

        mockMvc.perform(delete(this.baseUrl + "/contractor/{contractorId}", contractorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find contractor with Id " + contractorId))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}