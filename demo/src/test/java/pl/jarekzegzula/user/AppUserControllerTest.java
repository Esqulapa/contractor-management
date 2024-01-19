package pl.jarekzegzula.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.jarekzegzula.requests.addNewRequest.NewAppUserRequest;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AppUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AppUserService appUserService;

    @MockBean
    AppUserRepository appUserRepository;

    List<AppUser> appUsers;

    @Value("${api.endpoint.base-url}")
    String baseUrl;




    @BeforeEach
    void setUp() {
        AppUser user1 = new AppUser();
        user1.setId(1);
        user1.setUsername("Bobby");
        user1.setPassword("123456");
        user1.setRoles("admin");
        user1.setEnabled(true);

        AppUser user2 = new AppUser();
        user2.setId(2);
        user2.setUsername("Marian");
        user2.setPassword("123123");
        user2.setRoles("admin");
        user2.setEnabled(true);

        AppUser user3 = new AppUser();
        user3.setId(3);
        user3.setUsername("Jake");
        user3.setPassword("321321");
        user3.setRoles("admin");
        user3.setEnabled(true);

        appUsers = new ArrayList<>();
        appUsers.add(user1);
        appUsers.add(user2);
        appUsers.add(user3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testGetUsers() throws Exception {
        //Given
        given(appUserService.getAllUsers()).willReturn(appUsers);
        //When and Then
        mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(appUsers.size())))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("Bobby"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].username").value("Marian"));
    }

    @Test
    public void testGetUserById() throws Exception {
        //Given
        Integer userId = 1;

        given(appUserService.findById(userId)).willReturn(this.appUsers.get(0));

        //When and Then
        mockMvc.perform(get(this.baseUrl + "/users/{id}", userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("Bobby"))
                .andExpect(jsonPath("$.data.roles").value("admin"));
    }

    @Test
    void testFindUserByIdNotFound() throws Exception {

        //Given
        Integer userId = 5;

        given(this.appUserService.findById(userId)).willThrow(new ObjectNotFoundException("user", userId));


        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id " + userId))
                .andExpect(jsonPath("$.data").isEmpty());
    }



    @Test
    public void testCreateUserSuccess() throws Exception {
        //Given

        NewAppUserRequest newUserRequest = new NewAppUserRequest("Bobby", "123456");

        String json = objectMapper.writeValueAsString(newUserRequest);

        AppUser savedUser = new AppUser();
        savedUser.setId(1);
        savedUser.setUsername("Bobby");
        savedUser.setPassword("123456");
        savedUser.setEnabled(true);
        savedUser.setRoles("admin");

        given(this.appUserService.addNewUser(Mockito.any(NewAppUserRequest.class))).willReturn(savedUser);

        //When and Then
        mockMvc.perform(post(this.baseUrl + "/users/register").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Registered successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("Bobby"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin"));

    }

    @Test
    public void testDeleteUser() throws Exception {
        Integer userId = 1;

        mockMvc.perform(delete(this.baseUrl + "/users/{userId}", userId).accept((MediaType.APPLICATION_JSON)))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
    }

    @Test
    public void testDeleteUserErrorWithNonExistentId() throws Exception {
        Integer userId = 4;

        doThrow(new ObjectNotFoundException("user", userId)).when(this.appUserService).deleteUserById(userId);

        mockMvc.perform(delete(this.baseUrl + "/users/{userId}", userId).accept((MediaType.APPLICATION_JSON)))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id " + userId));
    }
}