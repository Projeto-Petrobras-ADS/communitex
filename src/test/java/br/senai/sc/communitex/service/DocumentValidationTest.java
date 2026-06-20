package br.senai.sc.communitex.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentValidationTest {

    @Test
    void validatesCpfCheckDigits() {
        assertTrue(RegistrationService.isValidCpf("52998224725"));
        assertFalse(RegistrationService.isValidCpf("12345678901"));
        assertFalse(RegistrationService.isValidCpf("11111111111"));
    }

    @Test
    void validatesCnpjCheckDigits() {
        assertTrue(RegistrationService.isValidCnpj("11222333000181"));
        assertFalse(RegistrationService.isValidCnpj("12345678000199"));
        assertFalse(RegistrationService.isValidCnpj("11111111111111"));
    }
}
