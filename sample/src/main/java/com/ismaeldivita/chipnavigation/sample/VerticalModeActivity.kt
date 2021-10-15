package com.ismaeldivita.chipnavigation.sample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.MediaStore
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.ismaeldivita.chipnavigation.sample.util.applyWindowInsets
import com.ismaeldivita.chipnavigation.sample.util.colorAnimation
import com.mongodb.*
import com.mongodb.client.model.Filters
import com.mongodb.gridfs.GridFS
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_vertical.*
import kotlinx.android.synthetic.main.activity_vertical.view.*
import kotlinx.android.synthetic.main.item.view.*
import net.kibotu.heartrateometer.HeartRateOmeter
import net.kibotu.kalmanrx.jama.Matrix
import net.kibotu.kalmanrx.jkalman.JKalman
import org.bson.Document
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Arrays.*
import kotlin.collections.ArrayList


val CAMERA = arrayOf(Manifest.permission.CAMERA)
val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
val CAMERA_CODE = 98
val STORAGE_CODE = 99
val BUTTON1 = 100
val BUTTON2 = 200
val BUTTON3 = 300
val BUTTON4 = 400
val BUTTON5 = 500

var photoUri: Uri? = null

var myUri: String? = null

class VerticalModeActivity : AppCompatActivity(){
    private val container by lazy { findViewById<ViewGroup>(R.id.container) }
    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val button by lazy { findViewById<ImageView>(R.id.expand_button) }
    private val menu by lazy { findViewById<ChipNavigationBar>(R.id.bottom_menu) }

    private val editText1 by lazy { findViewById<EditText>(R.id.editText1) }
    private val button1 by lazy { findViewById<Button>(R.id.button1) }
    private val button2 by lazy { findViewById<Button>(R.id.button2) }
    private val binding by lazy { findViewById<ImageView>(R.id.binding) }
    private val button3 by lazy { findViewById<Button>(R.id.button3) }
    private val textView by lazy { findViewById<TextView>(R.id.textView) }
    private val listView by lazy { findViewById<ListView>(R.id.list_view) }

    private val button4 by lazy { findViewById<Button>(R.id.button4) }
    private val textView1 by lazy { findViewById<TextView>(R.id.textView1) }

    private val LineChart by lazy { findViewById<LineChart>(R.id.LineChart) }

    private val button5 by lazy { findViewById<Button>(R.id.button5) }
    private val button6 by lazy { findViewById<Button>(R.id.button6) }
    private val preview by lazy { findViewById<SurfaceView>(R.id.preview) }
    private val label by lazy { findViewById<TextView>(R.id.label) }
    private val finger by lazy { findViewById<TextView>(R.id.finger) }

    private var lastColor: Int = 0

    private var subscription: CompositeDisposable? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContentView(R.layout.activity_vertical)

        editText1.visibility = View.GONE
        button1.visibility = View.GONE
        button2.visibility = View.GONE
        binding.visibility = View.GONE
        button3.visibility = View.GONE
        textView.visibility = View.GONE
        listView.visibility = View.GONE

        button4.visibility = View.GONE
        textView1.visibility = View.GONE

        LineChart.visibility = View.GONE

        button5.visibility = View.GONE
        button6.visibility = View.GONE
        preview.visibility = View.GONE
        label.visibility = View.GONE
        finger.visibility = View.GONE

        lastColor = ContextCompat.getColor(this, R.color.blank)



//        var mongoClient: MongoClient? = null
//        mongoClient = MongoClient(ServerAddress("10.0.2.2", 27017))
//        println("Connected to MongoDB!")
//        var database = mongoClient.getDatabase("diabetes")
//        try {
//            mongoClient = MongoClient(ServerAddress("10.0.2.2", 27017))
//            println("Connected to MongoDB!")
//
//            var database = mongoClient.getDatabase("diabetes")
//            var collection = database.getCollection("test")
//
//            var document = Document("name", "Activity")
//                .append("contact", Document("phone", "228-555-0149")
//                .append("email", "cafeconleche@example.com")
//                .append("location", listOf(-73.92502, 40.8279556)))
//                .append("stars", 3)
//                .append("categories", listOf("Bakery", "Coffee", "Pastries"));
//            collection.insertOne(document)
//        } catch (e: MongoException) {
//            e.printStackTrace()
//        }




