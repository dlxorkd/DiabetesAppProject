package com.ismaeldivita.chipnavigation.sample

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor.open
import android.provider.MediaStore
import android.system.Os.open
import android.text.Editable
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.ismaeldivita.chipnavigation.sample.util.applyWindowInsets
import com.ismaeldivita.chipnavigation.sample.util.colorAnimation
import com.mongodb.MongoClient
import com.mongodb.MongoException
import kotlinx.android.synthetic.main.activity_vertical.*
import kotlinx.android.synthetic.main.activity_vertical.view.*
import org.bson.Document
import java.io.*
import java.nio.channels.AsynchronousFileChannel.open
import java.nio.channels.AsynchronousServerSocketChannel.open
import java.nio.channels.FileChannel.open
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.*
import java.util.Date.from

val CAMERA = arrayOf(Manifest.permission.CAMERA)
val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
val CAMERA_CODE = 98
val STORAGE_CODE = 99

class VerticalModeActivity : AppCompatActivity() {
    private val container by lazy { findViewById<ViewGroup>(R.id.container) }
    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val button by lazy { findViewById<ImageView>(R.id.expand_button) }
    private val menu by lazy { findViewById<ChipNavigationBar>(R.id.bottom_menu) }

    private val editText1 by lazy { findViewById<EditText>(R.id.editText1) }
    private val button1 by lazy { findViewById<Button>(R.id.button1) }
    private val button2 by lazy { findViewById<Button>(R.id.button2) }
    private val binding by lazy { findViewById<ImageView>(R.id.binding) }
    private val button3 by lazy { findViewById<Button>(R.id.button3) }

    private val button4 by lazy { findViewById<Button>(R.id.button4) }
    private val textView1 by lazy { findViewById<TextView>(R.id.textView1) }

    private var lastColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical)

        editText1.visibility = View.GONE
        button1.visibility = View.GONE
        button2.visibility = View.GONE
        binding.visibility = View.GONE
        button3.visibility = View.GONE

        button4.visibility = View.GONE
        textView1.visibility = View.GONE

        lastColor = ContextCompat.getColor(this, R.color.blank)


        var mongoClient: MongoClient? = null
        try {
            mongoClient = MongoClient("localhost", 27017)
            println("Connected to MongoDB!")

//            var tmp = mongoClient.listDatabaseNames()
//            for(i in tmp) {
//                println(i)
//            }

            var database = mongoClient.getDatabase("diabetes")
            var collection = database.getCollection("test")

            var document = Document("name", "Activity")
                .append("contact", Document("phone", "228-555-0149")
                .append("email", "cafeconleche@example.com")
                .append("location", listOf(-73.92502, 40.8279556)))
                .append("stars", 3)
                .append("categories", listOf("Bakery", "Coffee", "Pastries"));
            collection.insertOne(document)
        } catch (e: MongoException) {
            e.printStackTrace()
        }



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
                        binding.visibility = View.GONE
                    }

                    button2.visibility = View.VISIBLE
                    button2.setOnClickListener {
                        GetAlbum()
                        binding.visibility = View.VISIBLE
                    }

                    binding.visibility = View.GONE

                    button3.visibility = View.VISIBLE
                    button3.setOnClickListener {

                    }

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE
                } else if(id == R.id.food) {
                    editText1.visibility = View.VISIBLE
                    editText1.setHint("Input Food")

                    button1.visibility = View.VISIBLE
                    button1.setOnClickListener {
                        CallCamera()
                        binding.visibility = View.GONE
                    }

                    button2.visibility = View.VISIBLE
                    button2.setOnClickListener {
                        GetAlbum()
                        binding.visibility = View.VISIBLE
                    }

                    binding.visibility = View.GONE

                    button3.visibility = View.VISIBLE
                    button3.setOnClickListener {

                    }

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE
                } else if(id == R.id.data) {
                    editText1.visibility = View.GONE
                    button1.visibility = View.GONE
                    button2.visibility = View.GONE
                    binding.visibility = View.GONE
                    button3.visibility = View.GONE
                    button4.visibility = View.VISIBLE
                    textView1.visibility = View.GONE
                    button4.setOnClickListener {
                        var displayData : String = ""
                        assets.list("")?.forEach {
                            if(it.contains("OkKim")) {
                                var minput = InputStreamReader(assets.open(it), "x-windows-949")
                                var reader = BufferedReader(minput)
                                var countRow = 0
                                val map: MutableMap<String, Any?> = mutableMapOf()
                                reader.forEachLine {
                                    var row = it.split(",")
                                    var countCol = 0
//                                    countRow++
//                                    if(countRow == 3) {
//                                        for(i in row) {
//                                            map[i] = null
//                                        }
//                                    }
//                                    if(countRow > 3) {
//                                        var tmp = map.keys.toTypedArray()
//                                        for(i in row) {
//                                            if(i == "") {
//                                                map[tmp[countCol]] = null
//                                            } else {
//                                                map[tmp[countCol]] = i
//                                            }
//                                            countCol++
//                                        }
//                                    }




                                    for(i in row) {
                                        countCol++
                                        if(countCol == 5 || countCol == 6) {
                                            if(i.toIntOrNull() != null) {
                                                displayData = displayData + i + ", "
                                            }
                                        }
                                    }
                                }
                                textView1.text = displayData
                            }
//                            displayData = displayData + it
                        }
//                        textView1.text = displayData
                        textView1.visibility = View.VISIBLE


//                        val minput = InputStreamReader(assets.open("OkKim_glucose_2020-10-16.csv"), "x-windows-949")
//                        val reader = BufferedReader(minput)
//
//                        var line : String?
//                        var displayData : String = ""
//
//                        while(reader.readLine().also {line = it} != null) {
//                            val row = line!!.split(",")
//                            displayData = displayData + row + "\n"
//                        }
//                        textView1.text = displayData
//                        textView1.visibility = View.VISIBLE
                    }
                } else {
                    editText1.visibility = View.GONE
                    button1.visibility = View.GONE
                    button2.visibility = View.GONE
                    binding.visibility = View.GONE
                    button3.visibility = View.GONE

                    button4.visibility = View.GONE
                    textView1.visibility = View.GONE
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

        mongoClient!!.close()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        val uri = saveFile(RandomFileName(), "image/jpg", img)
                        binding.setImageURI(uri)
                    }
                }

                STORAGE_CODE -> {
                    val uri = data?.data
                    binding.setImageURI(uri)
                }
            }
        }
    }

    fun RandomFileName() : String {
        val fineName = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        return fineName
    }

    fun GetAlbum() {
        if (checkPermission(STORAGE, STORAGE_CODE)) {
            val itt = Intent(Intent.ACTION_PICK)
            itt.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(itt, STORAGE_CODE)
        }
    }
}
