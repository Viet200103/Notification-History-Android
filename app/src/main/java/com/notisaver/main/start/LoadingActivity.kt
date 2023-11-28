package com.notisaver.main.start

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.notisaver.R
import com.notisaver.main.activities.FullScreenActivity
import com.notisaver.main.activities.MainActivity
import com.notisaver.misc.asNotisaveApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingActivity : FullScreenActivity() {
    private val notisaveApplication by lazy {
        asNotisaveApplication()
    }

    private val defaultPrefers by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val ioDispatcher = Dispatchers.IO

    private lateinit var contentLoadingContainerLayout: LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        lifecycleScope.launch {
            startMainActivity()
        }

        contentLoadingContainerLayout = findViewById(R.id.activity_loading_data_content_loading_container_layout)
        contentLoadingContainerLayout.isVisible = true

    }

    private suspend fun startMainActivity() = withContext(Dispatchers.IO) {
        startActivity(
            Intent(this@LoadingActivity, MainActivity::class.java)
        )
        finish()
    }
}