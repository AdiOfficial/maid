package com.hugocastelani.maid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.snatik.storage.Storage
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.os.Build
import android.provider.Settings


class MainActivity : AppCompatActivity() {
    private lateinit var storage: Storage
    private lateinit var directory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = Storage(applicationContext)
        directory = "${storage.externalStorageDirectory}/WhatsApp/Media/WhatsApp Video/Sent"

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
        if (hasPermission()) {
            storage.deleteDirectory(directory)

            Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(R.string.all_clean),
                5000
            ).show()

        } else {

            // the snackbar below is similar to SnackbarOnDeniedPermissionListener, so
            // the setAction callback code was taken from withOpenSettingsButton method

            Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(R.string.without_permission),
                8000
            ).setAction(
                resources.getString(R.string.settings),
                {
                    val context = view.context
                    val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.packageName))
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
                    myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(myAppSettings)
                }
            ).show()
        }
    }

    private fun hasPermission(): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return ActivityCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    // calligraphy library need
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}
