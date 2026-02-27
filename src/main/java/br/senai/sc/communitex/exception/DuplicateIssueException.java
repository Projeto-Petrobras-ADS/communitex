package br.senai.sc.communitex.exception;

public class DuplicateIssueException extends RuntimeException {

    public DuplicateIssueException(String message) {
        super(message);
    }

    public DuplicateIssueException(String message, Throwable cause) {
        super(message, cause);
    }
}

