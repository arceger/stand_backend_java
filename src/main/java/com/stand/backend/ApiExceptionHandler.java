package com.stand.backend;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(Instant.now(), exception.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), "Dados invalidos enviados para a API."));
    }
}

@io.swagger.v3.oas.annotations.media.Schema(description = "Estrutura padrão de resposta de erro")
record ErrorResponse(
    @io.swagger.v3.oas.annotations.media.Schema(description = "Data e hora em que o erro ocorreu (UTC)", example = "2026-08-26T20:55:00Z")
    Instant timestamp,
    @io.swagger.v3.oas.annotations.media.Schema(description = "Mensagem explicativa do erro", example = "Veículo não encontrado.")
    String message
) {
}
