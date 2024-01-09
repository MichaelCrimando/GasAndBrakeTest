package com.scamofty.gasandbraketest

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.input.InputManager
import android.os.SystemClock
import android.os.SystemClock.uptimeMillis
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Method

class GameControllerManager(context: Context) {
    private val TAG = "GameControllerManager"
    private var isRunning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val mInputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager

    @SuppressLint("DiscouragedPrivateApi")
    val injectInputEvent = InputManager::class.java.getDeclaredMethod(
        "injectInputEvent",
        InputEvent::class.java,
        Int::class.javaPrimitiveType
    )
//    @SuppressLint("DiscouragedPrivateApi", "BlockedPrivateApi")
//    val addUniqueIdAssociationByDescriptor = InputManager::class.java.getDeclaredMethod(
//        "addUniqueIdAssociation",
//        String::class.java,
//        String::class.java
//    )
//    @SuppressLint("DiscouragedPrivateApi", "BlockedPrivateApi")
//    val removeUniqueIdAssociationByDescriptor = InputManager::class.java.getDeclaredMethod(
//        "removeUniqueIdAssociation",
//        String::class.java
//    )



    @SuppressLint("BlockedPrivateApi")
    fun start() : String{
        try {
            val addUniqueIdAssociationByDescriptor: Method = InputManager::class.java.getDeclaredMethod(
                "addUniqueIdAssociationByDescriptor",
                String::class.java,
                String::class.java
            )
            addUniqueIdAssociationByDescriptor.isAccessible = true
            addUniqueIdAssociationByDescriptor.invoke(mInputManager, "a718a782d34bc767f4689c232d64d527998ea7fd", "0")
            Log.d(TAG, "Device associated")
        } catch (e: Exception) {
            Log.d(TAG, "Device association failed")
        }

        isRunning=true
        coroutineScope.launch {
            while(isRunning){
                sendButtonDownThenUp(KeyEvent.KEYCODE_BUTTON_B, 305)
                delay(1000L)
            }
        }
        return "Started"
    }
    fun stop() : String{
        try {
            val removeUniqueIdAssociationByDescriptor: Method = InputManager::class.java.getDeclaredMethod(
                "removeUniqueIdAssociationByDescriptor",
                String::class.java
            )
            removeUniqueIdAssociationByDescriptor.isAccessible = true
            removeUniqueIdAssociationByDescriptor.invoke(mInputManager, "a718a782d34bc767f4689c232d64d527998ea7fd")
            Log.d(TAG, "Device association removed")
        } catch (e: Exception) {
            Log.d(TAG, "Device association removal failed")
        }
        isRunning = false
        return "Stopped"
    }

    //Scancode cheatsheet, A =  304, B = 305, X = 307, Y = 308, L = 310, R = 311, LT = 312, RT = 313, Share = N/A,
    // MENU = 315, LCLICK = 317, RCLICK = 318, Xbox Button = 316
    fun sendButtonDownThenUp(keyEventCode: Int, scancode: Int){
        Log.d(TAG, "sendButtonDownThenUp: $keyEventCode")
        var eventTime = uptimeMillis()

        val downEvent = mInputManager.getInputDevice(-1)?.let {inputDevice ->
            KeyEvent(
                /* downTime= */ eventTime,
                /* eventTime= */ eventTime,
                /* action= */ KeyEvent.ACTION_DOWN,
                /* code= */ keyEventCode,
                /* repeat= */ 0,
                /* metaState= */ 0,
                /* deviceId= */ inputDevice.id, //Virtual is always -1
                /* scancode= */ scancode,
                /* flags= */ KeyEvent.FLAG_FROM_SYSTEM, //Consider FLAG_FROM_SYSTEM or
                // FLAG_SOFT_KEYBOARD
                /* source= */ InputDevice.SOURCE_GAMEPAD
            )
        }
        injectInputEvent.invoke(mInputManager, downEvent, 0)
        eventTime = uptimeMillis()
        val upEvent = mInputManager.getInputDevice(-1)?.let {inputDevice ->
            KeyEvent(
                /* downTime= */ eventTime,
                /* eventTime= */ eventTime,
                /* action= */ KeyEvent.ACTION_UP,
                /* code= */ keyEventCode,
                /* repeat= */ 0,
                /* metaState= */ 0,
                /* deviceId= */ inputDevice.id,
                /* scancode= */ scancode,
                /* flags= */ KeyEvent.FLAG_FROM_SYSTEM, //Consider FLAG_FROM_SYSTEM or
                // FLAG_SOFT_KEYBOARD.
                /* source= */ InputDevice.SOURCE_GAMEPAD
            )
        }
        injectInputEvent.invoke(mInputManager, upEvent, 0)
    }

    fun sendGasAxisEvent(gas: Float){
        Log.d(TAG, "sendGasAxisEvent: $gas")
        val eventTime = SystemClock.uptimeMillis()

        //On actual car will for sure need some sort of translation from gas pedal to gas axis
        val event = MotionEvent.obtain(
            eventTime,
            eventTime,
            MotionEvent.AXIS_GAS,
            0f,
            gas.toFloat(),
            0
        )
        event.source = InputDevice.SOURCE_GAMEPAD
        injectInputEvent.invoke(mInputManager, event, 0)

        event.recycle()

    }

    fun sendBrakeAxisEvent(brake: Float){
        Log.d(TAG, "sendBrakeAxisEvent: $brake")
        val eventTime = SystemClock.uptimeMillis()

        //On actual car will for sure need some sort of translation from brake pedal to brake axis
        val event = MotionEvent.obtain(
            eventTime,
            eventTime,
            MotionEvent.AXIS_BRAKE,
            0f,
            brake.toFloat(),
            0
        )
        event.source = InputDevice.SOURCE_GAMEPAD
        injectInputEvent.invoke(mInputManager, event, 0)

        event.recycle()

    }
}