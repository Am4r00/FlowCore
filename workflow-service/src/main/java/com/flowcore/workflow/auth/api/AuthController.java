package com.flowcore.workflow.auth.api;

import com.flowcore.workflow.account.entity.UserAccount;
import com.flowcore.workflow.account.repository.UserAccountRepository;
import com.flowcore.workflow.auth.api.dto.AccountSummary;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository repository;

    public AuthController(UserAccountRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token){
        return token;
    }

    @GetMapping("/me")
    public AccountSummary me(Principal principal){
        String login = principal.getName();
        Optional<UserAccount> account = repository.findByLogin(login);

        if(account.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        AccountSummary accountSummary = new AccountSummary(account.get().getId(),
                                                           account.get().getDisplayName(),
                                                           account.get().getLogin());
        return accountSummary;
    }

}
