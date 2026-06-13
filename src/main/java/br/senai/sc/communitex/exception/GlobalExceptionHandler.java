package br.senai.sc.communitex.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAdocaoException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAdocaoException(InvalidAdocaoException ex) {
        log.warn("Adoção inválida: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage()));
    }

    @SuppressWarnings("deprecation")
    @ExceptionHandler(BusinessExpection.class)
    public ResponseEntity<ProblemDetail> handleBusinesExpection(BusinessExpection ex) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbiddenException(ForbiddenException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problem(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateIssueException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateIssueException(DuplicateIssueException ex) {
        log.warn("Denúncia duplicada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "Denúncia duplicada", ex.getMessage()));
    }

    @ExceptionHandler({AuthenticationException.class, AuthenticationServiceException.class})
    public ResponseEntity<ProblemDetail> handleAuthenticationException(RuntimeException ex) {
        log.warn("Falha de autenticação: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(problem(HttpStatus.UNAUTHORIZED, "Não autenticado", "Credenciais inválidas"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", fieldError.getDefaultMessage() == null
                                ? "Valor inválido"
                                : fieldError.getDefaultMessage()
                ))
                .toList();

        var message = fieldErrors.stream()
                .map(error -> error.get("field") + ": " + error.get("message"))
                .reduce((a, b) -> a + ", " + b)
                .orElse("Erro de validação");

        log.warn("Erro de validação na requisição: {}", message);
        var error = problem(HttpStatus.BAD_REQUEST, "Erro de validação", message);
        error.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Erro interno do servidor", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Erro interno do servidor"));
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://communitex.local/problems/" + status.value()));
        problem.setProperty("message", detail);
        return problem;
    }
}
