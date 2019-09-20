package com.marco.smartrouterdev;

import android.Manifest;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.marco.smartrouterdev.checkList.CheckList;
import com.marco.smartrouterdev.netwokType.GetNetwork;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cc.mvdan.accesspoint.WifiApControl;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.M;
import static java.lang.Math.PI;
import static java.lang.Math.pow;

//import android.content.pm.ApplicationInfo;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.crashlytics.android.Crashlytics;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements LocationListener,
        SensorEventListener {

    //instancia da classe MyPpreferences
    MyPreferences myPreferences;

    String netWorkStatus;
    String netWorkStatusAnterior;

    private boolean HORIMETRO_ENABLE = false;
    //velocidade lida por GPS
    private boolean GPS_SPEED = true;

    private boolean CINTO_SEGURANÇA_ALARME = false;
    private boolean cintoEventoON = false;
    private boolean cintoEventoOFF = false;

    private boolean ALERT_DIALOG_SHOWING = false;
    //Flag para parar o runnable do socket quando ondestroy for chamado.
    private boolean RUN_SOCKET_STOP = false;

    //Indicador de ajuste de horimetro para o método QrCode()
    private final int AJUSTE_HORIMETRO = 100;
    //Indicador de registro de operador para o método QrCode()
    private final int REGISTRO_OPERADOR = 300;
    //Indicador de registro de Abastecedor para o método QrCode()
    private final int REGISTRO_ABASTECEDOR = 400;

    //Indica se houve ajuste de horímetro via operador
    public boolean ajusteHorimetroFeito = false;

    private long mStartRX = -1;
    private long mStartTX = -1;
    private long mStartALL = -1;

    GregorianCalendar time;

    //indica se a activity QrCodeReader está rodanso
    public static boolean qrCodeReaderRunning = false;

    // String para visualização no logCat
    private final String TAG = MainActivity.class.getSimpleName();

    //FireBase Autenticação
    private FirebaseAuth mAuth;
    private FirebaseUser fbUser;

    //FireBase listener de autenticação
    private FirebaseAuth.AuthStateListener mAuthListener;

    //WiFi
    private WifiManager wifiManager;

    //timer http connection
    Handler httpConnectionHandler = new Handler();
    private int httpConnectionInterval = 180000; //3 mitutos
    private int intervaloTxPoschaveDesligado = 3600000; //1 hora


    //timeout desliga app quando poschave desligado
    private int timeoutPoschave = 3600;// 1 hora
    private int counterPoschave = 0;// contador volátil de tempo de pos chave deslifgado

    //velocidade máxima gauge
    private int velMaxGauge = 0;

    //velocidade média
    Handler velocidadeMediaHandler =  new Handler();
    private double TempoTotalVelocidadeMedia;
    private double distanciaPercorrida=0.0;

    //Eventos de hora efetiva - contando, espera e parado
    private String parametroHoraEfetiva = "INIT";
    private boolean eventoHoraEfetivaGreen=false;
    private boolean eventoHoraEfetivaYellow=false;
    private boolean eventoHoraEfetivaRED=false;

    //socket
    private ServerSocket serverSocket;
    //private Handler updateConversationHandler;
    Thread serverThread = null;
    public  final int SERVER_PORT = 8080;

    private final float OFFSET_PONTEIRO_VELOCIDADE = 45.0f;
    private final float OFFSET_PONTEIRO_TEMPERATURA = 262.0f;

    //Instancias xml
    private TextView textViewHorimetro, textViewHoraEfetiva,
            textViewTemperaturaInstantanea, textViewHorimetroParcial,
            textViewNomeUsuario,textViewNomeMaquina,textViewNomeEmpresa,textViewHodometro,
            textViewHodometroParcial,textViewDataCombustivel,textViewHoraCombustivel,
            textViewCombustivelAbastecido, textViewReleDelay, textViewDataTx,textViewHoraTx,
            textViewDataManutencao,textViewHoraManutencao,textViewImpactoMaximo, textViewTemporario,
            textViewTemporario2,textViewTituloHodometro,textViewValorHodometroUnidade,
            textViewHodometroParcialTitulo,textViewHodParcialUnidade,textViewLitrosUnidade,
            textViewAccuracy,textViewVersion,testTextView;
    private ImageView imageViewPicoVelocidade,imageViewPonteiroVelocidade,imageViewTemperaturaGauge,
            imageViewPonteiroTemperartura,imageViewPontoVelocidadeMedia,imageViewAlertaTemperatura,
            imageViewCadeado,imageViewGaugeVelocidade,imageViewAlertaManutenção,imageViewBatteryLevel,
            imageViewServer,imvCintoSegurança,imvGpsFix,imageViewChamadaManutençao,
            imageViewBombaCombustivel,imageViewQrCode,imageViewCheckList,imageViewHoraEfetiva,imageViewHodometroGauge,
            imageViewVelMediaGauge,imageViewVelPicoGauge,imageViewPonteiroVelMedia,imageViewPonteiroVelPico;
    private ProgressBar pbAcelerometroMax;
    public  ImageView imageViewAccesspoint;
    public  TextView testTextView2;

    //Firebase DataBase - Real-Time Database
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    //Firebase Firestore DataBase Message
    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore fireStoredb = FirebaseFirestore.getInstance();
    //private DocumentReference  mDocRef = FirebaseFirestore.getInstance().document("mensagens");

    //Id user from firebase
    private String userID = "FireBase user init";

    private String userEmail = null;

    private float diametroDoPneuCm;
    private int numPulsosPorVolta;
    private int idMaquinaDB;
    private float horimetro;
    private float horimetroSoftware;
    private float stg = 0.0f;
    private float horimetroEmSegundos;
    private float horimetroAz;
    private int countHorimetro;
    private int countDelayHoraEfetiva =0;
    private int countMaquinaEmMovomento;
    private int socketTimeoutCounter=0;
    private int noHardwareResetCounter=0;
    private int hardwarePerdidoTimeout=3;
    private final float CENTESIMO_HORIMETRO = 0.0002778f;
    private float hodometro;
    private float horimetroParcial;
    private float horaEfetiva;
    private float perimetroDoPneu;
    private double velocidadeInstantanea;
    private double velInst1,velInst2,velInst3,velInst4,velInst5,velInst6,velInst7,velInst8,velInst9;
    private double velocidadeMaxima;
    private double velocidadeMaximaTx;
    private float hodometroParcial;
    private float horaEfetivaPercentual;
    private double velocidadeMedia= 0.0;
    private float velocidadeAnterior= 0.0f;
    private int temperaturaInstantanea,temperaturaMedia, tempInst4,tempInst3,tempInst2,tempInst1;
    private int temperaturaPico;
    private boolean TEMPERATURA_ALARME=false;
    private boolean SENSOR_ABERTO=false;

    private boolean piscaTemperatura;
    private float quantidadeCombustivel;
    private long timeStampCombustivel;
    private long timeStampParadaManutenção;
    private long timeStampManutençãoExecutada;
    private long timeStampAdjHorimetro;
    private float timeStampAtual;
    private float timeStampUltimoHorimetro;
    private String statusModoManut;

    private final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.

    //Status carregador/Bateria
    private boolean isCharging;
    private int battLevel;

    //timer 1 segundo para atualizações de sensores
    Handler handler1s = new Handler();

    private final int REQUEST_WRITE_SETTINGS =1;
    private WifiApControl apControl;

    //Acelerometro
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    public float setPointAcelerometro;

    private float accelX;
    private float accelY;
    private float accelZ;

    public double impactoTotalAnterior = 0.0;
    public double impactoTotalDisplay = 0.0;
    public double impactoTotal= 0.0;
    public int acellMax = 0;

    //Location
    public  final double gravidade = 9.81;
    private Double lastLongitude=0.0;
    private Double lastLatitude=0.0;


    private Double altitude=0.0;
    private Float bearing=0.0f;
    private float GPSaccuracy;
    private int GpsEventCount = 0;
    private final int GPS_EVENT_TIMEOUT = 3;
    //distância mínima para GPS OnLocationChanged em metros
    private int GpsMinDistance = 0;
    //intervalo mínimo para GPS OnLocationChanged em miliseconds
    private int GpsMinTime = 250;

    //Set point temperatura
    private  int setPointTemperatura;
    
    //View de temperatura
//    public  View tempRange1,tempRange2,tempRange3,tempRange4,tempRange5,tempRange6,
//            tempRange7,tempRange8,tempRange9,tempRange10;

    //indicador de status pós chave
    public boolean posChaveStatus = false;
    //flag status de falha ao comandar rele depois de passar o QrCode
    private boolean eventoLiberaRele = false;
    //operador que fez o último check list válido
    public static int operadorUltimoCheckList;
    //Indicador que o operador foi identificado pelo QR code
    private boolean isUserID = false;
    //Nome do operador, lido no QRcode
    private String userName =null;
    //Nome do Abastecedor, lido no QRcode
    private String abastecedorName =null;
    //Nome do Mecânico, lido no QRcode
    private String mecanicoName =null;

    //indicador de exame treinamento vencido
    boolean exameTreinamentoVencido;
    //indicador de exame médico  vencido
    boolean exameMedicoVencido;
    //Dias que faltam para vencer o exame de treinamento
    private int diasFaltantesExameTreinamento;
    //Dias  que faltam para vencer o exame médico
    private int diasFaltantesExameMedico;

    //Id lido no QrCode do operador
    public static int userIdQrCode=-1;
    ////Nome da empresa lido no QrCode do operador
    private String empresaNome=null;
    ////Nome da empresa lido no QrCode do operador
    private String maquinaNome=null;
    ////Senha para conexão http lido no QrCode do operador
    private String senhaHttp=null;
    //Indica estado de pós chave, "1" desligado e "0" ligado - variável public (pode ser vista por outras classes)
    public static String posChave="1";
    private String posChaveAnterior="nada";
    private boolean posChaveEvent=false;
    //Status luz de marcha ré
    private String luzMarcahRe;

    private int valorAD;
    private int numDePulsos200msInt;

    //Status de Sendor Cinto de Segurança
    private String sensorCinto;
    private boolean piscaCinto = false;

    //CheckList
    private int intervaloCheckList;
    private String checkListTrocaOperador;
    public static long ultimoCheckListFeito;
    public static boolean eventoChecklist=false;
    //private long idOperadorDoUltimoCheckList;

    //goto message
    private String gotoMessage="não";
    //rele
    //comando para esp8266 habilitar rele
    public boolean HABILITA_RELE=false;
    //comando para esp8266 desabilitar rele
    public boolean DESABILITA_RELE=false;
    //sirene
    //comando para esp8266 habilitar rele/sirene
    public boolean HABILITA_SIRENE=false;
    //comando para esp8266 desabilitar rele/sirene
    public boolean DESABILITA_SIRENE=false;
    //comando para esp8266 habilitar LED GOTO
    public boolean GOTO_RECEBIDO=false;
    //comando para esp8266 desabilitar LED GOTO
    public boolean GOTO_RESPONDIDO=false;
    // comando para esp8266 desabilitar pisca hora efetiva
    public boolean DESABILITA_HORA_EFETIVA=false;
    // comando para esp8266 habilitar LED hora efetiva
    public boolean HABILITA_HORA_EFETIVA=false;
    // comando para esp8266 habilitar rele/sirene CInto de Segurança
    public boolean HABILITA_SIRENE_CINTO=false;
    // comando para esp8266 desabilitar rele/sirene CInto de Segurança
    public boolean DESABILITA_SIRENE_CINTO=false;
    //comando para esp8266 Ajustar horimetro
    public boolean ESP8266_AJUSTE_HORIMETRO=false;


    //motivo pelo qual rele foi desabilitado - ("impacto" "comando" "checkList" "cercaEletronica" "cinto" "qrCode")
    public String motivoBloqueioRele ="livre";
    //Acionamento de rele por impacto permitido
    public  boolean ACIONA_RELE_PORIMPACTO=false;
    private String statusRele;
    private String fbAcionaReleImpacto;

    public boolean ACIONA_ALERTA_MANUTENÇÃO=false;
    public final int MANUTENÇÃO_OK=100;
    public final int MANUTENÇÃO_ON=200;

    private String email;
    private String password;

    private String dataAbastecimento;
    private String horaAbastecimento;

    private String dataParadaManutenção;
    private String horaParadaManutenção;

    private String dataManutençãoExcutada;
    private String horaManutençãoExecutada;

    private String dataTxServer;
    private String horaTxserver;

    private boolean SOCKET_TIMEOUT=false;
    //private boolean RECONECTAR=false;

    //Battery status check
    public IntentFilter mCheckBatteryStatusFilter;
    public BroadcastReceiver mCheckBatteryStatusReceiver;
    private boolean lowBatteryEvent=false;

    //qrCode Activity intent result
    private final int QR_CODE_RESULTS = 1;



    public static boolean httpTxEvent=false;

    private  int OPERADOR_TIME_OUT;//tempo em segundos
    private  int delayHoraEfetiva;//tempo em segundos
    private  int operadorTimeOutCounter = 0;
    private boolean RELE_TIMEOUT = false;

    private int intervaloUsuario; //tempo em horas

    private Context mainActivityContext;

    private int horaDoTxServer;
    //private int CountThread;

    //Permite transmissão via http
    private boolean HTTP_TX_ENABLED=false;

    // To keep track of activity's window focus
    private boolean currentFocus;
    // To keep track of activity's foreground/background status
    private boolean isPaused;
    // handler para ocultar status bar
    private Handler collapseNotificationHandler;
    private PowerManager powerManager;

    private Socket socket;
    private String read;
    private BufferedReader input;
    private BufferedWriter output;
    private String comandoToESP826;

    private String[] dadosRecebidos = null;
    private String numDePulsosEm200ms = null;
    private String valorAdReferencia1V = null;
    private String statusSinalização = null;
    private String horimetroHardware = null;
    private boolean HORIMETRO_HARDWARE = false;
    private boolean HARDWARE_TURN_OFF = false;

    public static String jsonArrayCriticos;
    public static String jsonArrayGeral;

    private ListenerRegistration registration;

    private static final int  MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION_FINE = 200;
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION_COARSE = 300;
    private boolean MY_PERMISSIONS_CAMERA=false;
    private boolean MY_PERMISSIONS_LOCATION_FINE=false;
    private boolean MY_PERMISSIONS_LOCATION_COARSE=false;
    private boolean myPermisions=false;

    //toca sirene quando cinto está desafivelado e a máquina está em movimento
    private boolean cintoAlarme=false;

    //Modo Limitador de velocidade sem chip
    private boolean limitadorVel=false;

    //Velocidade limite para acionar o alarme por falta de uso do cinto de segurança em Km/h
    private final int VELOCIDADE_ALARME_CINTO=6;

    private boolean eventoTxHardware=false;
    private String eventoRxHardware="Inicio";

    //RSSI
    private String rssi="-1";

    //Versão do App
    private int verCode=0;
    private String version="0.0";

    /*********** Firebase Variables *********/
    long fbDiametroPneu;
    long fbPulsosVolta;
    long fbalarmeTemperatura;
    String fbSetPointAcelerometro;
    String fbCriaAccessPoint;
    String fbVelocidadeGPS;
    String fbUserIdReq;
    long fbIntervaloTxNoPoschave;
    long fbIntervaloTX;
    long fbVelMaxGauge;
    long fbIntervaloCheckList;
    long fbGpsMinTime;
    long fbGpsMinDistance;
    String fbAlertaManutencao;
    String fbCheckListTrocaOperador;
    String fbGotoMessage;
    long fbUltimoCheckListFeito;
    long fbIdOperadorDoUltimoCheckList;
    long operadorTimeout;
    long fbDelayHoraEfetiva;
    long fbTimeoutPoschave;
    long fbHardwarePerdido;
    String fbCintoAlarme;
    String fbLimitadorVel;
    String fbHardwareTimeOut;
    String fbUserName;
    String fbValidUserName;
    long fbIntervaloUserName;
    /*************************************/

    /**************** USB ***************/
    private static final String ACTION_USB_PERMISSION = "com.marco.smartrouterdev.USB_PERMISSION";
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private boolean mPermissionRequestPending;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    private boolean USB_CONNECTED=false;
//    private RefWatcher refWatcher;
//    public static RefWatcher getRefWatcher(Context context) {
//        MainActivity application = (MainActivity) context.getApplicationContext();
//        return application.refWatcher;
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_panel);
        setContentView(R.layout.activity_main);

        /*************** USB *****************/
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessoryList = manager.getAccessoryList();

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usbReceiver, filter);

//        if (getLastNonConfigurationInstance() != null) {
//            mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
//            openAccessory(mAccessory);
//        }

        //final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        //List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

//        if(LeakCanary.isInAnalyzerProcess(this)){
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        //refWatcher = LeakCanary.install(getApplication());
//        LeakCanary.install(getApplication());


//        Fabric.with(this,new Crashlytics());
//        AdvertisingIdClient.Info advID = null;

        //Valor inicial para quando nenhum Qr Code foi passado
        QrCodeReader.operatorID="0";
        //Valor inicial data exame médico
        QrCodeReader.exameMedicoData = "00/00/0000";
        //Valor inicial exame treinamento
        QrCodeReader.treinamentoData = "00/00/0000";

        //Mantem tela acesa
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //wake lock - Keep CPU on
//        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
//                        PowerManager.ACQUIRE_CAUSES_WAKEUP,"MyWakelockTag");
//        wakeLock.acquire();



        //pega main Ativity Context
        mainActivityContext = getApplicationContext();

        myPreferences = new MyPreferences();
        myPreferences.setContext(MainActivity.this);

        if (myPreferences.isID_OPERADOR()) {
            //tela cheia, sem status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            View decorView = getWindow().getDecorView();
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        //Sensores
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }




        mAuth = FirebaseAuth.getInstance();

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        time = new GregorianCalendar();

        Bundle extras = getIntent().getExtras();
        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");
