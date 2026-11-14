package com.flowcore.workflow.account.bootstrap;

import com.flowcore.workflow.account.entity.Department;
import com.flowcore.workflow.account.entity.Approver;
import com.flowcore.workflow.account.entity.ApproverId;
import com.flowcore.workflow.account.entity.GroupMembership;
import com.flowcore.workflow.account.entity.GroupMembershipId;
import com.flowcore.workflow.account.entity.UserAccount;
import com.flowcore.workflow.account.entity.UserGroup;
import com.flowcore.workflow.account.repository.ApproverRepository;
import com.flowcore.workflow.account.repository.DepartmentRepository;
import com.flowcore.workflow.account.repository.GroupMembershipRepository;
import com.flowcore.workflow.account.repository.UserAccountRepository;
import com.flowcore.workflow.account.repository.UserGroupRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("dev")
public class DevelopmentAccountInitializer implements ApplicationRunner {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final DepartmentRepository departmentRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final ApproverRepository approverRepository;

    public DevelopmentAccountInitializer(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            DepartmentRepository departmentRepository,
            UserGroupRepository userGroupRepository,
            GroupMembershipRepository groupMembershipRepository,
            ApproverRepository approverRepository
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.departmentRepository = departmentRepository;
        this.userGroupRepository = userGroupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.approverRepository = approverRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final UUID departmentId = UUID.fromString("8cb265d9-e31b-4b87-9e47-7ab0db6f3692");
        final UUID departmentId2 = UUID.fromString("8cb265d7-e31b-4b87-9e47-7ab0db6f3692");
        final UUID groupId = UUID.fromString("8cb265d8-e31b-4b87-9e47-7ab0db6f3692");

        Department financeDepartment = ensureDepartmentAccounts(departmentId, "Financeiro");
        Department administrationDepartment = ensureDepartmentAccounts(departmentId2, "Administração");
        UserGroup financeGroup = ensureGroupsAccounts(groupId, "Aprov_Financeiro", "Grupo de aprovação financeiro");

        List<AccountSeed> accounts = List.of(
                new AccountSeed("Usuário Workflow", "usuario.workflow", "usuario.workflow@example.test", null, null),
                new AccountSeed("Solicitante Piloto", "solicitante.piloto", "solicitante.piloto@example.test", financeDepartment, "Solicitante"),
                new AccountSeed("Gestor Primeiro", "gestor.primeiro", "gestor.primeiro@example.test", financeDepartment, "Gestor"),
                new AccountSeed("Gestor Segundo", "gestor.segundo", "gestor.segundo@example.test", financeDepartment, "Gestor"),
                new AccountSeed("Gestor Sem Substituto", "gestor.sem.substituto", "gestor.sem.substituto@example.test", administrationDepartment, "Gestor"),
                new AccountSeed("Financeiro Piloto", "financeiro.piloto", "financeiro.piloto@example.test", financeDepartment, "Analista Financeiro")
        );

        Map<String, UserAccount> accountsByLogin = new HashMap<>();
        for (AccountSeed account : accounts) {
            accountsByLogin.put(account.login(), ensureAccount(account));
        }

        ensureGroupMembership(requireAccount(accountsByLogin, "solicitante.piloto"), financeGroup);
        ensureGroupMembership(requireAccount(accountsByLogin, "financeiro.piloto"), financeGroup);

        ensureApprover(financeDepartment, requireAccount(accountsByLogin, "gestor.primeiro"), (short) 1);
        ensureApprover(financeDepartment, requireAccount(accountsByLogin, "gestor.segundo"), (short) 2);
        ensureApprover(administrationDepartment, requireAccount(accountsByLogin, "gestor.sem.substituto"), (short) 1);

    }

    private UserAccount requireAccount(Map<String, UserAccount> accountsByLogin, String login) {
        UserAccount account = accountsByLogin.get(login);
        if (account == null) {
            throw new IllegalStateException("Conta fictícia não foi preparada: " + login);
        }
        return account;
    }

    private void ensureGroupMembership(UserAccount account, UserGroup group) {
        GroupMembershipId id = new GroupMembershipId(account.getId(), group.getId());
        if (!groupMembershipRepository.existsById(id)) {
            groupMembershipRepository.save(new GroupMembership(account, group));
        }
    }

    private void ensureApprover(Department department, UserAccount account, Short priority) {
        ApproverId id = new ApproverId(department.getId(), priority);
        if (!approverRepository.existsById(id)) {
            approverRepository.save(new Approver(department, account, priority));
        }
    }

    private UserAccount ensureAccount(AccountSeed seed){
        Optional<UserAccount> account = repository.findByLogin(seed.login());

        if(account.isPresent()){
            return account.get();
        }

        String rawPassword = environment.getProperty("WORKFLOW_DEV_ACCOUNT_PASSWORD");

        if(rawPassword == null || rawPassword.isBlank()){
            throw new IllegalStateException("rawPassword não está configurada, precisa configurar a conta de desenvolvimento(WORKFLOW_DEV_ACCOUNT_PASSWORD)");
        }

        String hashPassword = passwordEncoder.encode(rawPassword);

        UserAccount newAccount = new UserAccount(
                seed.displayName(),
                seed.login(),
                seed.email(),
                hashPassword,
                seed.department(),
                seed.jobTitle()
        );
        return repository.save(newAccount);

    }
    private UserGroup ensureGroupsAccounts(UUID groupId, String nameGroup, String description ){
        Optional<UserGroup> group = userGroupRepository.findById(groupId);
        return (group.isPresent())? group.get() : userGroupRepository.save(new UserGroup(groupId,nameGroup,description));
    }
    private Department ensureDepartmentAccounts(UUID departmentId, String departmentName){
        Optional<Department> department = departmentRepository.findById(departmentId);
        return (department.isPresent())? department.get() : departmentRepository.save(new Department(departmentId,departmentName));
    }

    private record AccountSeed(
            String displayName,
            String login,
            String email,
            Department department,
            String jobTitle
    ) {
    }
}
