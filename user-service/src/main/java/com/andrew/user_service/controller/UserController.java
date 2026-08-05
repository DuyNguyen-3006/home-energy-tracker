package com.andrew.user_service.controller;

import com.andrew.user_service.application.dto.UserDto;
import com.andrew.user_service.application.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
@Tag(name = "Users", description = "User Management APIs")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {

        UserDto created = userService.createUser(userDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable @Positive Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @PutMapping("{id}")
    public ResponseEntity<String> updateUser(@PathVariable @Positive Long id,
                                             @Valid @RequestBody UserDto userDto) {
        userService.updateUser(id, userDto);
        return ResponseEntity.ok("User updated");
    }
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Positive Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Delete sucessfully");
    }
}
