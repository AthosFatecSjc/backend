-- tables
-- Table: Concessionarias
CREATE TABLE IF NOT EXISTS energia.concessionarias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    codigo_aneel VARCHAR(50) UNIQUE NOT NULL,
    regiao VARCHAR(50),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: Indicadores
CREATE TABLE IF NOT EXISTS energia.indicadores (
    id SERIAL PRIMARY KEY,
    concessionaria_id INTEGER REFERENCES energia.concessionarias(id),
    ano INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    dec_anual DECIMAL(10,2), 
    fec_anual DECIMAL(10,2), 
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(concessionaria_id, ano, mes)
);

-- Table: Status
CREATE TABLE status (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    name varchar(50)  NOT NULL,
    CONSTRAINT uq_status_name UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT pk_status_id PRIMARY KEY (id)
);

-- Table: Role
CREATE TABLE role (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    name varchar(50)  NOT NULL,
    CONSTRAINT uq_role_name UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: TermType
CREATE TABLE term_type (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    name varchar(50)  NOT NULL,
    CONSTRAINT term_type_pk PRIMARY KEY (id)
);

-- Table: Terms
CREATE TABLE terms (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    term_type_id UUID  NOT NULL,
    version int  NOT NULL,
    created_at timestamp  NOT NULL,
    effectivity_start_at timestamp  NOT NULL,
    effectivity_end_at timestamp  NULL,
    content text  NOT NULL,
    CONSTRAINT uq_terms_type_version UNIQUE (term_type_id, version) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT terms_pk PRIMARY KEY (id)
);

-- Table: User
CREATE TABLE app_user (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    name varchar(100)  NOT NULL,
    email varchar(50)  NOT NULL,
    password varchar(255) NOT NULL,
    phone varchar(50),
    CONSTRAINT uq_app_user_email UNIQUE (email) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT user_pk PRIMARY KEY (id)
);

-- Table: UserRole
CREATE TABLE user_role (
    user_id UUID NULL,
    role_id UUID NULL,
    PRIMARY KEY (user_id, role_id)
);

-- Table: UserStatus
CREATE TABLE user_status (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    status_id UUID  NOT NULL,
    user_id UUID  NOT NULL,
    assigned_by_user_id UUID  NULL,
    assigned_at timestamp  NOT NULL,
    rationale_for_rejection text  NULL,
    CONSTRAINT user_status_pk PRIMARY KEY (id)
);

-- Table: UserTerms
CREATE TABLE user_terms (
    id UUID  NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID  NOT NULL,
    terms_id UUID  NOT NULL,
    accepted_at timestamp  NOT NULL,
    accepted_from_ip varchar(45)  NOT NULL,
    revoked_at timestamp  NULL,
    CONSTRAINT user_terms_pk PRIMARY KEY (id)
);

-- foreign keys
-- Reference: app_user (table: user_role)
ALTER TABLE user_role ADD CONSTRAINT app_user_user_role
    FOREIGN KEY (user_id)
    REFERENCES app_user (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: terms (table: user_terms)
ALTER TABLE user_terms ADD CONSTRAINT terms_user_terms
    FOREIGN KEY (terms_id)
    REFERENCES terms (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: app_user (table: user_terms)
ALTER TABLE user_terms ADD CONSTRAINT app_user_user_terms
    FOREIGN KEY (user_id)
    REFERENCES app_user (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: status (table: user_status)
ALTER TABLE user_status ADD CONSTRAINT status_user_status
    FOREIGN KEY (status_id)
    REFERENCES status (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: term_type (table: terms)
ALTER TABLE terms ADD CONSTRAINT term_type_terms
    FOREIGN KEY (term_type_id)
    REFERENCES term_type (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: role (table: user_role)
ALTER TABLE user_role ADD CONSTRAINT role_user_role
    FOREIGN KEY (role_id)
    REFERENCES role (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: app_user (table: user_status)
ALTER TABLE user_status ADD CONSTRAINT app_user_user_status_assigned_by_user_id
    FOREIGN KEY (assigned_by_user_id)
    REFERENCES app_user (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: app_user (table: user_status)
ALTER TABLE user_status ADD CONSTRAINT app_user_user_status_user_id
    FOREIGN KEY (user_id)
    REFERENCES app_user (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

--indexes
-- Indexes for table: user_role
CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- Indexes for table: user_status
CREATE INDEX idx_user_terms_user_id ON user_terms(user_id);
CREATE INDEX idx_user_terms_terms_id ON user_terms(terms_id);

-- Indexes for table: user_status
CREATE INDEX idx_user_status_user_id ON user_status(user_id);
CREATE INDEX idx_user_status_status_id ON user_status(status_id);
CREATE INDEX idx_user_status_assigned_by ON user_status(assigned_by_user_id);

-- Indexes for table: terms
CREATE INDEX idx_terms_term_type_id ON terms(term_type_id);
CREATE INDEX idx_terms_type_version_desc ON terms(term_type_id, version DESC);

-- Index for effectivity period in terms
CREATE INDEX idx_terms_effectivity ON terms(effectivity_start_at, effectivity_end_at);

-- End of file.