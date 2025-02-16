package com.pedrobneto.mock.engine.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Mock(
    val files: String = "",
    val ignoreAdditionalList: Boolean = false,
)
