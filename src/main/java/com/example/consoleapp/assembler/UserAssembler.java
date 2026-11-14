package com.example.consoleapp.assembler;

import com.example.consoleapp.controller.UserController;
import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.entity.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@Component
public class UserAssembler implements RepresentationModelAssembler<UserDto, EntityModel<UserDto>> {

    @Override
    public EntityModel<UserDto> toModel(UserDto userDto) {
        return EntityModel.of(userDto,
                linkTo(methodOn(UserController.class).getUserById(userDto.id())).withSelfRel(),

                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );
    }

}
//Он отвечает за превращение объекта DTO в EntityModel и добавление ссылок.