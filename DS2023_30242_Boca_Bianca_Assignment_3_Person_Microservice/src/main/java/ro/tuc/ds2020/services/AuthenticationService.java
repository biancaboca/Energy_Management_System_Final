package ro.tuc.ds2020.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.tuc.ds2020.auth.AuthenticationRequest;
import ro.tuc.ds2020.auth.AuthenticationResponse;
import ro.tuc.ds2020.configuration.JwtService;
import ro.tuc.ds2020.dtos.PersonDetailsDTO;
import ro.tuc.ds2020.dtos.UserDTO;
import ro.tuc.ds2020.entities.User;
import ro.tuc.ds2020.repositories.PersonRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Setter
@Getter
@RequiredArgsConstructor
public class AuthenticationService {
    private final PersonRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public String tokenFront;
    private final AuthenticationManager authenticationManager;

    public UUID register(PersonDetailsDTO request) {
        var user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("user")
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        System.out.println(jwtToken+" Econded");
        System.out.println("sss"+this.getTokenFront());
        return user.getId();
       // var refreshToken = jwtService.generateRefreshToken(user);
//          saveUserToken(savedUser, jwtToken);
//        return AuthenticationResponse.builder()
//                .accessToken(jwtToken)
//         //       .refreshToken(refreshToken)
//                .build();
    }


    public UUID authenticate(AuthenticationRequest request) {

        String username = request.getUsername();
        Optional<User> userReq = repository.findByUsername(username);
        UUID uuid = userReq.get().getId();


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        uuid,
                        request.getPassword()
                )
        );
        System.out.println("aaa"+username);
        System.out.println("bbb"+uuid);
        var user = repository.findByUsername(username).orElseThrow();


        var jwtToken = jwtService.generateToken(user);
        this.setTokenFront(jwtToken);

        System.out.println(jwtToken);
        return user.getId();
    }
    public String sendingTokenToFront()
    {
        System.out.println(this.getTokenFront());
        return this.getTokenFront();

    }
     //   AuthenticationResponse.builder()
//                .accessToken(jwtToken)
//         //      .refreshToken(refreshToken)
//                .build();
      // var refreshToken = jwtService.generateRefreshToken(user);
       // revokeAllUserTokens(user);
       // saveUserToken(user, jwtToken);
//        return AuthenticationResponse.builder()
//                .accessToken(jwtToken)
//         //      .refreshToken(refreshToken)
//                .build();


//    private void saveUserToken(User user, String jwtToken) {
//        var token = Token.builder()
//                .user(user)
//                .token(jwtToken)
//                .tokenType(TokenType.BEARER)
//                .expired(false)
//                .revoked(false)
//                .build();
//        tokenRepository.save(token);
//    }

//    private void revokeAllUserTokens(User user) {
//        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
//        if (validUserTokens.isEmpty())
//            return;
//        validUserTokens.forEach(token -> {
//            token.setExpired(true);
//            token.setRevoked(true);
//        });
//        tokenRepository.saveAll(validUserTokens);
//    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByUsername(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
          //      revokeAllUserTokens(user);
          //      saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
