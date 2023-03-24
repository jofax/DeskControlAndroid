package com.smartpods.android.pulseecho.Utilities
import java.lang.ref.WeakReference

class MainTabEventHandler {
    var onSelectTabScreen: ((Int) -> Unit)? = null
}

object SPAppEventHandler {
    private var listeners: MutableSet<WeakReference<MainTabEventHandler>> = mutableSetOf()

    fun registerListener(listener: MainTabEventHandler) {
        if (listeners.map { it.get() }.contains(listener)) { return }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
    }

    fun unregisterListener(listener: MainTabEventHandler) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<MainTabEventHandler>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
        }
    }

    fun showTabSelected(tabIdx: Int) {
        listeners.forEach { it.get()?.onSelectTabScreen?.invoke(tabIdx) }
    }
}

open class SPEventHandler<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}