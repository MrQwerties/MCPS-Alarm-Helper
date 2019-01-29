package vinvinb.mcpsalarmhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClosingBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent){
        context.startService(new Intent(context, CheckClosedService.class));
    }
}
