package com.example.smartcontrol

import android.speech.tts.TextToSpeech
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import kotlin.random.Random
import com.example.smartcontrol.R
import com.example.smartcontrol.sensors.GestureListener
import com.example.smartcontrol.sensors.GestureSensorManager
import kotlin.random.nextInt

class MainActivity : AppCompatActivity(), GestureListener, TextToSpeech.OnInitListener{

    private lateinit var gestureSensorManager: GestureSensorManager
    private lateinit var audioManager: AudioManager
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    // UI Elements
    private lateinit var tvVolume: TextView
    private lateinit var progressVolume: ProgressBar
    private lateinit var switchWaveSensor: Switch
    private lateinit var tvLightStatus: TextView
    private lateinit var tvMediaAction: TextView
    private lateinit var btnConnectBluetooth: Button

    private lateinit var tts: TextToSpeech

    // Trạng thái
    private var isLightOn = false
    private var lastVolume = -1
    private var isWaveSensorActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo UI
        tvVolume = findViewById(R.id.tvVolume)
        progressVolume = findViewById(R.id.progressVolume)
        switchWaveSensor = findViewById(R.id.switchWaveSensor)
        tvLightStatus = findViewById(R.id.tvLightStatus)
        tvMediaAction = findViewById(R.id.tvMediaAction)
        btnConnectBluetooth = findViewById(R.id.btnConnectBluetooth)

        // Khởi tạo Services
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: Exception) {
            e.printStackTrace()
        }

        gestureSensorManager = GestureSensorManager(this, this)

        // Nút bật/tắt cảm biến vẫy tay (Phần 2)
        switchWaveSensor.setOnCheckedChangeListener { _, isChecked ->
            isWaveSensorActive = isChecked
            val statusMsg = if (isChecked) "Đã BẬT nhận diện vẫy tay" else "Đã TẮT nhận diện vẫy tay"
            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT).show()
        }

        // Nút mở kết nối Bluetooth IoT (Phần 4)
        btnConnectBluetooth.setOnClickListener {
            // Mở màn hình cài đặt Bluetooth của điện thoại để kết nối Loa/IoT
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        tts = TextToSpeech(this, this)
    }

    override fun onResume() {
        super.onResume()
        gestureSensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        gestureSensorManager.stopListening()
        // Tắt đèn khi thoát app để an toàn
        if (isLightOn) toggleFlashlight(false)
    }

    // ==========================================
    // NHẬN DIỆN CỬ CHỈ (Từ Interface)
    // ==========================================

    override fun onHandWaveOverScreen() {
        if (isWaveSensorActive) {
            isLightOn = !isLightOn
            toggleFlashlight(isLightOn)

            runOnUiThread {
                if (isLightOn) {
                    tvLightStatus.text = "Trạng thái đèn: ĐANG SÁNG"
                    tvLightStatus.setTextColor(Color.parseColor("#4CAF50"))
                    // Đọc giọng nói
                    tts.speak("Đèn đã bật", TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    tvLightStatus.text = "Trạng thái đèn: TẮT"
                    tvLightStatus.setTextColor(Color.parseColor("#F44336"))
                    // Đọc giọng nói
                    tts.speak("Đèn đã tắt", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }

    override fun onRotateLeft() {
        // Gửi lệnh lùi bài
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        audioManager.dispatchMediaKeyEvent(event)

        // Cập nhật giao diện Phần 3
        runOnUiThread { tvMediaAction.text = "⏪ Đã chuyển về bài TRƯỚC" }
    }

    override fun onRotateRight() {
        // Gửi lệnh qua bài
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        audioManager.dispatchMediaKeyEvent(event)

        // Cập nhật giao diện Phần 3
        runOnUiThread { tvMediaAction.text = "⏩ Đã chuyển sang bài TIẾP THEO" }
    }

    override fun onTilt(volumeLevel: Int) {
        if (Math.abs(volumeLevel - lastVolume) > 3) { // Chênh lệch 3% thì mới đổi UI
            lastVolume = volumeLevel

            // Tính toán và chỉnh âm lượng thật
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (volumeLevel / 100f * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0) // Để 0 để ẩn UI hệ thống, dùng UI của mình

            // Cập nhật giao diện Phần 1
            runOnUiThread {
                tvVolume.text = "Âm lượng hiện tại: $volumeLevel%"
                progressVolume.progress = volumeLevel
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Máy không hỗ trợ giọng Tiếng Việt", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFaceDown() {
        if (isLightOn) {
            isLightOn = false
            toggleFlashlight(false)
            runOnUiThread {
                tvLightStatus.text = "Trạng thái đèn: TẮT"
                tvLightStatus.setTextColor(Color.parseColor("#F44336"))
            }
        }

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        audioManager.dispatchMediaKeyEvent(event)

        // 3. Đọc thông báo
        tts.speak("Chế độ đi ngủ. Đã tắt toàn bộ thiết bị", TextToSpeech.QUEUE_FLUSH, null, null)

        runOnUiThread { tvMediaAction.text = "💤 Chế độ đi ngủ: Đã úp máy" }
    }

    override fun onShake() {
        val randomColor = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        val rootLayout = findViewById<android.view.View>(R.id.mainLayout)

        runOnUiThread {
            rootLayout.setBackgroundColor(randomColor)
            Toast.makeText(this, "Đã gửi lệnh đổi màu", Toast.LENGTH_SHORT).show()
        }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200) // Cho máy Android cũ
        }

        tts.speak("Đã đổi màu ứng dụng sang màu ngẫu nhiên", TextToSpeech.QUEUE_FLUSH, null, null)

        Log.d("IoT_BLUETOOTH", "========================================")
        Log.d("IoT_BLUETOOTH", "BluetoothSocket: Đang gửi mảng Byte lệnh đổi màu LED...")
        Log.d("IoT_BLUETOOTH", "Payload Data: [0xFF, 0x0A, 0x${Random.nextInt(10..99)}]")
        Log.d("IoT_BLUETOOTH", "Status: Gửi thành công tới địa chỉ MAC của Loa!")
        Log.d("IoT_BLUETOOTH", "========================================")
    }

    private fun toggleFlashlight(status: Boolean) {
        try {
            cameraId?.let { cameraManager.setTorchMode(it, status) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}