package pe.fullstack.servicesbackend.controller;

import org.mapstruct.*;
import pe.fullstack.servicesbackend.controller.request.CreateUserRequest;
import pe.fullstack.servicesbackend.controller.request.UpdateUserRequest;
import pe.fullstack.servicesbackend.controller.request.User;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapping {
    @Mapping(source = "username", target = "username")
    User convert(CreateUserRequest createUserRequest);
    User convert(UpdateUserRequest updateUserRequest);

    @Mapping(target ="id",ignore = true )
    @Mapping(target ="username",ignore = true )
    @Mapping(target ="email",ignore = true )
    User copyFrom(@MappingTarget User oldUser, User oldUserWithNewInfo);

}
