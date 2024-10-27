package com.knov.myapplication2

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {


    //variables y demás

        //conectividad y permisos
    private val PERMISSION_REQUEST_BLUETOOTH = 2
    private val PERMISSION_REQUEST_LOCATION = 3
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val TAG = "MainActivity"

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSpinner: Spinner
    private lateinit var connectButton: Button
    private lateinit var devicesArrayAdapter: ArrayAdapter<String>
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private var isServiceBound = false
    private var bluetoothService: BluetoothService? = null


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    private val deviceConnectedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Dispositivo conectado")
        }
    }


    //soundpool
    @SuppressLint("UseSparseArrays")
    private val soundMap = SparseArray<Int>()
    @SuppressLint("UseSparseArrays")
    private val originalBackgrounds = SparseArray<Int>()
    private lateinit var soundPool: SoundPool



    //inicio del oncreate
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //programacion de la conectividad

        bluetoothSpinner = findViewById(R.id.bluetoothSpinner)
        connectButton = findViewById(R.id.conectarBluetooth)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        connectButton.setOnClickListener {
            val selectedDeviceIndex = bluetoothSpinner.selectedItemPosition
            if (selectedDeviceIndex != -1) {
                val selectedDevice = bluetoothDevices[selectedDeviceIndex]
                if (isServiceBound) {
                    bluetoothService?.connectToDevice(selectedDevice)
                }
            }
        }


        // Configuración del SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargar los sonidos
        soundMap.put(R.id.b2, soundPool.load(this, R.raw.re, 1))
        soundMap.put(R.id.b3, soundPool.load(this, R.raw.mi, 1))
        soundMap.put(R.id.b4, soundPool.load(this, R.raw.fa, 1))
        soundMap.put(R.id.b5, soundPool.load(this, R.raw.sol, 1))
        soundMap.put(R.id.b6, soundPool.load(this, R.raw.la, 1))
        soundMap.put(R.id.b7, soundPool.load(this, R.raw.si, 1))
        soundMap.put(R.id.b1, soundPool.load(this, R.raw.do1, 1))
        soundMap.put(R.id.b1_black, soundPool.load(this, R.raw.do1, 1))
        soundMap.put(R.id.b2_black, soundPool.load(this, R.raw.re, 1))
        soundMap.put(R.id.b4_black, soundPool.load(this, R.raw.fa, 1))
        soundMap.put(R.id.b5_black, soundPool.load(this, R.raw.sol, 1))
        soundMap.put(R.id.b6_black, soundPool.load(this, R.raw.la, 1))

        val bluetoothServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(bluetoothServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)


        val b1Button: View = findViewById(R.id.b1)
        b1Button.setOnClickListener {
            if (isServiceBound) {
                bluetoothService?.sendData("1") // Envía el comando "1" al Arduino
            }
        }

        val b2Button: View = findViewById(R.id.b2)
        b2Button.setOnClickListener {
            if (isServiceBound) {
                bluetoothService?.sendData("2") // Envía el comando "2" al Arduino
            }
        }


        // Colores originales de todos los botones
        originalBackgrounds.put(R.id.b1, R.color.white)
        originalBackgrounds.put(R.id.b2, R.color.white)
        originalBackgrounds.put(R.id.b3, R.color.white)
        originalBackgrounds.put(R.id.b4, R.color.white)
        originalBackgrounds.put(R.id.b5, R.color.white)
        originalBackgrounds.put(R.id.b6, R.color.white)
        originalBackgrounds.put(R.id.b7, R.color.white)
        originalBackgrounds.put(R.id.b1_black, R.color.black)
        originalBackgrounds.put(R.id.b2_black, R.color.black)
        originalBackgrounds.put(R.id.b4_black, R.color.black)
        originalBackgrounds.put(R.id.b5_black, R.color.black)
        originalBackgrounds.put(R.id.b6_black, R.color.black)

        val views = intArrayOf(
            R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7,
            R.id.b1_black, R.id.b2_black, R.id.b4_black, R.id.b5_black, R.id.b6_black
        )

        for (viewId in views) {
            val view = findViewById<View>(viewId)
            view.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        animateButtonPress(view)
                        playSound(viewId)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        resetButtonBackground(view)
                    }
                }
                true
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(deviceConnectedReceiver, IntentFilter(BluetoothService.ACTION_DEVICE_CONNECTED))
        checkBluetoothPermissions()
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        unregisterReceiver(deviceConnectedReceiver)
    }

    private fun checkBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCheck = ContextCompat.checkSelfPermission(this, permissions[0])
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_BLUETOOTH)
        } else {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionCheck = ContextCompat.checkSelfPermission(this, locationPermission)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(locationPermission), PERMISSION_REQUEST_LOCATION)
        } else {
            setupBluetoothSpinner()
        }
    }

    private fun setupBluetoothSpinner() {
        devicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        devicesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bluetoothSpinner.adapter = devicesArrayAdapter

        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            registerBluetoothReceiver()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }

        bluetoothAdapter.startDiscovery()
    }

    private fun registerBluetoothReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == BluetoothDevice.ACTION_FOUND) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (device != null) {
                            devicesArrayAdapter.add(device.name ?: "Dispositivo desconocido")
                            bluetoothDevices.add(device)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }


    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                    PERMISSION_REQUEST_BLUETOOTH
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            setupBluetoothSpinner()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_BLUETOOTH -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission()
                } else {
                    Toast.makeText(this, "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBluetoothSpinner()
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()

    }

    private fun animateButtonPress(view: View) {
        val animation = AlphaAnimation(1f, 0.5f)
        animation.duration = 100
        view.startAnimation(animation)
    }

    private fun playSound(viewId: Int) {
        val soundId = soundMap.get(viewId)
        soundId?.let {
            soundPool.play(it, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    private fun resetButtonBackground(view: View) {
        val originalBackground = originalBackgrounds.get(view.id)
        originalBackground?.let {
            view.setBackgroundResource(it)
        }
    }
}