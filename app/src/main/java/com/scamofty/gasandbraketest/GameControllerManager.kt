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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import kotlin.math.absoluteValue

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

    fun init(){
    }

    @SuppressLint("BlockedPrivateApi")
    fun start() : String{
        val virtualInputDevice = mInputManager.getInputDevice(-1)
        try {
            val addUniqueIdAssociation: Method = InputManager::class.java.getDeclaredMethod(
                "addUniqueIdAssociation",
                String::class.java,
                String::class.java
            )
            addUniqueIdAssociation.isAccessible = true
            addUniqueIdAssociation.invoke(mInputManager, virtualInputDevice?.name, "0")
            Log.d(TAG, "Device associated")
        } catch (e: Exception) {
            Log.d(TAG, "Device association failed")
        }

        isRunning=true
        var ii = 0.0f
        var incrementing = true
        coroutineScope.launch {
            while(isRunning){
                //sendButtonDownThenUp(KeyEvent.KEYCODE_BUTTON_B, 305)
                sendGasAxisEvent(ii.absoluteValue)
                sendBrakeAxisEvent(ii.absoluteValue)
                sendXAxisEvent(ii)
                if(ii >= 1.0f){
                    incrementing = false
                } else if(ii <= -1.0f){
                    incrementing = true
                }
                if(incrementing){
                    ii += 0.1f
                } else {
                    ii -= 0.1f
                }
                delay(1000L)
            }
        }
        return "Started"
    }
    @SuppressLint("BlockedPrivateApi")
    fun stop() : String{
        val virtualInputDevice = mInputManager.getInputDevice(-1)
        try {
            val removeUniqueIdAssociation: Method = InputManager::class.java.getDeclaredMethod(
                "removeUniqueIdAssociation",
                String::class.java
            )
            removeUniqueIdAssociation.isAccessible = true
            removeUniqueIdAssociation.invoke(mInputManager, virtualInputDevice?.name)
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

    fun sendXAxisEvent(angle: Float){
        Log.d(TAG, "sendXAxisEvent: $angle")
        val eventTime = SystemClock.uptimeMillis()

        //On actual car will for sure need some sort of translation from steering wheel angle to steering wheel axis
        val event = mInputManager.getInputDevice(-1)?.let {
            MotionEvent.obtain(
                /* downTime= */ eventTime,
                /* eventTime= */ eventTime,
                /* action= */ MotionEvent.ACTION_MOVE,
                /* x= */ angle,
                /* y= */ 0.0f,
                /* pressure= */ 1.0f,
                /* size= */ 0f,
                /* metaState= */ 0,
                /* xPrecision= */ 0.1f,
                /* yPrecision= */ 0.1f,
                /* deviceId= */ it.id,
                /* edgeFlags= */ 0
            )
        }
        if (event != null) {
            event.source = InputDevice.SOURCE_JOYSTICK
        }
        injectInputEvent.invoke(mInputManager, event, 0)

        event?.recycle()

    }
}