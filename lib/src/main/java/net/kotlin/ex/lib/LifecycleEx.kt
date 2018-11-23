package net.kotlin.ex.lib

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent

/**
 * 生命周期相关的扩展
 * Created by zhongyongsheng on 2018/11/20.
 */

/**
 * 可取消的接口
 */
interface Cancelable {

    /**
     * 取消时执行
     */
    fun cancel()
}

/**
 * 可取消的接口使用的生命周期绑定
 */
class CancelableLifecycle {
    var mLastEvent: Lifecycle.Event = Lifecycle.Event.ON_ANY
    var mTargetEvent: Lifecycle.Event = Lifecycle.Event.ON_ANY

    fun observe(lifecycleOwner: LifecycleOwner, cancelable: Cancelable) {
        observe(lifecycleOwner, cancelable, null)
    }

    fun observe(lifecycleOwner: LifecycleOwner, cancelable: Cancelable, cancelWhenEvent:Lifecycle.Event?) {
        if (cancelWhenEvent != null) {
            mTargetEvent = cancelWhenEvent
        }
        lifecycleOwner.lifecycle.addObserver(object: LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
                if (cancelWhenEvent == null && mLastEvent == Lifecycle.Event.ON_ANY) {
                    mLastEvent = event
                    //如果没有定义取消事件，则根据当前执行到的开始事件对应的结束来定义
                    when (mLastEvent) {
                        Lifecycle.Event.ON_CREATE -> mTargetEvent = Lifecycle.Event.ON_DESTROY
                        Lifecycle.Event.ON_START -> mTargetEvent = Lifecycle.Event.ON_STOP
                        Lifecycle.Event.ON_RESUME -> mTargetEvent = Lifecycle.Event.ON_PAUSE
                    }
                }

                if (event == mTargetEvent) {
                    //取消任务
                    cancelable.cancel()
                }

                if (owner.lifecycle.currentState == Lifecycle.Event.ON_DESTROY) {
                    //destroy时要取消监听
                    owner.lifecycle.removeObserver(this)
                    if (mTargetEvent == Lifecycle.Event.ON_ANY) {
                        //如果mTargetEvent还是默认值，代表没有被取消过，此时要最后取消一次
                        cancelable.cancel()
                    }
                }
            }
        })
    }
}

/**
 * 给Cancelable绑定生命周期，生命周期结束时，则调用cancel()方法
 * 例如onCreate的时候绑定，则onDestroy cancel；onStart时绑定，则onStop cancel
 *
 * @param lifecycleOwner 生命周期owner
 * @param cancelWhenEvent 当此生命周期出现时，才cancel
 * @param block block返回Cancelable用于取消
 */
fun bindCancelableBlockWithLifecycle(lifecycleOwner: LifecycleOwner?, cancelWhenEvent:Lifecycle.Event? = null, block: () -> Cancelable) {
    lifecycleOwner?.let {
        CancelableLifecycle().observe(it, block(), cancelWhenEvent)
    }
}

/**
 * 给Cancelable绑定生命周期，生命周期结束时，则调用cancel()方法
 * 例如onCreate的时候绑定，则onDestroy cancel；onStart时绑定，则onStop cancel
 *
 * @param lifecycleOwner 生命周期owner
 * @param cancelWhenEvent 当此生命周期出现时，才cancel
 * @param cancelable Cancelable用于取消
 */
fun bindCancelableWithLifecycle(lifecycleOwner: LifecycleOwner?, cancelWhenEvent:Lifecycle.Event? = null, cancelable: Cancelable) {
    lifecycleOwner?.let {
        CancelableLifecycle().observe(it, cancelable, cancelWhenEvent)
    }
}