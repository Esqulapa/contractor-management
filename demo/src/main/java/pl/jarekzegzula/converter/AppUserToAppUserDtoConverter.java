package pl.jarekzegzula.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import pl.jarekzegzula.user.AppUser;
import pl.jarekzegzula.user.dto.AppUserDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppUserToAppUserDtoConverter implements Converter<AppUser, AppUserDto> {

    @Override
    public AppUserDto convert(AppUser source) {

        return new AppUserDto(source.getId(),
                source.getUsername(),
                source.isEnabled(),
                source.getRoles());
    }

    public List<AppUserDto> convertList(List<AppUser> userList) {

        return userList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
