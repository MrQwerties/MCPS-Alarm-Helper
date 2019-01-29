package vinvinb.mcpsalarmhelper;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {
    private NotificationManager mNotificationManager;
    private SharedPreferences sharedPref;

    private AlarmManager alarmMgr;
    private PendingIntent checkIntent;

    private boolean on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Make sure the app has permission
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(!mNotificationManager.isNotificationPolicyAccessGranted()){
            Intent permissionsIntent = new Intent(this, RequestPermissionActivity.class);
            startActivity(permissionsIntent);
        }

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        boolean defaultValue = getResources().getBoolean(R.bool.on_off_default_key);
        on = sharedPref.getBoolean(getString(R.string.on_off_key), defaultValue);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, ClosingBroadcastReceiver.class);
        checkIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 0, intent, 0);

        if(on){
            enableCheck();
        } else{
            cancelCheck();
        }

        drawOnButton(on);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.on_off_key), on);
        editor.apply();
    }

    public void enableCheck(){
        cancelCheck();

        //Set the alarm to 5:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 45);

        //todo: uncomment this
        /*
        //Randomize the time slightly
        calendar.set(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(0, 15));
        calendar.set(Calendar.SECOND, ThreadLocalRandom.current().nextInt(0, 60));
        calendar.set(Calendar.MILLISECOND, ThreadLocalRandom.current().nextInt(0, 1000));
        */
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, 0, checkIntent);
    }

    public void cancelCheck(){
        alarmMgr.cancel(checkIntent);
    }

    public void toggleOnOff(View view) {
        on = !on;

        if(on){
            enableCheck();
        } else{
            cancelCheck();
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.on_off_key), on);
        editor.apply();

        drawOnButton(on);
    }

    public void goToInfo(View view){
        startActivity(new Intent(this, InfoActivity.class));
    }

    public void goToCredits(View view){
        startActivity(new Intent(this, CreditsActivity.class));
    }

    public void drawOnButton(boolean isOn){
        Button onButton = findViewById(R.id.onButton);
        if(isOn){
            onButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen));
            onButton.setText(R.string.on_text);
        } else{
            onButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightRed));
            onButton.setText(R.string.off_text);
        }
    }
}
