package com.flowcore.workflow.auth.security;

import com.flowcore.workflow.account.entity.UserAccount;
import com.flowcore.workflow.account.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DatabaseDetailService implements UserDetailsService {

    private final UserAccountRepository repository;

    public DatabaseDetailService(UserAccountRepository repository) {
        this.repository = repository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String login = username.strip().toLowerCase(Locale.ROOT);
        Optional<UserAccount> account = repository.findByLogin(login);

        if(account.isEmpty()){
            throw new UsernameNotFoundException("Conta não encontrada !");
        }

        UserAccount userAccount = account.get();
        return User.withUsername(userAccount.getLogin())
                .password(userAccount.getPasswordHash())
                .disabled(!userAccount.isActive())
                .authorities(List.of())
                .build();
    }
}
