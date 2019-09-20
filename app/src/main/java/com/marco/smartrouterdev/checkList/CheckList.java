package com.marco.smartrouterdev.checkList;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.marco.smartrouterdev.MyPreferences;
import com.marco.smartrouterdev.QrCodeReader;
import com.marco.smartrouterdev.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.marco.smartrouterdev.MainActivity.eventoChecklist;
import static com.marco.smartrouterdev.MainActivity.operadorUltimoCheckList;
import static com.marco.smartrouterdev.MainActivity.ultimoCheckListFeito;
import static com.marco.smartrouterdev.checkList.CheckListAdapter.jsonObjectCheckListEnvioCritico;
import static com.marco.smartrouterdev.checkList.CheckListAdapter.jsonObjectCheckListEnvioGeral;

//import com.marco.smartrouterdev.HttpConnection;
//import org.json.JSONObject;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Iterator;
//import java.util.List;
//
//import static android.R.id.input;
//import static com.marco.smartrouterdev.HttpConnection.*;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayCheckList;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayCintoOnOff;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayOnOff;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayTransmissao;
//import static com.marco.smartrouterdev.MainActivity.jsonObjectInjetec;
//import static com.marco.smartrouterdev.MainActivity.mainActivityContext;
//import static com.marco.smartrouterdev.MainActivity.tbOperadorArray;
//import static com.marco.smartrouterdev.MainActivity.userIdQrCode;

public class CheckList extends AppCompatActivity {

    private RecyclerView.Adapter checkListAdapter;

    public static final String IMPORTTANT_CHAR = "@";

    private MyPreferences myPreferences;

    private Button btnCheckListDone;
    //MainActivity mainActivity;

    // To keep track of activity's window focus
    private boolean currentFocus;
    // To keep track of activity's foreground/background status
    private boolean isPaused;
    // handler para ocultar status bar
    private Handler collapseNotificationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        btnCheckListDone = findViewById(R.id.button_concluido);

        myPreferences = new MyPreferences();
        myPreferences.setContext(CheckList.this);

        //tela cheia
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //status bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        //Mantem tela acesa
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //tela cheia, sem status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        Toolbar myToolbar = findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
//
//        // add back arrow to toolbar
//        if (getSupportActionBar() != null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }

        btnCheckListDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvaChecklistSai();
            }
        });

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        RecyclerView checkListRecyclerView = findViewById(R.id.checkList_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        checkListRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(this);
        checkListRecyclerView.setLayoutManager(recyclerViewLayoutManager);



        try {
            //cria nova Array para receber o check list
            //List<Object> checkListItens = new ArrayList<Object>();
            ArrayList checkListItens = new ArrayList();
            //converte  itens Criticos (lidos da preferences) em jsonArray
            JSONArray jsonCriticos = new JSONArray(myPreferences.getCheckListCriticosAtual());
            //converte a string itens Geral (lida da preferences) em jsonArray
            JSONArray jsonGeral = new JSONArray(myPreferences.getCheckListGearlAtual());

            //adiciona "@"ao final de cada item para sinalizar item crítico e adiona estes itens
            // a array checkListItens, está array é apenas para formar a tela do recycler view
            for (int i=0;i<jsonCriticos.length();i++){
                CheckListData checkListData = new CheckListData();
                checkListData.setCheckListItem(jsonCriticos.getString(i)+IMPORTTANT_CHAR);
                //checkListItens.add(String.valueOf(checkListData));
                checkListItens.add(checkListData);
                //Adiciona e codifica UTF-8, os ítens críticos a jsonObjectCheckListEnvioCritico (Json de envio para servidor)
                try {
                    jsonObjectCheckListEnvioCritico.put( URLEncoder.encode(jsonCriticos.getString(i),"UTF-8"),"Nao");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            for (int i=0;i<jsonGeral.length();i++){
                CheckListData checkListData = new CheckListData();
                checkListData.setCheckListItem(jsonGeral.getString(i));
                checkListItens.add(checkListData);
                //Adiciona e codifica UTF-8, os ítens a jsonObjectCheckListEnvioGeral (Json de envio para servidor)
                try {
                    jsonObjectCheckListEnvioGeral.put(URLEncoder.encode(jsonGeral.getString(i),"UTF8"),"Nao");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            //pega o adapter específico da preferences
            checkListAdapter = new CheckListAdapter(checkListItens);

        } catch (JSONException e) {
            e.printStackTrace();
        }




        checkListRecyclerView.setAdapter(checkListAdapter);
    }

    private void salvaChecklistSai(){
        //Marca o operador do últimocheck list
        operadorUltimoCheckList = Integer.valueOf(QrCodeReader.operatorID);
        //Salva operador do último checkList válido
        myPreferences.setOperdadorDoUltimoCheckList(operadorUltimoCheckList);
        //Salva ültimo Check List Feito

        ultimoCheckListFeito = System.currentTimeMillis()/1000;
        myPreferences.setUltimoCheckListFeito(ultimoCheckListFeito);

        eventoChecklist=true;

        finish(); // close this activity and return to preview activity (if there is any)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {

            salvaChecklistSai();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
        if(!hasFocus){
            // Method that handles loss of window focus
            collapseNow();
        }
    }


    private void collapseNow() {
        // Initialize 'collapseNotificationHandler'
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }
        // If window focus has been lost && activity is not in a paused state
        // Its a valid check because showing of notification panel
        // steals the focus from current activity's window, but does not
        // 'pause' the activity
        //if (!currentFocus && !isPaused) {
        if (!currentFocus) {

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // Use reflection to trigger a method from 'StatusBarManager'

                    Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        //Log.i("Collapse Handler",e.toString());
                    }

                    Method collapseStatusBar = null;

                    try {
                        if (statusBarManager != null) {
                            collapseStatusBar = statusBarManager .getMethod("collapsePanels");
                        }
                        if (collapseStatusBar != null) {
                            collapseStatusBar.setAccessible(true);
                        }

                        try {
                            if (collapseStatusBar != null) {
                                collapseStatusBar.invoke(statusBarService);
                            }
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        // Check if the window focus has been returned
                        // If it hasn't been returned, post this Runnable again
                        // Currently, the delay is 100 ms. You can change this
                        // value to suit your needs.
                        if (!currentFocus && !isPaused) {
                            collapseNotificationHandler.postDelayed(this, 100L);
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            },300L);
        }
    }

}
