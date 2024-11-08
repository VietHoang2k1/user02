package com.example.user02.controller;

import com.example.user02.dto.UserDto;
import com.example.user02.model.Role;
import com.example.user02.model.User;
import com.example.user02.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/index")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/index";
        }
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        User user = userService.findUserByEmail(email);
        if (user != null) {
            model.addAttribute("user", user);
            return "profile";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateUserProfile(@ModelAttribute("user") User user, Model model) {
        User existingUser = userService.findUserByEmail(user.getEmail());
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.save(existingUser);
        }
        model.addAttribute("message", "Profile updated successfully!");
        return "profile";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model) {
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null) {
            result.rejectValue("email", null, "There is already an account registered with the same email");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "register";
        }

        userService.saveUser(userDto);
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "reset_password";
        }

        User user = userService.findUserByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
            model.addAttribute("message", "Password updated successfully!");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Email not found.");
            return "reset_password";
        }
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "user";
    }

    @GetMapping("/users/manage")
    public String manageUser(@RequestParam(value = "id", required = false) Long id, Model model) {
        UserDto userDto;
        if (id != null) {
            User user = userService.findById(id);
            userDto = userService.convertToDto(user); // Chuyển đổi User thành UserDto
        } else {
            userDto = new UserDto(); // Đối tượng rỗng cho chế độ thêm mới
        }

        List<Role> roles = userService.findAllRoles();
        model.addAttribute("user", userDto); // Thêm đối tượng user vào Model
        model.addAttribute("roles", roles);
        return "edit_user";
    }


    @PostMapping("/users/manage")
    public String saveOrUpdateUser(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<Role> roles = userService.findAllRoles();
            model.addAttribute("roles", roles); // Truyền danh sách roles vào lại model nếu có lỗi
            return "edit_user";
        }

        if (userDto.getId() != null) {
            userService.updateUser(userDto);
        } else {
            userService.saveUser(userDto);
        }

        return "redirect:/users";
    }

    @GetMapping("/users/delete")
    public String deleteUser(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("error", "User ID is required.");
            return "redirect:/users"; // hoặc trả về trang thông báo lỗi
        }

        userService.delete(id);
        return "redirect:/users";
    }


    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/index";
        }
        return "login";
    }
}