//        if(extras!=null) {
//            myPermisions = extras.getBoolean("PERMISSION", false);
//            myPreferences.setMyPermisions(myPermisions);
//        }


        //**************************************************************************************
        // Instancias xml
        //textviewVelocidadeInstantanea =  findViewById(R.id.tv_velocidade_instantanea);
        //textViewPicoVelocidade =  findViewById(R.id.tv_velocidade_pico);
        imageViewPicoVelocidade =  findViewById(R.id.ponto_vel_max);
        imageViewPonteiroVelocidade =  findViewById(R.id.ponteiro_velocidade);

        //textViewVelocidadeMedia =  findViewById(R.id.tv_velocidade_media);
        textViewHorimetro =  findViewById(R.id.tv_horimetro_total);
        textViewHoraEfetiva =  findViewById(R.id.tv_hora_efetiva);
        textViewHoraEfetiva.setVisibility(View.INVISIBLE);
        //textViewDispositivo =  findViewById(R.id.tv_dispositivo);
        //textViewVelGPS = findViewById(R.id.tv_velocidade_instantanea);

        textViewHorimetroParcial = findViewById(R.id.tv_horimetro_parcial);
        textViewHodometro = findViewById(R.id.tv_valor_hodometrto);
        textViewHodometroParcial =  findViewById(R.id.tv_valor_hod_parcial);

        textViewImpactoMaximo =  findViewById(R.id.tv_impacto_maximo);
        pbAcelerometroMax =  findViewById(R.id.progressBar_acel_max);
        textViewNomeUsuario =  findViewById(R.id.tv_nome_usuario);
        textViewNomeMaquina =  findViewById(R.id.tv_nome_maquina);
        textViewNomeEmpresa =  findViewById(R.id.tv_empresa_id);

        textViewTemporario = findViewById(R.id.textView_temporario);
        textViewTemporario2 = findViewById(R.id.textView_temporario2);

        imageViewBatteryLevel =  findViewById(R.id.iv_battery_level);
        imageViewAccesspoint =  findViewById(R.id.iv_accessPoint);
        imageViewAccesspoint.setVisibility(View.INVISIBLE);

        //linearLayoutTemperartura = (LinearLayout) findViewById(R.id.ll_temperatura);
        //linearLayoutTermometro = (LinearLayout) findViewById(R.id.ll_termometro);

        //temperatura Gauge e Alerta
        imageViewTemperaturaGauge =  findViewById(R.id.imageView_temperatura_gauge);
        imageViewPonteiroTemperartura = findViewById(R.id.ponteiro_temperartura);
        imageViewAlertaTemperatura =  findViewById(R.id.alerta_temperatura);

        //ícone do servidor nuvem
        imageViewServer = findViewById(R.id.imageView_server);

        //Cadeado - Status do Rele
        imageViewCadeado =  findViewById(R.id.image_view_cadeado);
        //Gauge velocímetro
        imageViewGaugeVelocidade =  findViewById(R.id.imageView);
        //Image  View Alerta de Manutenção
        imageViewAlertaManutenção =  findViewById(R.id.alerta_manutenção);

        //Image  View chamada de Manutenção via nuvem
        imageViewChamadaManutençao =  findViewById(R.id.chamada_manutenção);

        //ponto marcador de velocidade media
        imageViewPontoVelocidadeMedia =  findViewById(R.id.ponto_vel_media);
        //Image View Bomba de combustível
        imageViewBombaCombustivel = findViewById(R.id.imageView_gas_pistol);

        //Image View QrCode -
        imageViewQrCode = findViewById(R.id.image_view_qrcode);

        //Image View CheckList
        imageViewCheckList = findViewById(R.id.imageView_CheckList);

        //Image Voew Hora Efetiva
        imageViewHoraEfetiva = findViewById(R.id.image_hora_efetiva);
        imageViewHoraEfetiva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // usbMessage();
            }
        });

        imvGpsFix =  findViewById(R.id.image_view_gps_fix);
        //pbOperadorIdentificado = (ProgressBar) findViewById(R.id.progressBar_id_op);

        //Gauge Velocidade média e pico
        imageViewVelMediaGauge = findViewById(R.id.iv_vel_media);
        imageViewVelPicoGauge = findViewById(R.id.iv_vel_pico);
        imageViewPonteiroVelMedia = findViewById(R.id.iv_ponteiro_media);
        imageViewPonteiroVelPico = findViewById(R.id.iv_ponteiro_pico);

        //TExt view data e hora do abastecimento
        textViewDataCombustivel =  findViewById(R.id.text_view_data_combustivel);
        textViewHoraCombustivel =  findViewById(R.id.text_view_hora_combustivel);
        textViewCombustivelAbastecido =  findViewById(R.id.text_view_combustivel_abastecido);

        //hodometro gauge
        imageViewHodometroGauge = findViewById(R.id.imageView_hodometro_gauge);
        textViewTituloHodometro = findViewById(R.id.textView_titulo_hodometro);
        textViewValorHodometroUnidade = findViewById(R.id.tv_hodometro_unidade);
        textViewHodometroParcialTitulo = findViewById(R.id.textView_hodometro_parcial);
        textViewHodParcialUnidade = findViewById(R.id.tv_hodometro_parcial_unidade);
        textViewLitrosUnidade = findViewById(R.id.tv_litros);
        //TExt view data e hora da manutenção
        textViewDataManutencao =  findViewById(R.id.text_view_data_manutencao);
        textViewHoraManutencao =  findViewById(R.id.text_view_hora_manutencao);

        textViewAccuracy =  findViewById(R.id.tv_accuracy);
        textViewTemperaturaInstantanea =  findViewById(R.id.tv_temperatura_instantanea);

        textViewReleDelay =  findViewById(R.id.tv_rele_delay);
        textViewReleDelay.setVisibility(View.INVISIBLE);

        //Data e hora da última transmissão http
        textViewDataTx =  findViewById(R.id.tv_dataTx);
        textViewHoraTx =  findViewById(R.id.tv_horaTx);

        textViewImpactoMaximo.setText(R.string.zero_g);
        pbAcelerometroMax.setProgress(0);

        imvCintoSegurança =  findViewById(R.id.alerta_cinto_segurança);
        //imvTermometro =  findViewById(R.id.imv_termometro);

        //Text view version
        textViewVersion = findViewById(R.id.tv_version);

        testTextView = findViewById(R.id.textView);
        testTextView2 = findViewById(R.id.textView2);
        testTextView.setText(" USb mensagens.");
        testTextView2.setText("0");
        //Zera acelerometro
        pbAcelerometroMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                impactoTotal=0.0;
                //impactoTotalAnterior=0.0;
                //Zera pico de força G na Tela
                impactoTotalDisplay=0.0;
                pbAcelerometroMax.setProgress(0);
                textViewImpactoMaximo.setText(R.string.zero_g);
            }
        });

        //zera pico de velocidade
        imageViewPicoVelocidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                velocidadeMaxima=0.0f;
                //textViewPicoVelocidade.setText(String.valueOf(String.format("%.0f", velocidadeMaxima)));
                imageViewPicoVelocidade.setRotation((float) velocidadeMaxima);
            }
        });

        //Zera hodometro parcial
        textViewHodometroParcial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hodometroParcial = 0.0f;
                textViewHodometroParcial.setText(String.format(Locale.ENGLISH, "%.1f", hodometroParcial ));
            }
        });

        //Zera horimetro parcial
        textViewHorimetroParcial.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                horimetroParcial = 0.0f;
                textViewHorimetroParcial.setText(String.format(Locale.ENGLISH, "%.1f", horimetroParcial ));

            }
        });

        //Ajuste de horimetro on Long Click Listener
        textViewHorimetro.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //alertAjusteHorimetro();
                qrCodeReader(horimetro,hodometro,AJUSTE_HORIMETRO);
                return false;
            }
        });


        //Abasteceimento click listener
        imageViewBombaCombustivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCodeReader(horimetro,hodometro,REGISTRO_ABASTECEDOR);

            }
        });

        //QrCode click listener
        imageViewQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogQrCode();

            }
        });

        //CkeckList listener
        imageViewCheckList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call checkList Activity
                Intent intent = new Intent(MainActivity.this, CheckList.class);
                startActivity(intent);

            }
        });
        //Server icone listener
        imageViewServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                alertDilaogInfoTel();
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

                Toast.makeText(MainActivity .this, "ültimo Usuário: "+fbUserName, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "INTERVALO USUÁRIO = "+intervaloUsuario+"h", Toast.LENGTH_SHORT).show();
                if(fbHardwareTimeOut != null) {
                    Toast.makeText(MainActivity.this, "Fecha App Hardware.", Toast.LENGTH_SHORT).show();
                }else if(fbHardwareTimeOut.equals(("não"))){
                    Toast.makeText(MainActivity.this, "Não Fecha App Hardware.", Toast.LENGTH_SHORT).show();
                }

                if(fbCintoAlarme != null) {
                    if(fbCintoAlarme.equals("sim")){
                        Toast.makeText(MainActivity.this, "Cinto Segurança Alarme.", Toast.LENGTH_SHORT).show();
                    }else if(fbCintoAlarme.equals(("não"))){
                        Toast.makeText(MainActivity.this, "Cinto Segurança Alarme Desabilitado.", Toast.LENGTH_SHORT).show();
                    }

                }

                if (fbUserIdReq != null) {
                    if (fbUserIdReq.equals("sim")) {
                        Toast.makeText(MainActivity.this, "QrCode REQUIRED.", Toast.LENGTH_SHORT).show();
                    } else if (fbUserIdReq.equals("não")) {
                        myPreferences.setID_OPERADOR(false);
                        Toast.makeText(MainActivity.this, "QrCode ID NOT REQUIRED.", Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(MainActivity.this, "SET POINT TEMP. "+setPointTemperatura+"°C", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "TX INTERVAL. "+httpConnectionInterval/60000+"min", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "TX DESLIGADO. "+intervaloTxPoschaveDesligado/60000+"min", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "HARDWARE TIMEOUT. "+hardwarePerdidoTimeout+"min", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "TIMEOUT POSCHAVE. "+timeoutPoschave/60+"min", Toast.LENGTH_SHORT).show();
                if (fbCriaAccessPoint != null) {
                    if (fbCriaAccessPoint.equals("sim")) {
                        Toast.makeText(MainActivity.this, "CRIAR ACCESS POINT", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "NO ACCESS POINT.", Toast.LENGTH_SHORT).show();
                    }
                }

                if (fbAcionaReleImpacto.equals("sim")) {
                    Toast.makeText(MainActivity.this, "ACIONA RELE IMPACTO", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "NÃO ACIONA RELE IMPACTO", Toast.LENGTH_SHORT).show();
                }

                if (fbVelocidadeGPS != null) {
                    if (fbVelocidadeGPS.equals("sim")) {
                        Toast.makeText(MainActivity.this, "VELOCIDADE POR GPS", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "VELOCIDADE POR PULSO", Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(MainActivity.this, "CHECK LIST INTERVAL. "+intervaloCheckList+"h", Toast.LENGTH_SHORT).show();

                if(checkListTrocaOperador.equals("sim")) {
                    Toast.makeText(MainActivity.this, "CHECK LIST POR TROCA DE OPERADOR.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(MainActivity.this, "GPS Min. Time = "+GpsMinTime+"ms", Toast.LENGTH_SHORT).show();

                if(gotoMessage.equals("sim")){
                    Toast.makeText(MainActivity.this, "MENSAGEM GOTO!", Toast.LENGTH_SHORT).show();
                }
                if(gotoMessage.equals("não")){
                    Toast.makeText(MainActivity.this, "MENSAGEM GOTO DESATIVADA.", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(MainActivity.this, "GPS Min. Distance = "+GpsMinDistance+" metros", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "RELÉ DELAY "+OPERADOR_TIME_OUT+"Segundos", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "DELAY HORA EFETIVA "+delayHoraEfetiva+"Segundos", Toast.LENGTH_SHORT).show();
                //mostra email cadastrado no firebase
                Toast.makeText(MainActivity.this, "USER EMAIL "+ userEmail, Toast.LENGTH_SHORT).show();
//                Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//                i.putExtra("android.intent.extra.KEY_CONFIRM", true);
//                startActivity(i);

                //Teste restart Aplicativo
//                Intent i = getBaseContext().getPackageManager()
//                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);

//                PackageManager packageManager = getBaseContext().getPackageManager();
//                Intent intent = packageManager.getLaunchIntentForPackage(getBaseContext().getPackageName());
//                ComponentName componentName = intent.getComponent();
//                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
//                getBaseContext().startActivity(mainIntent);
//                Runtime.getRuntime().exit(0);
//                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });

        //Firestore teste de menssagem
        imageViewCadeado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //se goto message está habilitada então, proceda
                if (gotoMessage.equals("sim")) {
                    // Create a new user with a first and last name
                    Map<String, Object> mMapDocument = new HashMap<>();
                    mMapDocument.put("msgFrom", idMaquinaDB);
                    mMapDocument.put("msgName", userName);
                    mMapDocument.put("msgTo", "Dpto Carga");
                    mMapDocument.put("msgMaquinaName", maquinaNome);
                    mMapDocument.put("message", "msg recebida");
                    mMapDocument.put("tipo", "caixa de diálogo");
                    mMapDocument.put("timestamp", FieldValue.serverTimestamp());

                    // Add a new document with a generated ID
                    fireStoredb.collection("mensagens")
                            .add(mMapDocument)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(mainActivityContext, "FireStore adicionado com Sucesso!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mainActivityContext, "Erro ao Adicionar FireStore! " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
//
                }else{
                    Toast.makeText(mainActivityContext, "MENSAGEM GOTO DESABILITADA!", Toast.LENGTH_SHORT).show();
                }
            }
        });

         /*
                AUTORIZAÇÃO DO FIREBASE
         */
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbUser = firebaseAuth.getCurrentUser();
                if (fbUser != null) {
                    // fbUser is signed in
                    userID = fbUser.getUid();
                    userEmail = fbUser.getEmail();
                    //se ainda não foi criado, Cria o campos no firebase
                    if(!myPreferences.isFB_CREATED()) {
                        mRootRef.child(userID).child("email").setValue(userEmail);
                        //Tela de ID do operador
                        mRootRef.child(userID).child("idOpRequired").setValue("sim");
                        //Diâmetro de pneu
                        mRootRef.child(userID).child("diametroPneu").setValue(70);
                        //Pulsos por volta
                        mRootRef.child(userID).child("pulsosVolta").setValue(40);
                        //Horímetro
                        mRootRef.child(userID).child("horimetro").setValue(0);
                        //Hodometro
                        mRootRef.child(userID).child("hodometro").setValue(0);
                        //alarme temperatura
                        mRootRef.child(userID).child("alarmeTemperatura").setValue(85);
                        //alarme velocidade
                        mRootRef.child(userID).child("alarmeVelocidade").setValue(14);
                        //aviso Cinto de Segurança
                        mRootRef.child(userID).child("avisoCintoSeg").setValue("sim");
                        //cria access point
                        mRootRef.child(userID).child("criaAccessPoint").setValue("sim");
                        //cria nome maquina
                        mRootRef.child(userID).child("nomeMaquina").setValue("E00");
                        //pico velocide
                        mRootRef.child(userID).child("picoVelocidade").setValue(12);
                        //Acelerometro Set Point
                        mRootRef.child(userID).child("setPointAcelerometro").setValue("1.6f");
                        //Velocidade por GPS ou Pulsos Set Point
                        mRootRef.child(userID).child("velocidadeGPS").setValue("sim");
                        //Firebase data ready
                        mRootRef.child(userID).child("isData").setValue("sim");
                        //Firebase Intervalo entre transmissÕes
                        mRootRef.child(userID).child("intervaloTX").setValue(180000);
                        //Acionamento rele por impacto
                        mRootRef.child(userID).child("acionaRelePorImpacto").setValue("não");
                        //Alerta de manutenção
                        mRootRef.child(userID).child("alertaManutencao").setValue("não");
                        //Acionamento rele por impacto
                        mRootRef.child(userID).child("vMaxGauge").setValue(30);
                        //Intervlo Check List
                        mRootRef.child(userID).child("intervaloCheckList").setValue(8);
                        //Check List por troca de operador
                        mRootRef.child(userID).child("checkListTrocaOperador").setValue("sim");
                        //ültimo Check List Feito
                        mRootRef.child(userID).child("ultimoCheckListFeito").setValue(0);
                        //operador do último checkList
                        mRootRef.child(userID).child("idOperadorDoUltimoCheckList").setValue(0);
                        //Relay Delay
                        mRootRef.child(userID).child("delayHoraEfetiva").setValue(15);
                        //Relay Delay
                        mRootRef.child(userID).child("releDelay").setValue(30);
                        //intervalo de transmissão com poschave desligado
                        mRootRef.child(userID).child("intervaloTxPosChaveOff").setValue(2400000);
                        //mensagem goto
                        mRootRef.child(userID).child("gotomessage").setValue("sim");
                        //Distancia mínima para atualização do GPS
                        mRootRef.child(userID).child("gpsMinDistance").setValue(0);
                        //Tempo mínimo para atualização do GPS
                        mRootRef.child(userID).child("gpsMinTime").setValue(1000);
                        //Timeout para desligar o app depois de pos chave desligado
                        mRootRef.child(userID).child("timeoutPoschave").setValue(3600);
                        //Alarme cinto segurança
                        mRootRef.child(userID).child("cintoAlarme").setValue("sim");
                        //modo limitador velocidade
                        mRootRef.child(userID).child("limitadorVel").setValue("não");
                        //Reset Hardware não encontrado
                        mRootRef.child(userID).child("hardwarePerdido").setValue(3);
                        //Fecha App por time out
                        mRootRef.child(userID).child("appOff").setValue("não");
                        //Intervalo de nome do usuário( em Horas), usado só em caso de não requerer qrCode
                        mRootRef.child(userID).child("intervaloUsuário").setValue(8);
                        //User name
                        mRootRef.child(userID).child("userName").setValue("Não Identificado");

                        //set boolean fireBase criado
                        myPreferences.setFB_CREATED(true);
                        //Log.d(TAG, "onAuthStateChanged:signed_in");
                        recreate();
                    }
                } else {
                    // fbUser is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                    //permissionRequest();
                    userLogin();

                }

            }
        };

        /*
                        FIREBASE EVENT LISTENER
         */

        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    //dataSnapshot.exists();

                    fbDiametroPneu = (long) dataSnapshot.child(userID).child("diametroPneu").getValue();
                    fbPulsosVolta = (long) dataSnapshot.child(userID).child("pulsosVolta").getValue();
                    fbalarmeTemperatura = (long) dataSnapshot.child(userID).child("alarmeTemperatura").getValue();
                    fbSetPointAcelerometro = (String) dataSnapshot.child(userID).child("setPointAcelerometro").getValue();
                    fbCriaAccessPoint = (String) dataSnapshot.child(userID).child("criaAccessPoint").getValue();
                    fbVelocidadeGPS = (String) dataSnapshot.child(userID).child("velocidadeGPS").getValue();
                    fbUserIdReq = (String) dataSnapshot.child(userID).child("idOpRequired").getValue();
                    fbAcionaReleImpacto = (String) dataSnapshot.child(userID).child("acionaRelePorImpacto").getValue();
                    fbIntervaloTX = (long) dataSnapshot.child(userID).child("intervaloTX").getValue();
                    fbIntervaloTxNoPoschave = (long) dataSnapshot.child(userID).child("intervaloTxPosChaveOff").getValue();
                    fbVelMaxGauge = (long) dataSnapshot.child(userID).child("vMaxGauge").getValue();
                    fbIntervaloCheckList = (long) dataSnapshot.child(userID).child("intervaloCheckList").getValue();
                    fbGpsMinTime = (long) dataSnapshot.child(userID).child("gpsMinTime").getValue();
                    fbGpsMinDistance = (long) dataSnapshot.child(userID).child("gpsMinDistance").getValue();
                    fbAlertaManutencao = (String) dataSnapshot.child(userID).child("alertaManutencao").getValue();
                    fbCheckListTrocaOperador = (String) dataSnapshot.child(userID).child("checkListTrocaOperador").getValue();
                    fbGotoMessage = (String) dataSnapshot.child(userID).child("gotomessage").getValue();
                    fbUltimoCheckListFeito = (long) dataSnapshot.child(userID).child("ultimoCheckListFeito").getValue();
                    fbIdOperadorDoUltimoCheckList = (long) dataSnapshot.child(userID).child("idOperadorDoUltimoCheckList").getValue();
                    operadorTimeout = (long) dataSnapshot.child(userID).child("releDelay").getValue();
                    fbDelayHoraEfetiva = (long) dataSnapshot.child(userID).child("delayHoraEfetiva").getValue();
                    fbTimeoutPoschave = (long) dataSnapshot.child(userID).child("timeoutPoschave").getValue();
                    fbHardwarePerdido = (long) dataSnapshot.child(userID).child("hardwarePerdido").getValue();
                    fbCintoAlarme = (String) dataSnapshot.child(userID).child("cintoAlarme").getValue();
                    fbLimitadorVel = (String) dataSnapshot.child(userID).child("limitadorVel").getValue();
                    fbHardwareTimeOut = (String) dataSnapshot.child(userID).child("appOff").getValue();
                    fbUserName = (String) dataSnapshot.child(userID).child("userName").getValue();
                    fbValidUserName = (String) dataSnapshot.child(userID).child("validUserName").getValue();
                    fbIntervaloUserName = (long) dataSnapshot.child(userID).child("intervaloUsuário").getValue();

                    if(fbUserName != null) {
                        myPreferences.setLastUserName(fbUserName);

                    }

                    intervaloUsuario =(int) fbIntervaloUserName;
                    myPreferences.setIntervaloUserName(intervaloUsuario);


                    if(fbValidUserName != null) {
                        if(fbValidUserName.equals("sim")){
                            myPreferences.setValidUserName(true);
                        }else if(fbValidUserName.equals(("não"))){
                            myPreferences.setValidUserName(false);
                        }

                    }

                    if(fbHardwareTimeOut != null) {
                        if(fbHardwareTimeOut.equals("sim")){
                            HARDWARE_TURN_OFF=true;
                            myPreferences.setHardwareTurnOff(true);
                        }else if(fbHardwareTimeOut.equals(("não"))){
                            HARDWARE_TURN_OFF=false;
                            myPreferences.setHardwareTurnOff(false);
                        }
                    }

                    if(fbLimitadorVel != null) {
                        if(fbLimitadorVel.equals("sim")){
                              telaModoLimitador();
                        }else if(fbLimitadorVel.equals(("não"))){
                            telaModoSmatLogger();
                        }

                    }

                    if(fbCintoAlarme != null) {
                        if(fbCintoAlarme.equals("sim")){
                            // To save value in Shared Preferences
                            myPreferences.setCintoAlarme(true);
                            cintoAlarme=true;
                        }else if(fbCintoAlarme.equals(("não"))){
                            // To save value in Shared Preferences
                            myPreferences.setCintoAlarme(false);
                            cintoAlarme=true;
                        }

                    }

                    if (fbUserIdReq != null) {
                        if (fbUserIdReq.equals("sim")) {
                            // To save value in Shared Preferences
                            myPreferences.setID_OPERADOR(true);
                        } else if (fbUserIdReq.equals("não")) {
                            // To save value in Shared Preferences
                            myPreferences.setID_OPERADOR(false);
                        }
                    }
                    //carrega settings de Diametro e Pulsos por volta do Firbase
                    diametroDoPneuCm = (int) fbDiametroPneu;
                    numPulsosPorVolta = (int) fbPulsosVolta;
                    //salva diametro do pneu e número de pulsos por volta em shared preferenences
                    myPreferences.setNumDePulsosPorVolta(numPulsosPorVolta);
                    myPreferences.setDiametroDoPneu(diametroDoPneuCm);
                    //Calculo do perímetro do pneu em metros
                    CalculaPerimetro();
                    //salva set point de temperatura em shared preferences
                    setPointTemperatura = (int) fbalarmeTemperatura;
                    myPreferences.setAlarmeTemperatura(setPointTemperatura);
                    //salva set point de Acelerometro em shared preferences
                    setPointAcelerometro = Float.valueOf(fbSetPointAcelerometro);
                    myPreferences.setSetPointAcelerometro(setPointAcelerometro);
                    //Salva intervalo de transmissão em shared preferences
                    httpConnectionInterval =(int) fbIntervaloTX;
                    myPreferences.setIntervaloTx(httpConnectionInterval);
                    //salva intervalo de tx quando poschave desligado
                    intervaloTxPoschaveDesligado = (int) fbIntervaloTxNoPoschave;
                    myPreferences.setIntervaloTxDesligado(intervaloTxPoschaveDesligado);
                    //salva Timeout Hardware Perdido
                    hardwarePerdidoTimeout = (int) fbHardwarePerdido;
                    myPreferences.setHardwareTimeout(hardwarePerdidoTimeout);
                    //salva timeout desligar app quando poschave desligado
                    timeoutPoschave = (int) fbTimeoutPoschave;
                    myPreferences.setTimeoutPoschave(timeoutPoschave);
                    //Salva velocidade máxima Gauge em shared preferences
                    velMaxGauge =(int) fbVelMaxGauge;
                    myPreferences.setVelMaxGauge(velMaxGauge);
                    if(velMaxGauge==180){
                        //imageViewGaugeVelocidade.setImageResource(R.drawable.vel180edit);
                        imageViewGaugeVelocidade.setImageResource(R.drawable.vel180edit_min);
                    }else if (velMaxGauge==25){
                        //imageViewGaugeVelocidade.setImageResource(R.drawable.vel25edit);
                        imageViewGaugeVelocidade.setImageResource(R.drawable.vel25edit_min);
                    }else if (velMaxGauge==30){
                        //imageViewGaugeVelocidade.setImageResource(R.drawable.vel30edit);
                        imageViewGaugeVelocidade.setImageResource(R.drawable.vel30edit_min);
                    }


                    //salva opção de criar o Acces Point ao iniciar o app
                    if (fbCriaAccessPoint != null) {
                        if (fbCriaAccessPoint.equals("sim")) {
                            myPreferences.setAccessPoint(true);
                        } else {
                            myPreferences.setAccessPoint(false);
                        }
                    }

                    //mostra velocidade por GPS ou Pulso
                    if (fbVelocidadeGPS != null) {
                        if (fbVelocidadeGPS.equals("sim")) {
                            myPreferences.setVelocidadeGPS(true);
                            //habilita horimetro para calcular hora efetiva
                            HORIMETRO_ENABLE = true;
                            GPS_SPEED=true;
                        } else {
                            myPreferences.setVelocidadeGPS(false);
                            GPS_SPEED=false;
                        }
                    }

                    //Acionamento do rele
                    if (fbAcionaReleImpacto.equals("sim")) {
                        myPreferences.setAcionaReleImpacto(true);
                    } else {
                        myPreferences.setAcionaReleImpacto(false);
                    }

                    if (fbAlertaManutencao != null) {
                        if (fbAlertaManutencao.equals("sim")) {
                            // To save value in Shared Preferences
                            myPreferences.setALERTA_MANUTENÇÃO(true);
                            ACIONA_ALERTA_MANUTENÇÃO=true;
                            //Toast.makeText(MainActivity.this, "ALERTA MANUTENÇÃO.", Toast.LENGTH_SHORT).show();
                        } else if (fbAlertaManutencao.equals("não")) {
                            // To save value in Shared Preferences
                            myPreferences.setALERTA_MANUTENÇÃO(false);
                            ACIONA_ALERTA_MANUTENÇÃO=false;
                            //Toast.makeText(MainActivity.this, "APAGA ALERTA MANUT.", Toast.LENGTH_SHORT).show();

                        }
                    }
                    //carrega estado do indicador de alerta de manutenção
                    ACIONA_ALERTA_MANUTENÇÃO = myPreferences.isALERTA_MANUTENÇÃO();
                    if(ACIONA_ALERTA_MANUTENÇÃO){
                        //imageViewAlertaManutenção.setVisibility(View.VISIBLE);
                        imageViewAlertaManutenção.setImageResource(R.mipmap.wrench);
                    }else{
                        //imageViewAlertaManutenção.setVisibility(View.INVISIBLE);
                        imageViewAlertaManutenção.setImageResource(R.mipmap.gear_check);
                    }

                    //Salva intervalo de CheckList
                    intervaloCheckList =(int) fbIntervaloCheckList;
                    myPreferences.setIntervaloCheckList(intervaloCheckList);


                    //Salva opção CheckList por troca de operador
                    checkListTrocaOperador = fbCheckListTrocaOperador;
                    myPreferences.setCheckListTrocaOperador(checkListTrocaOperador);


                    //salva intervalo minimo de atualização do GPS
                    GpsMinTime = (int) fbGpsMinTime;
                    myPreferences.setGpsMinTime(GpsMinTime);

                    //salva distância minima de atualização do GPS
                    GpsMinDistance = (int) fbGpsMinDistance;
                    myPreferences.setGpsMinDistance(GpsMinDistance);

                    //Salva opção goto message
                    gotoMessage = fbGotoMessage;
                    myPreferences.setGotoMessage(gotoMessage);


                    //Rele Delay
                    OPERADOR_TIME_OUT = (int) operadorTimeout;
                    myPreferences.setOperadorTimeOut(OPERADOR_TIME_OUT);
                    operadorTimeOutCounter=OPERADOR_TIME_OUT;

                    //Delay Hora efetiva
                    delayHoraEfetiva = (int) fbDelayHoraEfetiva;
                    myPreferences.setDelayHoraEfetiva(delayHoraEfetiva);


                    //Verifica se é para criar o Access Point
                    //accessPoint();

                    //se houver erro ao acessar o firebase
                }catch(Exception e){
                    //Log.i("Firebase Exception ",e.toString());
                    Toast.makeText(MainActivity.this, "erro ao acessar o firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                //Log.d(TAG, "Failed  Firebase onDataChange: " + databaseError);
                Toast.makeText(MainActivity.this, "erro firebase onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

        //Broadcast receiver Battery Status
        mCheckBatteryStatusFilter = new IntentFilter();
        mCheckBatteryStatusFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mCheckBatteryStatusFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mCheckBatteryStatusReceiver = new CheckBatteryStatus();

        //verifica se permissões foram dadas
        myPermisions=myPreferences.isMyPermissions();
        //se as permissões foram dadas
        if(myPermisions) {

            //***************************************************************************************
            //                  GPS LOCATION

            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                    return;
                }
            }

            //gera evento do location listener a cada 250 ms
            if (lm != null) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GpsMinTime, GpsMinDistance, this);
                //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, GpsMinDistance, this);
            }
            //Locations update a cada 10 metros
            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10,this);
            this.onLocationChanged(null);

            //Lê versão do aplicativo (Google Play Store)
            try {
                PackageInfo pInfo = mainActivityContext.getPackageManager().getPackageInfo(getPackageName(), 0);
                verCode = pInfo.versionCode;
                version = pInfo.versionName;
                textViewVersion.setText("V" + version);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            if (myPreferences.isFB_CREATED()) {

                //DISPOSITIVO_BLOQUEADO = myPreferences.isDISPOSITIVO_BLOQUEADO();

                //verifica se dispositivo está bloqueado no nível Admin
//            if(DISPOSITIVO_BLOQUEADO){
//                alertDialog(getString(R.string.titulo_dispositivo_desabilitado),motivoBloqueioRele
//                        + "\n" + getString(R.string.nivel_supervisor_requerido) );
//            }

                //verifica se o user ID é requerido, se for chama QrCode
                if (myPreferences.isID_OPERADOR()) {

                    //qrCodeReader(horimetro, hodometro, REGISTRO_OPERADOR);

                    //TODO procura pelo dispositivo USB para continuar
                    //TODO ou indica que o dispositivo est'a desconectado.

                }

                //Carrega Shared Preferences para Diâmetro de pneu e num. de pulsos por volta
                CarregaDiamPulsos();

                //Calculo do perímetro do pneu em metros
                CalculaPerimetro();

                //Carrega valor de contador de horímetro
                countHorimetro = myPreferences.getCountHorimetro6min();

                //Carrega vlaor de contador de máquina em movimento
                countMaquinaEmMovomento = myPreferences.getCountMaqMovimento();

                //Carrega valor de horímetro
                //horimetro = myPreferences.getHorimetro();
                horimetroSoftware = myPreferences.getHorimetro();
                textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro));

                //Carrega data do Ultimo horimetro salvo
                timeStampUltimoHorimetro = myPreferences.getTimeStampUltimo();

                //Carrega valor de horímetro parcial
                horimetroParcial = myPreferences.getHorimetroParcial();
                textViewHorimetroParcial.setText(String.format(Locale.ENGLISH, "%.1f", horimetroParcial));

                //Carrega valor de hodometro
                hodometro = myPreferences.getHodometro();
                textViewHodometro.setText(String.format(Locale.ENGLISH, "%.1f", hodometro));

                //Carrega valor de hodometro Parcial
                hodometroParcial = myPreferences.getHodometroParcial();
                textViewHodometroParcial.setText(String.format(Locale.ENGLISH, "%.1f", hodometroParcial));

                //Carrega ID da máquina
                idMaquinaDB = myPreferences.getMaquinaId();
                //Carrega nome da máquina
                maquinaNome = myPreferences.getNomeMaquina();
                textViewNomeMaquina.setText(maquinaNome);
                textViewNomeMaquina.append(" ");
                textViewNomeMaquina.append(String.valueOf(idMaquinaDB));


                //Carrega Senha Http
                senhaHttp = myPreferences.getSenhaHttp();

                //Carrega Velocidade máxima do gauge
                velMaxGauge = myPreferences.getVelMaxGauge();
                if (velMaxGauge == 180) {
                    //imageViewGaugeVelocidade.setImageResource(R.drawable.vel180edit);
                    imageViewGaugeVelocidade.setImageResource(R.drawable.vel180edit_min);
                } else if (velMaxGauge == 25) {
                    //imageViewGaugeVelocidade.setImageResource(R.drawable.vel25edit);
                    imageViewGaugeVelocidade.setImageResource(R.drawable.vel25edit_min);
                } else if (velMaxGauge == 30) {
                    //imageViewGaugeVelocidade.setImageResource(R.drawable.vel30edit);
                    imageViewGaugeVelocidade.setImageResource(R.drawable.vel30edit_min);
                }

                //Carrega nome da empresa
                empresaNome = myPreferences.getNomeEmpresa();
                textViewNomeEmpresa.setText(empresaNome);

                //Carrega valor de hora efetiva
                horaEfetiva = myPreferences.getHoraEfetiva();

                //Carrega valor da Horimetro Ajuste de Zero
                horimetroAz = myPreferences.getHorimetroAz();

                calculaMostraHoraEfetiva();

                //carrega valor do set point de temperatura
                setPointTemperatura = myPreferences.getAlarmeTemperatura();

                //carrega valor em minutos do intervalo TX transmissão pos chave desligado
                intervaloTxPoschaveDesligado = myPreferences.getIntervaloTxDesligado();

                //carrega valor em minutos do intervalo TX transmissão pos chave desligado
                timeoutPoschave = myPreferences.getTimeoutPoschave();


                //carrega valor do set point de acelerometro
                setPointAcelerometro = myPreferences.getSetPointAcelerometro();

                //carrega boolean acionamento rele
                ACIONA_RELE_PORIMPACTO = myPreferences.isAcionaReleImpacto();

                //carrega estado do indicador de alerta de manutenção
                ACIONA_ALERTA_MANUTENÇÃO = myPreferences.isALERTA_MANUTENÇÃO();
                if (ACIONA_ALERTA_MANUTENÇÃO) {
                    //imageViewAlertaManutenção.setVisibility(View.VISIBLE);
                    imageViewAlertaManutenção.setImageResource(R.mipmap.wrench);
                } else {
                    //imageViewAlertaManutenção.setVisibility(View.INVISIBLE);
                    imageViewAlertaManutenção.setImageResource(R.mipmap.gear_check);
                }

                //ícone de chamada de manutenção invisivel
                imageViewChamadaManutençao.setVisibility(View.INVISIBLE);

                //Carrega data do último abastecimento
                dataAbastecimento = myPreferences.getDataAbastecimento();
                textViewDataCombustivel.setText(dataAbastecimento);

                //Carrega hora do último abastecimento
                horaAbastecimento = myPreferences.getHoraAbastecimento();
                textViewHoraCombustivel.setText(horaAbastecimento);

                //carrega timeStamp do Abastecimento
                timeStampCombustivel = myPreferences.getTimeStampAbastecimento();

                //careega quantidade de combustível abastecida
                quantidadeCombustivel = myPreferences.getQuantidadeCombAbastecido();
                //mostra quantidade de combustivel
                textViewCombustivelAbastecido.setText(String.valueOf(quantidadeCombustivel));

                //carrega data da última transmissão para o servidor
                dataTxServer = myPreferences.getDataTxServer();

                //carrega hora da última transmissão para o servidor
                horaTxserver = myPreferences.getHoraTxServer();

                //Carrega data da última parada para manutenção
                dataParadaManutenção = myPreferences.getDataParadaManutenção();
                textViewDataManutencao.setText(dataParadaManutenção);

                //Carrega hora da última parada para manutenção
                horaParadaManutenção = myPreferences.getHoraParadaManutenção();
                textViewHoraManutencao.setText(horaParadaManutenção);

                //Carrega timeStamp da parada para manutenção
                timeStampParadaManutenção = myPreferences.getTimeStampManutençãoAberta();

                //Carrega Status manutenção
                statusModoManut = myPreferences.getStatusManutenção();

                //Carrega data de manutenção OK
                dataManutençãoExcutada = myPreferences.getDataManutençãoExecutada();
                textViewDataManutencao.setText(dataManutençãoExcutada);

                //Carrega hora de manutenção OK
                horaManutençãoExecutada = myPreferences.getHoraManutençãoExecutada();
                textViewHoraManutencao.setText(horaManutençãoExecutada);

                //Carrega timeStamp de manutenção executada
                timeStampManutençãoExecutada = myPreferences.getTimeStampManutençãoExecutada();

                //careega intervalo de checkList
                intervaloCheckList = myPreferences.getIntervaloCheckList();

                //careega checkList por troca de operador
                checkListTrocaOperador = myPreferences.getCheckListTrocaOperador();

                //carrega intervalo de atualização GPS
                GpsMinTime = myPreferences.getGpsMinTime();

                //carrega distancia mínima para atualização GPS
                GpsMinDistance = myPreferences.getGpsMinDistance();

                //carrega opção mensagem goto
                gotoMessage = myPreferences.getGotoMessage();

                //careega ültimo Check List Feito
                ultimoCheckListFeito = myPreferences.getUltimoCheckListFeito();

                //careega operador do últimoCheckList
                operadorUltimoCheckList = myPreferences.getOperdadorDoUltimoCheckList();

                //Rele delay / operador time out
                OPERADOR_TIME_OUT = myPreferences.getOperadorTimeOut();
                operadorTimeOutCounter=OPERADOR_TIME_OUT;

                //Carrega Delay Hora Efetiva
                delayHoraEfetiva = myPreferences.getDelayHoraEfetiva();

                //Carrega Alarme Cinto segurança
                cintoAlarme=myPreferences.isCintoAlarme();

                //Carrega modo de operação, Limitador ou Smart Logger
                limitadorVel=myPreferences.isLimitadorVel();

                //Carrega App turn Off
                HARDWARE_TURN_OFF = myPreferences.isHardwareTurnOff();

                //carrega última posição válida
                lastLatitude = Double.valueOf(myPreferences.getLastLatitude());
                lastLongitude = Double.valueOf(myPreferences.getLastLongitude());

                //Verifica se é para criar o Access Point
                //accessPoint();
                //telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                //IMEI = telephonyManager.getDeviceId();

                //timer 1 segundo para atualização da tela
                handler1s.post(updateTextRunnable);

                //timer 1 segundo para velocidade média
                velocidadeMediaHandler.post(velociMediaTimer);

                // timer http connection
                httpConnectionHandler.post(httpConnectionTimer);

                //Não Permite transmissões via http
                if(limitadorVel){
                    telaModoLimitador();
                }else{
                    telaModoSmatLogger();
                }


//                try {
//                    serverSocket = new ServerSocket(); // <-- create a new instance of an unbound socket first
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    //Log.i("SOCKET_CATCH ", "NEW SOCKET " + String.valueOf(e));
//                    alertDialogSocket(e.toString());
//
//                }
                this.registerReceiver(this.mBattInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


            }
        }

    }


    private void telaModoLimitador() {
        myPreferences.setLimitadorVel(true);
        limitadorVel=true;
        HTTP_TX_ENABLED = false;
        imageViewHodometroGauge.setVisibility(View.INVISIBLE);
        textViewTituloHodometro.setVisibility(View.INVISIBLE);
        textViewValorHodometroUnidade.setVisibility(View.INVISIBLE);
        textViewHodometro.setVisibility(View.INVISIBLE);
        textViewHodometroParcialTitulo.setVisibility(View.INVISIBLE);
        textViewHodometroParcial.setVisibility(View.INVISIBLE);
        textViewHodParcialUnidade.setVisibility(View.INVISIBLE);
        textViewDataCombustivel.setVisibility(View.INVISIBLE);
        textViewHoraCombustivel.setVisibility(View.INVISIBLE);
        textViewCombustivelAbastecido.setVisibility(View.INVISIBLE);
        textViewLitrosUnidade.setVisibility(View.INVISIBLE);
        imageViewBombaCombustivel.setVisibility(View.INVISIBLE);
        imageViewAlertaManutenção.setVisibility(View.INVISIBLE);
        textViewDataManutencao.setVisibility(View.INVISIBLE);
        textViewHoraManutencao.setVisibility(View.INVISIBLE);
        imageViewTemperaturaGauge.setVisibility(View.INVISIBLE);
        imageViewPonteiroTemperartura.setVisibility(View.INVISIBLE);
        textViewTemperaturaInstantanea.setVisibility(View.INVISIBLE);
        textViewDataTx.setVisibility(View.INVISIBLE);
        textViewHoraTx.setVisibility(View.INVISIBLE);
        imageViewServer.setVisibility(View.INVISIBLE);
        imageViewVelMediaGauge.setVisibility(View.VISIBLE);
        imageViewVelPicoGauge.setVisibility(View.VISIBLE);
        imageViewPonteiroVelMedia.setVisibility(View.VISIBLE);
        imageViewPonteiroVelPico.setVisibility(View.VISIBLE);
        //Toast.makeText(MainActivity.this, "Modo Limitador de Velocidade.", Toast.LENGTH_SHORT).show();
    }
    private void telaModoSmatLogger() {
        myPreferences.setLimitadorVel(false);
        limitadorVel=false;
        HTTP_TX_ENABLED = true;
        imageViewHodometroGauge.setVisibility(View.VISIBLE);
        textViewTituloHodometro.setVisibility(View.VISIBLE);
        textViewValorHodometroUnidade.setVisibility(View.VISIBLE);
        textViewHodometro.setVisibility(View.VISIBLE);
        textViewHodometroParcialTitulo.setVisibility(View.VISIBLE);
        textViewHodometroParcial.setVisibility(View.VISIBLE);
        textViewHodParcialUnidade.setVisibility(View.VISIBLE);
        textViewDataCombustivel.setVisibility(View.VISIBLE);
        textViewHoraCombustivel.setVisibility(View.VISIBLE);
        textViewCombustivelAbastecido.setVisibility(View.VISIBLE);
        textViewLitrosUnidade.setVisibility(View.VISIBLE);
        imageViewBombaCombustivel.setVisibility(View.VISIBLE);
        imageViewAlertaManutenção.setVisibility(View.VISIBLE);
        textViewDataManutencao.setVisibility(View.VISIBLE);
        textViewHoraManutencao.setVisibility(View.VISIBLE);
        imageViewTemperaturaGauge.setVisibility(View.VISIBLE);
        imageViewPonteiroTemperartura.setVisibility(View.VISIBLE);
        textViewTemperaturaInstantanea.setVisibility(View.VISIBLE);
        textViewDataTx.setVisibility(View.VISIBLE);
        textViewHoraTx.setVisibility(View.VISIBLE);
        imageViewServer.setVisibility(View.VISIBLE);
        imageViewVelMediaGauge.setVisibility(View.INVISIBLE);
        imageViewVelPicoGauge.setVisibility(View.INVISIBLE);
        imageViewPonteiroVelMedia.setVisibility(View.INVISIBLE);
        imageViewPonteiroVelPico.setVisibility(View.INVISIBLE);
        //Toast.makeText(MainActivity.this, "Modo SmatLogger.", Toast.LENGTH_SHORT).show();
    }


    private int getFirmwareProtocol(String version) {
        String major = "0";
        int positionOfDot;
        positionOfDot = version.indexOf('.');
        if(positionOfDot != -1) {
            major = version.substring(0, positionOfDot);
        }
        return new Integer(major).intValue();
    }


    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(MainActivity.this, "ACTION", Toast.LENGTH_SHORT).show();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this) {
                    Toast.makeText(MainActivity.this, "ACTION_USB_PERMISSION ", Toast.LENGTH_SHORT).show();
                    UsbAccessory routerAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (routerAccessory != null) {
                            //call method to set up accessory communication
                            //Toast.makeText(MainActivity.this, "USB REGISTERED", Toast.LENGTH_SHORT).show();
                            openAccessory(routerAccessory);
                            //imageViewAccesspoint.setImageResource(R.mipmap.usb);
                        }
                    } else {
                        Log.d(TAG, "permission denied for accessory " + routerAccessory);
                        Toast.makeText(MainActivity.this, "permission denied for accessory " + routerAccessory, Toast.LENGTH_SHORT).show();
                    }
                    mPermissionRequestPending = false;
                }
            }else if(UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){
                imageViewAccesspoint.setVisibility(View.INVISIBLE);
                UsbAccessory routerAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (routerAccessory != null && routerAccessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }

//            openAccessoryThread = new Thread(new openAccessoryThread());
//            openAccessoryThread.start();
        }
    };

    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = usbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            //Toast.makeText(MainActivity.this, "openAccessory ok", Toast.LENGTH_SHORT).show();
            imageViewAccesspoint.setImageResource(R.mipmap.usb);
            imageViewAccesspoint.setVisibility(View.VISIBLE);
            USB_CONNECTED=true;

        } else {
            //Toast.makeText(MainActivity.this, "openAccessory fail", Toast.LENGTH_SHORT).show();
            imageViewAccesspoint.setVisibility(View.INVISIBLE);
            USB_CONNECTED=false;

        }
    }

    private void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    //Bateria monitoramento
    BroadcastReceiver mBattInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            battLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            //int misCharging= intent.getIntExtra(BatteryManager.BATTERY_STATUS_CHARGING);
            //verifica se batt está abaixo de 4% para salvar variaveis em myPreferences
            if(battLevel < 4 && !lowBatteryEvent){
                lowBatteryEvent=true;
                //salva valor de hora efetiva
                myPreferences.setHoraEfetiva(horaEfetiva);
                //Toast.makeText(this, "ON STOP", Toast.LENGTH_SHORT).show();

                //Salva valor de horímetro em Shared Preferences
                myPreferences.setCountHorimetro6min(countHorimetro);
                //Slava contador de maquina em movimento
                myPreferences.setCountMaqMovimento(countMaquinaEmMovomento);
                //Salva valor de horímetro parcial em Shared Preferences
                myPreferences.setHorimetroParcial(horimetroParcial);
                //Salva valor de horímetro em Shared Preferences
                myPreferences.setHorimetro(horimetro);

                //salva data do abastecimento em preferences
                myPreferences.setDataTxServer(dataTxServer);

                //salva Hora do abastecimento em preferences
                myPreferences.setHoraTxServer(horaTxserver);

                //Salva valor de hodometro em Shared Preferences
                myPreferences.setHodometro(hodometro);

                //Salva valor de hodometro parcial em Shared Preferences
                myPreferences.setHodometroParcial(hodometroParcial);
            }
            if(battLevel < 20){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_10);
                lowBatteryEvent=false;
                // entre 20 e 29
            }else if(battLevel < 30 && battLevel >= 20){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_20);
                lowBatteryEvent=false;
                // entre 30 e 39
            }else if(battLevel < 40 && battLevel >= 30){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_30);
                lowBatteryEvent=false;
                // entre 40 e 49
            }else if(battLevel < 50 && battLevel >= 40){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_40);
                lowBatteryEvent=false;
                // entre 50 e 59
            }else if(battLevel < 60 && battLevel >= 50){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_50);
                lowBatteryEvent=false;
                // entre 60 e 69
            }else if(battLevel < 70 && battLevel >= 60){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_60);
                lowBatteryEvent=false;
                // entre 70 e 79
            }else if(battLevel < 80 && battLevel >= 70){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_70);
                lowBatteryEvent=false;
                // entre 80 e 89
            }else if(battLevel < 90 && battLevel >= 80){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_80);
                lowBatteryEvent=false;
                // entre 90 e 99
            }else if(battLevel < 100 && battLevel >= 90){
                imageViewBatteryLevel.setImageResource(R.drawable.battery_90);
                lowBatteryEvent=false;
                // igual a 100
            }else if(battLevel == 100 ){
                imageViewBatteryLevel.setImageResource(R.drawable.battery100);
                lowBatteryEvent=false;
            }
        }
    };

    //Alert Dialog mostrando IMEI e Email de cadastro
    private void alertDilaogInfoTel() throws InvocationTargetException, IllegalAccessException {

        // pega SSID do Access Point
        String hardwareId="No AccessPoint";
        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method m: methods) {
            if (m.getName().equals("getWifiApConfiguration")) {
                WifiConfiguration apconfig = (WifiConfiguration)m.invoke(wifiManager);
                hardwareId = apconfig.SSID;
            }


        }


        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Tel Info")
                .setCancelable(false)
                .setMessage("Email: "+ userEmail + "\n" + "Hardware :"+ hardwareId)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        //for negative side button
        //alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        //alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));

    }

