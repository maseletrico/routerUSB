package com.marco.smartrouterdev;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import static com.marco.smartrouterdev.MainActivity.qrCodeReaderRunning;
import static com.marco.smartrouterdev.R.string.hodometro;

//import static com.marco.smartrouterdev.MainActivity.DISPOSITIVO_BLOQUEADO;
//import static com.marco.smartrouterdev.MainActivity.NIVEL_SUPERVISOR;


public class QrCodeReader extends AppCompatActivity {


    private SurfaceView cameraView;
    private TextView barcodeInfo;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    public static String operatorID;
    public static String operatorName;
    public static String exameMedicoData;
    public static String treinamentoData;
    public static String maquinaName;
    public static String senhaHttpID;
    public static String maquinaID;
    public static String empresaName;
    public static String maquinaHorimetro;
    public static String maquinaHodometro;
    public static int QrType;
    public static boolean isMaquina = false;
    public static boolean isOperador = false;
    public static boolean isAjusteHorimetro = false;
    public static boolean isAbastecimento = false;
    public static boolean isMecanico = false;

    private int qrCodeAction;
    //private static int AJUSTE_HORIMETRO = 100;
    //Indicador de registro de máquina para o método QrCode()
    //private static int REGISTRO_MAQUINA = 200;
    //Indicador de registro de operador para o método QrCode()
    //private static int REGISTRO_OPERADOR = 300;
    //horimetro extras
    private  float horimetroExtra;
    //hodometro extras
    private float hodometroExtra;

    private static final String QR_CODE_OPERADOR = "Injetec Operador";
    private static final String QR_CODE_MAQUINA = "Injetec Maquina";
    private static final String QR_HORIMETRO_HODOMETRO = "Injetec Horimetro";
    private static final String QR_ABASTECIMENTO = "Injetec Abastecimento";
    private static final String QR_MECANICO = "Injetec Mecanico";
    public static final int NOVO_REGISTRO = 0;
    public static final int HORIMETRO_HODOMETRO = 1;
    public static final int HORIMETRO = 2;
    public static final int HODOMETRO = 3;
    public static final int ID_MAQUINA = 4;
    public static final int ID_EMPRESA = 5;
    public static final int AJUSTE_TOTAL = 6;
    private RelativeLayout qrCodeLayout;
    private Context contextDialog;

    private static final int MY_REQUEST_CAMERA = 100;

    // To keep track of activity's window focus
    private boolean currentFocus;
    // To keep track of activity's foreground/background status
    private boolean isPaused;
    // handler para ocultar status bar
    private Handler collapseNotificationHandler;


    //Resultado do pedido de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(contextDialog, "Uso da Câmera Permitido! ", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(contextDialog, "Uso da Câmera Negado! ", Toast.LENGTH_SHORT).show();
                }
                //return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_reader);
        //get intent extras
        Intent intent = getIntent();
        qrCodeAction = intent.getIntExtra("AÇÃO",-1);
        horimetroExtra = intent.getFloatExtra("HORIMETRO",-1);
        hodometroExtra = intent.getFloatExtra("HODOMETRO", -1);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //indica que a activity está rodando
        qrCodeReaderRunning = true;
        Log.d("PÓS QR_CODE", "onResume");
        //tela cheia, sem status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        View decorView = getWindow().getDecorView();
