package com.notabene.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.notabene.service.TagService;

@RestControllerAdvice
public class ApiExceptionHandler {

    private Map<String, Object> body(String message, int status) {
        Map<String, Object> m = new HashMap<>();
        m.put("message", message);
        m.put("status", status);
        return m;
    }

    @ExceptionHandler(TagService.DuplicateTagException.class)
    public ResponseEntity<Map<String, Object>> duplicate(TagService.DuplicateTagException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(e.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body("Violazione di vincoli dati (es. duplicato).", HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("Errore inatteso: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
