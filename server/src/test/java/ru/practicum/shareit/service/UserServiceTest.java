package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.InvalidEmailException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.service.DtoCreater.makeUserDto;

@Transactional
@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final EntityManager em;
    private final UserService service;

    @Test
    void createUser() {
        UserDto userDto = makeUserDto("user@user.com", "user");
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();
        Long userId = user.getId();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user, equalTo(UserMapper.toUser(userId, userDto)));
    }

    @Test
    void createUserDuplicateEmail() {
        User user = UserMapper.toUser(makeUserDto("user@user.com", "user"));
        em.persist(user);

        UserDto userDtoDuplicateEmail = makeUserDto("user@user.com", "user2");

        assertThrows(Throwable.class, () -> {
            service.createUser(userDtoDuplicateEmail);
        });
    }

    @Test
    void createUserEmptyEmail() {
        UserDto userDto = makeUserDto("", "user");

        assertThrows(InvalidEmailException.class, () -> {
            service.createUser(userDto);
        });
    }

    @Test
    void createUserBlankEmail() {
        UserDto userDto = makeUserDto("  ", "user");

        assertThrows(InvalidEmailException.class, () -> {
            service.createUser(userDto);
        });
    }

    @Test
    void createNullBlankEmail() {
        UserDto userDto = makeUserDto(null, "user");

        assertThrows(InvalidEmailException.class, () -> {
            service.createUser(userDto);
        });
    }

    @Test
    void updateUserNegativeId() {
        em.persist(UserMapper.toUser(makeUserDto("userForUpdate@user.com", "userForUpdate")));

        UserDto userDtoUpdated = DtoCreater.makeUserDtoWithId(DtoCreater.NEGATIVE_ID, "user_updated@user.com", "user_updated");

        assertThrows(InvalidUserIdException.class, () -> {
            service.updateUser(DtoCreater.NEGATIVE_ID, userDtoUpdated);
        });
    }

    @Test
    void updateUserInvalidId() {
        em.persist(UserMapper.toUser(makeUserDto("userForUpdate@user.com", "userForUpdate")));

        UserDto userDtoUpdated = DtoCreater.makeUserDtoWithId(DtoCreater.INVALID_ID, "user_updated@user.com", "user_updated");

        assertThrows(InvalidUserIdException.class, () -> {
            service.updateUser(DtoCreater.INVALID_ID, userDtoUpdated);
        });
    }

    @Test
    void updateUser() {
        User user = UserMapper.toUser(makeUserDto("userForUpdate@user.com", "userForUpdate"));
        em.persist(user);
        Long id = user.getId();

        UserDto userDtoUpdated = DtoCreater.makeUserDtoWithId(id, "user_updated@user.com", "user_updated");
        service.updateUser(id, userDtoUpdated);

        User userUpdated = em.find(User.class, id);

        assertThat(userUpdated.getId(), equalTo(userDtoUpdated.getId()));
        assertThat(userUpdated.getName(), equalTo(userDtoUpdated.getName()));
        assertThat(userUpdated.getEmail(), equalTo(userDtoUpdated.getEmail()));
    }

    @Test
    void deleteUser() {
        User user = UserMapper.toUser(makeUserDto("userForDelete@user.com", "userForDelete"));
        em.persist(user);
        Long id = user.getId();

        service.deleteUser(id);
        User userDeleted = em.find(User.class, id);

        assertThat(userDeleted, nullValue());
    }

    @Test
    void deleteUserInvalidId() {
        assertThrows(InvalidUserIdException.class, () -> {
            service.deleteUser(DtoCreater.NEGATIVE_ID);
        });
    }

    @Test
    void getUserById() {
        UserDto userDto = makeUserDto("userForGet@user.com", "userForGet");
        User user = UserMapper.toUser(userDto);
        em.persist(user);
        Long id = user.getId();

        User userGet = service.getUserById(id);

        assertThat(userGet.getId(), equalTo(id));
        assertThat(userGet.getName(), equalTo(userDto.getName()));
        assertThat(userGet.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getUserInvalidId() {
        assertThrows(InvalidUserIdException.class, () -> {
            service.getUserById(DtoCreater.INVALID_ID);
        });
    }

    @Test
    void getAllUsers() {
        List<UserDto> usersDto = List.of(
                makeUserDto("user1@email", "user1"),
                makeUserDto("user2@email", "user2"),
                makeUserDto("user3@email", "user3"));

        usersDto.forEach(userDto -> em.persist(UserMapper.toUser(userDto)));

        List<User> usersGet = service.getUsers();

        assertThat(usersGet, hasSize(usersDto.size()));
        for (UserDto userDto : usersDto) {
            assertThat(usersGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(userDto.getName())),
                    hasProperty("email", equalTo(userDto.getEmail())))));
        }
    }
}