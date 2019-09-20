package com.marco.smartrouterdev;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class UserLogin extends AppCompatActivity {

    private Button buttonOk;
    private EditText editTextEmail, editTextPass;
    //private EditText editTextIdMaquina;
    private ProgressBar progressBar;
    private  FirebaseAuth auth;

    private static final int  MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION_FINE = 200;
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION_COARSE = 300;
    private static final int  MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 400;
    private boolean MY_PERMISSIONS_CAMERA=false;
    private boolean MY_PERMISSIONS_LOCATION_FINE=false;
    private boolean MY_PERMISSIONS_LOCATION_COARSE=false;
    private boolean myPermisions=false;
    //instancia da classe MyPpreferences
    MyPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        buttonOk =  findViewById(R.id.btn_ok);
        editTextEmail =  findViewById(R.id.et_email);
        editTextPass =  findViewById(R.id.et_pass);
        //editTextIdMaquina = (EditText) findViewById(R.id.et_idMaquina);
        progressBar =  findViewById(R.id.progressBar);

        myPreferences = new MyPreferences();
        myPreferences.setContext(UserLogin.this);
        permissionRequest();

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = editTextEmail.getText().toString().trim();
                final String password = editTextPass.getText().toString().trim();
                //final String idMaquina = editTextIdMaquina.getText().toString().trim();


                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.msg_enter_email, Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), R.string.msg_enter_password, Toast.LENGTH_LONG).show();
                    return;
                }
//                if (TextUtils.isEmpty(idMaquina)) {
//                    Toast.makeText(getApplicationContext(), R.string.msg_enter_Id_maquina, Toast.LENGTH_LONG).show();
//                    return;
//                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_pass_too_short, Toast.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(UserLogin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(UserLogin.this, "USUÁRIO CRIADO: " + task.isSuccessful(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    String taskException = task.getException().toString();
                                    AlertDialog alertDialog = new AlertDialog.Builder(UserLogin.this)
                                            .setTitle("Falha na Autenticação!")
                                            .setCancelable(false)
                                            .setMessage(taskException)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // call registro de operador

                                                }
                                            })

                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                    //for negative side button
                                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
                                    //for positive side button
                                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));

                                    //Toast.makeText(UserLogin.this, "Authentication failed." + task.getException(),Toast.LENGTH_LONG).show();
                                } else {

                                    Intent intent = new Intent(UserLogin.this,MainActivity.class);
                                    //startActivity(new Intent(UserLogin.this, MainActivity.class));
                                    intent.putExtra("EMAIL", email);
                                    intent.putExtra("PASSWORD", password);
                                    intent.putExtra("PERMISSION", myPermisions);
                                    myPreferences.setMyPermisions(myPermisions);
                                    finish();

                                }
                            }

                        });

                //startActivity(new Intent(UserLogin.this,MainActivity.class));
                //finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void permissionRequest(){
        //Verifica as permissão camera
        if (ContextCompat.checkSelfPermission(UserLogin.this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(UserLogin.this,
                    android.Manifest.permission.CAMERA)) {
                Log.i("MSG PERMISSION", "CAMERA");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(UserLogin.this,
                        new String[]{android.Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
                Log.i("MSG  EXECUÃO PERMISSION", "CAMERA");
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.i("On RequestPermission ",permissions[0]+": "+  grantResults.toString());
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MY_PERMISSIONS_CAMERA=true;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Verifica as permissão location fine
                    if (ContextCompat.checkSelfPermission(UserLogin.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(UserLogin.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Log.i("MSG PERMISSION", "FINE LOCATION");
                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(UserLogin.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION_FINE);
                            Log.i("MSG EXECUÃO PERMISSION", "FINE LOCATION");
                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }

                return;

            case MY_PERMISSIONS_REQUEST_LOCATION_FINE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MY_PERMISSIONS_LOCATION_FINE=true;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Verifica as permissão location coarse

                    //Verifica as permissão location fine
                    if (ContextCompat.checkSelfPermission(UserLogin.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(UserLogin.this,
                                Manifest.permission.CHANGE_WIFI_STATE)) {
                            Log.i("MSG PERMISSION", "CHANGE_WIFI_STATE");
                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(UserLogin.this,
                                    new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE},MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE);
                            Log.i("MSG EXECUÃO PERMISSION", "CHANGE_WIFI_STATE");
                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            case MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Verifica as permissão location coarse

                    //Verifica as permissão location fine
                    if (ContextCompat.checkSelfPermission(UserLogin.this,
                            Manifest.permission.ACCESS_NETWORK_STATE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(UserLogin.this,
                                Manifest.permission.ACCESS_NETWORK_STATE)) {
                            Log.i("MSG PERMISSION", "NETWORK STATE");
                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(UserLogin.this,
                                    new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE},MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE);
                            Log.i("MSG EXECUÃO PERMISSION", "NETWORK STATE");
                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                            myPermisions=true;
                        }
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                myPermisions=true;
                return;

//            case MY_PERMISSIONS_REQUEST_LOCATION_COARSE:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    MY_PERMISSIONS_LOCATION_COARSE=true;
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//
//                return;


        }
//        if(MY_PERMISSIONS_CAMERA && MY_PERMISSIONS_LOCATION_FINE ){
//            userLogin();

//            myPreferences.setMyPermisions(true);
//
//        }
    }
}
