package BudgetTracker.Tracker.service;
//validation/prerequcition check
import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.DuplicateUserException;
import BudgetTracker.Tracker.exceptions.InvalidInputException;
import BudgetTracker.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
/**
 * Service class for handling business logic related to users.
 */
@Service
public class UserService {
    @Autowired
    private  UserRepository userRepository;
    /**
     * Checks if a user exists by ID.
     *
     * @param userId The ID of the user to check.
     * @return True if the user exists, false otherwise.
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
    /**
     * Creates a new user.
     *
     * @param user The user object to be created.
     * @return The created user.
     * @throws InvalidInputException   If the name is not alphanumeric or if the email format is invalid.
     * @throws DuplicateUserException  If a user with the same name and email already exists.
     */
    public User createNewUser(User user) {
        // Validate alphanumeric name
        if (!isValidAlphanumeric(user.getName())) {
            throw new InvalidInputException("Name must be alphanumeric");
        }

        // Validate email format
        if (!isValidEmailFormat(user.getEmail())) {
            throw new InvalidInputException("Invalid email format");
        }
        Optional<User> existingUser = userRepository.findByNameAndEmail(user.getName(), user.getEmail());
        if (existingUser.isPresent()) {
            throw new DuplicateUserException("An account with these credentials already exists.");
        }
        return userRepository.save(user);
    }
    /**
     * Finds a user by name and email.
     *
     * @param name  The name of the user to find.
     * @param email The email of the user to find.
     * @return Optional containing the found user, or empty if not found.
     */
    public Optional<User> findUserByNameAndEmail(String name, String email) {
        return userRepository.findByNameAndEmail(name, email);
    }

    /**
     * Checks if a string contains only alphanumeric characters or spaces.
     *
     * @param name The string to be checked.
     * @return True if the string contains only alphanumeric characters, false otherwise.
     */
    private boolean isValidAlphanumeric(String name) {
        return name!=null && name.matches("^(?=.*[a-zA-Z])[a-zA-Z0-9]+$");
    }

    /**
     * Validates the format of an email address.
     * This method checks if the provided email address matches the standard email format.
     * The email address is considered valid if it conforms to the following pattern:
     *
     * @param email The email address to be validated.
     * @return {@code true} if the email address format is valid, {@code false} otherwise.
     */
    private boolean isValidEmailFormat(String email) {
        // Trim the email string to remove leading and trailing whitespace characters
        email = email.trim();
        // Regular expression for email validation
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(regex);
    }
}
