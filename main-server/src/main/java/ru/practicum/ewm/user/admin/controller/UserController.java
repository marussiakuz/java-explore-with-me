package ru.practicum.ewm.user.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserOutDto> getUsers(@RequestParam(required = false) int[] ids,
                                     @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                     @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    public UserOutDto addUser(@RequestBody @Valid UserInDto userInDto) {
        return userService.createUser(userInDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable @Positive long userId) {
        userService.deleteUser(userId);
    }
}
