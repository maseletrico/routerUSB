package com.marco.smartrouterdev;

//import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.marco.smartrouterdev.checkList.CheckListData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.net.ssl.SSLException;

import static com.marco.smartrouterdev.MainActivity.eventoChecklist;
import static com.marco.smartrouterdev.checkList.CheckList.IMPORTTANT_CHAR;
import static com.marco.smartrouterdev.checkList.CheckListAdapter.jsonObjectCheckListEnvioCritico;
import static com.marco.smartrouterdev.checkList.CheckListAdapter.jsonObjectCheckListEnvioGeral;

//import android.util.Log;
//import com.marco.smartrouterdev.checkList.CheckList;
//import com.marco.smartrouterdev.checkList.CheckListAdapter;
//import java.io.DataInputStream;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//import java.lang.reflect.Array;
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import static android.R.attr.data;
//import static android.content.Context.MODE_PRIVATE;
//import static com.marco.smartrouterdev.MainActivity.httpTxEvent;
//import static com.marco.smartrouterdev.MainActivity.mainActivityContext;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayAdjHorimetro;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayCheckList;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayCintoOnOff;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayDataUsage;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayManutenção;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayOnOff;
//import static com.marco.smartrouterdev.MainActivity.jsonArrayTransmissao;
//import static com.marco.smartrouterdev.MainActivity.jsonObjectInjetec;
//import static com.marco.smartrouterdev.MainActivity.tbOperadorArray;


class HttpConnection {

    private static String senhaHttp;

    private static int maquinaID=-1;

    private MyPreferences myPreferences;