//    @Override
//    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
//        return super.registerReceiver(receiver, filter);
//
//    }

    @Override
    protected void onResume() {
        super.onResume();

        //Log.d("GOTO: ","OnResume");
        //callback GoTo MSG
        gotoListener();
        //Log.d("GOTO: ","Chama gotoListener OnResume");
        //Verifica se é para criar o Access Point
//        if(myPreferences.isFB_CREATED() && myPermisions){
//            accessPoint();
//        }


        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if(!RUN_SOCKET_STOP) {
            registerReceiver(mCheckBatteryStatusReceiver, mCheckBatteryStatusFilter);
        }

        // Activity's been resumed
        isPaused = false;

        /******************* USB *********************/
        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (usbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!mPermissionRequestPending) {
                        usbManager.requestPermission(accessory,permissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }

    }

    //Método para entrar quantidade de combustível abasteceida
    private void alertDialogCombustivel(){

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mainActivityContext);
        View promptsView = li.inflate(R.layout.input_combustivel,null);

        final EditText userInput =  promptsView
                .findViewById(R.id.editTextDialogCombInput);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                // set prompts.xml to alertdialog builder
                .setView(promptsView)
                .setCancelable(false)
                .setTitle("Abastecimento!")
                .setMessage("Entre com a quantidade de combustível abastecida.")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // pega quantidade de combustivel digitada e o timeStamp
                        if(userInput.getText().length()!=0){
                            quantidadeCombustivel = Float.parseFloat(userInput.getText().toString());
                        }else{
                            quantidadeCombustivel=0.0f;
                        }

                        timeStampCombustivel = System.currentTimeMillis()/1000;
                        //salva data do abastecimento em preferences
                        myPreferences.setTimeStampAbastecimento(timeStampCombustivel);

                        time = new GregorianCalendar();
                        int diaDoMesAbasteceimento = time.get(Calendar.DAY_OF_MONTH);
                        int MesDoAnoAbasteceimento = time.get(Calendar.MONTH)+1;
                        int anoAbasteceimento = time.get(Calendar.YEAR);
                        int horaDoAbastecimento = time.get(Calendar.HOUR_OF_DAY);
                        int minutoAbasteceimento = time.get(Calendar.MINUTE);


                        dataAbastecimento = diaDoMesAbasteceimento + "/"
                                +MesDoAnoAbasteceimento+"/"+anoAbasteceimento;
                        //mostra data do abastecimento
                        textViewDataCombustivel.setText(dataAbastecimento);
                        //salva data do abastecimento em preferences
                        myPreferences.setDataAbastecimento(dataAbastecimento);

                        horaAbastecimento = horaDoAbastecimento+":"+ String.format(Locale.ENGLISH,"%02d", minutoAbasteceimento);
                        //Mostra hora do abastecimento
                        textViewHoraCombustivel.setText(horaAbastecimento);
                        //salva Hora do abastecimento em preferences
                        myPreferences.setHoraAbastecimento(horaAbastecimento);

                        //mostra quantidade de combustivel
                        textViewCombustivelAbastecido.setText(String.valueOf(quantidadeCombustivel));
                        //salva quantidade de combustível abastecida em preferences
                        myPreferences.setQuantidadeCombAbastecido(quantidadeCombustivel);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
                //for negative side button
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
                //for positive side button
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
    }

    //Método para entrar quantidade de combustível abasteceida
    private void alertDialogMecanico(final int action){

        String msg ="";

        String[] alertManutOptions = {"CORRETIVA","PREVENTIVA","MANOBRISTA","CANCEL"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if(action==MANUTENÇÃO_ON){
                msg = "Confirma Este Veículo em Modo Manutenção.";
                builder.setTitle(msg);
                builder.setItems(alertManutOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            //caso corretiva
                            case 0:
                                alertDialogChoice(which,action);
                                statusModoManut="corretiva";
                                break;
                            //caso preventiva
                            case 1:
                                alertDialogChoice(which,action);
                                statusModoManut="preventiva";
                                break;
                            //caso manobrista
                            case 2:
                                //mostra nome do mecanico no campo operador
                                statusModoManut="manobrista";
                                textViewNomeUsuario.setText(mecanicoName);
                                DESABILITA_RELE=true;
                                dialog.cancel();

                        }

                    }
                });
            }else if(action==MANUTENÇÃO_OK){
                statusModoManut="executada";
                msg = "Confirma Manutenção executada.";
                builder.setTitle(msg);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialogChoice(which,action);
                    }
                });
            }

                //builder.setMessage(msg)
            builder.setCancelable(false);

