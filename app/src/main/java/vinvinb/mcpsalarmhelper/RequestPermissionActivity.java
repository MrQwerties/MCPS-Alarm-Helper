package vinvinb.mcpsalarmhelper;

import android.app.NotificationManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RequestPermissionActivity extends AppCompatActivity {
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permission);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(mNotificationManager.isNotificationPolicyAccessGranted()){
            Intent permissionsIntent = new Intent(this, RequestPermissionActivity.class);
            startActivity(permissionsIntent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(mNotificationManager.isNotificationPolicyAccessGranted()){
            Intent permissionsIntent = new Intent(this, RequestPermissionActivity.class);
            startActivity(permissionsIntent);
        }
    }

    public void goToSettings(View view){
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(settingsIntent);
    }
}