    private static JSONObject jsonObjectInjetec;
    private static JSONArray jsonArrayCintoOnOff;
    private static JSONArray jsonArrayOnOff;
    private static JSONArray jsonArrayTransmissao;
    private static JSONArray tbOperadorArray;
    private static JSONArray jsonArrayCheckList;
    private static JSONArray jsonArrayDataUsage;
    private static JSONArray jsonArrayAdjHorimetro;
    private static JSONArray jsonArrayManutenção;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    HttpConnection(Bundle webData) throws JSONException {

        jsonObjectInjetec = new JSONObject();
        jsonArrayCintoOnOff = new JSONArray();
        jsonArrayOnOff = new JSONArray();
        jsonArrayTransmissao = new JSONArray();
        tbOperadorArray = new JSONArray();
        jsonArrayCheckList = new JSONArray();
        jsonArrayDataUsage = new JSONArray();
        jsonArrayAdjHorimetro = new JSONArray();
        jsonArrayManutenção = new JSONArray();

        double velocidadeInstantanea =  webData.getDouble("VELOCIDADE_INSTANTANEA");
        double velocidadeMax =          webData.getDouble("VELOCIDADE_MAX");
        double velocidadeMedia =        webData.getDouble("VELOCIDADE_MEDIA");
        maquinaID =                     webData.getInt("ID_MAQUINA");
        senhaHttp =                     webData.getString("SENHA_HTTP");
        double latitude =               webData.getDouble("LATITUDE");
        double longitude =              webData.getDouble("LONGITUDE");
        double quantidadeCombustivel =  webData.getDouble("QUANTIDADE_COMBUSTIVEL");
        Long tsLong =                   System.currentTimeMillis()/1000;
        Long timeStampCombustivel =     webData.getLong("TIME_STAMP_COMBUSTIVEL");
        String operadorAbastecedor =    webData.getString("ABASTECEDOR");
        Long timeStampAdjHorimetro =    webData.getLong("TIME_STAMP_AJUSTE_HORIMETRO");
        Long timeStampManutençãoAberta =webData.getLong("DATA_MANUTENÇÃO_ABERTA");
        Long timeStampManutençãoExecutada =webData.getLong("DATA_MANUTENÇÃO_EXECUTADA");
        boolean emManutenção =          webData.getBoolean("EM_MANUTENÇÃO");
        String statusDeManutenção=      webData.getString("STATUS_MANUTENÇÃO");
        boolean ajusteHorimetroFeito =  webData.getBoolean("AJUSTE_HORIMETRO_FEITO");
        double accelTotal =             webData.getDouble("ACCEL_TOTAL");
        double velocidadeGPS =          webData.getDouble("VELOCIDADE_GPS");
        double altitude =               webData.getDouble("ALTITUDE");
        float precisaoGPS =             webData.getFloat("GPS_ACCURACY");
        float bearing =                 webData.getFloat("BEARING");
        float horimetro =               webData.getFloat("HORIMETRO");
        float horaEfetivaPercentual =   webData.getFloat("HORA_EFETIVA_PERCENTUAL");
        float hodometro =               webData.getFloat("HODOMETRO");
        float hodometroParcial =        webData.getFloat("HODOMETRO_PARCIAL");
        int temperatura =               webData.getInt("TEMPERATURA");
        int temperaturaPico =           webData.getInt("TEMPERATURA_PICO");
        float tempoMaquinaMovimento =   webData.getFloat("HORA_EFETIVA");
        int nivelbat =                  webData.getInt("NIVEL_BATERIA");
        boolean carregamento =          webData.getBoolean("CARREGAMENTO");
        String posChave =               webData.getString("POSCHAVE_ON_OFF");
        int valorRefAD1v =              webData.getInt("VALOR_AD_REF_1V");
        int pulsosEm200ms =             webData.getInt("PULSOS_EM_200MS");
        String sensorCintoSegurança =   webData.getString("CINTO_ON_OFF");
        String luzMarchaRe =            webData.getString("LUZ_MARCHA_RE");
        String statusRele =             webData.getString("STATUS_RELE_ON_OFF");
        String motivoRele =             webData.getString("MOTIVO_RELE");
        String motivoReleCode = null;
        long dataUsageTX =               webData.getLong("DATA_USAGE_TX",-1);
        long dataUsageRX =               webData.getLong("DATA_USAGE_RX",-1);
        String fireBaseUserId =          webData.getString("FIREBASE_USER_ID");

        try {
            motivoReleCode = URLEncoder.encode(motivoRele, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }
        int operadorID =            webData.getInt("OPERADOR_ID_DB");

        float accelX =                webData.getFloat("ACCEL_X");
        float accelY =                webData.getFloat("ACCEL_Y");
        float accelZ =                webData.getFloat("ACCEL_Z");

        //maquinaID=100;

        //myPreferences = new MyPreferences();
        //myPreferences.setContext(contex);


        // tabela operador
        JSONObject jsonObjectOperador = new JSONObject();
        jsonObjectOperador.put("id_operador",(operadorID == -1) ? JSONObject.NULL :operadorID );
        jsonObjectOperador.put("timestamp",tsLong);

        // Array tabela operador
        tbOperadorArray.put(jsonObjectOperador);

        //Objeto GPS
        JSONObject jsonObjectGPS = new JSONObject();
        jsonObjectGPS.put("lat",latitude);
        jsonObjectGPS.put("lng",longitude);
        jsonObjectGPS.put("velocidade",velocidadeGPS);
        jsonObjectGPS.put("altitude",altitude);
        jsonObjectGPS.put("precisao",precisaoGPS);
        jsonObjectGPS.put("bearing",bearing);



        //Objeto Bateria
        JSONObject jsonObjectBateria = new JSONObject();
        jsonObjectBateria.put("nivel",nivelbat);
        jsonObjectBateria.put("carregando",carregamento);

        //Objeto rele
        JSONObject jsonObjectRele = new JSONObject();
        jsonObjectRele.put("rele",statusRele);
        jsonObjectRele.put("motivo_status_rele",motivoReleCode);

        //Objeto Acelerometro
        JSONObject jsonObjectAccelerometer = new JSONObject();
        jsonObjectAccelerometer.put("total",accelTotal);
        jsonObjectAccelerometer.put("x",accelX);
        jsonObjectAccelerometer.put("y",accelY);
        jsonObjectAccelerometer.put("z",accelZ);

        //Objeto temperatura
        JSONObject jsonObjectTemperatura = new JSONObject();
        jsonObjectTemperatura.put("temperatura_instantanea",temperatura);
        jsonObjectTemperatura.put("temperatura_pico",temperaturaPico);

        //Objeto velocidade PCB
        JSONObject jsonObjectSpeedPCB = new JSONObject();
        jsonObjectSpeedPCB.put("velocidade_instantanea",velocidadeInstantanea);
        jsonObjectSpeedPCB.put("velocidade_maxima",velocidadeMax);
        jsonObjectSpeedPCB.put("velocidade_media",velocidadeMedia);

        //Objeto abastecimento
        JSONObject jsonObjectAbastecimento = new JSONObject();
        jsonObjectAbastecimento.put("abastecedor", operadorAbastecedor);
        jsonObjectAbastecimento.put("quantidade",quantidadeCombustivel);

        jsonObjectAbastecimento.put("timestamp",timeStampCombustivel);

        //Objeto hoodmetro
        JSONObject jsonObjectHodometro = new JSONObject();
        jsonObjectHodometro.put("hodometro",hodometro);
        jsonObjectHodometro.put("hodometro_parcial",hodometroParcial);

        //tabela transmissão
        JSONObject jsonObjectTransmissao = new JSONObject();
        jsonObjectTransmissao.put("timestamp",tsLong);
        jsonObjectTransmissao.put("gps",jsonObjectGPS);
        jsonObjectTransmissao.put("acelerometro",jsonObjectAccelerometer);
        jsonObjectTransmissao.put("horimetro",horimetro);
        jsonObjectTransmissao.put("temperatura",jsonObjectTemperatura);
        jsonObjectTransmissao.put("velocidade_pcb",jsonObjectSpeedPCB);
        jsonObjectTransmissao.put("tempo_maquina_movimento",tempoMaquinaMovimento);
        jsonObjectTransmissao.put("id_operador",operadorID);
        jsonObjectTransmissao.put("luz_marcha_re",luzMarchaRe);
        jsonObjectTransmissao.put("valor_ad_ref_1v",valorRefAD1v);
        jsonObjectTransmissao.put("pulsos_em_200ms",pulsosEm200ms);
        jsonObjectTransmissao.put("status_rele",jsonObjectRele);
        jsonObjectTransmissao.put("bateria",jsonObjectBateria);
        jsonObjectTransmissao.put("abastecimento",jsonObjectAbastecimento);
        jsonObjectTransmissao.put("hodometro",jsonObjectHodometro);
        jsonObjectTransmissao.put("hora_efetiva", horaEfetivaPercentual);

        //Array tabela transmissão
        jsonArrayTransmissao.put(jsonObjectTransmissao);

        //tabela on off pos chave
        JSONObject jsonObjectOnOff = new JSONObject();
        jsonObjectOnOff.put("estado",posChave);
        jsonObjectOnOff.put("timestamp",tsLong);

        //Array tabela on off
        jsonArrayOnOff.put(jsonObjectOnOff);

        //tabela data usage
        JSONObject jsonObjectDataUsage = new JSONObject();
        jsonObjectDataUsage.put("data_usage_tx",dataUsageTX);
        jsonObjectDataUsage.put("data_usage_rx",dataUsageRX);
        jsonObjectDataUsage.put("firebase_user",fireBaseUserId);

        //Array tabela data usage
        jsonArrayDataUsage.put(jsonObjectDataUsage);

        //tabela cinto on off
        JSONObject jsonObjectCintoOnOff = new JSONObject();
        jsonObjectCintoOnOff.put("id_operador",operadorID);
        if(sensorCintoSegurança==null || sensorCintoSegurança.equals(""))sensorCintoSegurança="N";
        jsonObjectCintoOnOff.put("estado",sensorCintoSegurança);
        jsonObjectCintoOnOff.put("timestamp",tsLong);

        //Array tabela Cinto on off
        jsonArrayCintoOnOff.put(jsonObjectCintoOnOff);

        //tabela Ajuste de horimetro
        JSONObject jsonObjectAjusteHorimetro = new JSONObject();
        jsonObjectAjusteHorimetro.put("ajuste_horimetro", "true");
        jsonObjectAjusteHorimetro.put("timestamp", timeStampAdjHorimetro);


        //Array tabela Ajuste de horimetro
        jsonArrayAdjHorimetro.put(jsonObjectAjusteHorimetro);

        //tabela Manutenção
        JSONObject jsonObjectManutenção = new JSONObject();
        jsonObjectManutenção.put("timestamp_manut_aberta", timeStampManutençãoAberta);
        jsonObjectManutenção.put("timestamp_manut_executada", timeStampManutençãoExecutada);
        jsonObjectManutenção.put("em_manut",emManutenção);
        jsonObjectManutenção.put("status_manut",statusDeManutenção);

        //Array tabela manutenção
        jsonArrayManutenção.put(jsonObjectManutenção);

        //tabela check list
        JSONObject jsonObjectCheckList = new JSONObject();
        if(eventoChecklist){
            eventoChecklist=false;
            jsonObjectCheckList.put("versao",0);
            //obtem lista de check list salvo na activity CheckList
            jsonObjectCheckList.put("criticos", jsonObjectCheckListEnvioCritico);
            jsonObjectCheckList.put("geral",jsonObjectCheckListEnvioGeral);
        }else{
            jsonObjectCheckList.put("versao",-1);
            //jsonObjectCheckList.put("criticos", jsonObjectCheckListEnvioCritico);
            //jsonObjectCheckList.put("geral",jsonObjectCheckListEnvioGeral);
        }



        //Array tabela Check List
        jsonArrayCheckList.put(jsonObjectCheckList);

        //Objeto JSON final
        jsonObjectInjetec.put("id_maquina",maquinaID);
        jsonObjectInjetec.put("tb_operador",tbOperadorArray);
        jsonObjectInjetec.put("tb_transmissao",jsonArrayTransmissao);
        jsonObjectInjetec.put("tb_on_off",jsonArrayOnOff);
        jsonObjectInjetec.put("tb_cinto_on_off",jsonArrayCintoOnOff);
        jsonObjectInjetec.put("tb_checklist",jsonArrayCheckList);
        jsonObjectInjetec.put("tb_data_usage",jsonArrayDataUsage);
        jsonObjectInjetec.put("tb_manutencao",jsonArrayManutenção);
        //verifica se ajuste de horimetro ocorreu
        if(ajusteHorimetroFeito){
            jsonObjectInjetec.put("tb_ajuste_horimetro",jsonObjectAjusteHorimetro);
        }else{
            //apaga tabela de ajuste de horimetro
            if(!jsonObjectInjetec.isNull("tb_ajuste_horimetro")){
                jsonObjectInjetec.remove("tb_ajuste_horimetro");
            }
        }


        //deleta o registro mais antigo e da lugar a um novo registro
        int jsonLenght = jsonArrayCintoOnOff.length();
        int NUMERO_MAX_ARMAZENAMENTO = 120;
        if(jsonLenght > NUMERO_MAX_ARMAZENAMENTO){
            for(int i=0;i<jsonObjectInjetec.length();i++){
                if(i==jsonLenght)
                    tbOperadorArray.remove(0);
                    jsonArrayTransmissao.remove(0);
                    jsonArrayOnOff.remove(0);
                    jsonArrayCintoOnOff.remove(0);
                    jsonArrayCheckList.remove(0);
                    jsonArrayDataUsage.remove(0);
                    jsonArrayManutenção.remove(0);
            }
        }

        AsyncTask asyncT = new AsyncTask();
        asyncT.execute();

    }

    private static class  AsyncTask extends android.os.AsyncTask<Void, Void, Void> {
        //        private WeakReference<HttpConnection> httpConnectionClass;
//        public AsyncTask(HttpConnection httpConnectionClass) {
//            this.httpConnectionClass = new WeakReference<>(httpConnectionClass);
//        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... params) {

            try {
                //URL url = new URL("http://www.monitor.injetec.com.br/servico/transmissao");
                URL url = new URL("http://www.smartlogger.com.br/servico/transmissao");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Senha", senhaHttp);
                conn.setRequestProperty("Id", String.valueOf(maquinaID));

                //TODO tentativa de coorreção do erro ECONNRESET
                //conn.setRequestProperty("connection", "close");
                //System.setProperty("http.keepAlive", "false")

                try {
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(jsonObjectInjetec.toString());
                    wr.flush();
                } catch (SSLException e) {
                    //Log.i("SSL_WR: ", e.toString());
                }
                //Seta evento de transmissão com sucesso
                MainActivity.httpTxEvent = true;

                //apaga jsonArray transmitido
                int jsonLenght = jsonArrayCintoOnOff.length();
                for (int i = 0; i < jsonLenght; i++) {
                    tbOperadorArray.remove(i);
                    jsonArrayTransmissao.remove(i);
                    jsonArrayOnOff.remove(i);
                    jsonArrayCintoOnOff.remove(i);
                    jsonArrayCheckList.remove(i);
                    jsonArrayDataUsage.remove(i);
                    jsonArrayManutenção.remove(i);

                }

                //Recebe Json resposta do servidor
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    BufferedReader reader;
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String line;
//                    line = reader.readLine();
//                    while (line != null  ) {
//                        stringBuilder.append(line);
//                    }
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        //Log.i("HTTP_Reading ", line);
                    }
                } catch (SSLException e) {
                    //Log.i("SSL_RD: ", e.toString());
                }
                //serverRX = true;

                JSONObject jsonObjectRespostaServidor = new JSONObject(stringBuilder.toString());
                JSONObject jObjectChecklist = jsonObjectRespostaServidor.getJSONObject("checklist");
                JSONObject jObjectConfig = jsonObjectRespostaServidor.getJSONObject("config");

                if (jObjectChecklist.getBoolean("novo")) {
                    JSONObject jsonItens = jObjectChecklist.getJSONObject("itens");
                    JSONArray jsonCriticosRecebidoServidor = jsonItens.getJSONArray("criticos");
                    JSONArray jsonGeralRecebidoServidor = jsonItens.getJSONArray("geral");

                    if (jsonCriticosRecebidoServidor != null) {
                        for (int i = 0; i < jsonCriticosRecebidoServidor.length(); i++) {
                            //adiciona caracter no final da string para sinalizar item crítico
                            CheckListData checkListData = new CheckListData();
                            checkListData.setCheckListItem(jsonCriticosRecebidoServidor.getString(i) + IMPORTTANT_CHAR);
                        }
                        //Salva Json check list críticos no preferences
                        //myPreferences.setCheckListCriticosAtual(jsonCriticosRecebidoServidor.toString());
                        MainActivity.jsonArrayCriticos =jsonCriticosRecebidoServidor.toString();
                    }

                    if (jsonGeralRecebidoServidor != null) {
                        for (int i = 0; i < jsonGeralRecebidoServidor.length(); i++) {
                            CheckListData checkListData = new CheckListData();
                            checkListData.setCheckListItem(jsonGeralRecebidoServidor.getString(i));
                        }
                        //Salva Json check list geral no preferences
                        //myPreferences.setCheckListGeralAtual(jsonGeralRecebidoServidor.toString());
                        MainActivity.jsonArrayGeral = jsonGeralRecebidoServidor.toString();
                    }


                }
                //Json Config
                int timeStamp = (int) (new Date().getTime() / 1000);
                int proximoCheckList = jObjectConfig.getInt("prox_checklist");
                int versaoCheckList = jObjectConfig.getInt("versao_checklist");
//                int vencimentoExameMedico = jObjectConfig.getInt("exame_medico");
//                int vencimentoExameTreinamento = jObjectConfig.getInt("exame_treinamento");
                if (timeStamp >= proximoCheckList) {
                    //Log.d("NOVO CHECKLIST = FALSE","");
                } else {
                    //Log.d("NOVO CHECKLIST = TRUE","");
                }


                //wr.close();
                conn.disconnect();

            } catch (ProtocolException e) {
                e.printStackTrace();
                //Log.i("HTTP_Err ", "PROTOCOL EXCEPTION " + e);
            } catch (IOException e) {
                e.printStackTrace();
                //Log.i("HTTP_Err ", "IO EXCEPTION " + e);
            } catch (JSONException e) {
                e.printStackTrace();
                //Log.i("HTTP_Err ", "JSON EXCEPTION " + e);
            } finally {

                return null;
            }

        }

   }

//    private static void setCheckListCriticos(JSONArray jsonArray) {
//        myPreferences.setCheckListCriticosAtual(jsonArray.toString());
//
//    }

}
