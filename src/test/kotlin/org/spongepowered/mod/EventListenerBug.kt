package org.spongepowered.mod

import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.entity.SpawnEntityEvent
import org.spongepowered.mctester.internal.event.StandaloneEventListener
import org.spongepowered.mctester.junit.TestUtils

class EventListenerBug {


    fun foo() {
        val listener: EventListener<SpawnEntityEvent>? = null

        FakeEventUser(SpawnEntityEvent::class.java, listener)
        StandaloneEventListener(SpawnEntityEvent::class.java, listener)
    }
}
