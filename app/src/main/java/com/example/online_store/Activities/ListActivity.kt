package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        val ivBanano : ImageView = findViewById(R.id.iv_banano)
        ivBanano.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val ivPina : ImageView = findViewById(R.id.iv_pina)
        ivPina.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val ivNaranja : ImageView = findViewById(R.id.iv_naranja)
        ivNaranja.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val ivLimon : ImageView = findViewById(R.id.iv_limon)
        ivLimon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

}


