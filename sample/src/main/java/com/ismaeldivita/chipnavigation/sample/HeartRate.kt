package com.ismaeldivita.chipnavigation.sample

import android.Manifest
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import org.w3c.dom.Text
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


class HeartRate : AppCompatActivity() {
    private val TAG = HeartRate::class.java.name
    private var googleApiClient: GoogleApiClient? = null
    private var authInProgress = false
    private var onDataPointListener: OnDataPointListener? = null
    private val missingPermission: MutableList<String> = ArrayList()
    private var bCheckStarted = false
    private var bGoogleConnected = false
    private var btnStart: Button? = null
    private var spinner1: ProgressBar? = null
    private var spinner2: ProgressBar? = null
    private var spinner3: ProgressBar? = null
    private var spinner4: ProgressBar? = null
    private var powerManager: PowerManager? = null
    private var wakeLock: WakeLock? = null
    private var textMon1: TextView? = null
    private var textMon2: TextView? = null
    private var textMon3: TextView? = null
    private var textMon4: TextView? = null

    //    @SuppressLint("InvalidWakeLockTag")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.heart_rate)

        //심박패턴을 측정하는 동안 화면이 꺼지지 않도록 제어하기 위해 전원관리자를 얻어옵니다
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager!!.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            TAG
        )
        initUI()
        //필요한 권한을 얻었는지 확인하고, 얻지 않았다면 권한 요청을 하기 위한 코드를 호출합니다
        checkAndRequestPermissions()
    }

    public override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun initUI() {
        //심박수를 측정하는 Google API의 호출을 위해 API 클라이언트를 초기화 합니다
        initGoogleApiClient()
        textMon1 = findViewById(R.id.textMon1)
        textMon2 = findViewById(R.id.textMon2)
        textMon3 = findViewById(R.id.textMon3)
        textMon4 = findViewById(R.id.textMon4)
        spinner1 = findViewById(R.id.progressBar1)
        spinner2 = findViewById(R.id.progressBar2)
        spinner3 = findViewById(R.id.progressBar3)
        spinner4 = findViewById(R.id.progressBar4)
        spinner1!!.visibility = View.INVISIBLE
        spinner2!!.visibility = View.INVISIBLE
        spinner3!!.visibility = View.INVISIBLE
        spinner4!!.visibility = View.INVISIBLE
        btnStart = findViewById(R.id.btnStart)
        btnStart!!.text = "Wait please ..."
        btnStart!!.isEnabled = false
        btnStart!!.setOnClickListener(View.OnClickListener {
            if (bCheckStarted) {
                //btnStart.setText(R.string.msg_start);
                btnStart!!.text = "Start"
                bCheckStarted = false
                unregisterFitnessDataListener()
                spinner1!!.visibility = View.INVISIBLE
                spinner2!!.visibility = View.INVISIBLE
                spinner3!!.visibility = View.INVISIBLE
                spinner4!!.visibility = View.INVISIBLE
                wakeLock!!.release()
            } else {
                //버튼을 처음 클릭할 경우 Google API 클라이언트에 로그인이 되어있는 상태인지를 확인합니다.
                //만약 로그인이 되어 있는 상태라면,
                if (bGoogleConnected == true) {
                    //심박수를 측정하기 위한 API를 설정합니다
//                    findDataSources()
                    //심박수의 측정이 시작되면 심박수 정보를 얻을 콜백함수를 등록/설정하는 함수를 호출합니다
//                    registerDataSourceListener(DataType.TYPE_HEART_RATE_BPM)

                    readBpmData()
                    readStepData()
                    readCaloriesData()
                    readWeightHeightData()

                    btnStart!!.text = "Stop"
                    //btnStart.setText(R.string.msg_stop);
                    bCheckStarted = true
                    spinner1!!.visibility = View.VISIBLE
                    spinner2!!.visibility = View.VISIBLE
                    spinner3!!.visibility = View.VISIBLE
                    spinner4!!.visibility = View.VISIBLE
                    //화면이 꺼지지 않도록 설정합니다
                    wakeLock!!.acquire()
                } else {
                    //Google API 클라이언트에 로그인 합니다
                    if (googleApiClient != null) googleApiClient!!.connect()
                }
            }
        })
    }

    private fun initGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.HISTORY_API)
            .addScope(Fitness.SCOPE_BODY_READ)
            .addScope(Fitness.SCOPE_ACTIVITY_READ)
