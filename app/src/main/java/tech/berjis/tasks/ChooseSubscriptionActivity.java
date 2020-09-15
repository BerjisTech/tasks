package tech.berjis.tasks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

public class ChooseSubscriptionActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    View casualView, professionalView;
    TextView casualPrice, casualText, professionalPrice, professionalText, pageText;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_subscription);

        initVars();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

        casualView = findViewById(R.id.casualView);
        professionalView = findViewById(R.id.professionalView);
        casualPrice = findViewById(R.id.casualPrice);
        casualText = findViewById(R.id.casualText);
        professionalPrice = findViewById(R.id.professionalPrice);
        professionalText = findViewById(R.id.professionalText);
        pageText = findViewById(R.id.pageText);

        loadText();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadText() {
        long nextMonth = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMonths(1)
                .toEpochSecond();


        long dv = nextMonth * 1000;// its need to be in milisecond
        Date df = new java.util.Date(dv);
        String vv = new SimpleDateFormat("MMMM yyyy").format(df);

        // Toast.makeText(this, "Next month: " + vv, Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pageText.setText(Html.fromHtml("<h3>Get started with Mwakazy.</h3> <br />Pay your monthly subscription and get access to the whole catalog. Hire taskers. Post services you offer. Get jobs and employ your peers.<br /><br /><small>By paying, you agree to Berjis Technologies <a href=\"https://berjis.tech/terms-and-conditions\">terms and conditions</a></small>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            pageText.setText(Html.fromHtml("<h3>Get started with Mwakazy.</h3> <br />Pay your monthly subscription and get access to the whole catalog. Hire taskers. Post services you offer. Get jobs and employ your peers.<br /><br /><small>By paying, you agree to Berjis Technologies <a href=\"https://berjis.tech/terms-and-conditions\">terms and conditions</a></small>"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            casualText.setText(Html.fromHtml("<h4>Casual Services and Taskers</h4><small>Delivery, Laundry, Home cleaning etc</small>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            casualText.setText(Html.fromHtml("<h4>Casual Services and Taskers</h4><small>Delivery, Laundry, Home cleaning etc</small>"));
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            professionalText.setText(Html.fromHtml("<h4>Professional Services and Taskers</h4><small>Customer Support, PAs, Book Keeping etc <br />(+ access to all casual services and taskers)</small>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            professionalText.setText(Html.fromHtml("<h4>Professional Services and Taskers</h4><small>Customer Support, PAs, Book Keeping etc <br />(+ access to all casual services and taskers)</small>"));
        }

        loadCustomPrice();
    }

    private void loadCustomPrice() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currency_code = snapshot.child("currency_code").getValue().toString();
                String currency_symbol = snapshot.child("currency_symbol").getValue().toString();
                String country_code = snapshot.child("country_code").getValue().toString();

                getActualPrice(currency_code, currency_symbol, country_code);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getActualPrice(String code, String symbol, String country_code) {
        double casualP = 0;
        double professionalP = 0;
        String currency = "";

        if (country_code.equals("AX")) {
            casualP = 4.99;
            professionalP = 9.99;
            currency = code;

        } else if (country_code.equals("US")) {
            casualP = 4.99;
            professionalP = 9.99;
            currency = code;
        } else if (country_code.equals("GB")) {
            casualP = 4.99;
            professionalP = 9.99;
            currency = code;
        } else if (country_code.equals("KE")) {
            casualP = 99.99;
            professionalP = 499.99;
            currency = code;
        } else {
            casualP = 0.99;
            professionalP = 4.99;
            currency = "USD";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            casualPrice.setText(Html.fromHtml("<small>" + symbol + "</small> " + casualP + "/<small>mo</small>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            casualPrice.setText(Html.fromHtml("<small>" + symbol + "</small> " + casualP + "/<small>mo</small>"));
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            professionalPrice.setText(Html.fromHtml("<small>" + symbol + "</small> " + professionalP + "/<small>mo</small>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            professionalPrice.setText(Html.fromHtml("<small>" + symbol + "</small> " + professionalP + "/<small>mo</small>"));
        }

        setOnClick(casualP, professionalP, currency, country_code);
    }

    private void setOnClick(final double casualP, final double professionalP, final String currency, final String country_code) {
//        professionalView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent p_i = new Intent(ChooseSubscriptionActivity.this, PaySubscriptionActivity.class);
//                Bundle p_b = new Bundle();
//                p_b.putString("country", country_code);
//                p_b.putString("currency", currency);
//                p_b.putString("subscription", "professional");
//                p_b.putDouble("price", professionalP);
//                p_i.putExtras(p_b);
//                startActivity(p_i);
//            }
//        });
        casualView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent p_i = new Intent(ChooseSubscriptionActivity.this, PaySubscriptionActivity.class);
                Bundle p_b = new Bundle();
                p_b.putString("country", country_code);
                p_b.putString("currency", currency);
                p_b.putString("subscription", "casual");
                p_b.putDouble("price", casualP);
                p_i.putExtras(p_b);
                startActivity(p_i);
            }
        });
    }
}