package com.knov.myapplication2

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

@Suppress("DEPRECATION")
class BluetoothService : Service() {

    private val TAG = "BluetoothService"
    private val binder = LocalBinder()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService {
            return this@BluetoothService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    fun connectToDevice(device: BluetoothDevice) {
        bluetoothDevice = device
        ConnectThread().start()
    }

    private inner class ConnectThread : Thread() {
        override fun run() {
            try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Vinculando...")
                    bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(MY_UUID)
                    bluetoothSocket?.connect()
                    Log.d(TAG, "Conectado exitosamente a: ${bluetoothDevice?.name}")
                    sendConnectionSuccessBroadcast()
                } else {
                    Log.e(TAG, "No se concedió el permiso de Bluetooth")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error al conectar con: ${bluetoothDevice?.name}", e)
            }
        }
    }

    private fun sendConnectionSuccessBroadcast() {
        val intent = Intent(ACTION_DEVICE_CONNECTED)
        sendBroadcast(intent)
    }

    fun sendData(data: String) {
        try {
            val outputStream = bluetoothSocket?.outputStream
            outputStream?.write(data.toByteArray())
        } catch (e: IOException) {
            Log.e(TAG, "Error al enviar datos", e)
        }
    }

    companion object {
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val ACTION_DEVICE_CONNECTED = "com.knov.myapplication2.ACTION_DEVICE_CONNECTED"
    }
}
