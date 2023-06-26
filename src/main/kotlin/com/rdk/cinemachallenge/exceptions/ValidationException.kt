package com.rdk.cinemachallenge.exceptions

class ValidationException(val errors: List<String>) : RuntimeException()
