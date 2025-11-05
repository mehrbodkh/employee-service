package com.mehrbod.anothermicroservice.notification

data class Notification(
    val to: String,
    val message: String,
)