        menu.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                val option = when (id) {
                    R.id.main -> R.color.main to "메인화면"
                    R.id.activity -> R.color.activity to "운동기록"
                    R.id.food -> R.color.food to "섭취음식"
                    R.id.data -> R.color.data to "데이터"
                    else -> R.color.white to ""
                }
                val color = ContextCompat.getColor(this@VerticalModeActivity, option.first)
                container.colorAnimation(lastColor, color)
                lastColor = color
                title.text = option.second

                if(id == R.id.activity) {
                    editText1.visibility = View.VISIBLE
                    editText1.setHint("Input Activity")

                    button1.visibility = View.VISIBLE
                    button1.setOnClickListener {
                        CallCamera()
//                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                        takePictureIntent.resolveActivity(packageManager)?.also {
//                            startActivityForResult(takePictureIntent, BUTTON1)
//                        }
                        binding.visibility = View.GONE
                        textView.visibility = View.GONE
                        listView.visibility = View.GONE

                        preview.visibility = View.GONE
                        label.visibility = View.GONE
                        finger.visibility = View.GONE
                    }

                    button2.visibility = View.VISIBLE
                    button2.setOnClickListener {
                        GetAlbum()
//                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                        val photoFile = File(
//                            File("${filesDir}/image").apply{
//                                if(!this.exists()){
//                                    this.mkdirs()
//                                }
//                            },
//                            newJpgFileName()
//                        )
//                        photoUri = FileProvider.getUriForFile(
//                            this@VerticalModeActivity,
//                            "com.ismaeldivita.chipnavigation.sample.fileprovider",
//                            photoFile
//                        )
//                        takePictureIntent.resolveActivity(packageManager)?.also{
//                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//                            startActivityForResult(takePictureIntent, BUTTON2)
//                        }
                        binding.visibility = View.VISIBLE
                        textView.visibility = View.GONE
                        listView.visibility = View.GONE

                        preview.visibility = View.GONE
                        label.visibility = View.GONE
                        finger.visibility = View.GONE
                    }

                    binding.visibility = View.GONE

                    button3.visibility = View.VISIBLE
                    button3.setOnClickListener {
                        preview.visibility = View.GONE
                        label.visibility = View.GONE
                        finger.visibility = View.GONE
                        val items = mutableListOf<ListViewItem>()
                        var displayData : String = ""
                        binding.visibility = View.GONE
                        var mongoClient: MongoClient? = null
//                        mongoClient = MongoClient(ServerAddress("10.0.2.2", 27017))
//                        mongoClient = MongoClient(ServerAddress("127.0.0.1", 27017))

                        mongoClient = MongoClient("6.tcp.ngrok.io", 18800)

                        println("Connected to MongoDB!")
                        var database = mongoClient!!.getDatabase("OkKim_Activity")
                        var collection = database.getCollection("Activity")

                        if(!editText1.text.toString().isNullOrEmpty()) {
                            println(editText1.text.toString())
                            var document = Document("Activity", editText1.text.toString())
                            collection.insertOne(document)
                        }

                        var activity = collection.find(Filters.exists("Activity"))
                        activity.forEach {
                            displayData = displayData + it["Activity"] + " "
                        }

                        textView.text = displayData

                        if(!myUri.isNullOrEmpty()) {
                            val imageFile = File(myUri)
                            val gfsPhoto = GridFS(mongoClient!!.getDB("OkKim_Activity"))
                            val gfsFile = gfsPhoto.createFile(imageFile)
                            gfsFile.save()

                            val cursor = gfsPhoto.fileList
                            while (cursor.hasNext()) {
                                var cursorNext = cursor.next()
                                var fileName = cursorNext.get("filename").toString()
                                var uploadDate = cursorNext.get("uploadDate").toString()
                                var imageForOutput = gfsPhoto.findOne(fileName)
                                imageForOutput.writeTo("/sdcard/Pictures/$fileName")
                                var file = FileInputStream("/sdcard/Pictures/$fileName")
                                var buf = BufferedInputStream(file)
                                var bitmap = BitmapFactory.decodeStream(buf)
                                items.add(ListViewItem(bitmap, uploadDate))
                            }
                            val adapter = ListViewAdapter(items)
                            listView.adapter = adapter
                            listView.visibility = View.VISIBLE
                        }

                        if(myUri.isNullOrEmpty()) {
                            val gfsPhoto = GridFS(mongoClient!!.getDB("OkKim_Activity"))
                            val cursor = gfsPhoto.fileList
                            while (cursor.hasNext()) {
                                var cursorNext = cursor.next()
                                var fileName = cursorNext.get("filename").toString()
                                var uploadDate = cursorNext.get("uploadDate").toString()
                                var imageForOutput = gfsPhoto.findOne(fileName)
                                imageForOutput.writeTo("/sdcard/Pictures/$fileName")
                                var file = FileInputStream("/sdcard/Pictures/$fileName")
                                var buf = BufferedInputStream(file)
                                var bitmap = BitmapFactory.decodeStream(buf)
                                items.add(ListViewItem(bitmap, uploadDate))
                            }
                            val adapter = ListViewAdapter(items)
                            listView.adapter = adapter
                            listView.visibility = View.VISIBLE
                        }

                        textView.visibility = View.VISIBLE
                        myUri = null
                        editText1.text.clear()
                    }
                    textView.visibility = View.GONE
                    listView.visibility = View.GONE

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE

                    LineChart.visibility = View.GONE

                    button5.visibility = View.VISIBLE
                    button5.setOnClickListener {
                        binding.visibility = View.GONE
                        textView.visibility = View.GONE
                        listView.visibility = View.GONE

                        onResume()

                        preview.visibility = View.VISIBLE
                        label.visibility = View.VISIBLE
                        finger.visibility = View.VISIBLE
                    }
                    button6.visibility = View.VISIBLE
                    button6.setOnClickListener {
                        onPause()
                        preview.visibility = View.GONE
                        label.visibility = View.GONE
                        finger.visibility = View.GONE
                    }
                    preview.visibility = View.GONE
                    label.visibility = View.GONE
                    finger.visibility = View.GONE
                } else if(id == R.id.food) {
                    editText1.visibility = View.VISIBLE
                    editText1.setHint("Input Food")

                    button1.visibility = View.VISIBLE
                    button1.setOnClickListener {
                        CallCamera()
                        binding.visibility = View.GONE
                        textView.visibility = View.GONE
                        listView.visibility = View.GONE
                    }

                    button2.visibility = View.VISIBLE
                    button2.setOnClickListener {
                        GetAlbum()
                        binding.visibility = View.VISIBLE
                        textView.visibility = View.GONE
                        listView.visibility = View.GONE
                    }

                    binding.visibility = View.GONE

                    button3.visibility = View.VISIBLE
                    button3.setOnClickListener {
                        val items = mutableListOf<ListViewItem>()
                        var displayData : String = ""
                        binding.visibility = View.GONE
                        var mongoClient: MongoClient? = null
//                        mongoClient = MongoClient(ServerAddress("10.0.2.2", 27017))
//                        mongoClient = MongoClient(ServerAddress("127.0.0.1", 27017))

                        mongoClient = MongoClient("6.tcp.ngrok.io", 18800)

                        println("Connected to MongoDB!")
                        var database = mongoClient!!.getDatabase("OkKim_Food")
                        var collection = database.getCollection("Food")

                        if(!editText1.text.toString().isNullOrEmpty()) {
                            println(editText1.text.toString())
                            var document = Document("Food", editText1.text.toString())
                            collection.insertOne(document)
                        }

                        var activity = collection.find(Filters.exists("Food"))
                        activity.forEach {
                            displayData = displayData + it["Food"] + " "
                        }

                        textView.text = displayData

                        if(!myUri.isNullOrEmpty()) {
                            val imageFile = File(myUri)
                            val gfsPhoto = GridFS(mongoClient!!.getDB("OkKim_Food"))
                            val gfsFile = gfsPhoto.createFile(imageFile)
                            gfsFile.save()

                            val cursor = gfsPhoto.fileList
                            while (cursor.hasNext()) {
                                var cursorNext = cursor.next()
                                var fileName = cursorNext.get("filename").toString()
                                var uploadDate = cursorNext.get("uploadDate").toString()
                                var imageForOutput = gfsPhoto.findOne(fileName)
                                imageForOutput.writeTo("/sdcard/Pictures/$fileName")
                                var file = FileInputStream("/sdcard/Pictures/$fileName")
                                var buf = BufferedInputStream(file)
                                var bitmap = BitmapFactory.decodeStream(buf)
                                items.add(ListViewItem(bitmap, uploadDate))
                            }
                            val adapter = ListViewAdapter(items)
                            listView.adapter = adapter
                            listView.visibility = View.VISIBLE
                        }

                        if(myUri.isNullOrEmpty()) {
                            val gfsPhoto = GridFS(mongoClient!!.getDB("OkKim_Food"))
                            val cursor = gfsPhoto.fileList
                            while (cursor.hasNext()) {
                                var cursorNext = cursor.next()
                                var fileName = cursorNext.get("filename").toString()
                                var uploadDate = cursorNext.get("uploadDate").toString()
                                var imageForOutput = gfsPhoto.findOne(fileName)
                                imageForOutput.writeTo("/sdcard/Pictures/$fileName")
                                var file = FileInputStream("/sdcard/Pictures/$fileName")
                                var buf = BufferedInputStream(file)
                                var bitmap = BitmapFactory.decodeStream(buf)
                                items.add(ListViewItem(bitmap, uploadDate))
                            }
                            val adapter = ListViewAdapter(items)
                            listView.adapter = adapter
                            listView.visibility = View.VISIBLE
                        }

                        textView.visibility = View.VISIBLE
                        myUri = null
                        editText1.text.clear()
                    }
                    textView.visibility = View.GONE
                    listView.visibility = View.GONE

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE

                    LineChart.visibility = View.GONE

                    button5.visibility = View.GONE
                    button6.visibility = View.GONE
                    preview.visibility = View.GONE
                    label.visibility = View.GONE
                    finger.visibility = View.GONE
                } else if(id == R.id.data) {
                    editText1.visibility = View.GONE
                    button1.visibility = View.GONE
                    button2.visibility = View.GONE
                    binding.visibility = View.GONE
                    button3.visibility = View.GONE
                    textView.visibility = View.GONE
                    listView.visibility = View.GONE

                    button4.visibility = View.VISIBLE
                    textView1.visibility = View.GONE

                    LineChart.visibility = View.GONE
                    button4.setOnClickListener {
                        var displayData : String = ""
                        var flag = true
                        var keys = mutableListOf<String>()
                        var map = mutableMapOf<String, MutableList<Any>>()
                        var count = 0
                        assets.list("")?.forEach { path ->
                            if(path.contains("OkKim")) {
                                var minput = InputStreamReader(assets.open(path), "x-windows-949")
                                var reader = BufferedReader(minput)
                                var countRow = 0
                                println(path)
                                println(countRow)
                                reader.forEachLine { line->
                                    println(line)
//                                var line : String?
//                                while(reader.readLine().also {line = it} != null) {
                                    var row = line!!.split(",")
                                    var countCol = 0
                                    countRow++
                                    if(countRow == 3) {
                                        for(i in row) {
                                            if(flag) {
                                                keys.add(i)
                                            }
                                        }
//                                        println(keys.size)
                                        flag = false
                                    }
                                    else if(countRow > 3) {
                                        var values = mutableListOf<Any>()
                                        for(i in row) {
                                            values.add(i)
                                        }
//                                        println(values.size)
                                        var idx = 0
                                        while(idx != keys.size) {
//                                            println(keys[idx] + "\t" + values[idx])
                                            map.getOrPut(keys[idx], ::mutableListOf) += values[idx]
                                            idx++
                                        }
                                    }
                                    count++
                                }
//                                textView1.text = displayData
//                                count++ // 6
                                println(countRow)
                            }
//                            displayData = displayData + it
//                            count++ // 208
                        }
                        println(count)
                        var mongoClient: MongoClient? = null
//                        mongoClient = MongoClient(ServerAddress("10.0.2.2", 27017))
//                        mongoClient = MongoClient(ServerAddress("127.0.0.1", 27017))

                        mongoClient = MongoClient("6.tcp.ngrok.io", 18800)

                        println("Connected to MongoDB!")
                        var database = mongoClient!!.getDatabase("diabetes")
                        var collection = database.getCollection("OkKim")
//                        var document = Document("name", "Data")
                        var documents = ArrayList<Document>()
//                        documents.add(document)
                        for(i in map) {
                            var doc = Document()
                            doc[i.key] = i.value
                            documents.add(doc)
                        }
                        if(collection.find().first().isNullOrEmpty()) {
                            collection.insertMany(documents)
                        }
//                        mongoClient!!.close()
                        textView1.text = "Success"
                        textView1.visibility = View.VISIBLE

                        var timeGlucose = mutableMapOf<String, String>()
                        var tmpList1 = mutableListOf<String>()
                        var tmpList2 = mutableListOf<String>()
                        var tmpList3 = mutableListOf<String>()

                        var timeStamp = collection.find(Filters.exists("장치 타임스탬프"))
                        timeStamp.forEach {
                            println(it["장치 타임스탬프"])
                            tmpList1 = it["장치 타임스탬프"] as MutableList<String>
                        }

                        var pastGlucose = collection.find(Filters.exists("과거 혈당 mg/dL"))
                        pastGlucose.forEach {
                            println(it["과거 혈당 mg/dL"])
                            tmpList2 = it["과거 혈당 mg/dL"] as MutableList<String>
                        }

                        var scanGlucose = collection.find(Filters.exists("혈당 스캔 mg/dL"))
                        scanGlucose.forEach {
                            println(it["혈당 스캔 mg/dL"])
                            tmpList3 = it["혈당 스캔 mg/dL"] as MutableList<String>
                        }

                        var idx = 0
                        while(idx != tmpList1.size) {
                            timeGlucose[tmpList1[idx]] = tmpList2[idx]
                            idx++
                        }
                        idx = 0
                        while(idx != tmpList1.size) {
                            if(timeGlucose[tmpList1[idx]].isNullOrEmpty()) {
                                timeGlucose[tmpList1[idx]] = tmpList3[idx]
                            }
                            idx++
                        }

                        val dateTimeStrToLocalDateTime: (String) -> LocalDateTime = {
                            LocalDateTime.parse(it, DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm]" + "[yyyy-MM-dd H:mm"))
                        }

                        var t = timeGlucose.toSortedMap(
                            compareBy<String> {
                                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm]" + "[yyyy-MM-dd H:mm"))
                            }.thenBy { it }
                        )
                        println(t.keys.first())
                        println(t.values.first())
                        println(t.keys.last())
                        println(t.values.last())

//                        val list = listOf("14-10-2016 | 15:48",
//                            "01-08-2015 | 09:29",
//                            "15-11-2016 | 19:43")
//
//// You will get List<String> which is sorted in ascending order
//                        list.sortedBy(dateTimeStrToLocalDateTime)
//                        println(list.sortedBy(dateTimeStrToLocalDateTime))
//
//// You will get List<String> which is sorted in descending order
//                        list.sortedByDescending(dateTimeStrToLocalDateTime)
//                        println(list.sortedByDescending(dateTimeStrToLocalDateTime))

                        var values = ArrayList<Entry>()
                        idx = 0
                        var maxVal = -1
                        t.forEach {
                            if(!it.value.isNullOrEmpty()) {
                                values.add(Entry(idx.toFloat(), it.value.toFloat()))
                                if(it.value.toFloat() > maxVal) {
                                    maxVal = it.value.toFloat().toInt()
                                }
                                idx++
                            }
                        }

                        var xAxis = LineChart.xAxis
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.valueFormatter = IndexAxisValueFormatter(t.keys)


                        var set1 = LineDataSet(values, "Glucose")

                        var dataset = ArrayList<ILineDataSet>()
                        dataset.add(set1)

                        var data = LineData(dataset)

                        set1.setColor(Color.BLACK)
                        set1.setCircleColor(Color.BLACK)

                        var ld = LineData()
                        var ll = LimitLine(maxVal.toFloat())
                        ll.lineColor = Color.RED
                        ll.lineWidth = 4f
                        ll.label = "DANGEROUS"
                        ll.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                        ll.textColor = Color.RED
//                        LineChart.xAxis.addLimitLine(ll)
                        LineChart.axisLeft.addLimitLine(ll)

                        LineChart.setDrawBorders(true)

                        LineChart.data = data
                        LineChart.visibility = View.VISIBLE
                    }

                    button5.visibility = View.GONE
                    button6.visibility = View.GONE
                    preview.visibility = View.GONE
                    label.visibility = View.GONE
                    finger.visibility = View.GONE
                } else {
                    editText1.visibility = View.GONE
                    button1.visibility = View.GONE
                    button2.visibility = View.GONE
                    binding.visibility = View.GONE
                    button3.visibility = View.GONE
                    textView.visibility = View.GONE
                    listView.visibility = View.GONE

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE

                    LineChart.visibility = View.VISIBLE

                    button5.visibility = View.GONE
                    button6.visibility = View.GONE
                    preview.visibility = View.GONE
                    label.visibility = View.GONE
                    finger.visibility = View.GONE
                }
            }
        })

        button.setOnClickListener {
            if (menu.isExpanded()) {
                TransitionManager.beginDelayedTransition(container, ChangeBounds())
                menu.collapse()
            } else {
                TransitionManager.beginDelayedTransition(container, ChangeBounds())
                menu.expand()
            }
        }

        button.applyWindowInsets(bottom = true)