//                .setPositiveButton(R.string.manutenção_corretiva, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        //Formata calendário para salvar data e hora em DD/MM/yyyy
//                        time = new GregorianCalendar();
//                        int diaDoMesManutenção = time.get(Calendar.DAY_OF_MONTH);
//                        int MesDoAnoManutenção = time.get(Calendar.MONTH)+1;
//                        int anoManutenção = time.get(Calendar.YEAR);
//                        int horaDaManutenção = time.get(Calendar.HOUR_OF_DAY);
//                        int minutoManutenção = time.get(Calendar.MINUTE);
//
//                        if(action==MANUTENÇÃO_ON) {
//                            //pega data e hora ao detectar o qr code mecanico
//                            timeStampParadaManutenção = System.currentTimeMillis()/1000;
//                            //Salva Time Stamp em preferences
//                            myPreferences.setTimeStampManutençãoAberta(timeStampParadaManutenção);
//                            //Formata string data da manutenção aberta
//                            dataParadaManutenção = diaDoMesManutenção + "/"
//                                    + MesDoAnoManutenção + "/" + anoManutenção;
//                            //mostra data da parada para manutenção
//                            textViewDataManutencao.setText(dataParadaManutenção);
//                            //salva data da parada para manutenção em preferences
//                            myPreferences.setDataParadaManutenção(dataParadaManutenção);
//
//                            horaParadaManutenção = horaDaManutenção + ":" + String.format(Locale.ENGLISH, "%02d", minutoManutenção);
//                            //Mostra hora da parada para manutenção
//                            textViewHoraManutencao.setText(horaParadaManutenção);
//                            //salva Hora da parada para manutenção em preferences
//                            myPreferences.setHoraParadaManutenção(horaParadaManutenção);
//                            // Salva Modo Manutenção
//                            myPreferences.setALERTA_MANUTENÇÃO(true);
//                            ACIONA_ALERTA_MANUTENÇÃO=true;
//                            //libera rele
//                            DESABILITA_RELE = true;
//                            //Salva Alerta de manutenção no fire base
//                            mRootRef.child(userID).child("alertaManutencao").setValue("sim");
//                            //MOstra status do mod manutenção
//                            imageViewAlertaManutenção.setImageResource(R.mipmap.wrench);
//                            //mostra nome do mecanico no campo operador
//                            textViewNomeUsuario.setText(mecanicoName);
//
//                        }else if(action==MANUTENÇÃO_OK){
//                            timeStampManutençãoExecutada= System.currentTimeMillis()/1000;
//                            //Salva Time Stamp em preferences
//                            myPreferences.setTimeStampManutençãoExecutada(timeStampManutençãoExecutada);
//                            //Formata string data da manutenção exxecutada
//                            dataManutençãoExcutada = diaDoMesManutenção + "/"
//                                    + MesDoAnoManutenção + "/" + anoManutenção;
//                            //mostra data de manutenção executada
//                            textViewDataManutencao.setText(dataManutençãoExcutada);
//                            //salva Data de manutenção executada em preferences
//                            myPreferences.setDataManutençãoExecutada(dataManutençãoExcutada);
//
//                            horaManutençãoExecutada = horaDaManutenção + ":" + String.format(Locale.ENGLISH, "%02d", minutoManutenção);
//                            //Mostra hora de manutenção executada
//                            textViewHoraManutencao.setText(horaManutençãoExecutada);
//                            //salva Hora de manutenção executada em preferences
//                            myPreferences.setHoraManutençãoExecutada(horaManutençãoExecutada);
//                            // Salva Modo Manutenção
//                            myPreferences.setALERTA_MANUTENÇÃO(false);
//                            ACIONA_ALERTA_MANUTENÇÃO=false;
//                            //Salva Alerta de manutenção no fire base
//                            mRootRef.child(userID).child("alertaManutencao").setValue("não");
//                            //MOstra status do mod manutenção
//                            imageViewAlertaManutenção.setImageResource(R.mipmap.gear_check);
//                            //mostra nome do mecanico no campo operador
//                            textViewNomeUsuario.setText(mecanicoName);
//                        }
//
//                    }
//                })
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
        //for negative side button
        //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
    }

    private void alertDialogChoice(int choice, int action){
        //Formata calendário para salvar data e hora em DD/MM/yyyy
        time = new GregorianCalendar();
        int diaDoMesManutenção = time.get(Calendar.DAY_OF_MONTH);
        int MesDoAnoManutenção = time.get(Calendar.MONTH)+1;
        int anoManutenção = time.get(Calendar.YEAR);
        int horaDaManutenção = time.get(Calendar.HOUR_OF_DAY);
        int minutoManutenção = time.get(Calendar.MINUTE);

        if(action==MANUTENÇÃO_ON) {
            //pega data e hora ao detectar o qr code mecanico
            timeStampParadaManutenção = System.currentTimeMillis()/1000;
            //Salva Time Stamp em preferences
            myPreferences.setTimeStampManutençãoAberta(timeStampParadaManutenção);
            //Salva status de manutenção
            myPreferences.setStatusManutenção(statusModoManut);
            //Formata string data da manutenção aberta
            dataParadaManutenção = diaDoMesManutenção + "/"
                    + MesDoAnoManutenção + "/" + anoManutenção;
            //mostra data da parada para manutenção
            textViewDataManutencao.setText(dataParadaManutenção);
            //salva data da parada para manutenção em preferences
            myPreferences.setDataParadaManutenção(dataParadaManutenção);

            horaParadaManutenção = horaDaManutenção + ":" + String.format(Locale.ENGLISH, "%02d", minutoManutenção);
            //Mostra hora da parada para manutenção
            textViewHoraManutencao.setText(horaParadaManutenção);
            //salva Hora da parada para manutenção em preferences
            myPreferences.setHoraParadaManutenção(horaParadaManutenção);
            // Salva Modo Manutenção
            myPreferences.setALERTA_MANUTENÇÃO(true);
            ACIONA_ALERTA_MANUTENÇÃO=true;
            //libera rele
            DESABILITA_RELE = true;
            //Salva Alerta de manutenção no fire base
            mRootRef.child(userID).child("alertaManutencao").setValue("sim");
            //MOstra status do mod manutenção
            imageViewAlertaManutenção.setImageResource(R.mipmap.wrench);
            //mostra nome do mecanico no campo operador
            textViewNomeUsuario.setText(mecanicoName);

        }else if(action==MANUTENÇÃO_OK){
            timeStampManutençãoExecutada= System.currentTimeMillis()/1000;
            //Salva Time Stamp em preferences
            myPreferences.setTimeStampManutençãoExecutada(timeStampManutençãoExecutada);
            //Salva status de manutenção
            myPreferences.setStatusManutenção(statusModoManut);
            //Formata string data da manutenção exxecutada
            dataManutençãoExcutada = diaDoMesManutenção + "/"
                    + MesDoAnoManutenção + "/" + anoManutenção;
            //mostra data de manutenção executada
            textViewDataManutencao.setText(dataManutençãoExcutada);
            //salva Data de manutenção executada em preferences
            myPreferences.setDataManutençãoExecutada(dataManutençãoExcutada);

            horaManutençãoExecutada = horaDaManutenção + ":" + String.format(Locale.ENGLISH, "%02d", minutoManutenção);
            //Mostra hora de manutenção executada
            textViewHoraManutencao.setText(horaManutençãoExecutada);
            //salva Hora de manutenção executada em preferences
            myPreferences.setHoraManutençãoExecutada(horaManutençãoExecutada);
            // Salva Modo Manutenção
            myPreferences.setALERTA_MANUTENÇÃO(false);
            ACIONA_ALERTA_MANUTENÇÃO=false;
            //Salva Alerta de manutenção no fire base
            mRootRef.child(userID).child("alertaManutencao").setValue("não");
            //MOstra status do mod manutenção
            imageViewAlertaManutenção.setImageResource(R.mipmap.gear_check);
            //mostra nome do mecanico no campo operador
            textViewNomeUsuario.setText(mecanicoName);
        }
    }

    //Método para acessar a classe QrCode
    private void alertDialogQrCode(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("QrCode!")
                .setCancelable(false)
                .setMessage("Click em OK para novo operador ou nova máquina. Esta operação não poderá ser revertida!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        qrCodeReader(horimetro,hodometro,-1);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        //for negative side button
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));

    }
    //Método de alerta para criação do socket
    private void alertDialogSocket(String erro){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("SOCKET!!")
                .setCancelable(false)
                .setMessage("Socket não criado, reinicie o aplicativo! "+ erro)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        qrCodeReader(horimetro,hodometro,-1);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        //for negative side button
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
    }
    //Método de alerta de mensagem recebida do firestore
    private void alertDialogGoto(String tipo, String msg, final String idMsg){

        switch  (tipo){
            case "texto":
                if(msg.length() > 9 && msg.substring(0,9).contentEquals("horaNova=")){
                    String novoHorimetro=msg.substring(9);

                    horimetro = Float.parseFloat(novoHorimetro);
                    stg=hodometro;
                    //carrega valor de Horimetro Ajuste de Zero
                    horimetroAz = horimetro;
                    //habilita transmissão de ajuste de horimetro para hardware(esp8266)
                    ESP8266_AJUSTE_HORIMETRO=true;
                    //Salva horimetro ajuste de zero
                    myPreferences.setHorimetroAz(horimetroAz);
                    horaEfetiva = 0;
                    //Salva hora efetiva
                    myPreferences.setHoraEfetiva(horaEfetiva);
                    //Zera contador de hhora efetiva
                    countMaquinaEmMovomento=0;
                    //Zera contador horimetro
                    countHorimetro=0;
                    //calcula e mostra hora efetiva
                    calculaMostraHoraEfetiva();
                    //Salva valor de horimetro em Shared Preferences
                    myPreferences.setHorimetro(horimetro);
                    //Atualisa textView
                    textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro));
                    ajusteHorimetroFeito = true;
                    timeStampAdjHorimetro = System.currentTimeMillis()/1000;

                }

                break;
            case "option":


                break;
            case "picture":

        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("MENSAGEM!!")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fireStoreAnswer(idMsg,"OK");
                        //Apaga LED de sinalização de mensagem
                        //DESABILITA_SIRENE=true;
                        GOTO_RESPONDIDO=true;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fireStoreAnswer(idMsg,"CANCEL");
                        //Apaga LED de sinalização de mensagem
                        //DESABILITA_SIRENE=true;
                        GOTO_RESPONDIDO=true;
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        //for negative side button
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));

    }

        //Resposta para mensagem Firestore
    private void fireStoreAnswer (String idMessage, String statusAnswer){

        // update a message
        DocumentReference mDocumentReference = fireStoredb.collection("mensagens").document(idMessage);
                mDocumentReference.update("status",statusAnswer,
                        "maquina",maquinaNome, "grupo","pendente")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(mainActivityContext, "Message Updated OK", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mainActivityContext, "Message Updated Failure "+ e, Toast.LENGTH_SHORT).show();
                    }
                });
    }




    //Verifica se é para criar o Access Point
