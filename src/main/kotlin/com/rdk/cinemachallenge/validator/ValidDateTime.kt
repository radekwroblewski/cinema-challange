package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TimeFormatter
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidDateTimeValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidDateTime(
    val message: String = "Invalid date time",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidDateTimeValidator : ConstraintValidator<ValidDateTime, String> {
    override fun isValid(dateTime: String?, context: ConstraintValidatorContext?): Boolean =
        dateTime?.let {
            try { // room to improve - validate by regex
                TimeFormatter.DATE_TIME_FORMATTER.parse(it)
                true
            } catch (ex: Exception) {
                false
            }
        } ?: true //nulls will be handled by other validator

}


