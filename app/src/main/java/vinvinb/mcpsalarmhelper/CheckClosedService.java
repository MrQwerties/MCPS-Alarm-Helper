package vinvinb.mcpsalarmhelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.app.NotificationManager;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckClosedService extends Service {
    private NotificationManager mNotificationManager;
    private SharedPreferences sharedPref;

    private AlarmManager alarmMgr;
    private PendingIntent checkIntent;

    private static final int MAX_COUNTER = 10;
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(this, CheckClosedService.class);
        checkIntent = PendingIntent.getBroadcast(this, 0, serviceIntent, 0);

        boolean defaultValue = getResources().getBoolean(R.bool.on_off_default_key);
        if(sharedPref.getBoolean(getString(R.string.on_off_key), defaultValue)) {
            if (isSchoolDay()) {
                readMain();
            }
        } else{
            cancelCheck();
        }

        silencePhone(); //todo: remove this line
        stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void cancelCheck(){
        alarmMgr.cancel(checkIntent);
    }

    protected void silencePhone(){
        if(mNotificationManager.isNotificationPolicyAccessGranted()){
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
    }

    public void readMain(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.montgomeryschoolsmd.org/press/";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        readPage(getLastId(response), 0);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public int getLastId(String source){
        //todo: uncomment this
        /*
        String[] parts = source.split("<a href='index\\.aspx\\?pagetype=showrelease&id=");
        if(parts.length < 2){
            return -1;
        }
        String x = parts[1];
        return Integer.parseInt(x.substring(0, x.indexOf('&')));
        */
        return 8269;
    }

    public void readPage(int id, int counter){
        if(counter > MAX_COUNTER){
            return;
        }
        final int copyId = id;
        final int copyCounter = counter;
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.montgomeryschoolsmd.org/press/index.aspx?id=" + Integer.toString(id);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //todo: uncomment this
                        /*
                        if(daysSince(getDateString(response)) > 0){
                            return;
                        }
                        */
                        if(postClosed(response) || postDelayed(response)){
                            silencePhone();
                        } else{
                            readPage(copyId - 1, copyCounter + 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(stringRequest);
    }

    public boolean postClosed(String source){
        return postMatches(source, getResources().getString(R.string.closed_title_regex), getResources().getString(R.string.closed_content_regex));
    }

    public boolean postDelayed(String source){
        return postMatches(source, getResources().getString(R.string.delayed_title_regex), getResources().getString(R.string.delayed_content_regex));
    }

    //Todo: rewrite this chunk of regex-matching code to be more efficient (like not calculating the title twice)
    public static boolean postMatches(String source, String titleRegex, String contentRegex){
        String title = getPostTitle(source);
        String content = getFirstSentence(source);
        Pattern titlePattern = Pattern.compile(titleRegex);
        Matcher titleMatcher = titlePattern.matcher(title);
        if (titleMatcher.find()){
            Pattern contentPattern = Pattern.compile(contentRegex);
            Matcher contentMatcher = contentPattern.matcher(content);
            if (contentMatcher.find()){
                return true;
            }
        }
        return false;
    }

    public static String getPostTitle(String source){
        String parts[] = source.split("<title>");
        if(parts.length < 2){
            return "";
        }
        String x = parts[1];
        return x.substring(0, x.indexOf('<')).replace("\n", "").trim();
    }

    public static String getFirstSentence(String source){
        String parts[] = source.split("<div class=\"presspage\"><p>(<span(.*?)>)*");
        if(parts.length < 2){
            return "";
        }
        try {
            String content = null;
            content = parts[1].substring(0, parts[1].indexOf('<'));
            if (content.contains(".")) {
                return content.substring(0, content.indexOf('.'));
            } else {
                return content;
            }
        } catch(Exception e){
            return "";
        }
    }

    public static String getDateString(String source){
        String[] parts = source.split("<span id=\"ctl00_ContentPlaceHolder1_pressLists_ltlDate\" class=\"textblack\">");
        if(parts.length < 2){
            return "";
        }
        String x = parts[1];
        return x.substring(0, x.indexOf('<'));
    }

    public static int daysSince(String dateName){
        String[] parts = dateName.split(" ");
        try {
            if (parts.length == 3) {
                Calendar c = GregorianCalendar.getInstance();
                c.set(Integer.parseInt(parts[2]), stringIndexOf(MONTHS, parts[0]), Integer.parseInt(parts[1].substring(0, parts[1].length() - 1)));
                return daysBetween(GregorianCalendar.getInstance(), c);
            }
        } catch(Exception e){
            return -1;
        }
        return -1;
    }

    public static int daysBetween(Calendar day1, Calendar day2){
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays ;
        }
    }

    public static int stringIndexOf(String[] list, String target){
        int index = -1;
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(target)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static boolean isSchoolDay(){
        //Todo: make the app actually check if it's a school day
        return true;
    }
}