//        mongoClient!!.close()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "카메라 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            STORAGE_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "저장소 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                        //finish() 앱을 종료함
                    }
                }
            }
        }
    }

    fun checkPermission(permissions: Array<out String>, type: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, type)
                    return false;
                }
            }
        }

        return true;
    }

    fun CallCamera() {
        if (checkPermission(CAMERA, CAMERA_CODE) && checkPermission(STORAGE, STORAGE_CODE)) {
            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE)
        }
    }

    fun saveFile(fileName: String, mimeType: String, bitmap: Bitmap): Uri? {
        var CV = ContentValues()
        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CV.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CV)

        if (uri != null) {
            var scriptor = contentResolver.openFileDescriptor(uri, "w")

            if (scriptor != null) {
                val fos = FileOutputStream(scriptor.fileDescriptor)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CV.clear()
                    CV.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, CV, null, null)
                }
            }
        }

        return uri;
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        val randomFileName = RandomFileName()
                        val uri = saveFile(randomFileName, "image/jpg", img)
//                        myUri = "/sdcard/Pictures/$randomFileName.jpg"
//                        println(uri!!.path)
//                        println(myUri)
                        val cursor = contentResolver.query(uri!!, null, null, null, null)
                        cursor!!.moveToNext()
                        val path = cursor.getString(cursor.getColumnIndex("_data"))
                        cursor.close()
                        println(path)
                        myUri = path
                        binding.setImageURI(uri)
                    }
                }

                STORAGE_CODE -> {
                    val uri = data?.data
                    val cursor = contentResolver.query(uri!!, null, null, null, null)
                    cursor!!.moveToNext()
                    val path = cursor.getString(cursor.getColumnIndex("_data"))
                    cursor.close()
                    println(path)
                    myUri = path
                    binding.setImageURI(uri)
                }

