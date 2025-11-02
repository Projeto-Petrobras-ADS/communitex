REPORT_FILE := target/site/jacoco/index.html

OPEN_CMD = echo "Sistema operacional não suportado para abrir automaticamente. Por favor, abra manualmente:"

ifeq ($(shell uname), Darwin) # macOS
	OPEN_CMD = open
else ifeq ($(shell uname -s | cut -c 1-5), Linux) # Linux
	OPEN_CMD = xdg-open
else ifneq ($(findstring MINGW,$(shell uname -s)),) # Windows (Git Bash/MinGW)
	OPEN_CMD = start
endif

.PHONY: all test coverage open-coverage view-coverage clean

all: view-coverage

test:
	@echo "--- Rodando testes (./mvnw test) ---"
	@./mvnw test

# Alvo 2: Rodar o ciclo completo, incluindo a geração do relatório JaCoCo
# (Assume que o jacoco-maven-plugin está configurado para a fase 'verify')
coverage:
	@echo "--- Limpando, testando e gerando relatório JaCoCo (./mvnw clean verify) ---"
	@./mvnw clean verify

# Alvo 3: Apenas abrir o relatório (se já existir)
open-coverage:
	@echo "--- Abrindo relatório JaCoCo: $(REPORT_FILE) ---"
	@$(OPEN_CMD) $(REPORT_FILE)

# Alvo 4: O "Tudo-em-um" - Gera o relatório E o abre em seguida
# Este alvo 'depende' do alvo 'coverage'. Ele só será executado
# depois que 'coverage' terminar com sucesso.
view-coverage: coverage
	@echo "--- Abrindo relatório JaCoCo recém-gerado ---"
	@$(OPEN_CMD) $(REPORT_FILE)

# Alvo bônus: Limpar o projeto
clean:
	@echo "--- Limpando projeto (./mvnw clean) ---"
	@./mvnw clean
