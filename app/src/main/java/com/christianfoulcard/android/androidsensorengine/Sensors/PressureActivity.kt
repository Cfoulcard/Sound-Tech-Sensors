package com.christianfoulcard.android.androidsensorengine.Sensors

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.christianfoulcard.android.androidsensorengine.Preferences.SettingsActivity
import com.christianfoulcard.android.androidsensorengine.R
import com.christianfoulcard.android.androidsensorengine.Sensors.PressureActivity
import com.christianfoulcard.android.androidsensorengine.UploadWorker
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import okhttp3.OkHttpClient
import java.io.File

class PressureActivity : AppCompatActivity(), SensorEventListener {

    //Dialog popup info
    private var pressureInfoDialog: Dialog? = null

    //TextViews
    private var pressure_text: TextView? = null
    private var currentPressure: TextView? = null
    private var pressureLevel: TextView? = null

    //ImageViews
    private var pressureInfo: ImageView? = null
    private var pressureLogo: ImageView? = null

    //Sensor initiation
    private var sensorManager: SensorManager? = null
    private var pressure: Sensor? = null
    private var mContext: Context? = null
    private var mActivity: Activity? = null

    //Get the preference settings
    private val mSharedPreferences: SharedPreferences? = null

    // Initiate Firebase Analytics
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    //For Ads
    private lateinit var mAdView : AdView

/*    override fun startService(service: Intent?): ComponentName? {
        return super.startService(service)
    }*/



    ////////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeSensors)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pressure_sensor)

        // Initialize Ads
        MobileAds.initialize(this) {} //ADMOB App ID
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //TextViews
        pressure_text = findViewById<View>(R.id.pressure) as TextView
        currentPressure = findViewById<View>(R.id.current_pressure) as TextView
        pressureLevel = findViewById<View>(R.id.pressure_sensor) as TextView

        //ImageViews
        pressureInfo = findViewById<View>(R.id.info_button) as ImageView
        pressureLogo = findViewById<View>(R.id.pressure_logo) as ImageView

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //Opens Pin Shortcut menu after long pressing the logo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pressureLogo!!.setOnLongClickListener() {
                sensorShortcut()
            }
        }

        // Get an instance of the sensor service, and use that to get an instance of
        // the relative temperature. If device does not support this sensor a toast message will
        // appear
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        assert(sensorManager != null)
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            Toast.makeText(this, R.string.unsupported_sensor, Toast.LENGTH_LONG).show()
        }

        // Ambient Temperature measures the temperature around the device
        pressure = sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)
        currentPressure = findViewById<View>(R.id.current_pressure) as TextView
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onSensorChanged(event: SensorEvent) {

        // Get the application context
        mContext = applicationContext

        // Get the activity
        mActivity = this@PressureActivity

        // Get the instance of SharedPreferences object
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val pressure_level = event.values[0].toInt()

        if (event.sensor.type == Sensor.TYPE_PRESSURE) {
            currentPressure!!.text = "$pressure_level hPa"
        }

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, PressureActivity::class.java)

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)



        // Get the PendingIntent containing the entire back stack
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        //Checks to see if the pressure alert notifications are turned on in root_preferences.xml
        if (settings.getBoolean("switch_preference_pressure", true)) {
            //Gets the string value from the edit_text_pressure key in root_preferences.xml
            val pressureNumber = settings.getString("edit_text_pressure", "")

            //Conditions that must be true for the notifications to work
            if (pressure_level.toString() == pressureNumber) {
                val textTitle = "Android Sensor Engine"
                val textContent = getString(R.string.notify_pressure_message) + " " + pressureNumber + " " + "hPa"

                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification_logo)
                        .setContentTitle(textTitle)
                        .setContentText(textContent)
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOnlyAlertOnce(true)
                val notificationManager = NotificationManagerCompat.from(this)

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(CHANNEL_ID.toInt(), builder.build())
            }
        }
    }

    //For handling notifications
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name_pressure)
            val description = getString(R.string.channel_description_pressure)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)!!
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onStart() {
        //Dialog Box for Temperature Info
        pressureInfoDialog = Dialog(this)
        val `in`: Animation = AlphaAnimation(0.0f, 1.0f)
        `in`.duration = 1500
        pressure_text!!.startAnimation(`in`)
        currentPressure!!.startAnimation(`in`)
        pressureLevel!!.startAnimation(`in`)
        pressureInfo!!.startAnimation(`in`)

        // Register a listener for the sensor.
        createNotificationChannel()
        super.onStart()
        sensorManager!!.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onResume() {
        super.onResume()
        // mAdView.resume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregisters the sensor when the activity pauses.
      //  sensorManager!!.unregisterListener(this)
    }

    fun showPressureDialogPopup(v: View?) {
        pressureInfoDialog!!.setContentView(R.layout.pressure_popup_info)
        pressureInfoDialog!!.show()
    }

    fun closePressureDialogPopup(v: View?) {
        pressureInfoDialog!!.setContentView(R.layout.pressure_popup_info)
        pressureInfoDialog!!.dismiss()
    }

    //This will add functionality to the menu button within the action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    //The following is for the menu items within the navigation_menu.xml file
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.preferences -> {
                val configurationsIntent = Intent(this, SettingsActivity::class.java)
                this.startActivity(configurationsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Adds Pin Shortcut Functionality
    @RequiresApi(Build.VERSION_CODES.O)
    fun sensorShortcut(): Boolean {

        val shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)
        val intent = Intent(this, PressureActivity::class.java)
                .setAction("Pressure")

        if (shortcutManager!!.isRequestPinShortcutSupported) {

            val pinShortcutInfo = ShortcutInfo.Builder(this, "pressure-shortcut")
                    .setShortLabel(getString(R.string.pressure_sensor))
                    .setLongLabel(getString(R.string.pressure_sensor))
                    .setIcon(Icon.createWithResource(this, R.drawable.barometer_icon))
                    .setIntent(intent)
                    .build()

            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the shortcut to be pinned. Note that, if the
            // pinning operation fails, your app isn't notified. We assume here that the
            // app has implemented a method called createShortcutResultIntent() that
            // returns a broadcast intent.
            val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            // Configure the intent so that your app's broadcast receiver gets
            // the callback successfully.For details, see PendingIntent.getBroadcast().
            val successCallback = PendingIntent.getBroadcast(this, /* request code */ 0,
                    pinnedShortcutCallbackIntent, /* flags */ 0)

            shortcutManager.requestPinShortcut(pinShortcutInfo,
                    successCallback.intentSender)
        }
        return true
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        //ID used for notifications
        private const val CHANNEL_ID = "4"
    }
}