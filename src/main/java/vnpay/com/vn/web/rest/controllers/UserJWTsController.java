package vnpay.com.vn.web.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.OptBoolean;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vnpay.com.vn.domain.RefreshToken;
import vnpay.com.vn.domain.User;
import vnpay.com.vn.security.jwt.JWTFilter;
import vnpay.com.vn.security.jwt.TokenProvider;
import vnpay.com.vn.service.RefreshTokenService;
import vnpay.com.vn.service.UserService;
import vnpay.com.vn.service.dto.UserDTO;
import vnpay.com.vn.web.rest.vm.LoginVM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class UserJWTsController {

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserJWTsController(TokenProvider tokenProvider,
                              AuthenticationManagerBuilder authenticationManagerBuilder,
                              UserService userService,
                              RefreshTokenService refreshTokenService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginVM loginVM) {
        Optional<User> user = userService.getUserByUserName(loginVM.getUsername());
        if (user.isPresent()) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginVM.getUsername(),
                loginVM.getPassword()
            );

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication);

            String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.get());

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(new JWTToken("Bearer " + jwt), httpHeaders, HttpStatus.OK);
        }
        return null;
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
