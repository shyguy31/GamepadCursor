package com.example.gamepadcursor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat

class GamepadCursorService : AccessibilityService() {
    private val TAG = "GamepadCursorService"
    private var windowManager: WindowManager? = null
    private var cursorView: ImageView? = null
    private var cursorX = 0f
    private var cursorY = 0f
    private val cursorSpeed = 15f
    private val screenWidth = 1080
    private val screenHeight = 2400

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
        initializeCursorOverlay()
    }

    private fun initializeCursorOverlay() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            cursorView = ImageView(this).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@GamepadCursorService,
                        R.drawable.overlay_cursor
                    )
                )
            }

            val params = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = android.graphics.PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                width = 80
                height = 80
                x = screenWidth / 2
                y = screenHeight / 2
            }

            cursorX = params.x.toFloat()
            cursorY = params.y.toFloat()

            windowManager?.addView(cursorView, params)
            Log.d(TAG, "Cursor overlay initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing cursor overlay", e)
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> handleKeyDown(event)
            KeyEvent.ACTION_UP -> false
            else -> super.onKeyEvent(event)
        }
    }

    private fun handleKeyDown(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            // D-Pad controls
            KeyEvent.KEYCODE_DPAD_UP -> {
                moveCursor(0f, -cursorSpeed)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveCursor(0f, cursorSpeed)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveCursor(-cursorSpeed, 0f)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveCursor(cursorSpeed, 0f)
                true
            }
            // Joystick analog controls
            KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                // Note: For full analog support, you'd need MotionEvent handling
                // This is a basic implementation for digital D-Pad
                true
            }
            // Click actions
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_DPAD_CENTER -> {
                performClickAtCursor()
                true
            }
            else -> super.onKeyEvent(event)
        }
    }

    private fun moveCursor(deltaX: Float, deltaY: Float) {
        cursorX = (cursorX + deltaX).coerceIn(0f, screenWidth.toFloat() - 80)
        cursorY = (cursorY + deltaY).coerceIn(0f, screenHeight.toFloat() - 80)

        val params = cursorView?.layoutParams as? WindowManager.LayoutParams
        params?.apply {
            x = cursorX.toInt()
            y = cursorY.toInt()
            windowManager?.updateViewLayout(cursorView, this)
        }
    }

    private fun performClickAtCursor() {
        try {
            val path = Path().apply {
                moveTo(cursorX + 40, cursorY + 40)
            }

            val gestureDescription = GestureDescription.Builder().apply {
                addStroke(
                    GestureDescription.StrokeDescription(
                        path,
                        0,
                        50
                    )
                )
            }.build()

            dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Gesture completed at ($cursorX, $cursorY)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Gesture cancelled")
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click", e)
        }
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // Not used for this service
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (cursorView != null && windowManager != null) {
                windowManager?.removeView(cursorView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing cursor view", e)
        }
        Log.d(TAG, "Service destroyed")
    }
}
