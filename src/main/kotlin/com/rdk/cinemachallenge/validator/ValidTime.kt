package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TimeFormatter
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.apache.commons.validator.GenericValidator
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidTimeValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidTime(
    val message: String = "Invalid time",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidTimeValidator : ConstraintValidator<ValidTime, String> {

    override fun isValid(time: String?, context: ConstraintValidatorContext?): Boolean =
        time?.let {
            GenericValidator.isDate(it, TimeFormatter.TIME_FORMAT, true)
        } ?: true //nulls will be handled by other validator
}
