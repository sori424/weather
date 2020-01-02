package com.example.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.androdocs.httprequest.HttpRequest;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    String lat;
    String lon;
    String API = "400c047eb6333f9fcba21f2c03564ea8";
    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lat = Double.toString(location.getLatitude());
            lon = Double.toString(location.getLongitude());
            Log.d("TAG", "onLocationChanged, lat = " + lat + "\tlon = " + lon);

            if(weatherTask == null || weatherTask.getStatus() == AsyncTask.Status.FINISHED) {
                weatherTask = new WeatherTask();
            }
            if(weatherTask.getStatus() != AsyncTask.Status.RUNNING)
                weatherTask.execute();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("TAG", "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("TAG", "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("TAG", "onProviderDisabled : " + provider);
        }
    };


    TextView addressTxt, latlonTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt;
    ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult");
        if (requestCode == 12345) {
            if (checkPermission()) {
                getLocationManager();
                Log.d("TAG", "onRequestPermissionsResult, check ok");
            } else {
                Toast.makeText(this, "Location permission require", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                Log.d("TAG", "ACCESS_COARSE_LOCATION is not granted");
            else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                Log.d("TAG", "ACCESS_FINE_LOCATION is not granted");
        }
        return false;
    }

    private boolean isLocationEnabled() {
        if(locationManager == null)
            getLocationManager();
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void getLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getLocationManager();
            return;
        }
        if (checkPermission()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            lat = String.valueOf(location.getLatitude());
                            lon = String.valueOf(location.getLongitude());
                            Log.d("TAG", "onComplete lat = " + lat + "\t\t lon = " + lon);

                            if(weatherTask == null || weatherTask.getStatus() == AsyncTask.Status.FINISHED) {
                                weatherTask = new WeatherTask();
                            }
                            if(weatherTask.getStatus() != AsyncTask.Status.RUNNING)
                                weatherTask.execute();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 12345);
        }
    }
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            Log.d("TAG", "onLocationResult, lat = " + location.getLatitude() + "\tlon = " + location.getLongitude());
        }
    };

    FusedLocationProviderClient mFusedLocationClient;
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(0);
//        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);

            addressTxt = findViewById(R.id.address);
            latlonTxt = findViewById(R.id.latlon);
            statusTxt = findViewById(R.id.status);
            tempTxt = findViewById(R.id.temp);
            temp_minTxt = findViewById(R.id.temp_min);
            temp_maxTxt = findViewById(R.id.temp_max);
            sunriseTxt = findViewById(R.id.sunrise);
            sunsetTxt = findViewById(R.id.sunset);
            windTxt = findViewById(R.id.wind);
            pressureTxt = findViewById(R.id.pressure);
            humidityTxt = findViewById(R.id.humidity);
            imageView = findViewById(R.id.icon);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000*10, 10, locationListener);
        Log.d("TAG","onResume");
    }


    WeatherTask weatherTask;
    class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                    "&lon=" + lon +
                    "&appid=" + API +
                    "&units=metric");
            return response;
        }

        public String getImage(String icon){
            return String.format("https://api.openweathermap.org/img/w/%s.png", icon);
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject coord = jsonObj.getJSONObject("coord");
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                String lat = coord.getString("lat");
                String lon = coord.getString("lon");
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity") + "%";

                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed") + "m/s";
                String weatherDescription = weather.getString("description");
                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                addressTxt.setText(address);
                latlonTxt.setText(lat + " , " + lon);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure + "hPa");
                humidityTxt.setText(humidity);
                Picasso.get().load(getImage(weather.getString("icon"))).into(imageView);

                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);


            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }

        }
    }
}