//    private void accessPoint() {
//
//
//
//            //verifica se é para criar o Access Point
//            if (myPreferences.isAccessPoint()) {
//                //linearLayoutTemperartura.setVisibility(View.VISIBLE);
//            /*
//                    conjunto Temperatura
//             */
//                if(!limitadorVel){
//                    imageViewPonteiroTemperartura.setVisibility(View.VISIBLE);
//                    imageViewTemperaturaGauge.setVisibility(View.VISIBLE);
//                    textViewTemperaturaInstantanea.setVisibility(View.VISIBLE);
//                }
//
//                /*                           */
//                //linearLayoutTermometro.setVisibility(View.VISIBLE);
//                textViewNomeMaquina.setVisibility(View.VISIBLE);
//                imvCintoSegurança.setVisibility(View.VISIBLE);
//                //textViewDispositivo.setVisibility(View.VISIBLE);
//                imageViewAccesspoint.setVisibility(View.VISIBLE);
//
//                //wifiVerify();
//
//                //verifica se as permissões foram dadas
//                if(myPermisions) {
//
//                    if (apControl != null && apControl.isEnabled()) {
//                        //Log.i("SOCKET_ ","Ap Control enabled");
////                if(serverSocket.isClosed()){
////                    Log.i("SOCKET_ ","SERVER SOCKET IS CLOSED");
////                }else{
////                    Log.i("SOCKET_ ","SERVER SOCKET IS OPEN");
////                }
//                    } else {
//                        //Log.i("SOCKET_ ","Ap Control disabled");
//                        ProgressDialog pd = new ProgressDialog(MainActivity.this);
//                        pd.setMessage("Aguardando Ponto de Acesso WiFi");
//                        pd.show();
//                        if (apControl != null) {
//                            while (!apControl.isEnabled()) {
//                                //Log.i("SOCKET_ ","waiting Access Point");
//                                apControl.enable();
//                            }
//                        }
//                        pd.dismiss();
//                    }
//
//                    //handler do socket
////                    this.serverThread = new Thread(new ServerThread());
////                    this.serverThread.start();
//                }
//
//            } else {
//                //adequa tela ao modo sem interface
//                //linearLayoutTemperartura.setVisibility(View.INVISIBLE);
//            /*
//                    conjunto Temperatura
//             */
//                imageViewPonteiroTemperartura.setVisibility(View.INVISIBLE);
//                imageViewTemperaturaGauge.setVisibility(View.INVISIBLE);
//                textViewTemperaturaInstantanea.setVisibility(View.INVISIBLE);
//
//
//                //linearLayoutTermometro.setVisibility(View.INVISIBLE);
//                //textViewNomeMaquina.setVisibility(View.INVISIBLE);
//                imvCintoSegurança.setVisibility(View.INVISIBLE);
//                //textViewDispositivo.setVisibility(View.INVISIBLE);
//                imageViewAccesspoint.setVisibility(View.INVISIBLE);
//            }
//
//    }

    //QrCode Reader Método
    private void qrCodeReader(float rHorimetro,float rHodometro, int action) {
        //Verifica se a activity QrCodeReader terminou o seu ciclo de vida
        //habilita tocar a sirene ligada no rele
        HABILITA_RELE = true;
//        if(!qrCodeReaderRunning){
//            Log.d("PÓS QR_CODE", "not still running");
            Intent intent = new Intent(this,QrCodeReader.class);
            intent.putExtra("HORIMETRO", rHorimetro);
            intent.putExtra("HODOMETRO", rHodometro);
            intent.putExtra("AÇÃO",action);

            startActivityForResult(intent,QR_CODE_RESULTS);
//        }else{
//            Log.d("PÓS QR_CODE", "still running");
//        }

    }

    /***********************************************************************************************
     *
     *                   EVENTO RESULTADO DO QrCode
     ***********************************************************************************************/
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //mostra horimetro na tela
        textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro ));
        textViewHorimetroParcial.setText(String.format(Locale.ENGLISH, "%.1f", horimetroParcial));
        if(requestCode == QR_CODE_RESULTS){
            //Verifica se qrCode é do tipo operador
            if (QrCodeReader.isOperador) {
                QrCodeReader.isOperador=false;
                //verifica se o qrCode operador não está vazio ou se houve erro de leitura
                if(!QrCodeReader.operatorID.equals("0")) {
                    if(isExameOk()){

                        isUserID = true;
                        //userName=lines[0];
                        userName = QrCodeReader.operatorName;
                        //se não for requerido qrCode salva o nome do usuário em Preferences
                        if(!myPreferences.isID_OPERADOR()){
                           myPreferences.setValidUserName(true);
                           myPreferences.setLastUserName(userName);
                        }
                        //Habilita Rele Timeout
                        //Log.i("PÓS RELE TIMEOUT ", "TRUE");
                        RELE_TIMEOUT = true;
                        textViewReleDelay.setVisibility(View.VISIBLE);
                        //CrashLytics User Information
                        // You can call any combination of these three methods
//                        Crashlytics.setUserIdentifier(userID);
//                        Crashlytics.setUserEmail(userEmail);
//                        Crashlytics.setUserName(userName);


                        textViewNomeUsuario.setText(userName);
                        try{
                            userIdQrCode = Integer.valueOf(QrCodeReader.operatorID);
                        }catch (NumberFormatException nfe){
                            userIdQrCode=0;
                            Toast.makeText(mainActivityContext, "Não foi possível ler o ID do Usuário", Toast.LENGTH_SHORT).show();
                        }

                        //função trocada
                        //HABILITA_RELE = true;
                        DESABILITA_RELE = true;

                        //motivo da habilitação do rele
                        motivoBloqueioRele = getString(R.string.rele_habilitado_por_operador);
                         //verifica se o operador atual é diferente do operador do último CHeckList
                        // ou se já ultrapassou o intervalo (em Horas) entre checklist
                        long timeStampSystem = System.currentTimeMillis() / 1000;
                        if (operadorUltimoCheckList != userIdQrCode ||
                                ultimoCheckListFeito + (3600 * intervaloCheckList) <= timeStampSystem) {
                            //Call checkList Activity
                            Intent intent = new Intent(this, CheckList.class);
                            this.startActivity(intent);
                        }
                    }
                }

            }else if(QrCodeReader.isMaquina){// verifica se Qr Code é máquina
                QrCodeReader.isMaquina=false;
                switch (QrCodeReader.QrType){
                    case QrCodeReader.NOVO_REGISTRO:
                        idMaquinaDB = Integer.parseInt(QrCodeReader.maquinaID);
                        maquinaNome = QrCodeReader.maquinaName;
                        empresaNome = QrCodeReader.empresaName;
                        stg= Float.parseFloat(QrCodeReader.maquinaHorimetro);
                        horimetro= stg;
                        horimetro = Float.parseFloat(QrCodeReader.maquinaHorimetro);
                        hodometro = Float.parseFloat(QrCodeReader.maquinaHodometro);
                        //carrega valor de Horimetro Ajuste de Zero
                        horimetroAz = horimetro;
                        //habilita transmissão de ajuste de horimetro para hardware(esp8266)
                        ESP8266_AJUSTE_HORIMETRO=true;
                        //Salva horimetro ajuste de zero
                        myPreferences.setHorimetroAz(horimetroAz);
                        horaEfetiva = 0;
                        //Salva hora efetiva
                        myPreferences.setHoraEfetiva(horaEfetiva);
                        //Zera contador de hora efetiva
                        countMaquinaEmMovomento=0;
                        //Zera contador horimetro
                        countHorimetro=0;
                        //calcula e mostra hora efetiva
                        calculaMostraHoraEfetiva();
                        senhaHttp = QrCodeReader.senhaHttpID;

                        //Salva valor de hodometro em Shared Preferences
                        myPreferences.setHodometro(hodometro);
                        //Salva valor de horimetro em Shared Preferences
                        myPreferences.setHorimetro(horimetro);
                        //Salva nome da máquina em Shared Preferences
                        myPreferences.setNomeMaquina(maquinaNome);
                        //Salva nome da empresa em Shared Preferences
                        myPreferences.setNomeEmpresa(empresaNome);
                        //salva ID maquina em preferences
                        myPreferences.setMaquinaId(idMaquinaDB);
                        //salva senha Http em preferences
                        myPreferences.setSenhaHttp(senhaHttp);

                        textViewNomeMaquina.setText(maquinaNome+ " "+ idMaquinaDB);

                        textViewNomeEmpresa.setText(empresaNome);

                        textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro));

                        break;

                    case QrCodeReader.HORIMETRO:

                        break;
                    case QrCodeReader.HODOMETRO:

                        break;
                    case QrCodeReader.ID_MAQUINA:
                        //salva maquina em preferences
                        myPreferences.setMaquinaId(idMaquinaDB);
                        break;
                    case QrCodeReader.ID_EMPRESA:

                        break;
                    case QrCodeReader.AJUSTE_TOTAL:


                }


                //Completed Alert dialog box dizendo que o horímetro foi ajustado e requisita qr do operador
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Dispositivo Atualizado!")
                        .setCancelable(false)
                        .setMessage("Este dispositivo foi atualizado, click em OK para registrar o usuário da máquina/veículo.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // call registro de operador
                                qrCodeReader(horimetro,hodometro,REGISTRO_OPERADOR);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                //for negative side button
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
                //for positive side button
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));

            }else if(QrCodeReader.isAjusteHorimetro){
                QrCodeReader.isAjusteHorimetro=false;

                stg= Float.parseFloat(QrCodeReader.maquinaHorimetro);
                horimetro= stg;
                horimetro = Float.parseFloat(QrCodeReader.maquinaHorimetro);
                hodometro = Float.parseFloat(QrCodeReader.maquinaHodometro);
                //carrega valor de Horimetro Ajuste de Zero
                horimetroAz = horimetro;
                //habilita transmissão de ajuste de horimetro para hardware(esp8266)
                ESP8266_AJUSTE_HORIMETRO=true;
                //Salva horimetro ajuste de zero
                myPreferences.setHorimetroAz(horimetroAz);
                horaEfetiva = 0;
                //Salva hora efetiva
                myPreferences.setHoraEfetiva(horaEfetiva);
                //Zera contador de hhora efetiva
                countMaquinaEmMovomento=0;
                //Zera contador horimetro
                countHorimetro=0;
                //calcula e mostra hora efetiva
                calculaMostraHoraEfetiva();
                //Salva valor de hodometro em Shared Preferences
                myPreferences.setHodometro(hodometro);
                //Salva valor de horimetro em Shared Preferences
                myPreferences.setHorimetro(horimetro);
                //Atualisa textView
               //textViewHorimetro.setText(String.valueOf(horimetro));
                textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro));
                textViewHodometro.setText(String.valueOf(hodometro));
                ajusteHorimetroFeito = true;
                timeStampAdjHorimetro = System.currentTimeMillis()/1000;

            }else if(QrCodeReader.isAbastecimento){
                QrCodeReader.isAbastecimento=false;
                //verifica se o qrCode operador não está vazio ou se houve erro de leitura
                if(!QrCodeReader.operatorID.equals("0")) {
                    if(isExameOk()){
                        //Abastecedor nome=lines[0];
                        abastecedorName = QrCodeReader.operatorName;
                        if(!abastecedorName.equals("inválido")){
                            alertDialogCombustivel();
                        }
                    }
                }else{
                    Toast.makeText(mainActivityContext, "Operador de Abastecimento Inválido.", Toast.LENGTH_SHORT).show();
                    abastecedorName = "inválido";
                }
            }else if(QrCodeReader.isMecanico){
                QrCodeReader.isMecanico=false;
                //verifica se o qrCode operador não está vazio ou se houve erro de leitura
                if(!QrCodeReader.operatorID.equals("0")) {
                    if(isExameOk()) {
                        //Mecânico nome=lines[0];
                        mecanicoName = QrCodeReader.operatorName;
                        if (!mecanicoName.equals("inválido")) {
                            //mecanicoName e userName são iguais
                            userName = mecanicoName;
                            //pega ID do mecanico no DB
                            userIdQrCode = Integer.valueOf(QrCodeReader.operatorID);
                            //se estiver no modo manutenção
                            if(ACIONA_ALERTA_MANUTENÇÃO){
                                alertDialogMecanico(MANUTENÇÃO_OK);
                            }else{
                                alertDialogMecanico(MANUTENÇÃO_ON);
                            }

                        }
                    }else{

                    }

                }else{
                    Toast.makeText(mainActivityContext, "Mecânico Inválido.", Toast.LENGTH_SHORT).show();
                    mecanicoName = "inválido";
                }
            }

            txDataNow();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        //}


    }
    //Método Alert dialog box
    private void alertDialog (String titulo, String mensagem){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setCancelable(false)
                .setMessage(mensagem)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        qrCodeReader(horimetro,hodometro,-1);
//                        if(NIVEL_SUPERVISOR && !DISPOSITIVO_BLOQUEADO){
//                            myPreferences.setDISPOSITIVO_BLOQUEADO(false);
//
//                        }else{
                            //reset activity
                            recreate();
//                        }


                    }

                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        //for negative side button
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
        //for positive side button
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
    }

    private boolean isExameOk () {
        //data do exame médico e treinamento, lines[3] e lines[4]
        //Locale localeBR = new Locale("pt", "BR");
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", localeBR);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateExam = null;
        Date dateTreina = null;
        try {
            dateExam = dateFormat.parse(QrCodeReader.exameMedicoData);
            dateTreina = dateFormat.parse(QrCodeReader.treinamentoData);
            Date dataVencimentoExameMedico = dateExam;
            Date dataVencimentoTreinamento = dateTreina;
            //Compara data de exames com data atual - 30 dias
            //Converte 30 dias em milisegundos
            //long milisenconds30days = TimeUnit.DAYS.toMillis(30);

            //Verifica Exame médico
            long dateDiff = dataVencimentoExameMedico.getTime() - (System.currentTimeMillis() );
            long dayDiff = TimeUnit.MILLISECONDS.toDays(dateDiff);
            //verifica se exame está vencendo entre 1 e 30 dias

            if (dayDiff <= 30 && dayDiff >=1) {
                //Exame vencendo entre 1 e 30 dias
                exameMedicoVencido = false;
            }else if(dayDiff < 0 && dayDiff > -737000){
                //Exame vencido
                exameMedicoVencido = true;
            }else if(dayDiff>30){
                exameMedicoVencido=false;

            }
            diasFaltantesExameMedico = (int)dayDiff*(-1);

            //Verifica exame treinamento
            dateDiff = dataVencimentoTreinamento.getTime() - (System.currentTimeMillis() );
            dayDiff = TimeUnit.MILLISECONDS.toDays(dateDiff);
            //verifica se exame está vencendo entre 1 e 30 dias

            if (dayDiff <= 30 && dayDiff >=1) {
                //Exame vencendo entre 1 e 30 dias
                exameTreinamentoVencido=false;
            }else if(dayDiff < 0 && dayDiff > -737000){
                //Exame vencido
                exameTreinamentoVencido = true;
                //Log.i("Time : ","TRUE");
            }else if(dayDiff>30){
                exameTreinamentoVencido=false;
            }
            diasFaltantesExameTreinamento = (int)dayDiff*(-1);

        } catch (ParseException e) {
            e.printStackTrace();
            //Log.i("EXAME OK ", " ERR "+ String.valueOf(e));
        }

        String msgExamesTitulo = "nada";
        String msgExames = "nada";

        //se os dois exames estão vencidos
        if(exameMedicoVencido && exameTreinamentoVencido){
            msgExamesTitulo="Exame Médico e Treinamento de Capacitação Vencidos!";
            msgExames="Exame Médico Periódico vencido a "+diasFaltantesExameMedico+
                    " Dias e Treinamento de Capacitação Vencido a "+diasFaltantesExameTreinamento+ " Dias.";
        }
        //se o exame médico está vencido
        if(exameMedicoVencido && !exameTreinamentoVencido){
            msgExamesTitulo="Exame Médico Vencido!";
            msgExames="Exame Médico Periódico vencido a "+diasFaltantesExameMedico+
                    " Dias.";
        }
        //se o exame de treinamento está vencido
        if(!exameMedicoVencido && exameTreinamentoVencido){
            msgExamesTitulo="Treinamento de Capacitação Vencido!";
            msgExames="Treinamento de Capacitação vencido a "+diasFaltantesExameTreinamento+
                    " Dias.";
        }
        if(exameMedicoVencido || exameTreinamentoVencido) {
            //Completed Alert dialog box dizendo que o horímetro foi ajustado e requisita qr do operador
            AlertDialog alertDialog =new AlertDialog.Builder(this)
                    .setTitle(msgExamesTitulo)
                    .setCancelable(false)
                    .setMessage(msgExames)
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            //for negative side button
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
            //for positive side button
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
            isUserID = false;
            userName = "";
            return false;
        }else{
            return true;
        }
    }
