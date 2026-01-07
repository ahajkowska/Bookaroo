package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.entity.User;

public class UserMapper {

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getBio(),
                user.getRole(),
                user.getCreatedAt(),
                user.isLocked()
        );
    }

    public static User toEntity(CreateUserDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setAvatar(dto.getAvatar());
        user.setBio(dto.getBio());

        return user;
    }
}