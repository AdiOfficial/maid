package com.hugocastelani.maid

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.karumi.dexter.Dexter
import com.snatik.storage.Storage
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.provider.Settings
import com.ajts.androidmads.fontutils.FontUtils
import com.chrisplus.rootmanager.RootManager
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.DataOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var storage: Storage
    private lateinit var directory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = Storage(applicationContext)
        directory = "${storage.externalStorageDirectory}/WhatsApp/Media/WhatsApp Video/Sent"

        // Applying Custom Font
        val typeface = Typeface.createFromAsset(assets, "fonts/Google-Sans-Bold.ttf")
        FontUtils().applyFontToView(toolbar_title, typeface)

        checkRoot()

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.KILL_BACKGROUND_PROCESSES
            ).withListener(getPermissionListener())
            .check()
    }

    private fun checkRoot() {
        if (RootManager.getInstance().hasRooted()) {
            if (!RootManager.getInstance().obtainPermission()) {
                showSnackbarWithTryAgain(R.string.without_superuser_permission, ::checkRoot)
            }
        } else {
            showSnackbarWithTryAgain(R.string.has_no_root, ::checkRoot)
        }
    }

    private fun getPermissionListener(): MultiplePermissionsListener {
        return SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
            .with(findViewById(android.R.id.content), resources.getString(R.string.permission_required))
            .withOpenSettingsButton(resources.getString(R.string.settings))
            .withDuration(8000)
            .build()
    }

    fun cleanIt(view: View) {
        if (hasWritingPermission()) {
            storage.deleteDirectory(directory)
            showSuccessSnackbar(R.string.all_clean)
        } else {
            showSnackbarWithSettingsRedirection()
        }
    }

    fun unbugWhatsApp(view: View) {
        if (hasKillingPermission()) {
            if (RootManager.getInstance().obtainPermission()) {
                val suProcess = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(suProcess.outputStream)

                os.writeBytes("adb shell\n")
                os.flush()
                os.writeBytes("am force-stop com.whatsapp\n")
                os.flush()

                showSuccessSnackbar(R.string.whatsapp_finished)
            } else {
                showSnackbarWithTryAgain(R.string.without_superuser_permission) { unbugWhatsApp(view) }
            }
        } else {
            showSnackbarWithSettingsRedirection()
        }
    }

    private fun hasWritingPermission(): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return ActivityCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasKillingPermission(): Boolean {
        val permission = Manifest.permission.KILL_BACKGROUND_PROCESSES
        return ActivityCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun showSuccessSnackbar(resource: Int) {
        Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(resource),
                3000
        ).show()
    }

    // the snackbar shown is similar to SnackbarOnDeniedPermissionListener, so
    // the setAction callback code was taken from withOpenSettingsButton method

    private fun showSnackbarWithSettingsRedirection() {
        Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(R.string.without_permission),
                5000
        ).setAction(resources.getString(R.string.settings)) {
            val context = baseContext
            val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.packageName))
            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
            myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(myAppSettings)
        }.show()
    }

    private fun showSnackbarWithTryAgain(resource: Int, tryAgainFunction: () -> Unit) {
        Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(resource),
                5000
        ).setAction(resources.getString(R.string.try_again)) {
            tryAgainFunction()
        }.show()
    }
}
