package br.senai.sc.communitex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessExpection extends RuntimeException {
    public BusinessExpection(String message) {
        super(message);
    }
}