//            .addScope(Fitness.SCOPE_LOCATION_READ)
//            .addScope(Scope(Scopes.FITNESS_BLOOD_GLUCOSE_READ))
//            .addScope(Scope(Scopes.FITNESS_BLOOD_PRESSURE_READ))
            .addConnectionCallbacks(
                object : GoogleApiClient.ConnectionCallbacks {

                    //Google API 클라이언트의 로그인에 성공하면 호출이 되는 콜백입니다
                    override fun onConnected(bundle: Bundle?) {
                        Log.d(TAG, "initGoogleApiClient() onConnected good...")
                        bGoogleConnected = true
                        btnStart!!.text = "Start"
                        btnStart!!.isEnabled = true
                    }

                    override fun onConnectionSuspended(i: Int) {
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.d(TAG, "onConnectionSuspended() network_lost bad...")
                        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.d(TAG, "onConnectionSuspended() service_disconnected bad...")
                        }
                    }
                }
            )
            .addOnConnectionFailedListener(
                GoogleApiClient.OnConnectionFailedListener { result ->
                    Log.d(TAG, "Connection failed. Cause: $result")
                    if (!result.hasResolution()) {
                        finish()
                        return@OnConnectionFailedListener
                    }
                    if (!authInProgress) {
                        try {
                            Log.d(TAG, "Attempting to resolve failed connection")
                            authInProgress = true
                            result.startResolutionForResult(
                                this@HeartRate,
                                HeartRate.Companion.AUTH_REQUEST
                            )
                        } catch (e: SendIntentException) {
                            Log.e(
                                TAG,
                                "Exception while starting resolution activity", e
                            )
                            finish()
                        }
                    } else {
                        finish()
                    }
                }
            )
            .build()
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private fun checkAndRequestPermissions() {
        // Check for permissions
        for (eachPermission in HeartRate.Companion.REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    eachPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermission.add(eachPermission)
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            if (googleApiClient != null) googleApiClient!!.connect()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                missingPermission.toTypedArray(),
                HeartRate.Companion.REQUEST_PERMISSION_CODE
            )
        } else {
            if (googleApiClient != null) googleApiClient!!.connect()
        }
    }

    /**
     * Result of runtime permission request
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check for granted permission and remove from missing list
        if (requestCode == HeartRate.Companion.REQUEST_PERMISSION_CODE) {
            for (i in grantResults.indices.reversed()) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i])
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            initGoogleApiClient()
            if (googleApiClient != null) googleApiClient!!.connect()
        } else {
            Toast.makeText(applicationContext, "Failed get permissions", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun findDataSources() {
        Fitness.SensorsApi.findDataSources(
            googleApiClient, DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_HEART_RATE_BPM) // .setDataTypes(DataType.TYPE_SPEED)
                // .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build()
        )
            .setResultCallback { dataSourcesResult ->
                for (dataSource in dataSourcesResult.dataSources) {
                    if (dataSource.dataType == DataType.TYPE_HEART_RATE_BPM && onDataPointListener == null) {
                        Log.d(
                            TAG,
                            "findDataSources onResult() registering dataSource=$dataSource"
                        )
                        registerDataSourceListener(DataType.TYPE_HEART_RATE_BPM)
                    }
                }
            }
    }

    private fun readBpmData() {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        Log.i(TAG, "Range Start: $startTime")
        Log.i(TAG, "Range End: $endTime")

        Fitness.HistoryApi.readData(
            googleApiClient, DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .bucketByActivityType(1, TimeUnit.SECONDS)
                .build()
        )
            .setResultCallback { response ->
                dumpDataSet(response.getDataSet(DataType.TYPE_HEART_RATE_BPM))
                response.buckets.forEach { bucket ->
                    bucket.dataSets.forEach { dataSet ->
                        dataSet.dataPoints.forEach { dataPoint ->
                            dataPoint.dataType.fields.forEach { field ->
                                println(dataPoint.getValue(field).asFloat())
                                addContentToView(dataPoint.getValue(field).asFloat())
                            }
                        }
                    }
                }
            }

//        Fitness.HistoryApi.readDailyTotal(
//            googleApiClient,
//            DataType.TYPE_HEART_RATE_BPM
//        )
//            .setResultCallback {
//                println(it.total.dataPoints)
//                it.total.dataPoints.forEach { dataPoint ->
//                    dataPoint.dataType.fields.forEach { field ->
//                        println(dataPoint.getValue(field).asFloat())
//                        addContentToView(dataPoint.getValue(field).asFloat())
//                    }
//                }
//            }
    }

    private fun readStepData() {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        Log.i(TAG, "Range Start: $startTime")
        Log.i(TAG, "Range End: $endTime")

//        Fitness.HistoryApi.readData(
//            googleApiClient, DataReadRequest.Builder()
////                .read(DataType.TYPE_HEART_RATE_BPM)
//            .read(DataType.TYPE_STEP_COUNT_DELTA)
//            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
//            .build()
//        )
//            .setResultCallback { response ->
//                dumpDataSet(response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA))
////                dumpDataSet(response.getDataSet(DataType.TYPE_HEART_RATE_BPM))
////                println(response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA))
//            }

        Fitness.HistoryApi.readDailyTotal(
            googleApiClient,
//            DataType.TYPE_HEART_RATE_BPM
            DataType.TYPE_STEP_COUNT_DELTA
        )
            .setResultCallback {
                println(it.total.dataPoints[0].getValue(Field.FIELD_STEPS).asInt())
                addContentToView2(it.total.dataPoints[0].getValue(Field.FIELD_STEPS).asInt())
//                println(it.total.dataPoints)
            }

//        onDataPointListener = OnDataPointListener { dataPoint ->
//
//            // 심박수가 측정되면 심박수를 얻어올 수 있는 콜백입니다
//            for (field in dataPoint.dataType.fields) {
//                val aValue = dataPoint.getValue(field)
//                Log.d(TAG, "Detected DataPoint field: " + field.getName());
//                Log.d(TAG, "Detected DataPoint value: " + aValue);
//
//                //addContentToView("dataPoint=" + field.getName() + " " + aValue + "\n");
//                addContentToView(aValue.asFloat())
//            }
//        }

//        Fitness.HistoryApi.readData(
//            googleApiClient, DataReadRequest.Builder()
//                .aggregate(DataType.TYPE_HEART_RATE_BPM)
//                .bucketByActivityType(1, TimeUnit.SECONDS)
//                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
//                .build()
//        )
//            .setResultCallback { response ->
//                // The aggregate query puts datasets into buckets, so flatten into a
//                // single list of datasets
//                for (dataSet in response.buckets.flatMap { it.dataSets }) {
//                    dumpDataSet(dataSet)
//                }
//            }
//
//        val readRequest1 = DataReadRequest.Builder()
//            // The data request can specify multiple data types to return,
//            // effectively combining multiple data queries into one call.
//            // This example demonstrates aggregating only one data type.
//            .aggregate(DataType.TYPE_HEART_RATE_BPM)
//            // Analogous to a "Group By" in SQL, defines how data should be
//            // aggregated.
//            // bucketByTime allows for a time span, whereas bucketBySession allows
//            // bucketing by <a href="/fit/android/using-sessions">sessions</a>.
//            .bucketByTime(1, TimeUnit.DAYS)
//            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
//            .build()
//
//        val readRequest2 = DataReadRequest.Builder()
//            .aggregate(DataType.TYPE_HEART_RATE_BPM)
//            .bucketByActivityType(1, TimeUnit.SECONDS)
//            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
//            .build()
    }

    private fun readCaloriesData() {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        Log.i(TAG, "Range Start: $startTime")
        Log.i(TAG, "Range End: $endTime")

        Fitness.HistoryApi.readDailyTotal(
            googleApiClient,
            DataType.TYPE_CALORIES_EXPENDED
        )
            .setResultCallback {
                println(it.total.dataPoints[0].getValue(Field.FIELD_CALORIES).asFloat())
                addContentToView3(it.total.dataPoints[0].getValue(Field.FIELD_CALORIES).asFloat().toInt())
            }
    }

    private fun readWeightHeightData() {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        Log.i(TAG, "Range Start: $startTime")
        Log.i(TAG, "Range End: $endTime")

        Fitness.HistoryApi.readData(
            googleApiClient, DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .read( DataType.TYPE_HEIGHT)
                .setTimeRange(1, endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()
        )
            .setResultCallback { response ->
//                dumpDataSet(response.getDataSet(DataType.TYPE_HEART_RATE_BPM))
                println(response.getDataSet(DataType.TYPE_WEIGHT).dataPoints[0].getValue(Field.FIELD_WEIGHT).asFloat())
                println(response.getDataSet(DataType.TYPE_HEIGHT).dataPoints[0].getValue(Field.FIELD_HEIGHT).asFloat() * 100)
                addContentToView4(Pair(response.getDataSet(DataType.TYPE_WEIGHT).dataPoints[0].getValue(Field.FIELD_WEIGHT).asFloat(),
                    response.getDataSet(DataType.TYPE_HEIGHT).dataPoints[0].getValue(Field.FIELD_HEIGHT).asFloat() * 100))
            }
    }

    private fun dumpDataSet(dataSet: DataSet) {
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i(TAG, "Data point:")
            Log.i(TAG, "\tType: ${dp.dataType.name}")
            Log.i(TAG, "\tStart: ${dp.getStartTimeString()}")
            Log.i(TAG, "\tEnd: ${dp.getEndTimeString()}")
            for (field in dp.dataType.fields) {
                Log.i(TAG, "\tField: ${field.name.toString()} Value: ${dp.getValue(field)}")
            }
        }
    }

    private fun DataPoint.getStartTimeString() =
        Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime().toString()

    private fun DataPoint.getEndTimeString() =
        Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime().toString()

    private fun registerDataSourceListener(dataType: DataType) {
        onDataPointListener = OnDataPointListener { dataPoint ->

            // 심박수가 측정되면 심박수를 얻어올 수 있는 콜백입니다
            for (field in dataPoint.dataType.fields) {
                val aValue = dataPoint.getValue(field)
                //Log.d(TAG, "Detected DataPoint field: " + field.getName());
                //Log.d(TAG, "Detected DataPoint value: " + aValue);

                //addContentToView("dataPoint=" + field.getName() + " " + aValue + "\n");
                addContentToView(aValue.asFloat())
            }
        }
        Fitness.SensorsApi.add(
            googleApiClient,
            SensorRequest.Builder()
                .setDataType(dataType)
                .setSamplingRate(2, TimeUnit.SECONDS)
                .setAccuracyMode(SensorRequest.ACCURACY_MODE_DEFAULT)
                .build(),
            onDataPointListener
        )
            .setResultCallback { status ->
                if (status.isSuccess) {
                    Log.d(TAG, "onDataPointListener  registered good")
                } else {
                    Log.d(TAG, "onDataPointListener failed to register bad")
                }
            }
    }

    private fun unregisterFitnessDataListener() {
        if (onDataPointListener == null) {
            return
        }
        if (googleApiClient == null) {
            return
        }
        if (googleApiClient!!.isConnected == false) {
            return
        }
        Fitness.SensorsApi.remove(
            googleApiClient,
            onDataPointListener
        )
            .setResultCallback { status ->
                if (status.isSuccess) {
                    Log.d(TAG, "Listener was removed!")
                } else {
                    Log.d(TAG, "Listener was not removed.")
                }
            }
        // [END unregister_data_listener]
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onStart connect attempted")
    }

    override fun onStop() {
        super.onStop()
        unregisterFitnessDataListener()
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HeartRate.Companion.AUTH_REQUEST) {
            authInProgress = false
            if (resultCode == RESULT_OK) {
                if (!googleApiClient!!.isConnecting && !googleApiClient!!.isConnected) {
                    googleApiClient!!.connect()
                    Log.d(TAG, "onActivityResult googleApiClient.connect() attempted in background")
                }
            }
        }
    }

    @Synchronized
    private fun addContentToView(value: Float) {
        runOnUiThread {
            if (spinner1!!.visibility == View.VISIBLE) spinner1!!.visibility =
                View.INVISIBLE
            Log.d(TAG, "Heart Beat Rate Value : ${value}bpm")
            textMon1!!.text = "Heart Beat Rate Value : ${value}bpm"
        }
    }

    @Synchronized
    private fun addContentToView2(value: Int) {
        runOnUiThread {
            if (spinner2!!.visibility == View.VISIBLE) spinner2!!.visibility =
                View.INVISIBLE
            Log.d(TAG, "Step Count Value : ${value}step")
            textMon2!!.text = "Step Count Value : ${value}step"
        }
    }

    @Synchronized
    private fun addContentToView3(value: Int) {
        runOnUiThread {
            if (spinner3!!.visibility == View.VISIBLE) spinner3!!.visibility =
                View.INVISIBLE
            Log.d(TAG, "Calories Expended Value : ${value}cal")
            textMon3!!.text = "Calories Expended Value : ${value}cal"
        }
    }

    @Synchronized
    private fun addContentToView4(value: Pair<Float, Float>) {
        runOnUiThread {
            if (spinner4!!.visibility == View.VISIBLE) spinner4!!.visibility =
                View.INVISIBLE
            Log.d(TAG, "Weight Value : ${value.first}kg \n Height Value : ${value.second}cm")
            textMon4!!.text = "Weight Value : ${value.first}kg \n Height Value : ${value.second}cm"
        }
    }

    companion object {
        private const val AUTH_REQUEST = 1
        private val REQUIRED_PERMISSION_LIST = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        private const val REQUEST_PERMISSION_CODE = 12345
    }
}