package com.example.android.soundtechsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

// TODO add menu

public class TemperatureActivity extends AppCompatActivity implements SensorEventListener {

    TextView temperature_text;
    TextView currentDegrees;
    TextView currentDegreesF;
    TextView currentDegreesK;
    private SensorManager sensorManager;
    private Sensor temperature;

    // Initiate Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_sensor);

        temperature_text = (TextView) findViewById(R.id.temperature);
        currentDegrees = (TextView) findViewById(R.id.current_temp);
        currentDegreesF = (TextView) findViewById(R.id.current_temp_f);
        currentDegreesK = (TextView) findViewById(R.id.current_temp_k);

        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(1500);
        temperature_text.startAnimation(in);
        currentDegrees.startAnimation(in);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Get an instance of the sensor service, and use that to get an instance of
        // the surrounding temperature. If device does not support this sensor a toast message will
        // appear
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) == null){
            Toast.makeText(this, "Your device does not support this sensor", Toast.LENGTH_LONG).show();
        }

        // Ambient Temperature measures the temperature around the device
        temperature = sensorManager.getDefaultSensor((Sensor.TYPE_AMBIENT_TEMPERATURE));
        currentDegrees = (TextView) findViewById(R.id.current_temp);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    //Main magic of the Temperature Sensor
    @Override
    public final void onSensorChanged(SensorEvent event) {
        //The default Android properties for event.values[0] is the formula for Celsius
        if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)
        {
            currentDegrees.setText(event.values[0] + " °C" );

            //The following converts celsius into fahrenheit
        } if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            int b = (int) event.values[0];
            int c = b * 9/5 + 32;
            currentDegreesF.setText(c + " °F");
        } if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            int b = (int) event.values[0];
            int k = (int) (b + 273.15);
            currentDegreesK.setText(k + " K");
        }
        }


    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    //This will add functionality to the menu button within the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }
}

