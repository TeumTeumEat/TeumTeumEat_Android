package com.teumteumeat.teumteumeat.ui.screen.a4_main

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppResumeNotifier @Inject constructor() {

    private val _resumeEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val resumeEvent = _resumeEvent.asSharedFlow()

    fun notifyResume() {
        _resumeEvent.tryEmit(Unit)
    }
}