//    public  void alertAjusteHorimetro(){
//        final EditText txtHorimetroADJ = new EditText(this);
//        txtHorimetroADJ.setInputType(InputType.TYPE_CLASS_NUMBER);
//        //pega valor do horimetro atual
//        //textViewHorimetroParcial.setText(String.format(Locale.ENGLISH,"%.1f", horimetroParcial));
//        txtHorimetroADJ.setText(String.format(Locale.ENGLISH,"%.1f",horimetro));
//
//        AlertDialog alertDialog = new AlertDialog.Builder(this)
//                .setTitle("Ajuste de Horímetro")
//                .setMessage("Novo valor de horímetro!")
//                .setView(txtHorimetroADJ)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        String HorValue = String.valueOf(txtHorimetroADJ.getText());
//                        horimetro = Float.parseFloat(HorValue);
//                        textViewHorimetro.setText(String.valueOf(horimetro));
//                        //Salva valor de horimetro em Shared Preferences
//                        myPreferences.setHorimetro(horimetro);
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                })
//                .show();
//        //for negative side button
//        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.yellow_color));
//        //for positive side button
//        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.yellow_color));
//    }
        //Incrementa horímetro
    //Runnable de atualização de 1 segundo
    Runnable updateTextRunnable=new Runnable(){
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {

            if(HORIMETRO_ENABLE){
                countHorimetro++;
                //textViewTemporario2.setText(String.valueOf(horimetro));
                //Conatdor de Hora Efetiva
                if(velocidadeInstantanea >=1 ){
                    horaEfetiva=horaEfetiva+CENTESIMO_HORIMETRO;
                    //countMaquinaEmMovomento++;
                    //Atualisa contador delay de hora efetiva com valor do firebase
                    countDelayHoraEfetiva = delayHoraEfetiva;
                    //textViewTemporario.setText(String.valueOf(horaEfetiva));
                    //textViewHoraEfetiva.setTextColor(getResources().getColor(R.color.green_color));

                    if(!eventoHoraEfetivaGreen){
                        parametroHoraEfetiva="HORA_GREEN";
                        eventoHoraEfetivaGreen=true;
                        eventoHoraEfetivaYellow=false;
                        eventoHoraEfetivaRED=false;
                    }
                    //timer de 6 minutos para hora efetiva
//                    if(countMaquinaEmMovomento>=360){
//                        countMaquinaEmMovomento=0;
//                        // Hora Efetiva
//                        //horaEfetiva=horaEfetiva+0.1f;
//                    }else{
//                        horaEfetiva=horaEfetiva+CENTESIMO_HORIMETRO;
//                    }
                //Delay de hora efetiva
                }else if(countDelayHoraEfetiva !=0){
                    countDelayHoraEfetiva--;
                    //countMaquinaEmMovomento++;
                    horaEfetiva=horaEfetiva+CENTESIMO_HORIMETRO;
                    //textViewTemporario.setText(String.valueOf(horaEfetiva));
                    //textViewHoraEfetiva.setTextColor(getResources().getColor(R.color.yellow_color));
                    if(!eventoHoraEfetivaYellow){
                        parametroHoraEfetiva="HORA_YELLOW";
                        eventoHoraEfetivaYellow=true;
                        eventoHoraEfetivaGreen=false;
                        eventoHoraEfetivaRED=false;
                    }

                }else{
                    //textViewHoraEfetiva.setTextColor(getResources().getColor(R.color.red_color));
                    if(!eventoHoraEfetivaRED){
                        parametroHoraEfetiva="HORA_RED";
                        eventoHoraEfetivaRED=true;
                        eventoHoraEfetivaGreen=false;
                        eventoHoraEfetivaYellow=false;
                    }
                }



                //Timer de 6 minutos para horimetro e hora efetiva
                if(countHorimetro>=360){
                    countHorimetro=0;
                    //horimetro=horimetro+0.1f;

                    //Salva valor de horímetro parcial em Shared Preferences
                    //myPreferences.setHorimetroParcial(horimetroParcial);
                    //Salva valor de horímetro em Shared Preferences
                    //myPreferences.setHorimetro(horimetro);
                    //Formata horimetro com uma casa decimal
                    textViewHorimetro.setText(String.format(Locale.ENGLISH, "%.1f", horimetro ));
                    textViewHorimetroParcial.setText(String.format(Locale.ENGLISH, "%.1f", horimetroParcial));
                    //Calcula hora efetiva percentual
                    calculaMostraHoraEfetiva();
                }else{
                    //TODO Horimetro modificado
                    //horimetro=horimetro+CENTESIMO_HORIMETRO;
                    horimetroParcial=horimetroParcial+CENTESIMO_HORIMETRO;
                }
                calculaMostraHoraEfetiva();

                //Temperatura instantanea

                tempInst4 = tempInst3;
                tempInst3 = tempInst2;
                tempInst2 = tempInst1;
                tempInst1 = temperaturaInstantanea;
                temperaturaMedia = (tempInst4+tempInst3+tempInst2+tempInst1+temperaturaInstantanea)/5;
                //Pico de temperatura

                if(temperaturaMedia>temperaturaPico){
                    temperaturaPico = temperaturaMedia;
                }

                if(temperaturaMedia<=40.0){
                    imageViewPonteiroTemperartura.setRotation(OFFSET_PONTEIRO_TEMPERATURA - 40);
                }else{
                    imageViewPonteiroTemperartura.setRotation(OFFSET_PONTEIRO_TEMPERATURA - temperaturaMedia);
                }

                //Alarme de temperatura
                TEMPERATURA_ALARME = temperaturaMedia > setPointTemperatura;

                if(TEMPERATURA_ALARME){
                     imageViewAlertaTemperatura.setImageResource(R.mipmap.alerta_temperatura);
                }
                //Sensor de temperartura desconectado
                SENSOR_ABERTO =temperaturaMedia < -8;
                   if( SENSOR_ABERTO){
                    imageViewAlertaTemperatura.setImageResource(R.drawable.temp_sensor_nc_min);
                       textViewTemperaturaInstantanea.setText(R.string.valor_temp_inst);
                }



                //Gera alerta sonoro de cinto de segurança
                if(CINTO_SEGURANÇA_ALARME){
                   if(cintoEventoON ){
                       HABILITA_SIRENE_CINTO=true;
                       cintoEventoON=false;
                   }
                   //Se não ha velocidade mínima o alarme do cinto é desligado
                    if(velocidadeInstantanea<=VELOCIDADE_ALARME_CINTO){
                        DESABILITA_SIRENE_CINTO=true;
                        CINTO_SEGURANÇA_ALARME=false;
                    }

                    piscaCinto =! piscaCinto;
                    if(piscaCinto){
                        imvCintoSegurança.setImageResource(R.mipmap.transparent_image);
                        //DESABILITA_SIRENE=true;
                    }else{
                        imvCintoSegurança.setImageResource(R.mipmap.seat_belt_open_red);
                        //HABILITA_SIRENE=true;
                    }

                }else{
                    imvCintoSegurança.setImageResource(R.mipmap.transparent_image);
                    //DESABILITA_SIRENE=true;
                    if(cintoEventoOFF){
                        DESABILITA_SIRENE_CINTO=true;
                        cintoEventoOFF=false;
                    }
                }

                switch (parametroHoraEfetiva){
                    case "HORA_GREEN":
                        //muda cor do icone hora efetiva para verde
                        imageViewHoraEfetiva.setImageResource(R.mipmap.hour_meter_green);
                        parametroHoraEfetiva="ADMITIDO";
                        //comando esp8266 - parar de piscar LED ou Sirene
                        DESABILITA_HORA_EFETIVA=true;
                        break;
                    case "HORA_YELLOW":
                        //muda cor do icone hora efetiva para amarelo
                        imageViewHoraEfetiva.setImageResource(R.mipmap.hour_meter_yellow);
                        parametroHoraEfetiva="ADMITIDO";
                        break;
                    case "HORA_RED":
                        //muda cor do icone hora efetiva para vermelho
                        imageViewHoraEfetiva.setImageResource(R.mipmap.hour_meter_red);
                        parametroHoraEfetiva="ADMITIDO";
                        //comando esp8266 - começar a piscar LED ou Sirene
                        HABILITA_HORA_EFETIVA=true;
                        break;
                }
            }

            //timeout Event GPS e velocidade
            GpsEventCount--;
            if (GpsEventCount ==0){
                GpsEventCount = 1;
                velocidadeAnterior = 0.0f;
                velocidadeInstantanea = 0.0;
                //zera ponteiro de velocidade
                float mRotationZero = OFFSET_PONTEIRO_VELOCIDADE + 0 /((float)velMaxGauge/180);
                imageViewPonteiroVelocidade.setRotation(mRotationZero);
            }
            //Timeout RELE 30 segundos
            if(RELE_TIMEOUT){
                operadorTimeOutCounter--;
                textViewReleDelay.setText(String.valueOf(operadorTimeOutCounter));

                if(operadorTimeOutCounter<=0){
                    //carrega operadorTimeOutCounter com valor inicial
                    operadorTimeOutCounter=OPERADOR_TIME_OUT;
                    RELE_TIMEOUT=false;
                    //Log.i("PÓS RELE TIMEOUT ","FALSE - CONTADOR");
                    //tira operadorTimeOutCounter da tela
                    textViewReleDelay.setVisibility(View.INVISIBLE);
                    //Se pós chave está desligado, limpa nome do operador
                    if(!posChaveStatus){
                        //se está no modo sem qrCode, não apagaga o nome do operdor ao desligar pós chave
                        if(!myPreferences.isID_OPERADOR()){

                        }else{
                            userName = "";
                            textViewNomeUsuario.setText(userName);
                        }

                        //Habilita relé se não estiver em modo manutenção
                        if(!ACIONA_ALERTA_MANUTENÇÃO){
                            HABILITA_RELE = true;
                            Toast.makeText(mainActivityContext, "Vehículo em Manutenção.", Toast.LENGTH_SHORT).show();
                        }


                        motivoBloqueioRele = getString(R.string.rele_desabilitado_pos_chave_desligado);
                        //Define que não ha operador reconhecido pelo qrCode
                        isUserID = false;
                        velocidadeMedia = 0.0;
                        velocidadeMaxima = 0.0;
                        horimetroParcial = 0.0f;
                    }
                }
            }

            //Se o pos chave está ligado e tem username, então desabilita o relé
            if(!eventoLiberaRele){
                if(posChaveStatus && userName!=null && !Objects.equals(userName, "")){
                    if(statusRele.equals("1")){
                        eventoLiberaRele = false;
                        DESABILITA_RELE = true;
                    }else{
                        eventoLiberaRele = true;
                    }
                }
            }

            if(posChaveEvent){
                //se pós chave ligado
                if(posChave.equals("0")){
                    //Log.i("PÓS CHAVE EVENT ","LIGADO");
                    posChaveStatus=true;
                    eventoLiberaRele = false;
                    //Listener GoTo MSG
                    //Log.d("GOTO: ","Chama gotoListener Pos Chave Ligado");
                    gotoListener();
                    //Se o time out do relé não está habilitado
                    if(!RELE_TIMEOUT){
                        //Log.i("PÓS CHAVE  ", "NÃO RELE TIMEOUT");
                        //Aumenta brilho da tela
                        screenBrightness(1);
                        //se não estiver em modo manutenção
                        if(!ACIONA_ALERTA_MANUTENÇÃO){
                            if (myPreferences.isID_OPERADOR()) {
                                qrCodeReader(horimetro, hodometro, -1);
                            }else{
                                //Está no modo sem qrCode, se houver operador identificado, libera o relé
                                if(userName!=null && !Objects.equals(userName, "")) {
                                    DESABILITA_RELE = true;
                                }
                            }

                        }else{
                            DESABILITA_RELE = true;
                        }
                        //TODO sinalizar se qrcode foi válido ou não
                        //Habilita Time out do Rele se houver operador válido
                        if(userName==null && Objects.equals(userName, "")) {

                            //Log.i("PÓS CHAVE  ", "SEM OPERADOR");
//                            //Make Relay timeout visible
//                            textViewReleDelay.setVisibility(View.VISIBLE);
//                        }else{
 //                           Log.i("PÓS RELE TIMEOUT ", "FALSE");
                            //espera fin de ciclo de vida da activity QrCodeReader
                            if (myPreferences.isID_OPERADOR()) {
                                qrCodeReader(horimetro, hodometro, -1);
                            }
//                            RELE_TIMEOUT = true;
//                            textViewReleDelay.setVisibility(View.VISIBLE);
                        }
                    }else{
                        //Log.i("PÓS CHAVE  ", "ELSE RELE TIMEOUT");
                        screenBrightness(1);
                        //se não houver operador identificado, não libera o relé
                        if(userName!=null && !Objects.equals(userName, "")) {
                            DESABILITA_RELE = true;
                        }
                        //se estiver em manutenção o relé fica liberado
                        if(ACIONA_ALERTA_MANUTENÇÃO){
                            DESABILITA_RELE = false;
                            Toast.makeText(MainActivity.this, "Vehículo em Manutenção.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //mantem tela acesa
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if(myPreferences.isID_OPERADOR()) {
//                        PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
//                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//                        screenLock.acquire();
//
//
//                        screenLock.release();
                    }
                    txDataNow();


                }else {
                    //Log.i("PÓS CHAVE EVENT ","DESLIGADO");
                    if(GPS_SPEED){
                        //desabilita manter tela acesa
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        screenBrightness(0);
                    }else{
                        //desabilita manter tela acesa
                        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        //screenBrightness(0);
                    }

                    //remove listener de GoTo MSG
                    if(registration!=null) {
                        registration.remove();
                        //Log.d("GOTO: ","Removed Listener Pos Chave Desligado");
                    }

                    posChaveStatus=false;
                    //salva preferências e desliga telefone
                    salvaPreferences();
                    if(RELE_TIMEOUT){
                        if(userName.length()<1){
                            //se estiver em manutenção o relé fica liberado
                            if(!ACIONA_ALERTA_MANUTENÇÃO){
                                HABILITA_RELE = true;
                            }

                            RELE_TIMEOUT=false;
                            //Log.i("PÓS RELE TIMEOUT ","FALSE");
                        }
                    }else {

                        //se estiver em manutenção o relé fica liberado
                        if(!ACIONA_ALERTA_MANUTENÇÃO){
                            HABILITA_RELE = true;
                            //deleta nome do operador se não estiver no modo manutenção
                            userName = "";
                            textViewNomeUsuario.setText(userName);

                        }

                        motivoBloqueioRele = getString(R.string.rele_desabilitado_inicio_operação);
                        isUserID = false;
                        velocidadeMedia = 0.0;
                        velocidadeMaxima = 0.0;
                        horimetroParcial = 0.0f;
                    }
                    txDataNow();
                }
                posChaveEvent=false;

            }

            //Evento http tx
            if(httpTxEvent){
                httpTxEvent=false;
                time = new GregorianCalendar();
                int diaDoMesTxServer = time.get(Calendar.DAY_OF_MONTH);
                int MesDoAnoTxServer = time.get(Calendar.MONTH)+1;
                int anoTxServer = time.get(Calendar.YEAR);
                int horaDoTxServer = time.get(Calendar.HOUR_OF_DAY);
                int minutoTxServer = time.get(Calendar.MINUTE);


                dataTxServer = diaDoMesTxServer + "/"
                        +MesDoAnoTxServer+"/"+anoTxServer;
                //mostra data do abastecimento
                textViewDataTx.setText(dataTxServer);
                //salva data do abastecimento em preferences
                //myPreferences.setDataTxServer(dataTxServer);

                horaTxserver = horaDoTxServer+":"+ String.format(Locale.ENGLISH,"%02d", minutoTxServer);
                //Mostra hora do abastecimento
                textViewHoraTx.setText(horaTxserver);
                //salva Hora do abastecimento em preferences
                //myPreferences.setHoraTxServer(horaTxserver);

                //zera pico de acelerometro (impacto)
                impactoTotalAnterior=0.0;

                //zera velocidade de pico
                velocidadeMaximaTx=0;
            }

            //se não encontrou a interface e está no modo AccessPoint
//            if(SOCKET_TIMEOUT  ){
//                imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_off);
//                HORIMETRO_HARDWARE=false;
//                //String de temperartura
//                textViewTemperaturaInstantanea.setText(R.string.valor_temp_inst);
//                SOCKET_TIMEOUT=false;
//                //Log.d("SOCKET: ","SOCKET_TIMEOUT=OnStop");
//            }else{
//                //imageViewAccesspoint.setRotation(0.0f);
//                if(rssi.equals("4")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_4);
//                }else if(rssi.equals("3")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_3);
//                }else if(rssi.equals("2")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_2);
//                }else if(rssi.equals("1")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_1);
//                }else if(rssi.equals("0")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_0);
//                }else if(rssi.equals("-1")){
//                    imageViewAccesspoint.setImageResource(R.mipmap.wifi_rssi_off);
//                }
//                //imageViewAccesspoint.setImageResource(R.mipmap.access_point_green);
//                //mostra valor de temperatura
//                textViewTemperaturaInstantanea.setText(String.valueOf(temperaturaMedia));
//
//                //Contador de socket timeout
//                socketTimeoutCounter++;
//                Log.d("SOCKET_LOG: ","SOCKET_TIMEOUT COUNTER= "+ socketTimeoutCounter);
//
//
//                if(socketTimeoutCounter % 5==0 ){
//                    //socketTimeoutCounter=0;
//                    SOCKET_TIMEOUT=true;
//                    //Log.d("SOCKET: ","SOCKET_TIMEOUT=true");
//                    noHardwareResetCounter+=5;
//                    //Log.d("CONTADOR NO HARDWARE: ", String.valueOf(noHardwareResetCounter));
//                    //Verifica se contador  atingiu set point (contador de 5 em 5 segundos)
//                    if(noHardwareResetCounter >= hardwarePerdidoTimeout*60 && HARDWARE_TURN_OFF){
//                        noHardwareResetCounter=0;
//                        socketTimeoutCounter=0;
//                        //Toast.makeText(mainActivityContext, "RESET POR HARDWARE PERDIDO", Toast.LENGTH_SHORT).show();
//                        //reset App
//                        //reset activity
//                        finish();
//                        //recreate();
//                    }
//                }
//            }

            //Gera alerta de sobretemperatura
            if(TEMPERATURA_ALARME || SENSOR_ABERTO){
//                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//                toneG.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 200);
                piscaTemperatura =! piscaTemperatura;
                if(piscaTemperatura){
                    imageViewAlertaTemperatura.setVisibility(View.VISIBLE);
                    //HABILITA_SIRENE=true;
                }else{
                    imageViewAlertaTemperatura.setVisibility(View.INVISIBLE);
                    //DESABILITA_SIRENE=true;
                }

            }else{
                imageViewAlertaTemperatura.setVisibility(View.INVISIBLE);
                //DESABILITA_SIRENE=true;
            }


            // GPRS CHIP - Data Usage
            mStartALL = NetworkStats.Bucket.METERED_ALL;
            
            mStartRX = TrafficStats.getMobileRxBytes();

            mStartTX =  TrafficStats.getMobileTxBytes();
//            Log.i("TXDADOS ", String.valueOf(mStartTX));
//            Log.i("RXDADOS ", String.valueOf(mStartRX));

            //busca estatus da rede telefonia móvel
            netWorkStatus = new GetNetwork().getNetworkClass(MainActivity.this);
            if(netWorkStatus != netWorkStatusAnterior) {
                netWorkStatusAnterior = netWorkStatus;
                switch (netWorkStatus) {
                    case "Unknown":
                        imageViewServer.setImageResource(R.mipmap.cloud_data);
                        break;
                    case "2G":
                        imageViewServer.setImageResource(R.mipmap.cloud_data_2g);
                        break;
                    case "3G":
                        imageViewServer.setImageResource(R.mipmap.cloud_data_3g);
                        break;
                    case "4G":
                        imageViewServer.setImageResource(R.mipmap.cloud_data_4g);
                        break;
                }
            }

            //se pos chave desligado incrementa contador timeout pos Chave
            //caso desligamento do App estiver ativo
            if(!posChaveStatus && HARDWARE_TURN_OFF){
                counterPoschave++;
                //Log.i("POS_CHAVE ", String.valueOf(counterPoschave));
                if(counterPoschave >= timeoutPoschave){
                    //salva preferências e desliga telefone
                    salvaPreferences();
                    try {

                        Process process = Runtime.getRuntime().exec(new String[]{ "su","-c","reboot -p"});
                        process.waitFor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    try {
                        Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
                        proc.waitFor();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                }
            }else{
                //zera contador pos chave desligado
                counterPoschave=0;
            }

             if(USB_CONNECTED) {
                int receiveData=0;
                int counterBufferUSB=0;
                int[] usbBuffer = new int[30];


                try {

                    while(receiveData!='*'){// * asterisco como caracter final
                        receiveData=mInputStream.read();
                        if(receiveData=='#'){// HashTag como caracter inicial
                            counterBufferUSB=0;
                        }else{
                            usbBuffer[counterBufferUSB++] =receiveData;
                        }
                        //Log.i(TAG, " DATA RECEIVED "+ Arrays.toString(usbBuffer));

                    }

                    //Recebe Valor Horimetro
//                    horimetroEmSegundos =  usbBuffer[16] * 4294967296.0F + usbBuffer[17] * 65536 +
//                            usbBuffer[18] * 256+
//                            usbBuffer[19];
                    horimetroEmSegundos =  usbBuffer[17] * 65536 +
                            usbBuffer[18] * 256+
                            usbBuffer[19];
                    //textView2.setText(Arrays.toString(usbBuffer));
                    testTextView.setText(String.valueOf(horimetroEmSegundos));
                    //transforma segundos em horas
                    horimetro=horimetroEmSegundos/3600;
//                    //horimetro lido do hardware
                    HORIMETRO_HARDWARE=true;
                    //hora atual
                    //timeStampAtual = System.currentTimeMillis()/3600000;
                    //verifica se horimetro salvo no smartphone é maior doque o horimetro que vem do hardware
                    if(horimetro < myPreferences.getHorimetro()) {
                        horimetro = myPreferences.getHorimetro();
                        //habilita ajustar horimetro do hardware
                        //TODO Descomentar;
                        //ESP8266_AJUSTE_HORIMETRO = true;
                        stg = horimetro;
                    }
                    //Recebe status do pos chave
                    posChave =String.valueOf(usbBuffer[8]);
                    if (posChave == null || posChave.equals("")) posChave = "N";

                    //Gera evento pós Chave
                    if (!posChaveAnterior.equals(posChave)) {
                        posChaveAnterior = posChave;
                        posChaveEvent = true;
                        //Log.i("PÓS CHAVE EVENT ","ACONTECEU");
                    }
                    //Habilitação de Horímetro
                    //Habilita ou desabilita incremento de horímetro
                    HORIMETRO_ENABLE = posChave.equals("0");

                    //verificação de luz de marcha ré
                    luzMarcahRe = String.valueOf(usbBuffer[12]);
                    if (luzMarcahRe.equals("1")) {
                        // TODO intent canera de ré
                        //Log.d("LUZ MARCHA RÉ","OFF");
                    } else {

                        // Log.d("LUZ MARCHA RÉ","ON");
                    }

                    //verificação do sensor de cinto de segurança
                    //CINTO_SEGURANÇA_ALARME = sensorCinto.equals("1");
                    sensorCinto = String.valueOf(usbBuffer[10]);
                    if(sensorCinto.equals("1") &&
                            cintoEventoON==false &&
                            CINTO_SEGURANÇA_ALARME==false &&
                            velocidadeInstantanea>=VELOCIDADE_ALARME_CINTO){
                        CINTO_SEGURANÇA_ALARME=true;
                        cintoEventoON=true;
                    }

                    if(sensorCinto.equals("0") && cintoEventoOFF==false && CINTO_SEGURANÇA_ALARME==true){
                        CINTO_SEGURANÇA_ALARME=false;
                        cintoEventoOFF=true;
                    }

                    //Zera contador de socket timeout
                    socketTimeoutCounter=0;
                    SOCKET_TIMEOUT=false;
                    //Log.d("SOCKET: ","SOCKET_TIMEOUT=false");
                    noHardwareResetCounter=0;

                    //Recebe Status Rele
                    statusRele = String.valueOf(usbBuffer[4]);
                    if (statusRele == null || statusRele.equals("")) statusRele = "N";

                    //Recebe evento Tx Hardware
                    eventoRxHardware = String.valueOf(usbBuffer[14]);
                    if (eventoRxHardware == null || eventoRxHardware.equals("")) {
                        eventoRxHardware = "Vazio";
                        testTextView2.setText("vazio");
                    }else{
                        eventoTxHardware=false;
                        //testTextView2.setText("false");
                    }

//                    //Cálculo da temperatura para senor NTC
                    valorAdReferencia1V = String.valueOf(usbBuffer[2]);
                    if (valorAdReferencia1V == null || valorAdReferencia1V.equals(""))
                        valorAdReferencia1V = "0";

                    valorAD = Integer.parseInt((valorAdReferencia1V));

                    if(valorAD <= 400 && valorAD >361){
                        temperaturaInstantanea = (int) (valorAD * 0.1303 - 17.4074);
                    }else if(valorAD <= 361 && valorAD >238){
                        temperaturaInstantanea = (int) (valorAD * 0.1018 - 6.1149);
                    }else if(valorAD <= 238){
                        temperaturaInstantanea = (int) (valorAD * 0.1344 - 13.676);


                    }else{
                        temperaturaInstantanea = (int) (pow(2.718281,0.0018 * valorAD)*18.794);
                    }
                    testTextView2.setText(String.valueOf(temperaturaInstantanea));

                /*********************** Escrita na USB **************************/
                    if(!eventoTxHardware) {
                        byte[] buffer = new byte[10];
                        if (HABILITA_RELE && !eventoTxHardware) {
                            comandoToESP826 = "ReleOn";
                            buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                            HABILITA_RELE = false;
                            eventoTxHardware = true;
                        }

                        if (DESABILITA_RELE && !eventoTxHardware) {
                            comandoToESP826 = "ReleOff";
                            buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                            DESABILITA_RELE = false;
                            eventoTxHardware=true;

                        }

                       if (HABILITA_SIRENE && !eventoTxHardware) {
                           comandoToESP826 = "LedSinalOn";
                           buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                           HABILITA_SIRENE = false;
                           eventoTxHardware=true;
                       }
                       if (DESABILITA_SIRENE && !eventoTxHardware) {
                           comandoToESP826 = "LedSinalOff";
                           buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                           DESABILITA_SIRENE = false;
                           eventoTxHardware=true;
                       }
                       if (DESABILITA_HORA_EFETIVA && !eventoTxHardware) {
                           comandoToESP826 = "HoraEfetivaOff";
                           buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                           DESABILITA_HORA_EFETIVA = false;
                           eventoTxHardware=true;
                       }
                       if (HABILITA_HORA_EFETIVA && !eventoTxHardware) {
                           comandoToESP826 = "HoraEfetivaOn";
                           buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                           HABILITA_HORA_EFETIVA = false;
                           eventoTxHardware=true;
                       }
                       if (DESABILITA_SIRENE_CINTO && !eventoTxHardware) {
                           comandoToESP826 = "ReleCintoOff";
                           DESABILITA_SIRENE_CINTO = false;
                           eventoTxHardware=true;
                               Log.d("EVENTOS ", "COMANDO DESABILITA ALARME CINTO");
                       }
                       if (HABILITA_SIRENE_CINTO && !eventoTxHardware) {
                           comandoToESP826 = "ReleCintoOn";
                           buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                           HABILITA_SIRENE_CINTO = false;
                           eventoTxHardware=true;
                       }
                       if (GOTO_RECEBIDO && !eventoTxHardware) {
                           comandoToESP826 = "gotoRecebido";
                            buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                            GOTO_RECEBIDO = false;
                       }
                       if (GOTO_RESPONDIDO && !eventoTxHardware) {
                           comandoToESP826 = "gotoRespondido";
                            buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                            GOTO_RESPONDIDO = false;
                            eventoTxHardware=true;
                       }
                       if (ESP8266_AJUSTE_HORIMETRO && !eventoTxHardware) {
                            stg=stg*3600;
                            int horimetroEmSegundos = (int) stg;
                           comandoToESP826 = "espHori"+horimetroEmSegundos;
                            buffer =  comandoToESP826.getBytes(StandardCharsets.UTF_8); // Java 7+ only
                            //output.write(comandoToESP826+horimetroEmSegundos);
                            ESP8266_AJUSTE_HORIMETRO = false;
                            eventoTxHardware=true;

                       }

                        if (mOutputStream != null) {
                            try {
                                mOutputStream.write(buffer, 0, buffer.length);
                                mOutputStream.flush();
                                //mOutputStream.close();
                                try {
                                    String response = new String(buffer, "UTF-8");
                                    //textView1.setText(String.valueOf(response));
                                    //Log.e(TAG, "write OK");
                                } catch (UnsupportedEncodingException e) {
                                    Log.e(TAG, "convert failed", e);
                                    //textView3.setText(String.valueOf(e));
                                }

                                //return;
                            } catch (IOException e) {
                                Log.e(TAG, "write failed", e);
                                //textView3.setText(String.valueOf(e));
                            }
                        }
                    }

                } catch (IOException e) {
                    //break;
                    Log.e(TAG, "read failed", e);
                    //textView2.setText("Read Failed: " + e);
                }

            }
            handler1s.postDelayed(this, 1000);// 1000 miliseconds
        }
    };

    private void txDataNow(){
        //Limpa qualquer runnable da fila
        httpConnectionHandler.removeCallbacks(httpConnectionTimer);
        //novo runnable adicionado a fila para ser rodado agora
        httpConnectionHandler.post(httpConnectionTimer);
    }

    private void calculaMostraHoraEfetiva(){
        if (horimetro - horimetroAz > 0 && horaEfetiva > 0) {
            horaEfetivaPercentual = (horaEfetiva / (horimetro - horimetroAz)) * 100;
        }else{
            horaEfetivaPercentual=-1.0f;
        }

        //printa horaEfetivaPercentual na tela
//        if(horaEfetivaPercentual>=0) {
//            textViewHoraEfetiva.setText(String.format(Locale.ENGLISH, "%.1f", horaEfetivaPercentual));
//        }else{
//            textViewHoraEfetiva.setText("INIT");
//        }
    }

    /***********************************************************************************************
     *
     *                   HTTP Connection TIMER
     ***********************************************************************************************/
    Runnable httpConnectionTimer = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            if(HTTP_TX_ENABLED) {
                //Lê horimetro do smartphone
                horimetroSoftware = myPreferences.getHorimetro();
                //verifica de horimetro do hardware foi lido
                if(HORIMETRO_HARDWARE){
                    timeStampAtual = System.currentTimeMillis();
                    timeStampAtual = timeStampAtual/3600000;

                    //horimetro = horímetro do hardare/3600
                    //horimetroSoftware == valor lido preferences
                    float deltaHorimetro = horimetro-horimetroSoftware;

                    float deltaTimestamp = (timeStampAtual -timeStampUltimoHorimetro)+0.06f;
                    //Log.i("_HH Hr Hard",String.valueOf(horimetro));
                    //Log.i("_HH Hr Soft",String.valueOf(horimetroSoftware));
                    //Log.i("_HH Ts Atual",String.valueOf(timeStampAtual));
                    //Log.i("_HH Ts Ultimo",String.valueOf(timeStampUltimoHorimetro));
                    //Log.i("_HH Delta Hr",String.valueOf(deltaHorimetro));
                    //Log.i("_HH Delta Ts",String.valueOf(deltaTimestamp));

                    //Testa se horímetro hardware é maior que as horas corridas
                    if((deltaHorimetro)>(deltaTimestamp)){
                        //corrige horimetro hardware com valor atual do horimetro software
                        Toast.makeText(mainActivityContext, "Horímetro Hardware Ajustado", Toast.LENGTH_SHORT).show();
                        //habilita ajustar horimetro do hardware com o valor do horimetro smartphone
                        ESP8266_AJUSTE_HORIMETRO=true;
                        stg = horimetroSoftware;
                    }else{
                        //Salva valor de horímetro em Shared Preferences
                        myPreferences.setHorimetro(horimetro);
                    }

                    timeStampUltimoHorimetro = System.currentTimeMillis();
                    timeStampUltimoHorimetro = timeStampUltimoHorimetro/3600000;
                    myPreferences.setTimeStumpUltimo(timeStampUltimoHorimetro);
                }

                Bundle webData = new Bundle();
                webData.putDouble("VELOCIDADE_INSTANTANEA", velocidadeInstantanea);
                webData.putDouble("VELOCIDADE_MAX", velocidadeMaximaTx);
                webData.putDouble("VELOCIDADE_MEDIA", velocidadeMedia);
                webData.putString("EMAIL", email);
                webData.putString("PASSWORD", password);
                webData.putInt("ID_MAQUINA", idMaquinaDB);
                webData.putString("SENHA_HTTP", senhaHttp);
                webData.putDouble("LATITUDE", lastLatitude);
                webData.putDouble("LONGITUDE", lastLongitude);
                webData.putDouble("ALTITUDE", altitude);
                webData.putDouble("QUANTIDADE_COMBUSTIVEL", quantidadeCombustivel);
                webData.putLong("TIME_STAMP_COMBUSTIVEL", timeStampCombustivel);
                webData.putString("ABASTECEDOR", abastecedorName);
                webData.putLong("TIME_STAMP_AJUSTE_HORIMETRO", timeStampAdjHorimetro);
                webData.putBoolean("AJUSTE_HORIMETRO_FEITO", ajusteHorimetroFeito);
                //zera auste de horimetro feito
                if (ajusteHorimetroFeito) {
                    ajusteHorimetroFeito = false;
                }
                webData.putLong("DATA_MANUTENÇÃO_ABERTA", timeStampParadaManutenção);
                webData.putLong("DATA_MANUTENÇÃO_EXECUTADA", timeStampManutençãoExecutada);
                webData.putBoolean("EM_MANUTENÇÃO",ACIONA_ALERTA_MANUTENÇÃO);
                webData.putString("STATUS_MANUTENÇÃO",statusModoManut);
                webData.putFloat("BEARING", bearing);
                webData.putDouble("VELOCIDADE_GPS", velocidadeInstantanea);
                webData.putDouble("ACCEL_TOTAL", impactoTotalAnterior);
                if(posChaveStatus){
                    webData.putString("POSCHAVE_ON_OFF", "0");
                }else{
                    webData.putString("POSCHAVE_ON_OFF","1");
                }
                webData.putString("LUZ_MARCHA_RE", luzMarcahRe);
                webData.putInt("VALOR_AD_REF_1V", valorAD);
                webData.putInt("PULSOS_EM_200MS", numDePulsos200msInt);
                webData.putString("STATUS_RELE_ON_OFF", statusRele);
                webData.putString("MOTIVO_RELE", motivoBloqueioRele);
                webData.putString("CINTO_ON_OFF", sensorCinto);
                if(HORIMETRO_HARDWARE){
                    webData.putFloat("HORIMETRO", horimetro);
                    //Log.i("_HU HrHd Enviado",String.valueOf(horimetro));
                }else{
                    webData.putFloat("HORIMETRO", horimetroSoftware);
                    //Log.i("_HU HrSt Enviado",String.valueOf(horimetroSoftware));
                }

                webData.putFloat("HORIMETRO_PARCIAL", horimetroParcial);
                webData.putFloat("HORA_EFETIVA_PERCENTUAL", horaEfetivaPercentual);
                webData.putFloat("HODOMETRO", hodometro);
                webData.putFloat("HODOMETRO_PARCIAL", hodometroParcial);
                webData.putFloat("GPS_ACCURACY", GPSaccuracy);
                webData.putInt("TEMPERATURA", temperaturaMedia);
                webData.putInt("TEMPERATURA_PICO", temperaturaPico);
                webData.putInt("NIVEL_BATERIA", battLevel);
                webData.putBoolean("CARREGAMENTO", isCharging);
                webData.putFloat("HORA_EFETIVA", horaEfetiva);
                webData.putDouble("VELOCIDADE_MEDIA", velocidadeMedia);
                webData.putInt("OPERADOR_ID_DB", userIdQrCode);
                webData.putFloat("ACCEL_X", accelX);
                webData.putFloat("ACCEL_Y", accelY);
                webData.putFloat("ACCEL_Z", accelZ);
                webData.putLong("DATA_USAGE_TX", mStartTX);
                webData.putLong("DATA_USAGE_RX", mStartRX);
                webData.putString("FIREBASE_USER_ID", userID);

                try {
                    new HttpConnection(webData);
                    //Toast.makeText(mainActivityContext, "Http Connection", Toast.LENGTH_SHORT).show();
                    //salva check list críticos e geral from web
                    if(jsonArrayCriticos!=null && jsonArrayGeral!=null) {
                        myPreferences.setCheckListCriticosAtual(jsonArrayCriticos);
                        myPreferences.setCheckListGeralAtual(jsonArrayGeral);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Log.i("VERIFY_JSON", String.valueOf(e));
                }


                if (posChave.equals("0")) {
                    httpConnectionHandler.postDelayed(this, httpConnectionInterval);
                } else {
                    //intervalo de transmissão com pós chave deligado em milisegundos
                    httpConnectionHandler.postDelayed(this, intervaloTxPoschaveDesligado);
                }
            }

        }
    };

    /***********************************************************************************************
     *
     *                   Velocidade Média TIMER
     ***********************************************************************************************/
    Runnable velociMediaTimer = new Runnable() {
        @Override
        public void run() {
            if(isUserID) {
                //se o operador foi reconhecido, inicia a contagem de tempo para média de velocidade
                TempoTotalVelocidadeMedia++;
                if (velocidadeInstantanea > 0.0) {
                    //Distancia percorrodida acumulativa
                    distanciaPercorrida = distanciaPercorrida + (velocidadeInstantanea / 3600);
                    //Hodometro total acumulativo
                    hodometro = hodometro + (float) (velocidadeInstantanea / 3600);

                    //Salva valor de hodometro em Shared Preferences
                    //myPreferences.setHodometro(hodometro);
                    //hodometro parcial
                    hodometroParcial = hodometroParcial + (float)(velocidadeInstantanea / 3600) ;
                    //Salva valor de hodometro parcial em Shared Preferences
                    //myPreferences.setHodometroParcial(hodometroParcial);

                    velocidadeMedia = (distanciaPercorrida / TempoTotalVelocidadeMedia * 3600);
                }
                //Velocidade média na tela
                //textViewVelocidadeMedia.setText(String.format( "%.0f", velocidadeMedia ));

                imageViewPontoVelocidadeMedia.setRotation((float) velocidadeMedia/((float)velMaxGauge/180));

                //Hodometro total na tela
                textViewHodometro.setText(String.format(Locale.ENGLISH,"%.1f", hodometro ));
                //Hodometro parcial na tela
                textViewHodometroParcial.setText(String.format(Locale.ENGLISH, "%.1f", hodometroParcial ));
                //Fprmata e mostra hora efetiva percentual
                //textViewHoraEfetiva.setText( String.format(Locale.ENGLISH, "%.1f", horaEfetivaPercentual ));

            }

            if(statusRele !=null) {
                if (statusRele.equals("1")) {
                    imageViewCadeado.setImageResource(R.mipmap.padlock);
                } else if (statusRele.equals("0")) {
                    imageViewCadeado.setImageResource(R.mipmap.padunlock);
                }
            }

            velocidadeMediaHandler.postDelayed(this,1000);
        }
    };
    //Carrega Shared Preferences para Diâmetro de pneu e num. de pulsos por volta
    private void CarregaDiamPulsos(){
        diametroDoPneuCm = myPreferences.getDiametroDoPneu();
        numPulsosPorVolta = myPreferences.getNumDePulsosPorVolta();
    }

    private void CalculaPerimetro (){
        //Calculo do perímetro do pneu em metros
        perimetroDoPneu =  (float) PI * diametroDoPneuCm * 0.01f;
    }

    private void userLogin() {
         myPreferences.setID_OPERADOR(true);
        // call user login activity
        Intent intent = new Intent(this,UserLogin.class);
        startActivity(intent);
    }

    private void screenBrightness(int brilho){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = brilho;
        getWindow().setAttributes(params);
    }

    private void gotoListener(){

        //se goto message está habilitada e o póschave está ligado e tem operador, então FireStore Listener
        if(gotoMessage.equals("sim") && posChaveStatus && userName !="") {
            if (registration != null) {
                registration.remove();
                //Log.d("GOTO: ","Listener Removido");
            }
            //Log.d("GOTO: ","Listener Ativado");
            registration = fireStoredb.collection("mensagens")
                    //query se mensagem está endereçada para a máquina (ID do operador)
                    .whereEqualTo("msgTo", String.valueOf(userIdQrCode))
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                //Log.w("MsgFirebase", "Listen failed.", e);
                                return;
                            }
                            String source = documentSnapshots != null && documentSnapshots.getMetadata().hasPendingWrites()
                                    ? "Local" : "Server";
                            //Log.d("MsgFirebase", "MSG_Recebida: " + source);

                            if (documentSnapshots != null && !documentSnapshots.isEmpty() && source.equals("Server")){
                                //Log.d("MsgFirebase", "MSG_Filtrada: ");
                                List<String> mensagensRecebidas = new ArrayList<>(documentSnapshots.size());
                                for (DocumentSnapshot doc : documentSnapshots) {
                                    //testa se o campo mensagem não está vazio e o status = "enviada"
                                    if (doc.get("message") != null && doc.get("status").equals("enviada") ) {
                                        mensagensRecebidas.add(doc.getString("message"));
                                        String tipoMSg = doc.getString("tipo");
                                        String mesgRecebida = doc.getString("message");
                                        String docRef = doc.getId();
                                        //sinaliza mensagem recebida mas ainda não lida
                                        fireStoreAnswer(docRef,"RECEBIDA");
                                        //Acende LED de Mensagem Recebida
                                        //HABILITA_SIRENE=true;
                                        GOTO_RECEBIDO=true;
                                        alertDialogGoto(tipoMSg,mesgRecebida,docRef);
                                    }
                                }
                                //Log.d(TAG, "Mensagens_FireStore: " + mensagensRecebidas);
                                //Toast.makeText(mainActivityContext, mensagensRecebidas.toString(), Toast.LENGTH_SHORT).show();
                            }else{
                                //Log.d("MsgFirebase", "MSG_Vazia: ");
                            }

                        }

                    });

        }
    }

    private void salvaPreferences(){
        //salva valor de hora efetiva
        myPreferences.setHoraEfetiva(horaEfetiva);
        //Toast.makeText(this, "ON STOP", Toast.LENGTH_SHORT).show();
        //salva hora efetiva percentual
        myPreferences.setHorimetroParcial(horimetroParcial);

        //Salva valor de contador de horímetro em segundos em Shared Preferences
        myPreferences.setCountHorimetro6min(countHorimetro);
        //Salva valor de contador de horas efetivas em segundos em Shared Preferences
        myPreferences.setCountHorimetro6min(countMaquinaEmMovomento);

        //Salva valor de horímetro parcial em Shared Preferences
        myPreferences.setHorimetroParcial(horimetroParcial);
        //Salva valor de horímetro em Shared Preferences
        myPreferences.setHorimetro(horimetro);

        //salva data do abastecimento em preferences
        myPreferences.setDataTxServer(dataTxServer);

        //salva Hora do abastecimento em preferences
        myPreferences.setHoraTxServer(horaTxserver);

        //Salva valor de hodometro em Shared Preferences
        myPreferences.setHodometro(hodometro);

        //Salva valor de hodometro parcial em Shared Preferences
        myPreferences.setHodometroParcial(hodometroParcial);

        //Salva última posção GPS
        myPreferences.setLastLatitude(lastLatitude.toString());
        myPreferences.setLastLongitude(lastLongitude.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d("GOTO: ","OnStart");
        //inicio o listener de autenticação
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d("GOTO: ","OnSTop");
        //removo o listener de autenticação
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }


    }



    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        //Log.d("GOTO: ","OnPause");
        //unregister GoTo message listener
        if(registration!=null) {
            registration.remove();
            //Log.d("GOTO: ","Removed Listener OnPause");
        }
        // Activity's been paused
        isPaused = true;

        Toast.makeText(this, "ON PAUSE", Toast.LENGTH_SHORT).show();
        //closeAccessory();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
