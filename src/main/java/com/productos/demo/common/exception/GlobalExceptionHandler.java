package com.productos.demo.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación (anotaciones de Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ErrorDetail> details = new ArrayList<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            
            details.add(new ErrorDetail(
                fieldName,
                "VALIDATION_ERROR",
                message
            ));
        });

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            generateTraceId(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "ERROR_VALIDACION",
            "Datos de entrada inválidos",
            request.getRequestURI(),
            details
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones cuando un producto no es encontrado
     */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleProductoNoEncontrado(
        ProductoNoEncontradoException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            generateTraceId(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getCodigoError(),
            ex.getMessage(),
            request.getRequestURI(),
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando el código EAN está duplicado
     */
    @ExceptionHandler(CodigoEanDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleCodigoEanDuplicado(
        CodigoEanDuplicadoException ex,
        HttpServletRequest request
    ) {
        List<ErrorDetail> details = List.of(
            new ErrorDetail(
                "codigo_ean",
                "DUPLICATE_KEY",
                ex.getMessage()
            )
        );

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            generateTraceId(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getCodigoError(),
            ex.getMessage(),
            request.getRequestURI(),
            details
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no previstas
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            generateTraceId(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "ERROR_INTERNO",
            "Ocurrió un error inesperado. Por favor, intenta más tarde.",
            request.getRequestURI(),
            null
        );

        // Log en consola para debugging (en producción usar logger real)
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Genera un trace ID único para cada request
     */
    private String generateTraceId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 13);
    }
}