//        // Hide both the navigation bar and the status bar.
//        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//        // a general rule, you should design your app to hide the status bar whenever you
//        // hide the navigation bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (TextView) findViewById(R.id.code_info);

        qrCodeLayout = (RelativeLayout) findViewById(R.id.qr_code_layout);
        contextDialog = qrCodeLayout.getContext();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(1)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {



            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        // Check Permissions Now

                        //  Esse método retorna true se o usuário já tiver negado uma solicitação de permissão realizada pelo aplicativo.
                        if(ActivityCompat.shouldShowRequestPermissionRationale(QrCodeReader.this, Manifest.permission.CAMERA)){
                            //Log.i("PERMITION SHOULD",getApplicationContext().toString());
                        }else{
                            //casp cpntrário peça a permissão
                            ActivityCompat.requestPermissions(QrCodeReader.this,new String[]{Manifest.permission.CAMERA},MY_REQUEST_CAMERA);
                        }



                        //Log.i("QR_CODE","PERMISSION "+getApplicationContext().toString());
                        return;
                    }
                    //Log.i("QR_CODE","SURFACE CREATED ");
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    //Log.e("QR_CODE","CAMERA SOURCE "+ ie.getMessage());
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e("QR_CODE","surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //Log.e("QR_CODE","surfaceDestroyed");
                cameraSource.stop();
//                if (cameraSource != null) {
//                    cameraSource.release();
//                    cameraSource = null;
//                }

            }
        });



        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            boolean stopBarcodeReader = false;
            @Override
            public void release() {
                //Log.d("QR_CODE","RELEASE");
                //cameraSource.stop();
                //cameraSource.release();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                //se pos chave estiver desligado, retorna para main activit
                if(MainActivity.posChave.equals("1")){
                    stopBarcodeReader = true;
                    finish();
                }
                if (barcodes.size() != 0) {
                    //Alerta sonoro de Bar code lido
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            //verifica se oqr code detectado é do tipo operador
                            String lines[] = barcodes.valueAt(0).displayValue.split(",");
                            if (lines[0].equals(QR_CODE_OPERADOR) ) {
                                barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                                operatorName = lines[1];
                                operatorID = lines[2];
                                isOperador=true;
                                //verifica se o QrCode tem data do exame médico e treinamento
                                if(lines.length>=4){
                                    exameMedicoData = lines[3];
                                    treinamentoData = lines[4];
//                                    if(lines[3].equals("Admin")) {
//                                        NIVEL_SUPERVISOR = true;
//                                        DISPOSITIVO_BLOQUEADO=false;
//                                    }else{
//                                        NIVEL_SUPERVISOR = false;
//                                    }
                                }
                                barcodeDetector.release();

                                //finalisa o barcodeInfo runnable
                                barcodeInfo.removeCallbacks(this);
                                finish();
                            } else if (lines[0].equals(QR_CODE_MAQUINA)) {

                                cameraSource.stop();

                                barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                                maquinaID = lines[1];
                                empresaName = lines[3];
                                maquinaName = lines[2];
                                maquinaHorimetro = lines[4];
                                maquinaHodometro = lines[5];
                                senhaHttpID = lines[6];
                                isMaquina = true;


                                //Seleciona título do AlertDialog
                                String tituloAlertDialog = "NOVO REGISTRO";
                                int selectAlertDialog = NOVO_REGISTRO;
                                //ajuste de horímetro e hodômetro
                                if (maquinaHorimetro.equals("-1") && maquinaHodometro.equals("-1")) {
                                    tituloAlertDialog = "AJUSTES DE HORÍMETRO E HODÔMETRO";
                                    selectAlertDialog = HORIMETRO_HODOMETRO;
                                    //ajuste de horímetro
                                } else if (maquinaHorimetro.equals("-1") && !maquinaHodometro.equals("-1")) {
                                    tituloAlertDialog = "AJUSTE DE HORÍMETRO";
                                    selectAlertDialog = HORIMETRO;
                                    //ajuste de hodômetro
                                } else if (!maquinaHorimetro.equals("-1") && maquinaHodometro.equals("-1")) {
                                    tituloAlertDialog = "AJUSTE DE HODÔMETRO";
                                    selectAlertDialog = HODOMETRO;
                                } else if (maquinaName.equals("-1")) {
                                    tituloAlertDialog = "IDENTIFICADOR MÁQUINA/VEÍCULO";
                                    selectAlertDialog = ID_MAQUINA;
                                } else if (empresaName.equals("-1")) {
                                    tituloAlertDialog = "IDENTIFICADOR EMPRESA";
                                    selectAlertDialog = ID_EMPRESA;
                                } else if (maquinaName.equals("nova")) {
                                    tituloAlertDialog = "NOVO DISPOSITIVO";
                                    selectAlertDialog = AJUSTE_TOTAL;
                                }
                                dialogBox(tituloAlertDialog, selectAlertDialog);
                            } else if (lines[0].equals(QR_HORIMETRO_HODOMETRO)) {

                                cameraSource.stop();

                                barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                                maquinaHorimetro = lines[1];
                                maquinaHodometro = lines[2];
                                isAjusteHorimetro = true;


                                //Seleciona título do AlertDialog
                                String tituloAlertDialog = "AJUSTE DE HORÍMETRO";
                                int selectAlertDialog = NOVO_REGISTRO;
                                //ajuste de horímetro e hodômetro
                                if (maquinaHorimetro.equals("-1") && maquinaHodometro.equals("-1")) {
                                    tituloAlertDialog = "AJUSTES DE HORÍMETRO E HODÔMETRO";
                                    selectAlertDialog = HORIMETRO_HODOMETRO;
                                    dialogBox(tituloAlertDialog, selectAlertDialog);
                                }

                            } else if (lines[0].equals(QR_ABASTECIMENTO)) {
                                cameraSource.stop();
                                barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                                operatorName = lines[1];
                                operatorID = lines[2];
                                isAbastecimento = true;
                                //verifica se o QrCode tem data do exame médico e treinamento
                                if(lines.length>=4){
                                    exameMedicoData = lines[3];
                                    treinamentoData = lines[4];
                                }
                                finish();

                            } else if (lines[0].equals(QR_MECANICO)) {
                                cameraSource.stop();
                                barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                                operatorName = lines[1];
                                operatorID = lines[2];
                                isMecanico = true;
                                //verifica se o QrCode tem data do exame médico e treinamento
                                if(lines.length>=4){
                                    exameMedicoData = lines[3];
                                    treinamentoData = lines[4];
                                }
                                finish();

                            } else{
                                cameraSource.stop();
                                //bar code não identificado
                                new AlertDialog.Builder(qrCodeLayout.getContext())
                                        .setTitle("Código Não Reconhecido!")
                                        .setMessage("Etiqueta ilegível, verifique se está suja ou arranhada. " +
                                                "Caso persista o erro entre em contato com o fornecedor.")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue on click
                                                recreate();

                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();


                            }
                        }
                    });
                //Bar code size = 0
                }else{
                    operatorName = "QrCode Vazio";
                    operatorID = "0";
                    exameMedicoData = "00/00/0000";
                    treinamentoData = "00/00/0000/";
                }

            }
        });

    }

    public void dialogBox (String title, final int select){

        //Layout de Dialog Box
        final LinearLayout alertDialoglayout = new LinearLayout(contextDialog);
        alertDialoglayout.setOrientation(LinearLayout.VERTICAL);

        //Titulo horímetro
        final TextView tvTitulotHorimetro = new TextView(contextDialog);
        tvTitulotHorimetro.setTextSize(20);
        tvTitulotHorimetro.setText(R.string.horimetro_string);

        //Edit Text horimetro
        final EditText editTextHorimetro = new EditText(contextDialog);
        editTextHorimetro.setHeight(50);
        editTextHorimetro.setWidth(340);
        editTextHorimetro.setKeyListener(new DigitsKeyListener());
        editTextHorimetro.setTextSize(18);
        editTextHorimetro.setPadding(2,2,2,2);
        editTextHorimetro.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        editTextHorimetro.setGravity(Gravity.CENTER_HORIZONTAL);
        editTextHorimetro.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextHorimetro.setText(String.format(Locale.ENGLISH,"%.1f", horimetroExtra));

        //Titulo hodômetro
        final TextView tvTitulotHodometro = new TextView(contextDialog);
        tvTitulotHodometro.setTextSize(20);
        tvTitulotHodometro.setText(hodometro);

        //Edit Text Hodometro
        final EditText editTextHodometro = new EditText(contextDialog);
        editTextHodometro.setHeight(50);
        editTextHodometro.setWidth(340);
        editTextHodometro.setKeyListener(new DigitsKeyListener());
        editTextHodometro.setTextSize(18);
        editTextHodometro.setPadding(2,2,2,2);
        editTextHodometro.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        editTextHodometro.setGravity(Gravity.CENTER_HORIZONTAL);
        editTextHodometro.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextHodometro.setText(String.format(Locale.ENGLISH,"%.1f", hodometroExtra));

        //Titulo ID maquina
        final TextView tvTitulotIdMaquina = new TextView(contextDialog);
        tvTitulotIdMaquina.setTextSize(20);
        tvTitulotIdMaquina.setText(R.string.id_maq_veiculo);

        //Titulo ID empresa
        final TextView tvTitulotIdEmpresa = new TextView(contextDialog);
        tvTitulotIdEmpresa.setTextSize(20);
        tvTitulotIdEmpresa.setText(R.string.nome_empresa);

        //Edit Text ID Maquina
        final EditText editTextIdMaquina = new EditText(contextDialog);
        editTextIdMaquina.setHeight(50);
        editTextIdMaquina.setWidth(340);
        //editTextIdMaquina.setKeyListener(new DigitsKeyListener());
        editTextIdMaquina.setTextSize(18);
        editTextIdMaquina.setPadding(2,2,2,2);
        editTextIdMaquina.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        editTextIdMaquina.setGravity(Gravity.CENTER_HORIZONTAL);
        editTextIdMaquina.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //Edit Text ID Empresa
        final EditText editTextIdEmpresa = new EditText(contextDialog);
        editTextIdEmpresa.setHeight(50);
        editTextIdEmpresa.setWidth(340);
       // editTextIdEmpresa.setKeyListener(new DigitsKeyListener());
        editTextIdEmpresa.setTextSize(18);
        editTextIdEmpresa.setPadding(2,2,2,2);
        editTextIdEmpresa.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        editTextIdEmpresa.setGravity(Gravity.CENTER_HORIZONTAL);
        editTextIdEmpresa.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //Adiciona views ao layout
        switch (select){
            case HORIMETRO_HODOMETRO:
                //pega valor de horimetro
                //editTextHorimetro.setText(String.format(Locale.ENGLISH,"%.1f",));
                alertDialoglayout.addView(tvTitulotHorimetro);
                alertDialoglayout.addView(editTextHorimetro);
                alertDialoglayout.addView(tvTitulotHodometro);
                alertDialoglayout.addView(editTextHodometro);
                break;
            case HODOMETRO:
                alertDialoglayout.addView(tvTitulotHodometro);
                alertDialoglayout.addView(editTextHodometro);
                break;
            case HORIMETRO:
                alertDialoglayout.addView(tvTitulotHorimetro);
                alertDialoglayout.addView(editTextHorimetro);
                break;
            case ID_MAQUINA:
                alertDialoglayout.addView(tvTitulotIdMaquina);
                alertDialoglayout.addView(editTextIdMaquina);
                break;
            case ID_EMPRESA:
                alertDialoglayout.addView(tvTitulotIdEmpresa);
                alertDialoglayout.addView(editTextIdEmpresa);
                break;
            case AJUSTE_TOTAL:
                alertDialoglayout.addView(tvTitulotIdEmpresa);
                alertDialoglayout.addView(editTextIdEmpresa);
                alertDialoglayout.addView(tvTitulotIdMaquina);
                alertDialoglayout.addView(editTextIdMaquina);
                alertDialoglayout.addView(tvTitulotHorimetro);
                alertDialoglayout.addView(editTextHorimetro);
                alertDialoglayout.addView(tvTitulotHodometro);
                alertDialoglayout.addView(editTextHodometro);
                break;
            case NOVO_REGISTRO:
                alertDialoglayout.addView(tvTitulotIdEmpresa);
                alertDialoglayout.addView(editTextIdEmpresa);
                alertDialoglayout.addView(tvTitulotIdMaquina);
                alertDialoglayout.addView(editTextIdMaquina);
                alertDialoglayout.addView(tvTitulotHorimetro);
                alertDialoglayout.addView(editTextHorimetro);
                alertDialoglayout.addView(tvTitulotHodometro);
                alertDialoglayout.addView(editTextHodometro);

                editTextIdEmpresa.setText(empresaName);
                editTextIdMaquina.setText(maquinaName);
                editTextHorimetro.setText(maquinaHorimetro);
                editTextHodometro.setText(maquinaHodometro);
        }



        new AlertDialog.Builder(contextDialog)
                .setTitle(title)
                .setMessage("")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //finalisa activity e vai para main activity
                        switch (select){
                            case HORIMETRO_HODOMETRO:
                                //verifica se valor é válido para horímetro
                                if(editTextHorimetro.getText().equals("")) {
                                    maquinaHorimetro = "0.0";
                                }else{
                                    maquinaHorimetro = editTextHorimetro.getText().toString();
                                }
                                //verifica se valor é válido para hodometro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHodometro = "0.0";
                                }else{
                                    maquinaHodometro = editTextHodometro.getText().toString();
                                }

                                QrType = HORIMETRO_HODOMETRO;
                                break;
                            case HODOMETRO:
                                //verifica se valor é válido para hodometro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHodometro = "0.0";
                                }else{
                                    maquinaHodometro = editTextHodometro.getText().toString();
                                }
                                QrType = HODOMETRO;
                                break;
                            case HORIMETRO:
                                //verifica se valor é válido para horímetro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHorimetro = "0.0";
                                }else{
                                    maquinaHorimetro = editTextHorimetro.getText().toString();
                                }
                                QrType = HORIMETRO;
                                break;
                            case ID_MAQUINA:
                                empresaName = editTextIdMaquina.getText().toString();
                                QrType = ID_MAQUINA;
                                break;
                            case ID_EMPRESA:
                                maquinaName = editTextIdEmpresa.getText().toString();
                                QrType = ID_EMPRESA;
                                break;
                            case AJUSTE_TOTAL:
                                //verifica se valor é válido para horímetro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHorimetro = "0.0";
                                }else{
                                    maquinaHorimetro = editTextHorimetro.getText().toString();
                                }
                                //verifica se valor é válido para hodometro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHodometro = "0.0";
                                }else{
                                    maquinaHodometro = editTextHodometro.getText().toString();
                                }
                                empresaName = editTextIdMaquina.getText().toString();
                                maquinaName = editTextIdEmpresa.getText().toString();
                                QrType = AJUSTE_TOTAL;
                            case NOVO_REGISTRO:
                                //verifica se valor é válido para horímetro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHorimetro = "0.0";
                                }else{
                                    maquinaHorimetro = editTextHorimetro.getText().toString();
                                }
                                //verifica se valor é válido para hodometro
                                if(editTextHorimetro.equals("")) {
                                    maquinaHodometro = "0.0";
                                }else{
                                    maquinaHodometro = editTextHodometro.getText().toString();
                                }
                                empresaName = editTextIdMaquina.getText().toString();
                                maquinaName = editTextIdEmpresa.getText().toString();
                                  QrType = NOVO_REGISTRO;

                        }

                        finish();
                        try {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling

                                return;
                            }
                            cameraSource.start(cameraView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        isAjusteHorimetro=false;
                        recreate();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)

                .setView(alertDialoglayout)
                .show();


    }


    @Override
    protected void onStop() {
        super.onStop();
        //indica que a activity não está rodando em segundo plano
        qrCodeReaderRunning= false;
        Log.d("PÓS QR_CODE", "onStop");
    }

    //desabilita back button
    @Override
    public void onBackPressed() {
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
