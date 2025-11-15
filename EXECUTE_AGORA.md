# ⚡ EXECUTE AGORA - FLYWAY MIGRATION

## ✅ Está Pronto!

A migração Flyway V9 foi criada e a configuração foi corrigida.

---

## 🚀 3 Passos para Executar

### 1️⃣ Limpe o banco (primeira execução)

```bash
dropdb -U postgres communitex
createdb -U postgres -O devuser communitex
```

### 2️⃣ Compile

```bash
./mvnw clean compile
```

### 3️⃣ Execute

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local-postgres"
```

---

## 📋 O Que Vai Acontecer

1. ✅ Flyway detectará as migrações
2. ✅ Executará V1 até V9 em sequência
3. ✅ V9 criará as colunas necessárias
4. ✅ Hibernate validará o schema
5. ✅ Aplicação inicia em 8080

---

## 📊 Logs Esperados

```
org.flywaydb.core.internal.command.DbMigrate : 
  Successfully validated 9 migrations

org.flywaydb.core.internal.command.DbMigrate : 
  Current version of schema "PUBLIC": 9

org.hibernate.dialect.Dialect : 
  HHH000262: Table [empresas] found

org.springframework.boot.web.embedded.tomcat.TomcatWebServer :
  Tomcat started on port(s): 8080 (http)
```

**SEM NENHUM ERRO!** ✅

---

## 🧪 Teste Após Iniciar

```bash
bash test-empresa-representante.sh
```

---

## ❓ Se Ainda Tiver Erro

**Erro: "user "devuser" does not exist"**
```bash
createuser -U postgres devuser
psql -U postgres -c "ALTER USER devuser WITH PASSWORD 'devpass';"
psql -U postgres -c "ALTER USER devuser CREATEDB;"
```

**Erro: "database "communitex" does not exist"**
```bash
createdb -U postgres -O devuser communitex
```

**Erro: Porta 5432 não responde**
```bash
sudo systemctl status postgresql
sudo systemctl start postgresql
```

---

## 🎯 Resumo da Solução

| Problema | Solução |
|----------|---------|
| Coluna ausente | ✅ Migração V9 criada |
| DDL-auto errado | ✅ Mantém validate |
| Schema não atualiza | ✅ Flyway gerencia |

---

**Pronto! Execute agora:** 🚀

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local-postgres"
```

