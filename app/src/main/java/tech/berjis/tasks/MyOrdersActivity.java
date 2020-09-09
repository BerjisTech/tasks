package tech.berjis.tasks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MyOrdersActivity extends AppCompatActivity {

    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        init_vars();
    }

    private void init_vars(){
        profile = findViewById(R.id.profile);

        statickOnClicks();
    }

    private void statickOnClicks(){
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, ProfileActivity.class));
            }
        });
    }
}
