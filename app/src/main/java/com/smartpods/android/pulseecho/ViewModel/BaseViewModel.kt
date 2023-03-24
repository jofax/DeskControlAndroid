package com.smartpods.android.pulseecho.ViewModel

import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry
import androidx.annotation.NonNull
import androidx.lifecycle.*
import com.dariopellegrini.spike.SpikeProvider
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.PulseCommands
import com.smartpods.android.pulseecho.Utilities.SPEventHandler
import io.realm.Realm
import java.util.logging.Handler


open class BaseViewModel : ViewModel(), Observable {
    @Transient

    private var mCallbacks: PropertyChangeRegistry = PropertyChangeRegistry()
    //val empty = MutableLiveData<Boolean>().apply { value = false }
    //val dataLoading = MutableLiveData<Boolean>().apply { value = false }
    //var toastMessage = MutableLiveData<String>()
    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    val commandAdapter = PulseCommands()

    val toastMessage = MutableLiveData<SPEventHandler<String>>()
    val navigateToDetails : LiveData<SPEventHandler<String>>
        get() = toastMessage

    init {

    }

    override fun addOnPropertyChangedCallback(@NonNull callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                mCallbacks = PropertyChangeRegistry()
            }
        }
        mCallbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(@NonNull callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks.remove(callback)
    }

    public fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun showToastMessage(message: String) {
        toastMessage.value = SPEventHandler(message)
    }

}