//        Toast.makeText(this, "ON DESTROY", Toast.LENGTH_SHORT).show();


        //Log.i("GOTO:", "OnDestroy");
        if(apControl!=null && apControl.isEnabled()){
            RUN_SOCKET_STOP = true;
            apControl.disable();
        }

        //Close socket

        if(serverSocket!=null &&!serverSocket.isClosed()){
            try {
                serverSocket.close();
                //Log.i("SocketSocket_CLOSED_OK ", "SOCKET CLOSED");
            } catch (IOException e) {
                e.printStackTrace();
                //Log.i("VerfySocketClosedErr ", e.toString());
            }

        }
        unregisterReceiver(usbReceiver);

    }

    /***********************************************************************************************
    *
    *                   GPS LOCATION LISTENER OVERRIDE METHODS
     ***********************************************************************************************/
    @Override
    public void onLocationChanged(Location location) {
        //seta ponteiro de velocidade em Zero se não existe GPS
        if (location==null){
            // if you can't get speed because reasons :)
            //textViewVelGPS.setText("00");
            if(GPS_SPEED) {
                //textviewVelocidadeInstantanea.setText("NC");
                imageViewPonteiroVelocidade.setRotation(OFFSET_PONTEIRO_VELOCIDADE);
            }
            imvGpsFix.setImageResource(R.mipmap.gps_searching);

        }
        else{
            //Se velocidade por GPS
            if(GPS_SPEED) {
                float speed = (location.getSpeed() * 3600) / 1000;
//                Log.d("GPS BEARING: ",String.valueOf(location.getBearing()));
//                Log.d("GPS ACCURACY: ",String.valueOf(location.getAccuracy()));
//                Log.d("GPS SPEED MS: ",String.valueOf(location.getSpeed()));
//                Log.d("GPS SPEED KM: ",String.valueOf(speed));
//                Log.d("GPS LAT: ",String.valueOf(location.getLatitude()));
//                Log.d("GPS LON: ",String.valueOf(location.getLongitude()));
//                Log.d("GPS PROVIDER: ",String.valueOf(location.getProvider()));
//                Log.d("GPS ALTITUDE: ",String.valueOf(location.getAltitude()));
//                Log.d("GPS SATTELITES: ",String.valueOf(location.getExtras().getInt("satellites")));

                //filtro passa baixa RC
                speed = lowPass(velocidadeAnterior, speed);
                velocidadeAnterior=speed;

                velocidadeInstantanea=(double)speed;

                //seta timeout envento de GPS
                GpsEventCount=GPS_EVENT_TIMEOUT;

                //Seta velocidade de pico
                double LIMITE_INFERIOR_VELOCIDADE_PICO = 4.9;
                if (speed > LIMITE_INFERIOR_VELOCIDADE_PICO && speed > velocidadeMaxima) {
                    velocidadeMaxima =(double) speed;
                }
                if (speed > velocidadeMaximaTx) {
                    velocidadeMaximaTx=(double) speed;
                }

                //textviewVelocidadeInstantanea.setText(String.valueOf(speed));
                float mRotation = OFFSET_PONTEIRO_VELOCIDADE + speed/((float)velMaxGauge/180);
                imageViewPonteiroVelocidade.setRotation(mRotation);

                //textViewPicoVelocidade.setText(String.valueOf(String.format("%.0f", velocidadeMaxima)));
                imageViewPicoVelocidade.setRotation((float) velocidadeMaxima /((float)velMaxGauge/180));
            }
            GPSaccuracy = location.getAccuracy();
            textViewAccuracy.setText(String.format(Locale.ENGLISH,"%.2f",GPSaccuracy));

            imvGpsFix.setImageResource(R.mipmap.gps_fix);

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            //salva última posição válida
            //String mLastLatitude = latitude.toString();
           // myPreferences.setLastLatitude(mLastLatitude);
            //String mLastLongitude = longitude.toString();
            //myPreferences.setLastLongitude(mLastLongitude);
            //Log.d("GPS SATTELITES: ","Ultima Posição");

            //lastLatitude = Double.valueOf(myPreferences.getLastLatitude());
            lastLatitude = location.getLatitude();
            //lastLongitude = Double.valueOf(myPreferences.getLastLongitude());
            lastLongitude = location.getLongitude();

            altitude = location.getAltitude();
            bearing = location.getBearing();
        }
    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //Log.i(TAG, "onStausChange"+s+" "+i+" "+bundle);
    }

    @Override
    public void onProviderEnabled(String s) {
        //Log.d(TAG, "onProviderEnabled "+s);
    }

    @Override
    public void onProviderDisabled(String s) {
        //Log.d(TAG, "onProviderDisabled "+s);
    }

    protected float lowPass( float input, float output ) {
        //if ( output == null ) return input;

//        Log.i("VELOCIDADE IN", String.valueOf(input));
//        Log.i("VELOCIDADE OUT", String.valueOf(output));
        output = output + ALPHA * (input - output);

        //Log.i("VELOCIDADE RES", String.valueOf(output));
        return output;
    }



    /***********************************************************************************************
     *
     *                   ACELERÔMETRO EVENTO
     ***********************************************************************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {

        float eventX = event.values[0];
        float eventY = event.values[1];
        float eventZ = event.values[2];


        final float alpha = 0.8f;
        final float gravity[]={ 9.81f,9.81f,9.81f};

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * eventX;
        gravity[1] = alpha * gravity[1] + (1 - alpha) * eventY;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * eventZ;

        float x = eventX - gravity[0];
        float y = eventY - gravity[1];
        float z = eventZ - gravity[2];

        //transfere valores dinâmicos do acelerometro para transmissão web
        accelX = x;
        accelY = y;
        accelZ = z;

        impactoTotal = Math.sqrt(x * x + y * y + z * z);

        impactoTotal = (impactoTotal-gravidade)/gravidade;

        // impactoTotal = abs(impactoTotal);
        double totalx10 = impactoTotal*10;
        acellMax = (int) totalx10;
        //String.format("%.2f", impactoTotal);
       //Slava pico de força G
        if(impactoTotalAnterior<impactoTotal){
            impactoTotalAnterior=impactoTotal;
            //textViewImpactoMaximo.setText(String.valueOf(impactoTotal));
            //pbAcelerometroMax.setProgress(acellMax);
        }
        //Salva pico de força G para Mostrar na tela
        if(impactoTotalDisplay<impactoTotal){
            impactoTotalDisplay=impactoTotal;
            textViewImpactoMaximo.setText(String.valueOf(impactoTotal));
            pbAcelerometroMax.setProgress(acellMax);
        }
        //Desabilita rele por limite de impacto se a AcionaReleImpacto opção estiver habilitada pelo usuário
        if(impactoTotal>=setPointAcelerometro && ACIONA_RELE_PORIMPACTO){
            //Motivo para desabilitação do relé
            motivoBloqueioRele = "Limite de Impacto Atingido";
            //função trocada
            //DESABILITA_RELE = true;
            //HABILITA_RELE = true;
        }

        //mostra alert dialog se rele for desabilitado por impacto
        //função trocada
        //if(DESABILITA_RELE && !ALERT_DIALOG_SHOWING && ACIONA_RELE_PORIMPACTO){
        if(HABILITA_RELE && !ALERT_DIALOG_SHOWING && ACIONA_RELE_PORIMPACTO){
            alertDialog(getString(R.string.titulo_dispositivo_desabilitado),motivoBloqueioRele
                + "\n" + getString(R.string.nivel_supervisor_requerido) );
            //previne mostrar multiplos dialogs
            ALERT_DIALOG_SHOWING = true;
            //Marca dispositivo como bloqueado
//            myPreferences.setDISPOSITIVO_BLOQUEADO(true);
//            DISPOSITIVO_BLOQUEADO = true;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Log.i("Accelerometer",String.valueOf(accuracy));
    }

//    private class openAccessoryThread implements Runnable{
//
//        @Override
//        public void run() {
//            try {
//                if(inputStream.available()>0){
//
//                    int ch;
//                    StringBuilder sb = new StringBuilder();
//                    while((ch = inputStream.read()) != -1)
//                        sb.append((char)ch);
//                    read=sb.toString();
//                    testTextView2.setText(read);
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                comandoToESP826 = "ReleOn";
//                outputStream.write(Integer.parseInt(comandoToESP826));
//            }catch (IOException e){
//
//            }
//        }
//    }

    /***********************************************************************************************
     *
     *                  SOCKET
     ***********************************************************************************************/
