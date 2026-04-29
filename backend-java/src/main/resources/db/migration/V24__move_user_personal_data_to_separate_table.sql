CREATE TABLE user_personal_data (
    user_id UUID NOT NULL,
    name varchar(100) NOT NULL,
    email varchar(50) NOT NULL,
    phone varchar(50),
    CONSTRAINT user_personal_data_pk PRIMARY KEY (user_id),
    CONSTRAINT uq_user_personal_data_email UNIQUE (email),
    CONSTRAINT fk_user_personal_data_app_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

INSERT INTO user_personal_data (user_id, name, email, phone)
SELECT id, name, email, phone
FROM app_user
ON CONFLICT (user_id) DO NOTHING;

ALTER TABLE app_user
    DROP CONSTRAINT IF EXISTS uq_app_user_email;

ALTER TABLE app_user
    DROP COLUMN IF EXISTS name,
    DROP COLUMN IF EXISTS email,
    DROP COLUMN IF EXISTS phone;