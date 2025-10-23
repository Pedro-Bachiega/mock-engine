package com.pedrobneto.mock.engine.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Mock(
    vararg val files: String,
    val allowCustomJson: Boolean = false
)
