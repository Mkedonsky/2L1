package ru.mkedonsky.myapplication.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import ru.mkedonsky.myapplication.R;

public class HomeFragment extends Fragment {
    private final Handler handler = new Handler();
    private final static String LOG_TAG = HomeFragment.class.getSimpleName();

    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTemperatureTextView;
    private TextView weatherIconTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
//        initFonts();

    }

    private void initViews() {
        cityTextView = cityTextView.findViewById(R.id.city_field);
        updatedTextView = updatedTextView.findViewById(R.id.updated_field);
        detailsTextView = detailsTextView.findViewById(R.id.details_field);
        currentTemperatureTextView = currentTemperatureTextView.findViewById(R.id.current_temperature_field);
        weatherIconTextView = weatherIconTextView.findViewById(R.id.weather_icon);

    }
//    private void initFonts() {
//        weatherFont = Typeface.createFromAsset(, "fonts/weather.ttf");
//        weatherIconTextView.setTypeface(weatherFont);
//    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        showInputDialog();
        return true;
    }

    @SuppressLint("Assert")
    private void showInputDialog() {
        Context context = null;
        assert false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.change_city);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateWeatherData(input.getText().toString());
            }
        });
        builder.show();
    }

    private void updateWeatherData(final String city) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject jsonObject = WeatherDataLoader.getJSONData(city);
                if (jsonObject == null) {
                    handler.post(new Runnable() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.place_not_found,
                                    Toast.LENGTH_LONG);
//                            Toast.setGravity(Gravity.CENTER, 0, 0).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(jsonObject);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject jsonObject) {
        Log.d(LOG_TAG, "json: " + jsonObject.toString());
        try {
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonObject.getJSONObject("main");

            setPlaceName(jsonObject);
            setDetails(details, main);
            setCurrentTemp(main);
            setUpdatedText(jsonObject);
            setWeatherIcon(details.getInt("id"),
                    jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                    jsonObject.getJSONObject("sys").getLong("sunset") * 1000);
        } catch (Exception exc) {
            exc.printStackTrace();
            Log.e(LOG_TAG, "One or more fields not found in the JSON data");
        }
    }

    private void setPlaceName(JSONObject jsonObject) throws JSONException {
        String cityText = jsonObject.getString("name").toUpperCase() + ", "
                + jsonObject.getJSONObject("sys").getString("country");
        cityTextView.setText(cityText);
    }

    private void setDetails(JSONObject details, JSONObject main) throws JSONException {
        String detailsText = details.getString("description").toUpperCase() + "\n"
                + "Humidity: " + main.getString("humidity") + "%" + "\n"
                + "Pressure: " + main.getString("pressure") + "hPa";
        detailsTextView.setText(detailsText);
    }

    private void setCurrentTemp(JSONObject main) throws JSONException {
        String currentTextText = String.format(Locale.getDefault(), "%.1f",
                main.getDouble("temp")) + "\u2103";
        currentTemperatureTextView.setText(currentTextText);
    }

    private void setUpdatedText(JSONObject jsonObject) throws JSONException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String updateOn = dateFormat.format(new Date(jsonObject.getLong("dt") * 1000));
        String updatedText = "Last update: " + updateOn;
        updatedTextView.setText(updatedText);
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2: {
                    icon = getString(R.string.weather_thunder);
                    break;
                }
                case 3: {
                    icon = getString(R.string.weather_drizzle);
                    break;
                }
                case 5: {
                    icon = getString(R.string.weather_rainy);
                    break;
                }
                case 6: {
                    icon = getString(R.string.weather_snowy);
                    break;
                }
                case 7: {
                    icon = getString(R.string.weather_foggy);
                    break;
                }
                case 8: {
                    icon = getString(R.string.weather_cloudy);
                    break;
                }
            }
        }
        weatherIconTextView.setText(icon);
    }

}

