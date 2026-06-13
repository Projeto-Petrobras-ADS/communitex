ALTER TABLE pessoas_fisicas ADD COLUMN cep VARCHAR(8);
ALTER TABLE pessoas_fisicas ADD COLUMN logradouro VARCHAR(255);
ALTER TABLE pessoas_fisicas ADD COLUMN numero VARCHAR(30);
ALTER TABLE pessoas_fisicas ADD COLUMN complemento VARCHAR(255);
ALTER TABLE pessoas_fisicas ADD COLUMN bairro VARCHAR(255);
ALTER TABLE pessoas_fisicas ADD COLUMN cidade VARCHAR(255);
ALTER TABLE pessoas_fisicas ADD COLUMN estado VARCHAR(2);

ALTER TABLE empresas ADD COLUMN cep VARCHAR(8);
ALTER TABLE empresas ADD COLUMN logradouro VARCHAR(255);
ALTER TABLE empresas ADD COLUMN numero VARCHAR(30);
ALTER TABLE empresas ADD COLUMN complemento VARCHAR(255);
ALTER TABLE empresas ADD COLUMN bairro VARCHAR(255);
ALTER TABLE empresas ADD COLUMN cidade VARCHAR(255);
ALTER TABLE empresas ADD COLUMN estado VARCHAR(2);
