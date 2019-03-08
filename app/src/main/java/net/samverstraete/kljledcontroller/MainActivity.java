package net.samverstraete.kljledcontroller;

/*
using https://github.com/LarsWerkman/HoloColorPicker
 */


import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.larswerkman.holocolorpicker.*;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

public class MainActivity extends AppCompatActivity implements ColorPicker.OnColorChangedListener,NsdHelper.MDNSListener {
    OkHttpClient client = new OkHttpClient();
    NsdHelper nsdHelper;
    String currentIp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spinner = (Spinner) findViewById(R.id.serverSelector);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> serverAdapter = ArrayAdapter.createFromResource(this,
                R.array.servers, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(serverAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                refresh(selectedItemView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
        ValueBar valBar = (ValueBar) findViewById(R.id.valBar);
        SaturationBar satBar = (SaturationBar) findViewById(R.id.satBar);
        picker.addValueBar(valBar);
        picker.addSaturationBar(satBar);
        picker.setOnColorChangedListener(this);
        //opacityBar.setOnOpacityChangeListener(new OnOpacityChangedListener());

        nsdHelper = new NsdHelper(this, this);
        nsdHelper.discoverServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorChanged(int color) {

    }

    public void changeColor(android.view.View view){
        ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
        Spinner spinner = (Spinner) findViewById(R.id.serverSelector);

        float[] hsv = new float[3];
        Color.colorToHSV(picker.getColor(), hsv);
        int h = Math.round(hsv[0]*255/360); //float 0..360 to 0..255
        int s = Math.round(hsv[1]*255); //float 0..1 to 0..255
        int v = Math.round(hsv[2]*255); //float 0..1 to 0..255

        try {
            String url = "http://" + currentIp + "/kleur?h=" + String.valueOf(h) +
                    "&s=" + String.valueOf(s) + "&v=" + String.valueOf(v);
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            parseOldColor(view, response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh(android.view.View view){
        Spinner spinner = (Spinner) findViewById(R.id.serverSelector);

        try {
            Request request = new Request.Builder()
                    .url("http://" + currentIp + "/kleur")
                    .build();
            Response response = client.newCall(request).execute();
            parseOldColor(view, response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetColor(android.view.View view){
        ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
        picker.setColor(picker.getOldCenterColor());
    }

    public void parseOldColor(View view, String json){
        ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);

        try {
            JSONObject jObject = new JSONObject(json);
            int h = jObject.getInt("h");
            int s = jObject.getInt("s");
            int v = jObject.getInt("v");
            float[] hsv = new float[3];
            hsv[0] = (float) h * 360 / 255;
            hsv[1] = (float) s / 255;
            hsv[2] = (float) v / 255;
            picker.setOldCenterColor(Color.HSVToColor(hsv));
        } catch (JSONException je){
            je.printStackTrace();
        }
    }

    @Override
    public void ipFound(final String ip) {
        Log.d("MainActivity", "IP FOUND: "+ip);
        currentIp = ip;
    }

    @Override
    public void error(String error) {
        Log.d("MainActivity", "Error: "+error);
    }


}
