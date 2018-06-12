package com.hugocastelani.maid

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.snatik.storage.Storage

class MainActivity : AppCompatActivity() {
    private lateinit var storage: Storage
    private lateinit var directory: String
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = Storage(applicationContext)
        directory = "${storage.externalStorageDirectory}/WhatsApp/Media/WhatsApp Video/Sent"
        snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(R.string.all_clean),
                5000)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(getPermissionListener())
            .check()
    }

    private fun getPermissionListener(): PermissionListener {
        return SnackbarOnDeniedPermissionListener.Builder
            .with(findViewById(android.R.id.content), resources.getString(R.string.permission_required))
            .withOpenSettingsButton(resources.getString(R.string.settings))
            .withDuration(8000)
            .build()
    }

    fun cleanIt(view: View) {
        storage.deleteDirectory(directory)
        snackbar.show()
    }

    // calligraphy library need
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
