package com.rdk.cinemachallenge.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidUUIDValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidUUID(
    val message: String = "Invalid UUID",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidUUIDValidator : ConstraintValidator<ValidUUID, String> {

    companion object {
        //Pattern copied from UUID class
        val UUID_PATTERN: Pattern =
            Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
    }

    override fun isValid(uuid: String?, context: ConstraintValidatorContext?): Boolean =
        uuid?.let {
            UUID_PATTERN.matcher(it).matches()
        } ?: true //nulls will be handled by other validator

}

