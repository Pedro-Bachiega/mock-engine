package com.pedrobneto.mock.engine.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Mock(
    val files: String = "",
)
