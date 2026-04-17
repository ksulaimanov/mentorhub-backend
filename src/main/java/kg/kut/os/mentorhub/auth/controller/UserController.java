package kg.kut.os.mentorhub.auth.controller;

import kg.kut.os.mentorhub.auth.dto.UserMeResponse;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.service.UserService;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getCurrentUser(@CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserMeInfo(user));
    }
}



