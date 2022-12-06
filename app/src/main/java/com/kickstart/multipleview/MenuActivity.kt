package com.kickstart.multipleview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.provider.ContactsContract.Data
import android.widget.SeekBar
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.activity_menu.view.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class MenuActivity : AppCompatActivity() {
//    var numDriver : Int = 0
    var isrunning = false
    var randomLevel: Int = 0
    var speedRandomLevel: Int = 0
    var currSpeed = 0
//    val threshHoldScale = 5.0
//    val threshHoldStableCnt = 10
//    var prevScale = 0.0
//    var currScale = 0.0
//    var stableCnt = 10
//    var consecutiveUnstable = 0
//    var consecutiveStable = 0
//    var currentWeight = 0
//    var prevWeight = 0
//    var oneStep = 0
//    var twoStep = 0
    var stopped = false
    var helmetOn = false
    var scaleManager: ScaleManager = ScaleManager()
    var startStatus = false
    var isMoving = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        backButton.setOnClickListener {
            stopped = true

            finish()
        }

//        incButton.setOnClickListener {
//            //numDriver++
//            randomLevel += 10
//            speedRandomLevel += 5
//            //driverNumText.text = "$numDriver 명"
//        }
//
//        decButton.setOnClickListener {
//            //numDriver--
//            randomLevel -= 10
//            speedRandomLevel -= 5
//
//            //driverNumText.text = "$numDriver 명"
//        }

        startButton.setOnClickListener {
            if (startStatus == true) {
                isMoving = true
                speedRandomLevel = 0
                startButton.text = "운전 중"
            }
        }
        //setLineChartData()
        //setLineChart2()

        speedBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                currSpeed = p1

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        if (!isrunning) {
            isrunning = true
            var scaleThread = this.ScaleThread()
            scaleThread.start()
            var helmetThread = this.HelmetThread()
            helmetThread.start()
            var startButtonThread = this.StartButtonThread()
            startButtonThread.start()
        }
    }

    inner class StartButtonThread: Thread() {
        override fun run () {
            while (!stopped) {
                SystemClock.sleep (1000)
                if (startStatus == false) {
                    if (scaleManager.numDriver != 2 && helmetOn) {
                        startStatus = true
                        runOnUiThread {
                            startButton.text = "시동"
                            startButton.setBackgroundColor(R.color.black.toInt())
                            startButton.isEnabled = true
                            //speedBar.progress = 0
                        }
                    }
                }
                else {

                    if (scaleManager.numDriver == 2 || helmetOn == false) {
                        isMoving = false
                        startStatus = false
                        runOnUiThread {
                            startButton.text = "시동 불가"
                            startButton.setTextColor(Color.WHITE)
                            startButton.setBackgroundColor(Color.parseColor("#F95454"))
                            startButton.isEnabled = false
                        }
                    }
                    if (scaleManager.numDriver == 2) {
                        runOnUiThread {
                            editTextTextPersonName5.setBackgroundResource(R.drawable.rect4)
                            imageView2.setBackgroundResource(R.drawable.blackcircle)
                            window.statusBarColor = 4278190080.toInt()
                        }
                    }
                }


            }
        }
    }
    inner class HelmetThread: Thread () {

        override fun run() {
            var socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2

            var helmetStats = 1
            while (!stopped) {
                SystemClock.sleep(500)

                try {
                    helmetStats = readHelmetUDP(socket)
                } catch (e: SocketTimeoutException) {
                    socket = DatagramSocket()

                }


                //val helmetStats = readHelmetInfo()
                //val helmetStats = readHelmetInfoRandom()
                if (helmetStats == 1) {
                    runOnUiThread {
                        helmetText.text = "미착용"
                        editTextTextPersonName5.setBackgroundResource(R.drawable.rect2)
                        imageView2.setBackgroundResource(R.drawable.yellowcircle)
                        window.statusBarColor = 4294955016.toInt()

                    }
                    helmetOn = false
                }
                else if (helmetStats == 2) {
                    runOnUiThread {
                        helmetText.text = "오착용"
                        editTextTextPersonName5.setBackgroundResource(R.drawable.rect2)
                        imageView2.setBackgroundResource(R.drawable.yellowcircle)
                        window.statusBarColor = 4294955016.toInt()

                    }
                    helmetOn = false
                }
                else {

                    runOnUiThread {
                        if (helmetText.text != "착용")
                            editTextTextPersonName5.setBackgroundResource(R.drawable.rect1)
                            imageView2.setBackgroundResource(R.drawable.greencircle)
                            window.statusBarColor = 4290570965.toInt()

                        helmetText.text = "착용"
                    }
                    helmetOn = true
                }

                var speed = readSpeedRandom()
                if (isMoving == true) {
                    runOnUiThread {
                        speedText.text = "속도\n${speed} km/h"
                    }
                    if (speed >= 30) {
                        runOnUiThread {
                            editTextTextPersonName5.setBackgroundResource(R.drawable.rect3)
                            imageView2.setBackgroundResource(R.drawable.redcircle)
                            window.statusBarColor = 4294530132.toInt()
                        }
                    }
                    else {
                        runOnUiThread {
                            editTextTextPersonName5.setBackgroundResource(R.drawable.rect1)
                            imageView2.setBackgroundResource(R.drawable.greencircle)
                            window.statusBarColor = 4290570965.toInt()
                        }
                    }
                }
//                else {
//                    speed = 0
//                    runOnUiThread {
//                        speedText.text = "속도\n${speed} km/h"
//                        editTextTextPersonName5.setBackgroundResource(R.drawable.rect1)
//                    }
//                }
            }

        }
        fun readSpeedRandom () : Int {
            /*
            var speedRandom:Double = Math.random() * 5 + speedRandomLevel
            if (speedRandom >= 50)
                speedRandom = 50.0
            return speedRandom.toInt()

             */
            return currSpeed
        }
        fun readHelmetInfoRandom (): Int {
            val helmet_stats:Double = Math.random () * 3 + 1
            //return helmet_stats.toInt()
            return 3
        }
        fun readHelmetInfo(): Int {
            try {
                val url = URL("http://192.168.0.49")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                var lines = ""
                BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                    var line: String?

                    while (br.readLine().also { line = it } != null) {

                        //println(line)
                        lines += line
                    }

                }
                val mapJson = lines
                val jsonObject: JsonObject = JsonParser().parse(mapJson).asJsonObject
                val resultMap = Gson().fromJson<HashMap<String, Double>>(
                    jsonObject.toString(),
                    HashMap::class.java
                )

                println("--------------> " + resultMap)
                println("--------------> " + resultMap.keys)

                var stats_val: Double? = resultMap.get("helmet_stats")
                if (stats_val == null)
                    return 1
                else
                    return stats_val.toInt()
            } catch (e:java.lang.Exception) {
                return 1
            }

        }
        fun readHelmetUDP(socket: DatagramSocket): Int {
            // Hack Prevent crash (sending should be done using an async task)
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()

            val RemoteHost = "192.168.100.7"
            val RemotePort = 2391

            StrictMode.setThreadPolicy(policy)
            try {
                //Open a port to send the package

                val sendData = "Hello".toByteArray()
                val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(RemoteHost), RemotePort)

                socket.send(sendPacket)
                println("fun sendBroadcast: packet sent to: " + InetAddress.getByName(RemoteHost) + ":" + RemotePort)

                val Buffer = ByteArray(1024)
                val packet = DatagramPacket(Buffer, Buffer.size)
                socket.receive(packet)
                var data = packet.data.toString(StandardCharsets.UTF_8)

                val slashIndex = data.indexOf("}")
                data = data.substring(0, slashIndex+1)
                println("----> $data")

                val gson = GsonBuilder().setLenient().create()
                val resultMap = gson.fromJson<HashMap<String, Double>>(data, HashMap::class.java)

                val stats_val: Double? = resultMap.get("helmet_stats")
                if (stats_val == null)
                    return 1
                else
                    return stats_val.toInt()


            } catch (e: IOException) {
                //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
                return 1
            }
        }
    }


    inner class ScaleThread: Thread () {
        fun readScaleRandom (): Double {
            val randNum = Math.random() * 10 + randomLevel
            return randNum
        }

        var lastScale1 = 0.0;
        var lastScale2 = 0.0;


        fun readScaleUDP(socket: DatagramSocket): Double {
            // Hack Prevent crash (sending should be done using an async task)
//            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//
//            StrictMode.setThreadPolicy(policy)
            try {
                //Open a port to send the package

                val RemoteHost = "192.168.100.9"    // ScaleSensor Target IP
                val RemotePort = 2390               // ScaleSensor Target Port

                val sendData = "Hello".toByteArray()
                val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(RemoteHost), RemotePort)

                socket.send(sendPacket)
                println("fun sendBroadcast: packet sent to: " + InetAddress.getByName(RemoteHost) + ":" + RemotePort)

                val Buffer = ByteArray(1024)
                val packet = DatagramPacket(Buffer, Buffer.size)
                socket.receive(packet)
                var data = packet.data.toString(StandardCharsets.UTF_8)

                val slashIndex = data.indexOf("}")
                data = data.substring(0, slashIndex+1)
                println("----> $data")

                val gson = GsonBuilder().setLenient().create()
                val resultMap = gson.fromJson<HashMap<String, Double>>(data, HashMap::class.java)

                var scale1: Double? = resultMap.get("scale1")
                var scale2: Double? = resultMap.get("scale2")
//                if (scale1 == null)
//                    scale1 = 0.0
//                if (scale2 == null)
//                    scale2 = 0.0
                if (scale1 == null) {
                    scale1 = lastScale1
                }
                else if (scale1 != 0.0) {
                    lastScale1 = scale1
                }

                if (scale2 == null) {
                    scale2 = lastScale2
                }
                else if (scale1 != 0.0) {
                    lastScale2 = scale2
                }

                println("scale = $scale1 $scale2 from Device")

                return scale1 + scale2


            } catch (e: IOException) {
                println(e.message)
                return lastScale1 + lastScale2
            }
        }


        fun readScale(): Double {

//            val randNum = Math.random() * 10 + randomLevel
//
//            return randNum

            val url = URL("http://192.168.0.43")

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            var lines = ""
            try {
                BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                    var line: String?

                    while (br.readLine().also { line = it } != null) {

                        //println(line)
                        lines += line
                    }

                }
                val mapJson = lines
                val jsonObject: JsonObject = JsonParser().parse(mapJson).asJsonObject
                val resultMap = Gson().fromJson<HashMap<String, Double>>(
                    jsonObject.toString(),
                    HashMap::class.java
                )

//            println(resultMap)
//            println(resultMap.keys)

                var scale2: Double? = resultMap.get("scale2")
                var scale1: Double? = resultMap.get("scale1")
                if (scale1 == null)
                    scale1 = 0.0
                if (scale2 == null)
                    scale2 = 0.0
                println("scale = $scale1 $scale2 from Device")
                return scale1 + scale2
            }
            catch(e: java.lang.Exception) {
                return 0.0
            }
        }

        override fun run() {
            var entries: ArrayList<Entry> = ArrayList<Entry>()
            entries.add(Entry(0f, 0f))

            var dataset: LineDataSet = LineDataSet(entries, "input")
            dataset.apply {
                setDrawCircles(false)
                lineWidth = 3f
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                //mode = LineDataSet.Mode.LINEAR
                color = R.color.black
            }
            var data = LineData(dataset)

            lineChart.data = data
            lineChart.getXAxis().setDrawGridLines(false);
            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getAxisRight().setDrawGridLines(false);
            runOnUiThread {
                lineChart.animateX(100)
            }

            var socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2

            var scale = 0.0

            var i = 0
            while (!stopped) {
                SystemClock.sleep(250)
                try {
                    scale = readScaleUDP(socket)


                } catch (e: SocketTimeoutException) {
                    socket = DatagramSocket()

                }
                //readScale() //readScaleRandom()
                scaleManager.addScale(scale)

                if (scaleManager.isStable()) {
                    runOnUiThread {
                        scaleText.setTextColor(resources.getColor(R.color.black))
                        scaleText.text = "${scaleManager.currScale.toInt()} kg" ////////////////////몸무게
                        driverNumText.text = "${scaleManager.numDriver} 명" /////////////////////탑승인원
                    }
                    if (scaleManager.numDriver == 0) {
                        isMoving = false
                        runOnUiThread {
                            startButton.text = "시동"
                            startButton.setBackgroundColor(R.color.black.toInt())
                            startButton.isEnabled = true
                        }
                    }
                } else {
                    runOnUiThread {
                        scaleText.setTextColor(resources.getColor(R.color.purple_500))
                        scaleText.text = "불안정" ////////////////////몸무게
                        driverNumText.text = "--"/////////////////////탑승인원
                    }
                }
                runOnUiThread {
                    //scaleText.text = "${currScale.toInt()} kg"
//                    stableCountText.text = "${scaleManager.stableCnt}"
                }


                data.addEntry(Entry(i.toFloat(), scale.toFloat()), 0)
                i++

                data.notifyDataChanged()
                lineChart.notifyDataSetChanged()


                //lineChart.invalidate()

                lineChart.setVisibleXRangeMaximum(30f)
                lineChart.moveViewTo((data.entryCount).toFloat(), 100f, YAxis.AxisDependency.LEFT)
            }
            isrunning = false
        }
    }
