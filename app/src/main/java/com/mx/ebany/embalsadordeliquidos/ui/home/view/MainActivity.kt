package com.mx.ebany.embalsadordeliquidos.ui.home.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mx.ebany.embalsadordeliquidos.R
import com.mx.ebany.embalsadordeliquidos.databinding.ActivityMainBinding
import com.mx.ebany.embalsadordeliquidos.tools.Constants
import com.mx.ebany.embalsadordeliquidos.ui.dialogs.ProcesDialogFragment
import com.mx.ebany.embalsadordeliquidos.ui.home.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import androidx.activity.viewModels
import com.mx.ebany.embalsadordeliquidos.core.room.AppDataBase
import com.mx.ebany.embalsadordeliquidos.core.room.DataConfiguration
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModel<ViewModel>()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSION_BLUETOOTH = 2
    private var isConnected: Boolean = false
    private var isConnecting: Boolean = false
    private lateinit var inputStream: InputStream
    private lateinit var quantyBags : TextView
    private lateinit var titleActualProcess : TextView
    private lateinit var ivProcess : ImageView
    private lateinit var btnEndProcess: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var processDialog: AlertDialog
    private lateinit var dialog: ProcesDialogFragment
    private var contConfig = 0
    private val TAG = "MainActivityLifecycle"






    private var cantidad = "0"
    private var ancho = ""
    private var litros = ""
    private var tiempoCorte = ""
    private var tiempoSellado = ""
    private var constanteLlenado = ""
    private var delayMotor = ""

    private val deviceUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")



    val handler = Handler(Looper.getMainLooper())
    val logRunnable = object : Runnable {
        override fun run() {
            Log.d("MiBoton", "Botón presionado")
            handler.postDelayed(this, 500) // Vuelve a ejecutar cada 500 ms
            if(delayMotor == "") delayMotor = "500"
            if(constanteLlenado == "") constanteLlenado = "50"
            sendData("BOMB-1-$delayMotor-100-100-100-100-$constanteLlenado")// Vueltas - delay motor - XX - XX - XX - XX - constanteDeLlenado
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setVisualContent()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no está disponible", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Por favor, enciende el Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        setInitialValues()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.getConfiguration.observe(this){
            if(it != null){
                constanteLlenado = it.constanteLlenado
                delayMotor = it.delayMotor
                binding.inCantidad.etData.setText(it.cantidadBolsas)
                binding.inWidth.etData.setText(it.anchoBolsa)
                binding.inLitros.etData.setText(it.cantidadLitros)
                binding.inTimeCut.etData.setText(it.tiempoCorte)
                binding.inTimeSell.etData.setText(it.tiempoSellado)
                cantidad = it.cantidadBolsas
                ancho = it.anchoBolsa
                litros = it.cantidadLitros
                tiempoCorte = it.tiempoCorte
                tiempoSellado = it.tiempoSellado
            }
        }
    }

    private fun setInitialValues() {
        binding.inTimeCut.etData.isEnabled = false
        binding.inTimeSell.etData.isEnabled = false
        setProcessDialog()
    }

    private fun setListeners() {

        binding.tvSelectParams.setOnClickListener {
            contConfig ++
            if(contConfig > 3){
                contConfig = 0
                showConfigDialog()
            }
        }

        binding.inCantidad.spParam.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.inCantidad.etData.setText(
                        if (Constants.cantidadList[position] == "Selecciona"
                        ) "" else Constants.cantidadList[position]
                    )
                    cantidad = binding.inCantidad.etData.text.toString()
                    binding.inCantidad.tlData.error = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        binding.inWidth.spParam.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.inWidth.etData.setText(
                        if (Constants.anchoList[position].replace(
                                "mm",
                                ""
                            ) == "Selecciona"
                        ) "" else Constants.anchoList[position].replace("mm", "")
                    )
                    ancho = binding.inWidth.etData.text.toString()
                    binding.inWidth.tlData.error = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        binding.inLitros.spParam.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.inLitros.etData.setText(
                        if (Constants.litrosList[position].replace(
                                "L",
                                ""
                            ) == "Selecciona"
                        ) "" else Constants.litrosList[position].replace("L", "")
                    )
                    litros = binding.inLitros.etData.text.toString()
                    binding.inLitros.tlData.error = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        binding.inTimeCut.spParam.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.inTimeCut.etData.setText(
                        if (Constants.timeCut[position].replace(
                                "seg",
                                ""
                            ) == "Selecciona"
                        ) "" else Constants.timeCut[position].replace("seg", "")
                    )
                    tiempoCorte = binding.inTimeCut.etData.text.toString()
                    binding.inTimeCut.tlData.error = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        binding.inTimeSell.spParam.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.inTimeSell.etData.setText(
                        if (Constants.timeSell[position].replace(
                                "seg",
                                ""
                            ) == "Selecciona"
                        ) "" else Constants.timeSell[position].replace("seg", "")
                    )
                    tiempoSellado = binding.inTimeSell.etData.text.toString()
                    binding.inTimeSell.tlData.error = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        binding.btSend.setOnClickListener {
            if(verifyData()){
                if(delayMotor == "") delayMotor = "500"
                if(constanteLlenado == "") constanteLlenado = "50"
                sendData("START-$cantidad-$ancho-$litros-$tiempoSellado-$tiempoCorte-$delayMotor-$constanteLlenado")// cantidad - ancho - litroas - tiempo compress - tiempo corte - delay motor - constanteDeLlenado
                val data = DataConfiguration()
                data.cantidadBolsas = cantidad
                data.anchoBolsa = ancho
                data.cantidadLitros = litros
                data.tiempoCorte = tiempoCorte
                data.tiempoSellado = tiempoSellado
                viewModel.addConfiguration(data)
            }
        }

        binding.btnVincule.setOnClickListener {
            getBluetoothPermissions()
        }

        binding.btIsActive.setOnClickListener {
            disconnectBluetooth()
        }

        binding.cvActualProcess.setOnClickListener {
            cantidad = "20"
            setProcessDialog()
            showProcessDialog()
        }

        binding.btStarPumps.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.post(logRunnable) // Inicia el log cuando se presiona el botón
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(logRunnable) // Detiene el log cuando se suelta el botón
                    true
                }
                else -> false
            }
        }
    }

    private fun verifyData(): Boolean {
        var validacion = 5
        cantidad = binding.inCantidad.etData.text.toString()
        ancho = binding.inWidth.etData.text.toString()
        litros = binding.inLitros.etData.text.toString()
        if(cantidad.isNullOrEmpty()){
            binding.inCantidad.tlData.error = "Seleccione Cantidad"
            validacion --
        }
        if(ancho.isNullOrEmpty()){
            binding.inWidth.tlData.error = "Seleccione Ancho"
            validacion --
        }
        if(litros.isNullOrEmpty()){
            binding.inLitros.tlData.error = "Seleccione Litros"
            validacion --
        }
        if(tiempoCorte.isNullOrEmpty()){
            binding.inTimeCut.tlData.error = "Seleccione Tiempo de Corte"
            validacion --
        }
        if(tiempoSellado.isNullOrEmpty()){
            binding.inTimeSell.tlData.error = "Seleccione Tiempo de Sellado"
            validacion --
        }
        return validacion == 5
    }

    private fun setVisualContent() {
        binding.inCantidad.tvTitleParam.text = "Cantidad de bolsas"
        binding.inWidth.tvTitleParam.text = "Ancho de la bolsa"
        binding.inLitros.tvTitleParam.text = "Cantidad de litros"
        binding.inTimeSell.tvTitleParam.text = "Tiempo de sellado"
        binding.inTimeCut.tvTitleParam.text = "Tiempo de corte"

        binding.inCantidad.tvMedida.text = "U"
        binding.inWidth.tvMedida.text = "mm"
        binding.inLitros.tvMedida.text = "Litros"
        binding.inTimeSell.tvMedida.text = "Seg"
        binding.inTimeCut.tvMedida.text = "Seg"

        binding.inCantidad.ivIconParam.setImageResource(R.drawable.ic_cantidad)
        binding.inWidth.ivIconParam.setImageResource(R.drawable.ic_ancho)
        binding.inLitros.ivIconParam.setImageResource(R.drawable.ic_litros)
        binding.inTimeSell.ivIconParam.setImageResource(R.drawable.ic_sellado)
        binding.inTimeCut.ivIconParam.setImageResource(R.drawable.ic_time)

        val adapterLitros: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterLitros.addAll(Constants.litrosList)
        binding.inLitros.spParam.adapter = adapterLitros

        val adapterCantidad: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterCantidad.addAll(Constants.cantidadList)
        binding.inCantidad.spParam.adapter = adapterCantidad

        val adapterAncho: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterAncho.addAll(Constants.anchoList)
        binding.inWidth.spParam.adapter = adapterAncho

        val adapterCut: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterCut.addAll(Constants.timeCut)
        binding.inTimeCut.spParam.adapter = adapterCut

        val adapterSell: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterSell.addAll(Constants.timeSell)
        binding.inTimeSell.spParam.adapter = adapterSell
    }

    private fun showPairedDevices() {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        val deviceList = ArrayList<String>()
        val deviceMap = HashMap<String, BluetoothDevice>()
        val deviceMutableList = mutableListOf<String>()
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceAddress = device.address
                val keyDevice = if(deviceAddress.contains("20:16:10:24:90:42")) "Dispositivo De Sellado" else  "$deviceName - $deviceAddress"
                deviceList.add(keyDevice)
                deviceMap[keyDevice] = device
            }
        } else {
            deviceList.add("No hay dispositivos emparejados")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        showBluetoothDialog(adapter, deviceList, deviceMap)
    }

    private suspend fun connectToDevice2(device: BluetoothDevice, isConnect : (Boolean) -> Unit) {
        val socket: BluetoothSocket?
        Log.e("BLUETHOOTH", "INICIANDO CONEXION")
        try {
            socket = device.createRfcommSocketToServiceRecord(deviceUUID)
            bluetoothAdapter.cancelDiscovery()
            socket.connect()
            bluetoothSocket = socket
            isConnected = true
            isConnecting = false
            inputStream = bluetoothSocket!!.inputStream
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Conectado a Dispositivo De Sellado", Toast.LENGTH_SHORT).show()
                binding.btIsActive.visibility = View.VISIBLE
                binding.btnVincule.visibility = View.GONE
                startConnectionChecker()
                startReadingData()
            }
            isConnect(true)
        } catch (e: IOException) {
            e.printStackTrace()
            isConnecting = false
            isConnected = false
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error de conexión: Verifique la conexión de su dispositivo", Toast.LENGTH_SHORT).show()
                binding.btIsActive.visibility = View.GONE
                binding.btnVincule.visibility = View.VISIBLE
            }
            Log.e("Bluetooth", "Error de conexión", e)
            isConnect(false)
        }
    }


    private suspend fun connectToDevice(device: BluetoothDevice, isConnect: (Boolean) -> Unit) {
        val socket: BluetoothSocket?
        Log.e("BLUETOOTH", "INICIANDO CONEXION")

        try {
            socket = device.createRfcommSocketToServiceRecord(deviceUUID)
            bluetoothAdapter.cancelDiscovery()

            // Extender timeout a 10 segundos (ajustable según necesidad)
            val timeoutMillis = 100_000L
            val result = withTimeoutOrNull(timeoutMillis) {
                socket.connect() // Intentar conectar dentro del tiempo límite
                true
            }

            if (result == null) {
                throw IOException("Tiempo de espera agotado al conectar con el dispositivo")
            }

            bluetoothSocket = socket
            isConnected = true
            isConnecting = false
            inputStream = bluetoothSocket!!.inputStream

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Conectado a Dispositivo De Sellado", Toast.LENGTH_SHORT).show()
                binding.btIsActive.visibility = View.VISIBLE
                binding.btnVincule.visibility = View.GONE
                startConnectionChecker()
                startReadingData()
            }
            isConnect(true)

        } catch (e: IOException) {
            e.printStackTrace()
            isConnecting = false
            isConnected = false

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error de conexión: Verifique la conexión de su dispositivo", Toast.LENGTH_SHORT).show()
                binding.btIsActive.visibility = View.GONE
                binding.btnVincule.visibility = View.VISIBLE
            }

            Log.e("Bluetooth", "Error de conexión", e)
            isConnect(false)
        }
    }


    private fun sendData(data: String) {
        try {

            if (bluetoothSocket == null) {
                Toast.makeText(this, "No conectado a ningún dispositivo", Toast.LENGTH_SHORT).show()
                return
            }

            val outputStream = bluetoothSocket?.outputStream
            if (outputStream != null) {
                outputStream.write(data.toByteArray())
                outputStream.flush()
                setProcessDialog()
                showProcessDialog()
                binding.btSend.isEnabled = false
                Toast.makeText(this, "Datos enviados: $data", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: OutputStream es null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Bluetooth", "Error al enviar datos", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_BLUETOOTH -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    showPairedDevices()
                } else {
                    Toast.makeText(this, "Permisos de Bluetooth denegados", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getBluetoothPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )

            val permissionsNeeded = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toTypedArray(),
                    REQUEST_PERMISSION_BLUETOOTH
                )
            } else {
                showPairedDevices()
            }
        } else {
            showPairedDevices()
        }
    }


    private fun showBluetoothDialog(
        adapter: ArrayAdapter<String>,
        deviceList: ArrayList<String>,
        deviceMap: HashMap<String, BluetoothDevice>
    ) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.bluetooth_dialog, null)

        builder.setView(view)

        val bluetoothDialog = builder.create()
        bluetoothDialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        bluetoothDialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        bluetoothDialog.show()

        val listBt = view.findViewById<ListView>(R.id.listBluetooth)
        val buttonVincule = view.findViewById<Button>(R.id.btnVinculeBt)
        val progressBar = view.findViewById<ProgressBar>(R.id.progresBarBt)

        progressBar.visibility = View.GONE
        buttonVincule.isEnabled = false
        listBt.adapter = adapter
        listBt.setOnItemClickListener { _, _, position, _ ->
            val selectedDeviceName = deviceList[position]
            Log.e("DEVICEA", "$selectedDeviceName")
            val selectedDevice = deviceMap[selectedDeviceName]
            Log.e("DEVICE1", "$selectedDevice")
            buttonVincule.isEnabled = true
            buttonVincule.setOnClickListener {
                buttonVincule.isEnabled = false
                progressBar.visibility = View.VISIBLE
                Log.e("DEVICE", "$selectedDevice")
                if (selectedDevice != null) {
                    if (isConnecting || isConnected) {
                        Toast.makeText(this, "Ya se está intentando conectar o ya está conectado", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            connectToDevice(selectedDevice) {
                                bluetoothDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }


    }

    private fun setProcessDialog(){
        val builderProcess = AlertDialog.Builder(this@MainActivity)
        val viewProcess = layoutInflater.inflate(R.layout.process_dialog, null)
        builderProcess.setView(viewProcess)

        processDialog = builderProcess.create()
        processDialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        processDialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        quantyBags = viewProcess.findViewById<TextView>(R.id.tvProcessQuanty)
        titleActualProcess = viewProcess.findViewById<TextView>(R.id.tvActualProcess)
        ivProcess = viewProcess.findViewById<ImageView>(R.id.ivProcess)
        btnEndProcess = viewProcess.findViewById<Button>(R.id.btnEndProcess)
        progressBar = viewProcess.findViewById<ProgressBar>(R.id.progressBar)
    }

    private fun showProcessDialog(){
        progressBar.progress = 0

        btnEndProcess.setOnClickListener {
            Toast.makeText(this, "Proceso finalizado, espere un momento", Toast.LENGTH_LONG).show()
            sendData("STOP")
        }
        processDialog.show()
    }

    private fun showConfigDialog() {
        dialog = ProcesDialogFragment()
        dialog.show(supportFragmentManager.beginTransaction().remove(dialog), dialog.getTag());
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: Error) {
            Toast.makeText(this, "Ocurrio un error en -> ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startConnectionChecker() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000)
                if (bluetoothSocket?.isConnected == false) {
                    withContext(Dispatchers.Main) {
                        isConnected = false
                        Toast.makeText(this@MainActivity, "Se ha perdido la conexión Bluetooth", Toast.LENGTH_SHORT).show()
                        binding.btIsActive.visibility = View.GONE
                        binding.btnVincule.visibility = View.VISIBLE
                    }
                    break
                }
            }
        }
    }

    private fun disconnectBluetooth() {
        try {
            bluetoothSocket?.close()
            isConnected = false
            binding.btIsActive.visibility = View.GONE
            binding.btnVincule.visibility = View.VISIBLE
            Toast.makeText(this, "Dispositivo Desconectado", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error al cerrar el socket", e)
        }
    }

    private fun startReadingData() {
        val delimiter = "XX"
        CoroutineScope(Dispatchers.IO).launch {
            val reader = BufferedReader(InputStreamReader(bluetoothSocket?.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (true) {
                try {
                    line = reader.readLine()
                    if (line != null) {
                        stringBuilder.clear()
                        stringBuilder.append(line)
                        Log.e("DATALECT", "$stringBuilder")
                        if (line.endsWith(delimiter)) {
                            // Procesar el mensaje completo
                            val message = stringBuilder.toString().trim()
                            withContext(Dispatchers.Main) {
                                processReceivedData(message)
                            }
                            stringBuilder.clear()
                        }
                    }
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Error al leer datos", e)
                    break
                }
            }
        }
    }

    private fun processReceivedData(data: String) {
        //val receivedText = data.toString(Charsets.UTF_8)
        Log.e("DATARECIBE", "$data")
        actualProcess(data)
    }

    private fun actualProcess(process: String){
        var process2 = process.replace("XX", "")
        try {
            var processTitle = process2.split("_").first()

            var bagsQuanty ="Bolsas procesadas: " +  process2.split("_")[1]
            try {
                progressBar.max = process2.split("_").last().split(".").first().toInt()
            }catch (e: Error){
                progressBar.max = 0
            }
            titleActualProcess.text = when(processTitle){
                "AA" ->{
                    binding.cvActualProcess.visibility = View.VISIBLE
                    binding.ivProcessActual.setImageResource(R.drawable.ic_roll)
                    binding.btSend.isEnabled = false
                    binding.btStarPumps.isEnabled = false
                    ivProcess.setImageResource(R.drawable.ic_roll)
                    quantyBags.text = bagsQuanty
                    progressBar.progress = process2.split("_")[1].toInt()
                    "Preparando Bolsa"
                }
                "BB" ->{
                    binding.cvActualProcess.visibility = View.VISIBLE
                    binding.ivProcessActual.setImageResource(R.drawable.ic_fill)
                    binding.btSend.isEnabled = false
                    binding.btStarPumps.isEnabled = false
                    ivProcess.setImageResource(R.drawable.ic_fill)
                    quantyBags.text = bagsQuanty
                    progressBar.progress = process2.split("_")[1].toInt()
                    "Llenando Bolsas"
                }
                "CC" ->{
                    binding.cvActualProcess.visibility = View.VISIBLE
                    binding.ivProcessActual.setImageResource(R.drawable.ic_sell)
                    binding.btSend.isEnabled = false
                    binding.btStarPumps.isEnabled = false
                    ivProcess.setImageResource(R.drawable.ic_sell)
                    quantyBags.text = bagsQuanty
                    progressBar.progress = process2.split("_")[1].toInt()
                    "Sellando Bolsa"
                }
                "DD" ->{
                    binding.cvActualProcess.visibility = View.VISIBLE
                    binding.ivProcessActual.setImageResource(R.drawable.ic_cut)
                    binding.btSend.isEnabled = false
                    binding.btStarPumps.isEnabled = false
                    ivProcess.setImageResource(R.drawable.ic_cut)
                    quantyBags.text = bagsQuanty
                    progressBar.progress = process2.split("_")[1].toInt()
                    "Cortando Bolsa"
                }
                "EE" ->{
                    binding.cvActualProcess.visibility = View.GONE
                    ivProcess.setImageResource(R.drawable.ic_check)
                    btnEndProcess.text = "Cerrar"
                    binding.btSend.isEnabled = true
                    binding.btStarPumps.isEnabled = true
                    btnEndProcess.setOnClickListener {
                        processDialog.dismiss()
                    }
                    "Proceso finalizado"

                } else -> ""
            }
        }catch (e: Error){

        }

    }

    override fun onResume() {
        super.onResume()
        Log.e("ON RESUME", "")
        viewModel.getDataConfiguration()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
    }




}