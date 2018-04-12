package xyz.vinesh.togglebuttondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import xyz.vinesh.toggleslider.ToggleSlider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleSlider toggleSlider = findViewById(R.id.tsToggleButton1);

        toggleSlider.setOnStateChangeListener(new ToggleSlider.OnStateChangeListener() {
            @Override
            public void onStateChange(boolean newState) {
                Toast.makeText(MainActivity.this, "" + newState, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