/*
        fun run2 () {
            //val input = Array<Double>(100,{Math.random()})
            val input = ArrayList<Double>();

            var entries: ArrayList<Entry> = ArrayList<Entry>()
            entries.add(Entry(0f,0f))

            var dataset:LineDataSet = LineDataSet(entries, "input")
            //dataset.setColor(R.color.black,1)
            dataset.apply {
                setDrawCircles(false)
                lineWidth = 3f
                //mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                mode = LineDataSet.Mode.LINEAR
                color = R.color.black
            }
            var data = LineData(dataset)

            lineChart.data = data
            runOnUiThread {
                lineChart.animateX(100)
            }

            //for (i in 0 until input.size) {
            for (i in 0 until 600) {
                SystemClock.sleep(200)
                val randNum = Math.random() * 10 + randomLevel
                //data.addEntry(Entry(i.toFloat(), input[i].toFloat()), 0)

                currScale = randNum
                val diff = currScale - prevScale
                if (diff >= -threshHoldScale && diff <= threshHoldScale) {
                    stableCnt ++
                    consecutiveUnstable --
                    if (consecutiveUnstable < 0)
                        consecutiveUnstable = 0
                    consecutiveStable ++
                    if (consecutiveStable > 5)
                        consecutiveStable = 5
                }
                else {
                    stableCnt--
                    consecutiveUnstable ++
                    if (consecutiveUnstable > 3)
                        consecutiveUnstable = 3
                    consecutiveStable --
                    if (consecutiveStable < 0 )
                        consecutiveStable = 0
                }

                //stableCountText.text = "$stableCnt"
                if (stableCnt >= threshHoldStableCnt || consecutiveStable >= 5) {
                    stableCnt = threshHoldStableCnt
                    //println ("안정한 값 " + currScale)
                    //scaleText.setBackgroundResource(R.color.white)

                    if (currScale < 10)
                        numDriver = 0
                    else
                        numDriver = 1

                    runOnUiThread {
                        scaleText.setTextColor(resources.getColor(R.color.black))
                        scaleText.text = "${currScale.toInt()} kg"
                        driverNumText.text = "${numDriver} 명"
                    }
                }
                // 3회 연속 unstable 탐지
                if (stableCnt <= threshHoldStableCnt / 3 || consecutiveUnstable >= 3) {
                    //println ("불안정한 값")
//                    scaleText.setTextColor(resources.getColor(R.color.purple_500))
                    runOnUiThread {
                        scaleText.setTextColor(resources.getColor(R.color.purple_500))
                        scaleText.text = "불안정"
                        driverNumText.text = "--"
                    }
                }

                if (stableCnt < 0)
                    stableCnt = 0


                runOnUiThread {
                    //scaleText.text = "${currScale.toInt()} kg"
                    stableCountText.text = "$stableCnt"
                }


                println ("currScale = ${currScale} stableCnt = $stableCnt")
                prevScale = currScale
                //stableCountText.text = "$stableCnt"

                input.add(randNum)
                data.addEntry(Entry(i.toFloat(), randNum.toFloat()), 0)

                data.notifyDataChanged()
                lineChart.notifyDataSetChanged()
                //lineChart.invalidate()

                lineChart.setVisibleXRangeMaximum(30f)
                lineChart.moveViewTo((data.entryCount).toFloat(), 100f, YAxis.AxisDependency.LEFT)
            }
            isrunning = false
        }
    }
    */


    fun setLineChart2 () {
        lineChart.setDrawGridBackground(true)
        lineChart.setBackgroundColor(Color.BLACK)
        //lineChart.setDrawGridBackground(R.color.black)

        lineChart.xAxis.setDrawGridLines(true)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.xAxis.isEnabled = true
        lineChart.xAxis.setDrawGridLines(false)

        lineChart.axisLeft.textColor = R.color.purple_200
        lineChart.axisLeft.setDrawGridLines(true)

        lineChart.invalidate()

    }

//    fun setLineChartData () {
//        val xvalue = ArrayList<String> ()
//        xvalue.add ("1")
//        xvalue.add ("2")
//        xvalue.add ("3")
//        xvalue.add ("4")
//
//        val lineentry = ArrayList<Entry> ()
//        lineentry.add (Entry(20.0f, 0))
//        lineentry.add (Entry(25.0f, 1))
//        lineentry.add (Entry(22f, 2))
//        lineentry.add (Entry(24f, 3))
//
//        val linedataset = LineDataSet (lineentry, "First")
//        linedataset.color = R.color.black
//
//        val data = LineData (xvalue, linedataset)
//
//        lineChart.data = data
//        //lineChart.setBackgroundColor(R.color.white)
//        lineChart.animateXY(3000, 3000)
//    }

}