//    private class ServerThread implements Runnable {
//
//        //ServerThread class - run method
//        @Override
//        public void run() {
//            //Socket socket;
//
//            try {
//                if (!serverSocket.isBound() && !serverSocket.isClosed()) {
//                        Log.i("SOCKET_LOG ", "Binding Socket");
//                        serverSocket.setReuseAddress(true);
//                        serverSocket.bind(new InetSocketAddress(SERVER_PORT)); // <-- now bind it
//                        Log.i("SOCKET_LOG ", "Criado e Vinculado a porta " + SERVER_PORT);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.i("SOCKET_LOG ", "Erro Criando e veiculando a porta "+ String.valueOf(e));
//                if(!serverSocket.isClosed()){
//                    try {
//                        serverSocket.close();
//                        Log.i("SOCKET_LOG ", "SOCKET CLOSED OK");
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                        Log.i("SOCKET_LOG ", "CLOSED ERR "+ String.valueOf(e1));
//                    }
//
//                }
//            }
//
//            if (!Thread.currentThread().isInterrupted()) {
//                Log.i("SOCKET_LOG ", "Se Current Thread.isInterrupted.. ");
//                if (!serverSocket.isClosed()) {
//                    Log.i("SOCKET_LOG ", "Se socket não está fechado");
//                    try {
//                        socket = serverSocket.accept();
//                        Log.i("SOCKET_LOG ", "Socket Accepted");
//                        CommunicationThread commThread = new CommunicationThread(socket);
//                        new Thread(commThread).start();
//                        Log.i("SOCKET_LOG ", "commThread Started");
//                        //CountThread++;
//                        Log.i("SOCKET_LOG ", "GET LOCAL PORT " + serverSocket.getLocalPort());
//                        Log.i("SOCKET_LOG ", "GET INET ADDRESS " + serverSocket.getInetAddress());
//                        Log.i("SOCKET_LOG ", "GET CHANNEL " + serverSocket.getChannel());
//                        Log.i("SOCKET_LOG ", "GET LOCAL SOCKET ADDRESS " + serverSocket.getLocalSocketAddress());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.i("SOCKET_LOG ", "Erro new Thread(commThread).start() " + e.toString());
//
//                    }
//                }
//            }else{
//                Log.i("SOCKET_LOG ", "Current Thread.isInterrupted = true ");
//            }
//
//        }
//    }
//
//    class CommunicationThread implements Runnable {
//
//        private Socket socket;
//        private BufferedReader input;
//        //Socket Writer
//        private BufferedWriter output;
//
//        CommunicationThread(Socket mSocket) {
//            this.socket = mSocket;
//
//            try {
//                //Socket Reader
//                this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
//                //Socket Writer
//                this.output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
//                //no time out
//                this.socket.setSoTimeout(0);
//
//            } catch (SocketTimeoutException e){
//                if(socket.isConnected()){
//                    Log.d("SOCKET_LOG ","Timout(conectado) ao criar buffer Ler/Escrever/" + e.toString());
//                }
//                Log.d("SOCKET_LOG ","Timout(desconectado) ao criar buffer Ler/Escrever/" + e.toString());
//            } catch (IOException e) {
//               e.printStackTrace();
//               Log.d("SOCKET_LOG ","Exxception ao criar buffer Ler/Escrever/" + e.toString());
//            }
//        }
//
//        //CommunicationThread Class - run method
//        public void run() {
//           while (!Thread.currentThread().isInterrupted()) {
//               if(RUN_SOCKET_STOP){
//                   Thread.currentThread().interrupt();
//               }
//                try {
//                    read = input.readLine();
//                    //updateConversationHandler.post(new updateUIThread(read));
//                    if(read!=null){
//                        if(!read.isEmpty()) {
//                            Log.d("SOCKET_LOG ","read length "+  String.valueOf(read.length()));
//                            updateUIThread(read);
//                        }
//                    }else{
//                        Log.d("SOCKET_LOG ","READ NULL");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.d("SOCKET_LOG ","Erro ao tentar ler readLine() "+e.toString());
//                    //SERVER_SOCKET4 :: java.net.SocketException: recvfrom failed: ETIMEDOUT (Connection timed out)
//                }
//               //Socket Writer
//               //if(!eventoTxHardware) {
//                   if (HABILITA_RELE && !eventoTxHardware) {
//                       //String comandoToESP826 = getString(R.string.TurnsReleOn);
//                       comandoToESP826 = "ReleOn";
//                       try {
//                           output.write(comandoToESP826);
//                           //output.newLine();
//                           //output.write(userName);
//                           output.newLine();
//                           output.flush();
//                           HABILITA_RELE = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO HABILITA RELE ");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO HABILITA RELE " + e.toString());
//                       }
//                   }
//                   if (DESABILITA_RELE && !eventoTxHardware) {
//                       //String comandoToESP826 = getString(R.string.TurnsReleOff);
//                       comandoToESP826 = "ReleOff";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           DESABILITA_RELE = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO DESABILITA RELE " );
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO DESABILITA RELE " + e.toString());
//                       }
//                   }
//                   if (HABILITA_SIRENE && !eventoTxHardware) {
//                       comandoToESP826 = "LedSinalOn";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           HABILITA_SIRENE = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO HABILITA SIRENE ");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO HABILITA SIRENE " + e.toString());
//                       }
//                   }
//                   if (DESABILITA_SIRENE && !eventoTxHardware) {
//                       comandoToESP826 = "LedSinalOff";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           DESABILITA_SIRENE = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO DESABILITA");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO DESABILITA SIRENE " + e.toString());
//                       }
//                   }
//                   if (DESABILITA_HORA_EFETIVA && !eventoTxHardware) {
//                       comandoToESP826 = "HoraEfetivaOff";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           DESABILITA_HORA_EFETIVA = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO DESABILITA HORA EFETIVA ");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO DESABILITA HORA EFETIVA " + e.toString());
//                       }
//                   }
//                   if (HABILITA_HORA_EFETIVA && !eventoTxHardware) {
//                       comandoToESP826 = "HoraEfetivaOn";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           HABILITA_HORA_EFETIVA = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO HABILITA HORA EFETIVA ");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "COMANDO HABILITA HORA EFETIVA " + e.toString());
//                       }
//                   }
//                   if (DESABILITA_SIRENE_CINTO && !eventoTxHardware) {
//                       comandoToESP826 = "ReleCintoOff";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           DESABILITA_SIRENE_CINTO = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO DESABILITA ALARME CINTO");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("SOCKET_LOG ", "COMANDO DESABILITA ALARME CINTO " + e.toString());
//                       }
//                   }
//                   if (HABILITA_SIRENE_CINTO && !eventoTxHardware) {
//                       comandoToESP826 = "ReleCintoOn";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           HABILITA_SIRENE_CINTO = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO HABILITA ALARME CINTO");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "ERRO COMANDO HABILITA ALARME CINTO " + e.toString());
//                       }
//                   }
//                   if (GOTO_RECEBIDO && !eventoTxHardware) {
//                       comandoToESP826 = "gotoRecebido";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           GOTO_RECEBIDO = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO GOTO RECEBIDO");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "ERRO COMANDO RECEBIDO " + e.toString());
//                       }
//                   }
//                   if (GOTO_RESPONDIDO && !eventoTxHardware) {
//                       comandoToESP826 = "gotoRespondido";
//                       try {
//                           output.write(comandoToESP826);
//                           output.newLine();
//                           output.flush();
//                           GOTO_RESPONDIDO = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO GOTO RESPONDIDO");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "ERRO GOTO RESPONDIDO " + e.toString());
//                       }
//                   }
//                   if (ESP8266_AJUSTE_HORIMETRO && !eventoTxHardware) {
//                       comandoToESP826 = "espHori";
//                       try {
//                           stg=stg*3600;
//                           int horimetroEmSegundos = (int) stg;
//                           output.write(comandoToESP826+horimetroEmSegundos);
//                           output.newLine();
//                           output.flush();
//                           ESP8266_AJUSTE_HORIMETRO = false;
//                           eventoTxHardware=true;
//                           Log.d("EVENTOS ", "COMANDO AJUSTE HORIMETRO");
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                           Log.d("EVENTOS ", "ERRO COMANDO AJUSTE HORIMETRO " + e.toString());
//                       }
//                   }
//
//               //}
//           }
//        }
//    }
//
//
//
//       void updateUIThread(String str) {
//            String msg = str;
//
//            if(str!=null) {
//                //Separa String recebida
//                dadosRecebidos = msg.split("\\|");
//
//                if(dadosRecebidos.length ==9 || dadosRecebidos.length ==10){
//
//                    Log.i("SOCKET_LOG String ", String.valueOf(dadosRecebidos.length) + "-" + msg.toString());
//                    numDePulsosEm200ms = dadosRecebidos[0];
//                    if (numDePulsosEm200ms == null || numDePulsosEm200ms.equals(""))
//                        numDePulsosEm200ms = "0";
//                    valorAdReferencia1V = dadosRecebidos[1];
//                    if (valorAdReferencia1V == null || valorAdReferencia1V.equals(""))
//                        valorAdReferencia1V = "0";
//                    statusRele = dadosRecebidos[2];
//                    if (statusRele == null || statusRele.equals("")) statusRele = "N";
//                    statusSinalização = dadosRecebidos[3];
//                    if (statusSinalização == null || statusSinalização.equals("")) statusSinalização = "N";
//                    posChave = dadosRecebidos[4];
//                    if (posChave == null || posChave.equals("")) posChave = "N";
//                    sensorCinto = dadosRecebidos[5];
//                    if (sensorCinto == null || sensorCinto.equals("")) sensorCinto = "N";
//                    luzMarcahRe = dadosRecebidos[6];
//                    if (luzMarcahRe == null || luzMarcahRe.equals("")) luzMarcahRe = "N";
//                    //converte string numero de pulsos em 200ms em inteiro
//                    numDePulsos200msInt = Integer.parseInt(numDePulsosEm200ms);
//
//                    eventoRxHardware = dadosRecebidos[7];
//                    if (eventoRxHardware == null || eventoRxHardware.equals("")) {
//                        eventoRxHardware = "Vazio";
//                    }else{
//                        eventoTxHardware=false;
//                    }
//
//                    horimetroHardware = dadosRecebidos[8];
//                    if (horimetroHardware == null || horimetroHardware.equals(""))
//                        horimetroHardware = "0";
//                    horimetroEmSegundos= Float.parseFloat(horimetroHardware);
//                    //transforma segundos em horas
//                    horimetro=horimetroEmSegundos/3600;
//                    //horimetro lido do hardware
//                    HORIMETRO_HARDWARE=true;
////                    //hora atual
//                    //timeStampAtual = System.currentTimeMillis()/3600000;
//                    //verifica se horimetro salvo no smartphone é maior doque o horimetro que vem do hardware
//                    if(horimetro < myPreferences.getHorimetro()){
//                        horimetro = myPreferences.getHorimetro();
//                        //habilita ajustar horimetro do hardware
//                        ESP8266_AJUSTE_HORIMETRO=true;
//                        stg = horimetro;
////                    //verifica se horimetro do hardware é maior que as horas corridas
//                    }
//
//                    //RSSI - intensidade do sinal WiFi
//                    if(dadosRecebidos.length==10){
//                        rssi=dadosRecebidos[9];
//                        if(rssi== null || rssi.equals("")) rssi = "não veio";
//
//                    }
//
//                    //Cálculo da temperatura para senor NTC
//                    valorAD = Integer.parseInt((valorAdReferencia1V));
//                    if(valorAD <= 400 && valorAD >361){
//                        temperaturaInstantanea = (int) (valorAD * 0.1303 - 17.4074);
//                    }else if(valorAD <= 361 && valorAD >238){
//                        temperaturaInstantanea = (int) (valorAD * 0.1018 - 6.1149);
//                    }else if(valorAD <= 238){
//                        temperaturaInstantanea = (int) (valorAD * 0.1344 - 13.676);
//
//
//                    }else{
//                        temperaturaInstantanea = (int) (pow(2.718281,0.0018 * valorAD)*18.794);
//                    }
//
//                    //Gera evento pós Chave
//
//                    if (!posChaveAnterior.equals(posChave)) {
//                        posChaveAnterior = posChave;
//                        posChaveEvent = true;
//                        //Log.i("PÓS CHAVE EVENT ","ACONTECEU");
//                    }
//
//                    //Habilitação de Horímetro
//                    //Habilita ou desabilita incremento de horímetro
//
//                    HORIMETRO_ENABLE = posChave.equals("0");
//
//                    //verificação de luz de marcha ré
//                    if (luzMarcahRe.equals("1")) {
//                        // TODO intent canera de ré
//                        //Log.d("LUZ MARCHA RÉ","OFF");
//                    } else {
//
//                        // Log.d("LUZ MARCHA RÉ","ON");
//                    }
//                    //verificação do sensor de cinto de segurança
//                    //CINTO_SEGURANÇA_ALARME = sensorCinto.equals("1");
//
//                    if(sensorCinto.equals("1") &&
//                            cintoEventoON==false &&
//                            CINTO_SEGURANÇA_ALARME==false &&
//                            velocidadeInstantanea>=VELOCIDADE_ALARME_CINTO){
//                        CINTO_SEGURANÇA_ALARME=true;
//                        cintoEventoON=true;
//                    }
//
//                    if(sensorCinto.equals("0") && cintoEventoOFF==false && CINTO_SEGURANÇA_ALARME==true){
//                        CINTO_SEGURANÇA_ALARME=false;
//                        cintoEventoOFF=true;
//                    }
//
//                    //Zera contador de socket timeout
//                    socketTimeoutCounter=0;
//                    SOCKET_TIMEOUT=false;
//                    //Log.d("SOCKET: ","SOCKET_TIMEOUT=false");
//                    noHardwareResetCounter=0;
//                }else {
//
//                    Log.i("SOCKET_LOG String incompleta ", String.valueOf(dadosRecebidos.length)+"-"+ String.valueOf(msg)+ " Length "+ dadosRecebidos.length);
//                    SOCKET_TIMEOUT = false;
//                    //Zera contador de socket timeout
//                    socketTimeoutCounter = 0;
//                    Log.d("SOCKET_LOG: ","SOCKET_TIMEOUT=false");
//                    noHardwareResetCounter = 0;
//                }
//
//            }else{
//                Log.i("SOCKET_LOG String Null ", String.valueOf(dadosRecebidos.length)+"-"+ String.valueOf(msg)+ " Length "+ dadosRecebidos.length);
//                //Zera contador de socket timeout
//                socketTimeoutCounter=0;
//                //Log.i("STR NULL"," NULL");
//                SOCKET_TIMEOUT=false;
//                Log.d("SOCKET_LOG: ","SOCKET_TIMEOUT=false  STRING Null");
//                noHardwareResetCounter=0;
//            }
//        }



    private class CheckBatteryStatus extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String chargeAction = intent.getAction();
            if (chargeAction != null) {
                isCharging = chargeAction.equals(Intent.ACTION_POWER_CONNECTED);
            }
            if (isCharging){
                //Mantem tela acesa
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //Toast.makeText(context, "PLUGED", Toast.LENGTH_SHORT).show();

            }else{
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //Toast.makeText(context, "UNPLUGED", Toast.LENGTH_SHORT).show();

            }

        }
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
