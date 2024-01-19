package pl.jarekzegzula.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.jarekzegzula.requests.addNewRequest.NewAppUserRequest;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.UserAlreadyExistException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    AppUserRepository appUserRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AppUserService appUserService;

    List<AppUser> appUsers;

    @BeforeEach
    void setUp() {
        AppUser appUser1 = new AppUser();
        appUser1.setId(1);
        appUser1.setUsername("Bobby");
        appUser1.setPassword("123456");
        appUser1.setEnabled(true);
        appUser1.setRoles("admin");

        AppUser appUser2 = new AppUser();
        appUser2.setId(2);
        appUser2.setUsername("Zbyszko");
        appUser2.setPassword("123456");
        appUser2.setEnabled(true);
        appUser2.setRoles("user");

        this.appUsers = new ArrayList<>();
        this.appUsers.add(appUser1);
        this.appUsers.add(appUser2);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetAllUsersSuccess() {

        given(this.appUserRepository.findAll()).willReturn(this.appUsers);

        List<AppUser> allUsers = this.appUserService.getAllUsers();

        assertThat(allUsers.size()).isEqualTo(appUsers.size());

        verify(this.appUserRepository, times(1)).findAll();
    }

    @Test
    void findById() {
        //Given
        AppUser user = new AppUser();
        user.setId(1);
        user.setUsername("Bobby");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRoles("admin");

        given(this.appUserRepository.findById(1)).willReturn(Optional.of(user));

        //When
        AppUser returnedUser = this.appUserService.findById(1);

        //Then
        assertEquals(returnedUser, user);


    }


    @Test
    void testAddNewUserSuccess() throws UserAlreadyExistException {
        //Given

        NewAppUserRequest newAppUserRequest = new NewAppUserRequest("Bobby", "123456");

        AppUser expectedUser = new AppUser();
        expectedUser.setUsername("Bobby");
        expectedUser.setPassword("EncodedPassword");
        expectedUser.setRoles("admin");
        expectedUser.setEnabled(true);

        given(appUserRepository.existsByUsername(newAppUserRequest.username())).willReturn(false);
        given(passwordEncoder.encode(newAppUserRequest.password())).willReturn("EncodedPassword");

        //When
        ArgumentCaptor<AppUser> appUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        appUserService.addNewUser(newAppUserRequest);
        verify(appUserRepository).save(appUserCaptor.capture());
        AppUser capturedAppUser = appUserCaptor.getValue(); //AppUser object captured from save() method

        //Then
        assertEquals(expectedUser, capturedAppUser);

    }

    @Test
    public void testAddNewUserWhenUsernameIsTaken() {

        NewAppUserRequest newAppUserRequest = new NewAppUserRequest("Bobby", "123456");


        when(appUserRepository.existsByUsername(newAppUserRequest.username())).thenReturn(true);


        Throwable userAlreadyExistException = assertThrows(UserAlreadyExistException.class, () -> appUserService.addNewUser(newAppUserRequest));

        assertThat(userAlreadyExistException).isInstanceOf(UserAlreadyExistException.class)
                .hasMessage("Requested username is already taken.");
    }

    @Test
    void testDeleteSuccess() {

        //Given
        given(this.appUserRepository.findById(1)).willReturn(Optional.of(new AppUser()));

        // When
        this.appUserService.deleteUserById(1);

        // Then
        verify(this.appUserRepository, times(1)).deleteById(1);
    }

    @Test
    public void testDeleteUserByIdNotFound() {
        //Given
        Integer userId = 1;

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        //When
        Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> appUserService.deleteUserById(userId));

        //Then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with Id " + userId);

    }

}