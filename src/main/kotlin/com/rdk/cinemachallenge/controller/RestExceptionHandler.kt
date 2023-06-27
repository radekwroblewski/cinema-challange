package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.exceptions.ValidationException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [ConstraintViolationException::class, ConcurrentModificationException::class])
    protected fun handleConstraintViolation(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? =
        handleException(ex, ex.message.orEmpty(), HttpStatus.BAD_REQUEST, request)

    @ExceptionHandler(value = [ValidationException::class])
    protected fun handleValidationException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? =
        handleException(
            ex,
            (ex as? ValidationException)?.errors?.joinToString() ?: ex.message.orEmpty(),
            HttpStatus.BAD_REQUEST,
            request
        )

    @ExceptionHandler(value = [NoSuchElementException::class])
    protected fun handleNoSuchElement(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? =
        handleException(
            ex,
            ex.message.orEmpty(),
            HttpStatus.NOT_FOUND,
            request
        )

    protected override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any?>? =
        ResponseEntity(
            ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage },
            HttpStatus.BAD_REQUEST
        )

    protected override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any?>? {
        return handleExceptionInternal(
            ex,
            "Unable to read request",
            headers,
            status,
            request
        ) //simple error message not to expose we're using Java
    }

    private fun handleException(ex: RuntimeException, body: String, status: HttpStatus, request: WebRequest) =
        handleExceptionInternal(ex, body, HttpHeaders(), status, request)

}