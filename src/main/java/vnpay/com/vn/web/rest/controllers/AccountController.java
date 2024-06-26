package vnpay.com.vn.web.rest.controllers;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.netty.util.internal.ObjectUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vnpay.com.vn.domain.Authority;
import vnpay.com.vn.domain.User;
import vnpay.com.vn.repository.UserRepository;
import vnpay.com.vn.security.SecurityUtils;
import vnpay.com.vn.service.MailService;
import vnpay.com.vn.service.UserRedisService;
import vnpay.com.vn.service.UserService;
import vnpay.com.vn.service.dto.AdminUserDTO;
import vnpay.com.vn.service.model.MessageBase;
import vnpay.com.vn.service.dto.PasswordChangeDTO;
import vnpay.com.vn.service.model.UserResp;
import vnpay.com.vn.web.rest.errors.*;
import vnpay.com.vn.web.rest.vm.KeyAndPasswordVM;
import vnpay.com.vn.web.rest.vm.MailVM;
import vnpay.com.vn.web.rest.vm.ManagedUserVM;

@RestController
@RequestMapping("/api")
public class AccountController {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;
    private final UserRedisService userRedisService;

    public AccountController(UserRepository userRepository, UserService userService, MailService mailService, UserRedisService userRedisService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.userRedisService = userRedisService;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException  {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException          {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUserNormal(userDTO);
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     * Get key from mail to change password when forgot
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public ResponseEntity<?> requestPasswordReset(@RequestBody MailVM mail) {
        if (!StringUtils.isEmpty(mail.getEmail())) {
            Optional<User> user = userService.requestPasswordReset(mail.getEmail());
            if (user.isPresent()) {
                mailService.sendPasswordResetMail(user.get());
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                // Pretend the request has been successful to prevent checking which emails really exist
                // but log that an invalid attempt has been made
                log.warn("Password reset requested for non existing mail");
            }
        }
        return new ResponseEntity<>(mail, HttpStatus.BAD_REQUEST);
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
                password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }

    @GetMapping("/users/info")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        MessageBase messageBase = new MessageBase();
        Set<Authority> authorities = new HashSet<>();
        try {
            String userLogin = SecurityUtils
                .getCurrentUserLogin()
                .orElseThrow(() -> new AccountResourceException("Current user login not found"));
            User user = new User();
            user = userRedisService.findUserByUserLogin(userLogin);
            if (!ObjectUtils.isEmpty(user)) {
                authorities = user.getAuthorities();
            } else {
                Optional<User> userOps = userService.getUserWithAuthoritiesByLogin(userLogin);
                if (userOps.isPresent()) {
                    user = userOps.get();
                    authorities = userOps.get().getAuthorities();
                    userRedisService.saveUserIntoRedis(user);
                }
            }

            UserResp userResp = new UserResp();
            userResp.setUser(user);
            userResp.setAuthorities(authorities);
            messageBase.setData(userResp);
            return new ResponseEntity<>(messageBase, HttpStatus.OK);
        } catch (Exception e) {
        }
        return new ResponseEntity<>(messageBase, HttpStatus.BAD_REQUEST);
    }
}
