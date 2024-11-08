package com.example.user02.service;

import com.example.user02.dto.UserDto;
import com.example.user02.model.Role;
import com.example.user02.model.User;
import com.example.user02.repository.RoleRepository;
import com.example.user02.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = convertToEntity(userDto); // Chuyển đổi UserDto thành User
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Mã hóa mật khẩu

        Role role = userDto.getEmail().equalsIgnoreCase("admin@example.com") ?
                roleRepository.findByName("ROLE_ADMIN") : roleRepository.findByName("ROLE_USER");

        if (role == null) {
            role = new Role();
            role.setName(userDto.getEmail().equalsIgnoreCase("admin@example.com") ? "ROLE_ADMIN" : "ROLE_USER");
            roleRepository.save(role);
        }

        user.setRoles(List.of(role));
        userRepository.save(user);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDto userDto) {
        User existingUser = userRepository.findById(userDto.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setName(userDto.getFirstName() + " " + userDto.getLastName());
            existingUser.setEmail(userDto.getEmail());
            existingUser.setRoles(userDto.getRoles());
            userRepository.save(existingUser);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public boolean processForgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name.length > 1 ? name[1] : "");
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles()); // Đảm bảo roles được chuyển vào UserDto
        return userDto;
    }

    @Override
    public User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setRoles(userDto.getRoles()); // Đảm bảo roles được chuyển vào User
        return user;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElse(null); // Thực hiện tìm Role theo ID
    }
}
