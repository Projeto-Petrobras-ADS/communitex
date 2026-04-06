# =============================================================================
# Stage 1: Build
# Compila a aplicação Spring Boot com Maven
# =============================================================================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copia os arquivos de configuração do Maven primeiro (melhora cache de camadas)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Baixa as dependências sem compilar o código fonte (cache eficiente)
RUN ./mvnw dependency:go-offline -B

# Copia o código fonte e compila a aplicação (pula testes no build da imagem)
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# =============================================================================
# Stage 2: Runtime
# Imagem final leve, somente com o JRE e o JAR compilado
# =============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Variáveis de ambiente esperadas pela aplicação
# ----------------------------------------------------------------------------
# SPRING_PROFILES_ACTIVE  : Perfil Spring ativo (ex: prod, local-postgres)
# DB_URL                  : URL JDBC do banco de dados PostgreSQL
#                           ex: jdbc:postgresql://db:5432/communitex
# DB_USERNAME             : Usuário do banco de dados
# DB_PASSWORD             : Senha do banco de dados
# JWT_SECRET              : Chave secreta Base64 para assinar tokens JWT (OBRIGATÓRIO em produção)
# JWT_EXPIRATION          : Tempo de expiração do JWT em ms (padrão: 3600000)
# JWT_REFRESH_EXPIRATION  : Tempo de expiração do refresh token em ms (padrão: 604800000)
# RABBITMQ_HOST           : Host do RabbitMQ (padrão: localhost)
# RABBITMQ_PORT           : Porta do RabbitMQ (padrão: 5672)
# RABBITMQ_USERNAME       : Usuário do RabbitMQ (padrão: guest)
# RABBITMQ_PASSWORD       : Senha do RabbitMQ (padrão: guest)
# ----------------------------------------------------------------------------

ENV SPRING_PROFILES_ACTIVE=prod \
    DB_URL=jdbc:postgresql://localhost:5432/communitex \
    DB_USERNAME=postgres \
    DB_PASSWORD=postgres \
    JWT_EXPIRATION=3600000 \
    JWT_REFRESH_EXPIRATION=604800000 \
    RABBITMQ_HOST=localhost \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=guest \
    RABBITMQ_PASSWORD=guest

WORKDIR /app

# Cria usuário não-root para segurança
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --no-create-home appuser

# Copia o JAR gerado no stage de build
COPY --from=build /app/target/*.jar app.jar

# Define o dono dos arquivos
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Health check: verifica o endpoint de health do Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
