package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.DuplicateUserException;
import BudgetTracker.Tracker.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createNewUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (DuplicateUserException e) {
            // Specific handling for duplicate user
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Fallback for other data integrity issues, including unique constraint violations not caught by DuplicateUserException
            return ResponseEntity.badRequest().body("A user with the provided email already exists.");
        }
    }


    @GetMapping("/find")
    public ResponseEntity<?> findUser(@RequestParam String name, @RequestParam String email) {
        Optional<User> userOpt = userService.findUserByNameAndEmail(name, email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            // Instead of returning 404, return a custom message or status
            return ResponseEntity.status(HttpStatus.OK).body("User not found. Proceed with creation.");
        }
    }
}