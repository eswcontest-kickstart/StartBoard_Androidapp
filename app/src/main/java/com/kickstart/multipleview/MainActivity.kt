package com.kickstart.multipleview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        showButton.setOnClickListener {
            val showIntent = Intent(this, MenuActivity::class.java)
            startActivityForResult(showIntent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            101 -> {
                /*showToast ("응답 받았음 $requestCode, $resultCode")*/
            }
        }
    }

    fun showToast(message:String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}