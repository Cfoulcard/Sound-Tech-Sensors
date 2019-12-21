package com.christianfoulcard.android.androidsensorengine.Sensors;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.christianfoulcard.android.androidsensorengine.R;
import com.google.firebase.analytics.FirebaseAnalytics;

public class WalkActivity extends AppCompatActivity implements SensorEventListener {

    TextView steps_text;
    TextView currentSteps;
    TextView stepAmount;
    private SensorManager sensorManager;
    private Sensor steps;
    private Context mContext;
    private Activity mActivity;
    private SharedPreferences mSharedPreferences;

    // Initiate Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walk_sensor);

        steps_text = (TextView) findViewById(R.id.walk_sensor);
        currentSteps = (TextView) findViewById(R.id.current_steps);
        stepAmount = (TextView) findViewById(R.id.steps);

        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(1500);
        steps_text.startAnimation(in);
        currentSteps.startAnimation(in);
        stepAmount.startAnimation(in);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Get an instance of the sensor service, and use that to get an instance of
        // the relative temperature. If device does not support this sensor a toast message will
        // appear
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
            Toast.makeText(this, "Your device does not support this sensor", Toast.LENGTH_LONG).show();
        }

        // Ambient Temperature measures the temperature around the device
        steps = sensorManager.getDefaultSensor((Sensor.TYPE_STEP_COUNTER));
        currentSteps = (TextView) findViewById(R.id.current_steps);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Get the application context
        mContext = getApplicationContext();

        // Get the activity
        mActivity = WalkActivity.this;

        int water_vapor = (int) event.values[0];

        //The default Android properties for event.values[0] is the formula for Celsius
        //This is for Fahrenheit
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            currentSteps.setText(water_vapor + " Steps");
//TODO change to string
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, steps, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}