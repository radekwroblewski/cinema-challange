package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TimeFormatter.toDuration
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidDurationValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidDuration(
    val message: String = "Invalid duration",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidDurationValidator : ConstraintValidator<ValidDuration, String> {
    override fun isValid(duration: String?, context: ConstraintValidatorContext?): Boolean =
        duration?.ifBlank { null }?.let {
            try { // room to improve - validate by regex
                it.toDuration()
                true
            } catch (ex: Exception) {
                false
            }
        } ?: true //nulls and blanks will be handled by other validator

}


