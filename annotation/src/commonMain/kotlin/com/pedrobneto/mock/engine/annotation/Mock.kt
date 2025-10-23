package com.pedrobneto.mock.engine.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Mock(
    val files: Array<String>,
    val allowCustomJson: Boolean = false,
)
