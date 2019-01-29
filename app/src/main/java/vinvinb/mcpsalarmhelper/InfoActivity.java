package vinvinb.mcpsalarmhelper;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView infoText = findViewById(R.id.infoText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            infoText.setText(Html.fromHtml(getResources().getString(R.string.app_info_html), Html.FROM_HTML_MODE_COMPACT));
        } else {
            infoText.setText(Html.fromHtml(getResources().getString(R.string.app_info_html)));
        }
    }
}
