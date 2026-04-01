package com.example.smartcontrol.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

interface GestureListener {
    fun onHandWaveOverScreen()
    fun onRotateLeft()
    fun onRotateRight()
    fun onTilt(volumeLevel: Int)
    fun onFaceDown()
    fun onShake()
}
class GestureSensorManager(context: Context, private val listener: GestureListener) : SensorEventListener {

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Khai báo 3 loại cảm biến
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // Các biến phục vụ thuật toán
    private val ROTATE_THRESHOLD = 4.0f
    private var lastRotateTime: Long = 0
    private var lastProximityTime: Long = 0
    private val COOLDOWN_TIME_MS = 1000L
    private var isFaceDownActive = false

    fun startListening() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val currentTime = System.currentTimeMillis()

        when (event.sensor.type) {
            // 1. CẢM BIẾN TIỆM CẬN (Vẫy tay qua mặt trước điện thoại)
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                // Nếu khoảng cách < 3cm (Nghĩa là có bàn tay che ngang qua camera trước)
                if (distance < 3.0f) {
                    if (currentTime - lastProximityTime > COOLDOWN_TIME_MS) {
                        listener.onHandWaveOverScreen()
                        lastProximityTime = currentTime
                    }
                }
            }

            // 2. GIA TỐC KẾ (Tính góc nghiêng cho âm lượng)
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                var tiltNormalized = (x + 9.8f) / (9.8f * 2)
                if (tiltNormalized < 0f) tiltNormalized = 0f
                if (tiltNormalized > 1f) tiltNormalized = 1f
                val volumeLevel = (tiltNormalized * 100).toInt()
                listener.onTilt(volumeLevel)

                if (z > 0f) {
                    isFaceDownActive = false
                }

                if (z < -8.5f && !isFaceDownActive) {
                    listener.onFaceDown()
                    isFaceDownActive = true
                }

                val gX = x / 9.8f
                val gY = y / 9.8f
                val gZ = z / 9.8f
                val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                // Nếu lực lắc mạnh hơn 2.0 G (Gấp đôi trọng lực bình thường)
                if (gForce > 2.0f) {
                    // Dùng chung thời gian chờ 1 giây để tránh nhận diện liên tục
                    if (currentTime - lastProximityTime > COOLDOWN_TIME_MS) {
                        listener.onShake()
                        lastProximityTime = currentTime
                    }
                }
            }

            // 3. CON QUAY HỒI CHUYỂN (Xoay máy)
            Sensor.TYPE_GYROSCOPE -> {
                val y = event.values[1]
                if (abs(y) > ROTATE_THRESHOLD) {
                    if (currentTime - lastRotateTime > COOLDOWN_TIME_MS) {
                        if (y > 0) listener.onRotateLeft() else listener.onRotateRight()
                        lastRotateTime = currentTime
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }




}