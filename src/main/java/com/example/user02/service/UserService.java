package com.example.user02.service;

import com.example.user02.dto.UserDto;
import com.example.user02.model.Role;
import com.example.user02.model.User;

import java.util.List;

public interface UserService {

    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    boolean processForgotPassword(String email);

    List<UserDto> findAllUsers();

    void updatePassword(User user, String newPassword);

    void updateUser(UserDto userDto); // Cập nhật User từ UserDto

    void save(User user);

    User findById(Long id);

    void delete(Long id);

    UserDto convertToDto(User user); // Chuyển đổi User thành UserDto

    User convertToEntity(UserDto userDto); // Chuyển đổi UserDto thành User

    List<Role> findAllRoles(); // Lấy danh sách tất cả các vai trò

    Role findRoleById(Long id); // Thêm phương thức này để tìm kiếm vai trò theo ID
}
