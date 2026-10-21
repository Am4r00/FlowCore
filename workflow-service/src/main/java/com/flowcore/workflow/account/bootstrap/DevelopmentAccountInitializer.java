package com.flowcore.workflow.account.bootstrap;

import com.flowcore.workflow.account.entity.UserAccount;
import com.flowcore.workflow.account.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("dev")
public class DevelopmentAccountInitializer implements ApplicationRunner {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public DevelopmentAccountInitializer(UserAccountRepository repository, PasswordEncoder passwordEncoder, Environment environment) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String login = "usuario.workflow";
        Optional<UserAccount> account = repository.findByLogin(login);

        if(account.isPresent()){
           return;
        }

        String rawPassword = environment.getProperty("WORKFLOW_DEV_ACCOUNT_PASSWORD");

        if(rawPassword == null || rawPassword.isBlank()){
            throw new IllegalStateException("rawPassword não está configurada, precisa configurar a conta de desenvolvimento(WORKFLOW_DEV_ACCOUNT_PASSWORD)");
        }

        String hashPassword = passwordEncoder.encode(rawPassword);

        String displayName = "Usuário Workflow";
        String email = "usuario.workflow@example.test";

        repository.save(new UserAccount(displayName,login,email,hashPassword));

    }
}
