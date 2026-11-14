CREATE TABLE department (
    id UUID PRIMARY KEY  DEFAULT GEN_RANDOM_UUID(),
    department_name VARCHAR(150) NOT NULL
);
CREATE TABLE user_group(
    id UUID PRIMARY KEY  DEFAULT  GEN_RANDOM_UUID(),
    name_group VARCHAR(150) NOT NULL,
    description VARCHAR(350) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE group_membership(
    user_account_id UUID,
    user_group_id UUID,

    CONSTRAINT fk_group_membership_user_account
        FOREIGN KEY(user_account_id)
        REFERENCES user_account(id),

    CONSTRAINT fk_group_membership_user_group
        FOREIGN KEY(user_group_id)
        REFERENCES user_group(id),

    CONSTRAINT pk_group_membership
        PRIMARY KEY (user_account_id, user_group_id)
);
CREATE TABLE approvers(
    department_id UUID,
    approval_priority SMALLINT NOT NULL,
    user_account_id UUID NOT NULL,

    CONSTRAINT fk_approvers_department
        FOREIGN KEY(department_id)
        REFERENCES department(id),

    CONSTRAINT fk_approvers_user_account
        FOREIGN KEY(user_account_id)
        REFERENCES user_account(id),

    CONSTRAINT pk_approvers_users
        PRIMARY KEY(department_id,approval_priority),

    CONSTRAINT ck_approvers_priority
        CHECK (approval_priority IN (1, 2))

);
ALTER TABLE user_account
ADD COLUMN job_title VARCHAR(100);

ALTER TABLE user_account
    ADD COLUMN department_id UUID,
    ADD CONSTRAINT fk_user_account_department
        FOREIGN KEY (department_id) REFERENCES department(id);