package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {

    //These are the objects needed
    //It is the verification id that will be sent to the user
    private String mVerificationId, country, country_code, code, name, symbol;

    //The edittext to input the code
    private PinView editTextCode;

    //firebase auth object
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        //initializing objects
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        editTextCode = findViewById(R.id.editTextCode);


        //getting mobile number from the previous activity
        //and sending the verification code to the number
        Intent intent = getIntent();
        String mobile = intent.getStringExtra("mobile");
        String countryCode = intent.getStringExtra("countryCode");
        country = intent.getStringExtra("country");
        country_code = intent.getStringExtra("country_code");
        Toast.makeText(this, countryCode + mobile, Toast.LENGTH_SHORT).show();
        sendVerificationCode("+" + countryCode, mobile);


        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editTextCode.setError("Enter valid code");
                    editTextCode.requestFocus();
                    return;
                }

                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

    }

    //the method is sending verification code
    //the country id is concatenated
    //you can take the country id as user input as well
    private void sendVerificationCode(String countryCode, String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                countryCode + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }


    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };


    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            final String UID = mAuth.getCurrentUser().getUid();
                            String UPhone = mAuth.getCurrentUser().getPhoneNumber();

                            Map<String, Object> user = new HashMap<>();
                            user.put("country", country);
                            user.put("country_code", country_code);
                            user.put("user_id", UID);
                            user.put("user_phone", UPhone);
                            user.put("user_type", "default");
                            user.put("user_image", "");
                            user.put("user_email", "");
                            user.put("user_name", "");
                            user.put("first_name", "");
                            user.put("last_name", "");
                            user.put("user_description", "");


                            db.collection("Users").document(UID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    saveCurrency(UID);
                                }
                            });

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Something is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.editTextCode), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    private void saveCurrency(String UID) {
        if (country_code.equals("AX")) {
            code = "EUR";
            name = "Euro";
            symbol = "€";
        } else if (country_code.equals("US")) {
            code = "USD";
            name = "United States Dollar";
            symbol = "$";
        } else if (country_code.equals("GB")) {
            code = "GBP";
            name = "British Pound";
            symbol = "£";
        } else if (country_code.equals("CZ")) {
            code = "CZK";
            name = "Czech Koruna";
            symbol = "Kč";
        } else if (country_code.equals("TR")) {
            code = "TRY";
            name = "Turkish Lira";
            symbol = "₺";
        } else if (country_code.equals("AE")) {
            code = "AED";
            name = "Emirati Dirham";
            symbol = "د.إ";
        } else if (country_code.equals("AF")) {
            code = "AFN";
            name = "Afghanistan Afghani";
            symbol = "؋";
        } else if (country_code.equals("AR")) {
            code = "ARS";
            name = "Argentine Peso";
            symbol = "$";
        } else if (country_code.equals("AU")) {
            code = "AUD";
            name = "Australian Dollar";
            symbol = "$";
        } else if (country_code.equals("BB")) {
            code = "BBD";
            name = "Barbados Dollar";
            symbol = "$";
        } else if (country_code.equals("BD")) {
            code = "BDT";
            name = "Bangladeshi Taka";
            symbol = " Tk";
        } else if (country_code.equals("BG")) {
            code = "BGN";
            name = "Bulgarian Lev";
            symbol = "лв";
        } else if (country_code.equals("BH")) {
            code = "BHD";
            name = "Bahraini Dinar";
            symbol = "BD";
        } else if (country_code.equals("BM")) {
            code = "BMD";
            name = "Bermuda Dollar";
            symbol = "$";
        } else if (country_code.equals("BN")) {
            code = "BND";
            name = "Brunei Darussalam Dollar";
            symbol = "$";
        } else if (country_code.equals("BO")) {
            code = "BOB";
            name = "Bolivia Bolíviano";
            symbol = "$b";
        } else if (country_code.equals("BR")) {
            code = "BRL";
            name = "Brazil Real";
            symbol = "R$";
        } else if (country_code.equals("BT")) {
            code = "BTN";
            name = "Bhutanese Ngultrum";
            symbol = "Nu.";
        } else if (country_code.equals("BZ")) {
            code = "BZD";
            name = "Belize Dollar";
            symbol = "BZ$";
        } else if (country_code.equals("CA")) {
            code = "CAD";
            name = "Canada Dollar";
            symbol = "$";
        } else if (country_code.equals("CH")) {
            code = "CHF";
            name = "Switzerland Franc";
            symbol = "CHF";
        } else if (country_code.equals("CL")) {
            code = "CLP";
            name = "Chile Peso";
            symbol = "$";
        } else if (country_code.equals("CN")) {
            code = "CNY";
            name = "China Yuan Renminbi";
            symbol = "¥";
        } else if (country_code.equals("CO")) {
            code = "COP";
            name = "Colombia Peso";
            symbol = "$";
        } else if (country_code.equals("CR")) {
            code = "CRC";
            name = "Costa Rica Colon";
            symbol = "₡";
        } else if (country_code.equals("DK")) {
            code = "DKK";
            name = "Denmark Krone";
            symbol = "kr";
        } else if (country_code.equals("DO")) {
            code = "DOP";
            name = "Dominican Republic Peso";
            symbol = "RD$";
        } else if (country_code.equals("EG")) {
            code = "EGP";
            name = "Egypt Pound";
            symbol = "£";
        } else if (country_code.equals("ET")) {
            code = "ETB";
            name = "Ethiopian Birr";
            symbol = "Br";
        } else if (country_code.equals("GE")) {
            code = "GEL";
            name = "Georgian Lari";
            symbol = "₾";
        } else if (country_code.equals("GH")) {
            code = "GHS";
            name = "Ghana Cedi";
            symbol = "¢";
        } else if (country_code.equals("GM")) {
            code = "GMD";
            name = "Gambian dalasi";
            symbol = "D";
        } else if (country_code.equals("GY")) {
            code = "GYD";
            name = "Guyana Dollar";
            symbol = "$";
        } else if (country_code.equals("HR")) {
            code = "HRK";
            name = "Croatia Kuna";
            symbol = "kn";
        } else if (country_code.equals("HU")) {
            code = "HUF";
            name = "Hungary Forint";
            symbol = "Ft";
        } else if (country_code.equals("ID")) {
            code = "IDR";
            name = "Indonesia Rupiah";
            symbol = "Rp";
        } else if (country_code.equals("IL")) {
            code = "ILS";
            name = "Israel Shekel";
            symbol = "₪";
        } else if (country_code.equals("IN")) {
            code = "INR";
            name = "India Rupee";
            symbol = "0";
        } else if (country_code.equals("IS")) {
            code = "ISK";
            name = "Iceland Krona";
            symbol = "kr";
        } else if (country_code.equals("JM")) {
            code = "JMD";
            name = "Jamaica Dollar";
            symbol = "J$";
        } else if (country_code.equals("JP")) {
            code = "JPY";
            name = "Japanese Yen";
            symbol = "¥";
        } else if (country_code.equals("KE")) {
            code = "KES";
            name = "Kenyan Shilling";
            symbol = "KSh";
        } else if (country_code.equals("KR")) {
            code = "KRW";
            name = "Korea (South) Won";
            symbol = "₩";
        } else if (country_code.equals("KY")) {
            code = "KYD";
            name = "Cayman Islands Dollar";
            symbol = "$";
        } else if (country_code.equals("KZ")) {
            code = "KZT";
            name = "Kazakhstan Tenge";
            symbol = "лв";
        } else if (country_code.equals("LA")) {
            code = "LAK";
            name = "Laos Kip";
            symbol = "₭";
        } else if (country_code.equals("LK")) {
            code = "LKR";
            name = "Sri Lanka Rupee";
            symbol = "₨";
        } else if (country_code.equals("LR")) {
            code = "LRD";
            name = "Liberia Dollar";
            symbol = "$";
        } else if (country_code.equals("LT")) {
            code = "LTL";
            name = "Lithuanian Litas";
            symbol = "Lt";
        } else if (country_code.equals("MA")) {
            code = "MAD";
            name = "Moroccan Dirham";
            symbol = "MAD";
        } else if (country_code.equals("MD")) {
            code = "MDL";
            name = "Moldovan Leu";
            symbol = "MDL";
        } else if (country_code.equals("MK")) {
            code = "MKD";
            name = "Macedonia Denar";
            symbol = "ден";
        } else if (country_code.equals("MN")) {
            code = "MNT";
            name = "Mongolia Tughrik";
            symbol = "₮";
        } else if (country_code.equals("MU")) {
            code = "MUR";
            name = "Mauritius Rupee";
            symbol = "₨";
        } else if (country_code.equals("MW")) {
            code = "MWK";
            name = "Malawian Kwacha";
            symbol = "MK";
        } else if (country_code.equals("MX")) {
            code = "MXN";
            name = "Mexico Peso";
            symbol = "$";
        } else if (country_code.equals("MY")) {
            code = "MYR";
            name = "Malaysia Ringgit";
            symbol = "RM";
        } else if (country_code.equals("MZ")) {
            code = "MZN";
            name = "Mozambique Metical";
            symbol = "MT";
        } else if (country_code.equals("NA")) {
            code = "NAD";
            name = "Namibia Dollar";
            symbol = "$";
        } else if (country_code.equals("NG")) {
            code = "NGN";
            name = "Nigeria Naira";
            symbol = "₦";
        } else if (country_code.equals("NI")) {
            code = "NIO";
            name = "Nicaragua Cordoba";
            symbol = "C$";
        } else if (country_code.equals("NO")) {
            code = "NOK";
            name = "Norway Krone";
            symbol = "kr";
        } else if (country_code.equals("NP")) {
            code = "NPR";
            name = "Nepal Rupee";
            symbol = "₨";
        } else if (country_code.equals("NZ")) {
            code = "NZD";
            name = "New Zealand Dollar";
            symbol = "$";
        } else if (country_code.equals("OM")) {
            code = "OMR";
            name = "Oman Rial";
            symbol = "﷼";
        } else if (country_code.equals("PE")) {
            code = "PEN";
            name = "Peru Sol";
            symbol = "S/.";
        } else if (country_code.equals("PG")) {
            code = "PGK";
            name = "Papua New Guinean Kina";
            symbol = "K";
        } else if (country_code.equals("PH")) {
            code = "PHP";
            name = "Philippines Peso";
            symbol = "₱";
        } else if (country_code.equals("PK")) {
            code = "PKR";
            name = "Pakistan Rupee";
            symbol = "₨";
        } else if (country_code.equals("PL")) {
            code = "PLN";
            name = "Poland Zloty";
            symbol = "zł";
        } else if (country_code.equals("PY")) {
            code = "PYG";
            name = "Paraguay Guarani";
            symbol = "Gs";
        } else if (country_code.equals("QA")) {
            code = "QAR";
            name = "Qatar Riyal";
            symbol = "﷼";
        } else if (country_code.equals("RO")) {
            code = "RON";
            name = "Romania Leu";
            symbol = "lei";
        } else if (country_code.equals("RS")) {
            code = "RSD";
            name = "Serbia Dinar";
            symbol = "Дин.";
        } else if (country_code.equals("RU")) {
            code = "RUB";
            name = "Russia Ruble";
            symbol = "₽";
        } else if (country_code.equals("SA")) {
            code = "SAR";
            name = "Saudi Arabia Riyal";
            symbol = "﷼";
        } else if (country_code.equals("SE")) {
            code = "SEK";
            name = "Sweden Krona";
            symbol = "kr";
        } else if (country_code.equals("SG")) {
            code = "SGD";
            name = "Singapore Dollar";
            symbol = "$";
        } else if (country_code.equals("SO")) {
            code = "SOS";
            name = "Somalia Shilling";
            symbol = "S";
        } else if (country_code.equals("SR")) {
            code = "SRD";
            name = "Suriname Dollar";
            symbol = "$";
        } else if (country_code.equals("TH")) {
            code = "THB";
            name = "Thailand Baht";
            symbol = "฿";
        } else if (country_code.equals("TT")) {
            code = "TTD";
            name = "Trinidad and Tobago Dollar";
            symbol = "TT$";
        } else if (country_code.equals("TW")) {
            code = "TWD";
            name = "Taiwan New Dollar";
            symbol = "NT$";
        } else if (country_code.equals("TZ")) {
            code = "TZS";
            name = "Tanzanian Shilling";
            symbol = "TSh";
        } else if (country_code.equals("UA")) {
            code = "UAH";
            name = "Ukraine Hryvnia";
            symbol = "₴";
        } else if (country_code.equals("UG")) {
            code = "UGX";
            name = "Ugandan Shilling";
            symbol = "USh";
        } else if (country_code.equals("UY")) {
            code = "UYU";
            name = "Uruguay Peso";
            symbol = "$U";
        } else if (country_code.equals("VE")) {
            code = "VEF";
            name = "Venezuela Bolívar";
            symbol = "Bs";
        } else if (country_code.equals("VN")) {
            code = "VND";
            name = "Viet Nam Dong";
            symbol = "₫";
        } else if (country_code.equals("YE")) {
            code = "YER";
            name = "Yemen Rial";
            symbol = "﷼";
        } else if (country_code.equals("ZA")) {
            code = "ZAR";
            name = "South Africa Rand";
            symbol = "R";
        } else {
            code = "USD";
            name = "United States Dollar";
            symbol = "$";
        }

        Map<String, Object> user = new HashMap<>();
        user.put("currency_code", code);
        user.put("currency_symbol", symbol);
        user.put("currency_name", name);

        db.collection("Users").document(UID).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(VerifyPhoneActivity.this, EditProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

}
