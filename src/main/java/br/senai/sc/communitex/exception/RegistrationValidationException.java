package br.senai.sc.communitex.exception;

import java.util.Map;

public class RegistrationValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public RegistrationValidationException(Map<String, String> errors) {
        super("Verifique os dados informados");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
