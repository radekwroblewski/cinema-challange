package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TimeFormatter
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidDateValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidDate(
    val message: String = "Invalid date",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidDateValidator : ConstraintValidator<ValidDate, String> {

    override fun isValid(date: String?, context: ConstraintValidatorContext?): Boolean =
        date?.let {
            try { // room to improve - validate by regex
                TimeFormatter.DATE_FORMATTER.parse(it)
                true
            } catch (ex: Exception) {
                false
            }
        } ?: true //nulls will be handled by other validator

}


