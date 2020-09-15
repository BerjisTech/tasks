package tech.berjis.tasks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PaySubscriptionActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, transactionRef;

    String UID, subscription, country, currency;
    double amount;
    long nextMonth = OffsetDateTime.now(ZoneOffset.UTC)
            .plusMonths(1)
            .toEpochSecond();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_subscription);

        initVars();
    }


    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

        Intent p_i = getIntent();
        Bundle p_b = p_i.getExtras();
        assert p_b != null;
        country = p_b.getString("country");
        currency = p_b.getString("currency");
        subscription = p_b.getString("subscription");
        amount = p_b.getDouble("price");

        // Toast.makeText(this, country + "", Toast.LENGTH_SHORT).show();

        setUserDetails();
    }

    private void setUserDetails() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String first_name = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString();
                String last_name = Objects.requireNonNull(snapshot.child("last_name").getValue()).toString();
                String user_email = Objects.requireNonNull(snapshot.child("user_email").getValue()).toString();
                String user_phone = Objects.requireNonNull(snapshot.child("user_phone").getValue()).toString();

                validateEntries(first_name, last_name, user_email, user_phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validateEntries(String fName, String lName, String email, String phone_number) {
        long time_start = System.currentTimeMillis() / 1000L;

        long dv = time_start * 1000;// its need to be in milisecond
        Date df = new java.util.Date(dv);
        String vv = new SimpleDateFormat("MMMM yyyy").format(df);

        String note = fName + " " + lName + "'s " + vv + " subscription";

        transactionRef = dbRef.child("Transactions").push();
        String trans_ref = transactionRef.getKey();
        transactionRef.child("time_start").setValue(time_start);

        String publicKey = getString(R.string.public_key);
        String encryptionKey = getString(R.string.encryption_key);
        String narration = fName + " " + lName + " (deposit)";

        transactionRef.child("user").setValue(UID);
        transactionRef.child("narration").setValue(note);
        transactionRef.child("amount").setValue(amount);
        transactionRef.child("text_ref").setValue(trans_ref);
        transactionRef.child("time").setValue(time_start);
        transactionRef.child("country").setValue(country);
        transactionRef.child("currency").setValue(currency);

        boolean valid = true;

        //isAmountValid for compulsory fields

        if (valid) {
            RavePayManager ravePayManager = new RavePayManager(this).setAmount(amount)
                    .setCountry(country)
                    .setCurrency(currency)
                    .setEmail(email)
                    .setfName(fName)
                    .setlName(lName)
                    .setPhoneNumber(phone_number)
                    .setNarration(narration)
                    .setPublicKey(publicKey)
                    .setEncryptionKey(encryptionKey)
                    .setTxRef(trans_ref)
                    .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .allowSaveCardFeature(true)
                    .acceptAchPayments(false)
                    .acceptFrancMobileMoneyPayments(false)
                    .acceptBankTransferPayments(false)
                    .acceptUssdPayments(false)
                    .acceptBarterPayments(true)
                    .onStagingEnv(false)
                    .isPreAuth(true)
                    .showStagingLabel(false)
//                    .setMeta(meta)
//                    .withTheme(R.style.TestNewTheme)
                    .shouldDisplayFee(true);

            assert country != null;
            if (country.equals("KE")) {
                ravePayManager.acceptMpesaPayments(true);
            } else {
                ravePayManager.acceptMpesaPayments(false);
            }

            if (country.equals("GH")) {
                ravePayManager.acceptGHMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptGHMobileMoneyPayments(false);
            }

            if (country.equals("UG")) {
                ravePayManager.acceptUgMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptUgMobileMoneyPayments(false);
            }

            if (country.equals("ZM")) {
                ravePayManager.acceptZmMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptZmMobileMoneyPayments(false);
            }

            if (country.equals("RW")) {
                ravePayManager.acceptRwfMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptRwfMobileMoneyPayments(false);
            }

            if (country.equals("UK")) {
                ravePayManager.acceptUkPayments(true);
            } else {
                ravePayManager.acceptUkPayments(false);
            }

            if (country.equals("ZA")) {
                ravePayManager.acceptSaBankPayments(true);
            } else {
                ravePayManager.acceptSaBankPayments(false);
            }

            ravePayManager.initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {

            String message = data.getStringExtra("response");

            if (message != null) {
                Log.d("rave response", message);
            }

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                long end_time = System.currentTimeMillis() / 1000L;
                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("success");
                dbRef.child("Users").child(UID).child("subscription").setValue(subscription);
                dbRef.child("Users").child(UID).child("next_month").setValue(nextMonth);
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                startActivity(new Intent(PaySubscriptionActivity.this, ProfileActivity.class));
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                long end_time = System.currentTimeMillis() / 1000L;
                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("error");
                Toast.makeText(this, "There has been an error completing this transaction", Toast.LENGTH_LONG).show();
                PaySubscriptionActivity.super.finish();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                long end_time = System.currentTimeMillis() / 1000L;
                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("cancelled");
                Toast.makeText(this, "This transaction has been cancelled", Toast.LENGTH_LONG).show();
                PaySubscriptionActivity.super.finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}