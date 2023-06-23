package com.example.weatherpulse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;



public class MainActivity extends AppCompatActivity {

    private TextView city_name , temp_in_celcius , temp_in_farenheit , humidity , wind_speed_in_kmph , wind_speed_in_mph , weather_message;
    private EditText enter_city;
    private ImageView img_icon;
    private Button enter_btn;
    private String url;

    public static String cname = null , msg = null , hmdty = null , temp_in_c = null , temp_in_f = null , wind_speed_in_kh = null , wind_speed_in_mh = null;
    public static Bitmap img = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        city_name = findViewById(R.id.city_name_display);
        temp_in_celcius = findViewById(R.id.temp_in_celcius);
        temp_in_farenheit = findViewById(R.id.temp_in_farenheit);
        humidity = findViewById(R.id.humidity);
        wind_speed_in_kmph = findViewById(R.id.wind_speed_in_kmph);
        wind_speed_in_mph = findViewById(R.id.wind_speed_in_mph);
        enter_btn = findViewById(R.id.search_btn);
        enter_city = findViewById(R.id.search_city);
        img_icon = findViewById(R.id.weather_image);
        weather_message = findViewById(R.id.weather_message);

        SharedPreferences getSharedValues = getSharedPreferences("weatherdetails",MODE_PRIVATE);
        String temp = getSharedValues.getString("cname","CITY");
        getInfoFromAPI(temp);


        enter_btn.setOnClickListener(view -> {

            if(datadisabled(MainActivity.this) && Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                startActivity(intent);
            }
            else if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.P){
                Toast.makeText(this,"Enable data connectivity",Toast.LENGTH_SHORT).show();
            }
            else{
                String cityname = String.valueOf(enter_city.getText());
                if (cityname.isEmpty())
                    Toast.makeText(MainActivity.this, "Enter valid city name", Toast.LENGTH_SHORT).show();
                else
                    getInfoFromAPI(cityname);
            }
        });
    }

    private boolean datadisabled(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = connectivityManager.getActiveNetworkInfo();
        return network == null || !network.isConnected();
    }

    private void getInfoFromAPI(String cityname){
        url = "https://api.weatherapi.com/v1/current.json?key=936c901c4a84404db74130424232705&q=" + cityname + "&aqi=yes";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest obj = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {

                temp_in_c = response.getJSONObject("current").getString("temp_c");
                temp_in_f = response.getJSONObject("current").getString("temp_f");
                hmdty = response.getJSONObject("current").getString("humidity");
                wind_speed_in_kh = response.getJSONObject("current").getString("wind_kph");
                wind_speed_in_mh = response.getJSONObject("current").getString("wind_mph");
                cname = response.getJSONObject("location").getString("name");
                msg = response.getJSONObject("current").getJSONObject("condition").getString("text");

                temp_in_celcius.setText(temp_in_c + "°C");
                temp_in_farenheit.setText(temp_in_f + "°F");
                humidity.setText(hmdty + "%");
                wind_speed_in_kmph.setText(wind_speed_in_kh + " km/hr");
                wind_speed_in_mph.setText(wind_speed_in_mh + " Mi/hr");
                city_name.setText(cname);
                weather_message.setText(msg);
                Picasso.get().load("https:" + response.getJSONObject("current").getJSONObject("condition").getString("icon")).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        img = bitmap;
                        img_icon.setImageIcon(Icon.createWithBitmap(img));
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
                saveInSharedPreference();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, error -> Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_SHORT));
        queue.add(obj);
    }

    private void saveInSharedPreference(){
        SharedPreferences shrd = getSharedPreferences("weatherdetails",MODE_PRIVATE);
        SharedPreferences.Editor editor = shrd.edit();
        editor.putString("cname" , cname);
        editor.apply();
    }
}