package com.mehrbod.anothermicroservice.eventconsumer

import com.mehrbod.notification.model.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object EventReceiver {
    private val _eventStream = MutableSharedFlow<Event>()
    val eventStream: SharedFlow<Event> = _eventStream

    suspend fun eventReceived(event: Event) {
        _eventStream.emit(event)
    }
}
