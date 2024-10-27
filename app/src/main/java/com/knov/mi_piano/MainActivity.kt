package com.knov.mi_piano

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

const val REQUEST_ENABLE_BT = 1
const val REQUEST_LOCATION_PERMISSION = 2

class MainActivity : AppCompatActivity() {

    // BluetoothAdapter
    private lateinit var mBtAdapter: BluetoothAdapter
    private var mAddressDevices: ArrayAdapter<String>? = null
    private var mNameDevices: ArrayAdapter<String>? = null
    private lateinit var bluetoothSpinner: Spinner

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    //soundpool
    @SuppressLint("UseSparseArrays")
    private val soundMap = SparseArray<Int>()
    @SuppressLint("UseSparseArrays")
    private val originalBackgrounds = SparseArray<Int>()
    private lateinit var soundPool: SoundPool


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        //declaracion del boton conectar
        val conectarBluetooth = findViewById<Button>(R.id.conectarBluetooth)
        bluetoothSpinner = findViewById(R.id.bluetoothSpinner)


        //declaracion de las teclas blancas
        val b1 = findViewById<View>(R.id.b1)
        val b2 = findViewById<View>(R.id.b2)
        val b3 = findViewById<View>(R.id.b3)
        val b4 = findViewById<View>(R.id.b4)
        val b5 = findViewById<View>(R.id.b5)
        val b6 = findViewById<View>(R.id.b6)
        val b7 = findViewById<View>(R.id.b7)

        //declaracion de teclas negras
        val b1_black = findViewById<View>(R.id.b1_black)
        val b2_black = findViewById<View>(R.id.b2_black)
        val b3_black = findViewById<View>(R.id.b3_black)
        val b4_black = findViewById<View>(R.id.b4_black)
        val b5_black = findViewById<View>(R.id.b5_black)


        //--------------------------------------------------
        //--------------------------------------------------
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
                // Si el usuario ha habilitado correctamente el Bluetooth, se puede realizar el escaneo y establecer la conexión.
                checkLocationPermission()
            }
        }

        // Inicialización del adaptador Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBtAdapter = bluetoothManager.adapter

        // Verificar si el Bluetooth está disponible en el dispositivo
        Toast.makeText(this, "Bluetooth está disponible en este dispositivo", Toast.LENGTH_LONG).show()
        // Verificar si el Bluetooth está habilitado
        if (!mBtAdapter.isEnabled) {
            // Si no está habilitado, solicitar al usuario que lo habilite
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            someActivityResultLauncher.launch(enableBtIntent)
        } else {
            // Si ya está habilitado, realizar el escaneo y establecer la conexión
            checkLocationPermission()
        }
        //--------------------------------------------------
        //--------------------------------------------------

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
        soundMap.put(R.id.b1, soundPool.load(this, R.raw.do1, 1))
        soundMap.put(R.id.b2, soundPool.load(this, R.raw.re, 1))
        soundMap.put(R.id.b3, soundPool.load(this, R.raw.mi, 1))
        soundMap.put(R.id.b4, soundPool.load(this, R.raw.fa, 1))
        soundMap.put(R.id.b5, soundPool.load(this, R.raw.sol, 1))
        soundMap.put(R.id.b6, soundPool.load(this, R.raw.la, 1))
        soundMap.put(R.id.b7, soundPool.load(this, R.raw.si, 1))
        soundMap.put(R.id.b1_black, soundPool.load(this, R.raw.do1, 1))
        soundMap.put(R.id.b2_black, soundPool.load(this, R.raw.re, 1))
        soundMap.put(R.id.b3_black, soundPool.load(this, R.raw.mi, 1))
        soundMap.put(R.id.b4_black, soundPool.load(this, R.raw.fa, 1))
        soundMap.put(R.id.b5_black, soundPool.load(this, R.raw.sol, 1))



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
        originalBackgrounds.put(R.id.b3_black, R.color.black)
        originalBackgrounds.put(R.id.b4_black, R.color.black)
        originalBackgrounds.put(R.id.b5_black, R.color.black)


        //teclas blancas

        b1.setOnClickListener {
            sendCommand("A")
            animateButtonPress(b1)
            playSound(R.id.b1)
        }

        b2.setOnClickListener {
            sendCommand("B")
            animateButtonPress(b2)
            playSound(R.id.b2)
        }

        b3.setOnClickListener {
            sendCommand("C")
            animateButtonPress(b3)
            playSound(R.id.b3)
        }

        b4.setOnClickListener {
            sendCommand("D")
            animateButtonPress(b4)
            playSound(R.id.b4)
        }

        b5.setOnClickListener {
            sendCommand("E")
            animateButtonPress(b5)
            playSound(R.id.b5)
        }

        b6.setOnClickListener {
            sendCommand("F")
            animateButtonPress(b6)
            playSound(R.id.b6)
        }

        b7.setOnClickListener {
            sendCommand("G")
            animateButtonPress(b7)
            playSound(R.id.b7)
        }

        //teclas negras
        b1_black.setOnClickListener {
            sendCommand("H")
            animateButtonPress(b1_black)
            playSound(R.id.b1_black)
        }

        b2_black.setOnClickListener {
            sendCommand("I")
            animateButtonPress(b2_black)
            playSound(R.id.b2_black)
        }

        b3_black.setOnClickListener {
            sendCommand("J")
            animateButtonPress(b3_black)
            playSound(R.id.b3_black)
        }

        b4_black.setOnClickListener {
            sendCommand("K")
            animateButtonPress(b4_black)
            playSound(R.id.b4_black)
        }

        b5_black.setOnClickListener {
            sendCommand("L")
            animateButtonPress(b5_black)
            playSound(R.id.b5_black)
        }


        conectarBluetooth.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValSpin = bluetoothSpinner.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_address, Toast.LENGTH_LONG).show()

                    // Cancelar el escaneo porque de lo contrario ralentizará la conexión.
                    mBtAdapter.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this, "CONEXION EXITOSA", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "CONEXION EXITOSA")

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "ERROR DE CONEXION")
            }
        }

    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // El permiso de ubicación ya está concedido
            scanBluetoothDevices()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido
                scanBluetoothDevices()
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para escanear dispositivos Bluetooth", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scanBluetoothDevices() {
        if (mBtAdapter.isEnabled) {
            val pairedDevices: Set<BluetoothDevice>? = try {
                mBtAdapter.bondedDevices
            } catch (e: SecurityException) {
                e.printStackTrace()
                null
            }
            mAddressDevices!!.clear()
            mNameDevices!!.clear()

            try {
                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    mNameDevices!!.add(deviceName)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Error al acceder a los dispositivos Bluetooth emparejados", Toast.LENGTH_LONG).show()
            }

            bluetoothSpinner.adapter = mNameDevices
        } else {
            val noDevices = "Ningún dispositivo pudo ser emparejado"
            mAddressDevices!!.add(noDevices)
            mNameDevices!!.add(noDevices)
            Toast.makeText(this, "Primero vincule un dispositivo Bluetooth", Toast.LENGTH_LONG).show()
        }
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


    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}