//                BUTTON1 -> {
//                    val imageBitmap = data?.extras?.get("data") as Bitmap
//                    binding.setImageBitmap(imageBitmap)
//                }
//
//                BUTTON2 -> {
//                    val imageBitmap = data?.extras?.get("data") as Bitmap
//                    saveBitmapAsJPGFile(imageBitmap)
//                    binding.setImageBitmap(imageBitmap)
//                    val imageBitmap = photoUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
//                    binding.setImageBitmap(imageBitmap?.let { ImageDecoder.decodeBitmap(it) })
//                    Toast.makeText(this, photoUri?.path, Toast.LENGTH_LONG).show()
//                }

                BUTTON5 -> {
//                    val kalman = JKalman(2, 1)
//
//                    // measurement [x]
//                    val m = Matrix(1, 1)
//
//                    // transitions for x, dx
//                    val tr = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
//                    kalman.transition_matrix = Matrix(tr)
//
//                    // 1s somewhere?
//                    kalman.error_cov_post = kalman.error_cov_post.identity()
//
//                    val bpmUpdates = HeartRateOmeter()
//                        .withAverageAfterSeconds(3)
//                        .setFingerDetectionListener(this::onFingerChange)
//                        .bpmUpdates(preview)
//                        .subscribe({
//
//                            if (it.value == 0)
//                                return@subscribe
//
//                            m.set(0, 0, it.value.toDouble())
//
//                            // state [x, dx]
//                            val s = kalman.Predict()
//
//                            // corrected state [x, dx]
//                            val c = kalman.Correct(m)
//
//                            val bpm = it.copy(value = c.get(0, 0).toInt())
//                            Log.v("HeartRateOmeter", "[onBpm] ${it.value} => ${bpm.value}")
//                            onBpm(bpm)
//                        }, Throwable::printStackTrace)
//
//                    subscription?.add(bpmUpdates)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onBpm(bpm: HeartRateOmeter.Bpm) {
        // Log.v("HeartRateOmeter", "[onBpm] $bpm")
        label.text = "$bpm bpm"
    }

    private fun onFingerChange(fingerDetected: Boolean){
        finger.text = "$fingerDetected"
    }

    private fun startHeartRate() {
        checkPermission(CAMERA, CAMERA_CODE)
        val kalman = JKalman(2, 1)

        // measurement [x]
        val m = Matrix(1, 1)

        // transitions for x, dx
        val tr = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
        kalman.transition_matrix = Matrix(tr)

        // 1s somewhere?
        kalman.error_cov_post = kalman.error_cov_post.identity()

        val bpmUpdates = HeartRateOmeter()
            .withAverageAfterSeconds(3)
            .setFingerDetectionListener(this@VerticalModeActivity::onFingerChange)
            .bpmUpdates(preview)
            .subscribe({

                if (it.value == 0)
                    return@subscribe

                m.set(0, 0, it.value.toDouble())

                // state [x, dx]
                val s = kalman.Predict()

                // corrected state [x, dx]
                val c = kalman.Correct(m)

                val bpm = it.copy(value = c.get(0, 0).toInt())
                Log.v("HeartRateOmeter", "[onBpm] ${it.value} => ${bpm.value}")
                onBpm(bpm)
            }, Throwable::printStackTrace)

        subscription?.add(bpmUpdates)
    }

    override fun onResume() {
        super.onResume()
        dispose()
        subscription = CompositeDisposable()
        startHeartRate()
    }

    override fun onPause() {
        dispose()
        super.onPause()
    }

    private fun dispose() {
        if (subscription?.isDisposed == false)
            subscription?.dispose()
    }

    private fun RandomFileName() : String {
        val fineName = SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis())
//        val fineName = SimpleDateFormat("yyyy-MM-dd HH:mm").format(System.currentTimeMillis())
        return fineName
    }

    private fun GetAlbum() {
        if (checkPermission(STORAGE, STORAGE_CODE)) {
            val itt = Intent(Intent.ACTION_PICK)
            itt.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(itt, STORAGE_CODE)
        }
    }

    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    private fun saveBitmapAsJPGFile(bitmap: Bitmap) {
        val path = File(filesDir, "image")
        if(!path.exists()){
            path.mkdirs()
        }
        val file = File(path, newJpgFileName())
        var imageFile: OutputStream? = null
        try{
            file.createNewFile()
            imageFile = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageFile)
            imageFile.close()
            Toast.makeText(this, file.absolutePath, Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            null
        }
    }

    class ListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
        override fun getCount(): Int = items.size

        override fun getItem(position: Int): ListViewItem = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
            var convertView = view
            if (convertView == null) convertView = LayoutInflater.from(parent?.context).inflate(R.layout.item, parent, false)
            val item: ListViewItem = items[position]
            convertView!!.binding2.setImageBitmap(item.icon)
            convertView.tv_title.text = item.title

            return convertView
        }
    }


    data class ListViewItem(val icon: Bitmap, val title: String)
}