package com.xiaoyuz.drivingreporter.extensions

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import java.util.*

/**
 * Main event dispatcher for distributing/subscribing to events
 * sent and received on the main thread. It will only allow
 * posting events from the main thread and this is the expected
 * behavior and will be enough for almost all cases. In the odd
 * situation where events need to be posted from a background
 * thread the ThreadEventDispatcher can be used however all UI
 * updating must be done on the main thread so all such subscribers
 * MUST be registered to this event dispatcher.
 */
class EventDispatcher {

    /**
     * Interface to implement by any object being posted to the event bus
     * in case it wants to be notified after the object has been successfully
     * posted to all its event handlers.
     */
    interface DisposableEvent {
        /**
         * Called when the event has been completely handled by all its
         * event handlers.
         * Implementors MUST NOT post any new events to the bus from this method.
         */
        fun dispose(): Unit
    }

    enum class Group {
        Main
    }

    private var mEventBus: Bus = Bus(ThreadEnforcer.MAIN, "main-bus")
    private val mGroups = EnumMap<Group, MutableList<Any>>(Group::class.java)
    private val mDisposables = LinkedList<DisposableEvent>()
    private var mPostingDepth: Int = 0

    companion object {
        private val TAG: String = "EventDispatcher"
        private val instance: EventDispatcher by lazy { EventDispatcher() }

        /**
         * Posts an event to all registered handlers. Any exceptions thrown by
         * handlers will be rethrown by the event dispatcher. This is to ensure
         * that exceptions doesn't get hidden.
         *
         * Note: When posting an event to the bus that event will be distributed
         * to all its subscribers before the end of the run loop however if you
         * are posting an event (B) as a response to another event (A) happening
         * your new event will only be queued before your call to post() returns.
         * This means the event (B) may not yet have been delivered on the return
         * of your call to post(). The initial call (post(A)) will not return until
         * all queued events (B in this case) have also been delivered. It works
         * this way to ensure same order of deliveries of events to all subscribers.
         *
         * <p>
         * If no handlers have been subscribed for {@code event}'s class, and
         * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
         * DeadEvent and reposted.
         *
         * @param event event to post.
         */
        fun post(event: Any) {
            instance.handlePost(event)
        }

        /**
         * Registers all handler methods on {@code object} to receive events.
         * Handler methods are selected and classified using this EventBus's
         * {@link HandlerFindingStrategy}; the default strategy is the
         * {@link AnnotatedHandlerFinder}.
         *
         * @param object object whose handler methods should be registered.
         */
        fun register(objecte: Any) {
            instance.mEventBus.register(objecte)
        }

        /**
         * Unregisters all handler methods on a registered {@code object}.
         *
         * @param object object whose handler methods should be unregistered.
         */
        fun unregister(objecte: Any) {
            instance.mEventBus.unregister(objecte)
            instance.mGroups.values.forEach { it.remove(objecte) }
        }

        /**
         * Registers all handler methods on `object` to receive events.
         * Handler methods are selected and classified using this EventBus's
         * [HandlerFindingStrategy]; the default strategy is the
         * [AnnotatedHandlerFinder]. The `object` will be associated
         * with the `group` and will be unregistered when the Group
         * is unregistered.

         * @param object object whose handler methods should be registered.
         * *
         * @param group Group that the object belongs to.
         */
        fun register(objecte: Any, group: Group) {
            val eventHandlers: MutableList<Any>?

            if (instance.mGroups.containsKey(group)) {
                eventHandlers = instance.mGroups[group]
            } else {
                eventHandlers = LinkedList<Any>()
                instance.mGroups.put(group, eventHandlers)
            }

            // Register to event bus first since it may throw exception and we
            // don't want to keep a reference in that case
            register(objecte)

            eventHandlers?.add(objecte)
        }

        /**
         * Unregisters all event handlers from the given `group`.

         * @param group Group whose event handlers should be unregistered.
         */
        fun unregister(group: Group) {
            val eventHandlers = instance.mGroups[group]
            if (eventHandlers != null) {
                eventHandlers.forEach { instance.mEventBus.unregister(it) }
                instance.mGroups.remove(group)
            }
        }
    }

    private fun handlePost(event: Any) {
        mPostingDepth++
        if (event is DisposableEvent) {
            mDisposables.add(event)
        }
        mEventBus.post(event)
        if (--mPostingDepth == 0) {
            mDisposables.forEach { it.dispose() }
            mDisposables.clear()
        }
    }

}