package pl.jarekzegzula.user;

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
import pl.jarekzegzula.requests.addNewRequest.NewAppUserRequest;
import pl.jarekzegzula.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@DisplayName("Integration tests for AppUser API endpoints")
@Tag("integration")
public class AppUserControllerIntegrationTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  String token;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  @BeforeEach
  void setup() throws Exception {

    ResultActions resultActions =
        this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("user", "1234")));
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    this.token = "Bearer " + json.getJSONObject("data").getString("token");
  }

  @Test
  @DisplayName("Check findAllUsers (GET)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllUsersSuccess() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(2)));
  }

  @Test
  @DisplayName("Check findUserById (GET)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindUserByIdSuccess() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("user"));
  }

  @Test
  @DisplayName("Check findUserById with non-existent id (GET)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindUserByIdNotFound() throws Exception {
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users/5")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check addUser with valid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddUserSuccess() throws Exception {

    NewAppUserRequest newAppUserRequest = new NewAppUserRequest("Rysio", "qazwsxqwe123");

    String json = this.objectMapper.writeValueAsString(newAppUserRequest);

    this.mockMvc
        .perform(
            post(this.baseUrl + "/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Registered successfully"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("Rysio"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("admin"));

    this.mockMvc
        .perform(
            get(this.baseUrl + "/users")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  @DisplayName("Check addUser with invalid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddUserErrorWithInvalidInput() throws Exception {

    NewAppUserRequest newAppUserRequest = new NewAppUserRequest("", "");

    String json = this.objectMapper.writeValueAsString(newAppUserRequest);

    this.mockMvc
        .perform(
            post(this.baseUrl + "/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(
            jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.username").value("username is required."))
        .andExpect(jsonPath("$.data.password").value("password is required."));
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(2)));
  }

  @Test
  @DisplayName("Check deleteUser with valid input (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteUserSuccess() throws Exception {
    this.mockMvc
        .perform(
            delete(this.baseUrl + "/users/2")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users/2")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 2"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteUser with non-existent id (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteUserErrorWithNonExistentId() throws Exception {
    this.mockMvc
        .perform(
            delete(this.baseUrl + "/users/5")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteUser with insufficient permission (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteUserNoAccessAsRoleUser() throws Exception {
    ResultActions resultActions =
        this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("user2", "1234")));
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);

    String userToken = "Bearer " + json.getJSONObject("data").getString("token");

    this.mockMvc
        .perform(
            delete(this.baseUrl + "/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
    this.mockMvc
        .perform(
            get(this.baseUrl + "/users")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].username").value("user"));
  }
}
