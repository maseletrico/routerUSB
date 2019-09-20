package com.marco.smartrouterdev;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

//import android.util.Log;
//import org.json.JSONObject;
//
//import java.lang.reflect.Array;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//import static android.R.attr.type;
//import static android.R.id.list;
//import static android.media.CamcorderProfile.get;

public class MyPreferences  {

    private Context context;

    public void setContext(Context context) {
        this.context = context;

    }

    int getOperdadorDoUltimoCheckList() {
        SharedPreferences prefs = context.getSharedPreferences("operdadorDoUltimoCheckList", MODE_PRIVATE);
        return prefs.getInt("operdador_Do_Ultimo_Check_List", -1); // will return -1 if no  value is saved
    }
    public void setOperdadorDoUltimoCheckList(int mOperador) {
        SharedPreferences.Editor editor = context.getSharedPreferences("operdadorDoUltimoCheckList", MODE_PRIVATE).edit();
        editor.putInt("operdador_Do_Ultimo_Check_List", mOperador);
        editor.apply();
    }
    long getUltimoCheckListFeito() {
        SharedPreferences prefs = context.getSharedPreferences("ultimoCheckListFeito", MODE_PRIVATE);
        return prefs.getLong("ultimo_Check_List_Feito", -1); // will return -1 if no  value is saved
    }

    public void setUltimoCheckListFeito(long mUltimoCheckList) {
        SharedPreferences.Editor editor = context.getSharedPreferences("ultimoCheckListFeito", MODE_PRIVATE).edit();
        editor.putLong("ultimo_Check_List_Feito", mUltimoCheckList);
        editor.apply();
    }
    int getIntervaloCheckList() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloCheckList", MODE_PRIVATE);
        return prefs.getInt("intervalo_CheckList", -1); // will return -1 if no  value is saved
    }

    void setIntervaloCheckList(int mIntervalo) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloCheckList", MODE_PRIVATE).edit();
        editor.putInt("intervalo_CheckList", mIntervalo);
        editor.apply();
    }
    String getCheckListTrocaOperador() {
        SharedPreferences prefs = context.getSharedPreferences("checkListTrocaOperador", MODE_PRIVATE);
        return prefs.getString("check_List_Troca_Operador", "sim");
    }

    void setCheckListTrocaOperador(String chkLst) {
        SharedPreferences.Editor editor = context.getSharedPreferences("checkListTrocaOperador", MODE_PRIVATE).edit();
        editor.putString("check_List_Troca_Operador", chkLst);
        editor.apply();
    }

    String getGotoMessage() {
        SharedPreferences prefs = context.getSharedPreferences("gotomessage", MODE_PRIVATE);
        return prefs.getString("goto_message", "sim");
    }

    void setGotoMessage(String chkLst) {
        SharedPreferences.Editor editor = context.getSharedPreferences("gotomessage", MODE_PRIVATE).edit();
        editor.putString("goto_message", chkLst);
        editor.apply();
    }

    String getHoraAbastecimento() {
        SharedPreferences prefs = context.getSharedPreferences("horaAbastecimento", MODE_PRIVATE);
        return prefs.getString("hora_abastecimento", "hora");
    }

    void setHoraAbastecimento(String horaAbast) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horaAbastecimento", MODE_PRIVATE).edit();
        editor.putString("hora_abastecimento", horaAbast);
        editor.apply();
    }
    String getDataAbastecimento() {
        SharedPreferences prefs = context.getSharedPreferences("dataAbastecimento", MODE_PRIVATE);
        return prefs.getString("data_abastecimento", "data");
    }

    void setDataAbastecimento(String dataAbast) {
        SharedPreferences.Editor editor = context.getSharedPreferences("dataAbastecimento", MODE_PRIVATE).edit();
        editor.putString("data_abastecimento", dataAbast);
        editor.apply();
    }
    float getQuantidadeCombAbastecido() {
        SharedPreferences prefs = context.getSharedPreferences("combustivelAbastecido", MODE_PRIVATE);
        return prefs.getFloat("combustivel_Abastecido", 0.001f); // will return 1 if no  value is saved
    }

    void setQuantidadeCombAbastecido(float combAbastecido) {
        SharedPreferences.Editor editor = context.getSharedPreferences("combustivelAbastecido", MODE_PRIVATE).edit();
        editor.putFloat("combustivel_Abastecido", combAbastecido);
        editor.apply();

    }
    public String getCheckListCriticosAtual() {
        SharedPreferences prefs = context.getSharedPreferences("checkListCriticos", MODE_PRIVATE);
        return prefs.getString("check_List_Criticos", "nothing");
    }

    void setCheckListCriticosAtual(String checkList) {
        //String checkListCriticosAtual = checkList;
        SharedPreferences.Editor editor = context.getSharedPreferences("checkListCriticos", MODE_PRIVATE).edit();
        editor.putString("check_List_Criticos", checkList);
        editor.apply();
    }
    public String getCheckListGearlAtual() {
        SharedPreferences prefs = context.getSharedPreferences("checkListGeral", MODE_PRIVATE);
        return prefs.getString("check_List_Geral", "nothing");
    }

    void setCheckListGeralAtual(String checkList) {
        //String checkListGeralAtual = checkList;
        SharedPreferences.Editor editor = context.getSharedPreferences("checkListGeral", MODE_PRIVATE).edit();
        editor.putString("check_List_Geral", checkList);
        editor.apply();
    }
    String getNomeMaquina() {
        SharedPreferences prefs = context.getSharedPreferences("nomeDaMaquina", MODE_PRIVATE);
        return prefs.getString("nome_Da_Maquina", "Máquina?");
    }

    void setNomeMaquina(String nomeMaquina) {
        SharedPreferences.Editor editor = context.getSharedPreferences("nomeDaMaquina", MODE_PRIVATE).edit();
        editor.putString("nome_Da_Maquina", nomeMaquina);
        editor.apply();
    }

    String getNomeEmpresa() {
        SharedPreferences prefs = context.getSharedPreferences("nomeDaEmpresa", MODE_PRIVATE);
        return prefs.getString("nome_Da_Empresa", "nothing");
    }

    void setNomeEmpresa(String nomeEmpresa) {
        SharedPreferences.Editor editor = context.getSharedPreferences("nomeDaEmpresa", MODE_PRIVATE).edit();
        editor.putString("nome_Da_Empresa", nomeEmpresa);
        editor.apply();
    }

    String getSenhaHttp() {
        SharedPreferences prefs = context.getSharedPreferences("senhaHttpQrcode", MODE_PRIVATE);
        return prefs.getString("senha_Http_QrCode", "nothing");
    }

    void setSenhaHttp(String mySenhaHttp) {
        SharedPreferences.Editor editor = context.getSharedPreferences("senhaHttpQrcode", MODE_PRIVATE).edit();
        editor.putString("senha_Http_QrCode", mySenhaHttp);
        editor.apply();
    }


    //String de comunicação
//    public String getJsonTx() {
//        SharedPreferences prefs = context.getSharedPreferences("jsonTx", MODE_PRIVATE);
//        return prefs.getString("json_tx", "nothing");
//    }
//
//    public void setJsonTx(String jsonTx) {
//        SharedPreferences.Editor editor = context.getSharedPreferences("jsonTx", MODE_PRIVATE).edit();
//        editor.putString("json_tx", jsonTx);
//        editor.apply();
//    }

    boolean isFB_CREATED() {
        SharedPreferences prefs = context.getSharedPreferences("firebase_created", MODE_PRIVATE);
        return prefs.getBoolean("firebaseCreated",false); // will return 0 if no  value is saved

    }

    void setFB_CREATED(boolean fbCreated) {
        SharedPreferences.Editor editor = context.getSharedPreferences("firebase_created", MODE_PRIVATE).edit();
        editor.putBoolean("firebaseCreated", fbCreated);//
        editor.apply();
    }

    float getHodometroParcial(){
        SharedPreferences prefs = context.getSharedPreferences("hodometroParcial", MODE_PRIVATE);
        return prefs.getFloat("hodometro_parcial", 1); // will return 1 if no  value is saved
    }

    void setHodometroParcial(Float hodometroParcial) {
        SharedPreferences.Editor editor = context.getSharedPreferences("hodometroParcial", MODE_PRIVATE).edit();
        editor.putFloat("hodometro_parcial", hodometroParcial);
        editor.apply();

    }
    float getHodometro(){
        SharedPreferences prefs = context.getSharedPreferences("hodometroMaquina", MODE_PRIVATE);
        return prefs.getFloat("hodometro_maquina", 1); // will return 1 if no  value is saved

    }

    void setHodometro(Float hodometro) {
        SharedPreferences.Editor editor = context.getSharedPreferences("hodometroMaquina", MODE_PRIVATE).edit();
        editor.putFloat("hodometro_maquina", hodometro);
        editor.apply();

    }
    boolean isAccessPoint() {
        SharedPreferences prefs = context.getSharedPreferences("access_point", MODE_PRIVATE);
        return prefs.getBoolean("accesspoint",true);
    }

    void setAccessPoint(boolean accessPoint) {
        SharedPreferences.Editor editor = context.getSharedPreferences("access_point", MODE_PRIVATE).edit();
        editor.putBoolean("accesspoint", accessPoint);//
        editor.apply();
    }

    boolean isALERTA_MANUTENÇÃO() {
        SharedPreferences prefs = context.getSharedPreferences("alerta_manutenção", MODE_PRIVATE);
        return prefs.getBoolean("alertaManutenção",false); // will return false if no  value is saved
    }

    void setALERTA_MANUTENÇÃO(boolean ALERTA_MANUTENÇÃO) {
        SharedPreferences.Editor editor = context.getSharedPreferences("alerta_manutenção", MODE_PRIVATE).edit();
        editor.putBoolean("alertaManutenção", ALERTA_MANUTENÇÃO);//
        editor.apply();
    }

    boolean isAcionaReleImpacto() {
        SharedPreferences prefs = context.getSharedPreferences("aciona_rele_impacto", MODE_PRIVATE);
        return prefs.getBoolean("acionareleimpacto",false); // will return 1 if no  value is saved
    }

    void setAcionaReleImpacto(boolean aciona_rele_impacto) {
        SharedPreferences.Editor editor = context.getSharedPreferences("aciona_rele_impacto", MODE_PRIVATE).edit();
        editor.putBoolean("acionareleimpacto", aciona_rele_impacto);//
        editor.apply();
    }

    boolean isVelocidadeGPS() {
        SharedPreferences prefs = context.getSharedPreferences("velocidade_gps", MODE_PRIVATE);
        return prefs.getBoolean("velocidadegps",true); // will return 1 if no  value is saved
    }

    void setVelocidadeGPS(boolean velGPS) {
        SharedPreferences.Editor editor = context.getSharedPreferences("velocidade_gps", MODE_PRIVATE).edit();
        editor.putBoolean("velocidadegps", velGPS);//
        editor.apply();
    }

    float getHoraEfetiva(){
        SharedPreferences prefs = context.getSharedPreferences("horaefetiva", MODE_PRIVATE);
        return prefs.getFloat("hora_efetiva", 1); // will return 1 if no  value is saved

    }

    void setHoraEfetiva(Float horaEfetiva) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horaefetiva", MODE_PRIVATE).edit();
        editor.putFloat("hora_efetiva", horaEfetiva);
        editor.apply();

    }

    float getHorimetroAz(){
        SharedPreferences prefs = context.getSharedPreferences("horimetroAz", MODE_PRIVATE);
        return prefs.getFloat("horimetro_Az", 1); // will return 1 if no  value is saved

    }

    void setHorimetroAz(Float horimetroAz) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horimetroAz", MODE_PRIVATE).edit();
        editor.putFloat("horimetro_Az", horimetroAz);
        editor.apply();

    }


    float getHorimetro(){
        SharedPreferences prefs = context.getSharedPreferences("horimetro_da_maquina", MODE_PRIVATE);
        return prefs.getFloat("horimetro_maquina", 1); // will return 1 if no  value is saved

    }

    void setHorimetro(Float horimetro) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horimetro_da_maquina", MODE_PRIVATE).edit();
        editor.putFloat("horimetro_maquina", horimetro);
        editor.apply();

    }

    float getHorimetroParcial(){
        SharedPreferences prefs = context.getSharedPreferences("horimetro_parcial_da_maquina", MODE_PRIVATE);
        return prefs.getFloat("horimetro_parcial_maquina", 1); // will return 1 if no  value is saved
    }

    void setHorimetroParcial(Float horimetro) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horimetro_parcial_da_maquina", MODE_PRIVATE).edit();
        editor.putFloat("horimetro_parcial_maquina", horimetro);
        editor.apply();

    }

    float getTimeStampUltimo(){
        SharedPreferences prefs = context.getSharedPreferences("ultimo_horimetro", MODE_PRIVATE);
        return prefs.getFloat("ultimo_horimetro_salvo", 1); // will return 1 if no  value is saved

    }

    void setTimeStumpUltimo(Float horimetroTimestamp) {
        SharedPreferences.Editor editor = context.getSharedPreferences("ultimo_horimetro", MODE_PRIVATE).edit();
        editor.putFloat("ultimo_horimetro_salvo", horimetroTimestamp);
        editor.apply();

    }


    float getDiametroDoPneu() {
        SharedPreferences prefs = context.getSharedPreferences("diametro_do_pneu", MODE_PRIVATE);
        return prefs.getFloat("diametro_pneu", 1.0f); // will return 1 if no  value is saved
    }

    void setDiametroDoPneu(Float diametroDoPneu) {
        SharedPreferences.Editor editor = context.getSharedPreferences("diametro_do_pneu", MODE_PRIVATE).edit();
        editor.putFloat("diametro_pneu", diametroDoPneu);
        editor.apply();

    }

    int getCountHorimetro6min() {
        SharedPreferences prefs = context.getSharedPreferences("contadorHorimetro6m", MODE_PRIVATE);
        return prefs.getInt("contador_Horimetro6m", 1); // will return 1 if no  value is saved
    }

    void setCountHorimetro6min(int countHorimetro6min) {
        SharedPreferences.Editor editor = context.getSharedPreferences("contadorHorimetro6m", MODE_PRIVATE).edit();
        editor.putInt("contador_Horimetro6m", countHorimetro6min);
        editor.apply();
    }

    int getCountMaqMovimento() {
        SharedPreferences prefs = context.getSharedPreferences("contadorMaqMovimento", MODE_PRIVATE);
        return prefs.getInt("contador_Maq_Movimento", 1); // will return 1 if no  value is saved
    }

    void setCountMaqMovimento(int countMaqMovimento) {
        SharedPreferences.Editor editor = context.getSharedPreferences("contadorMaqMovimento", MODE_PRIVATE).edit();
        editor.putInt("contador_Maq_Movimento", countMaqMovimento);
        editor.apply();
    }


    int getNumDePulsosPorVolta() {
        SharedPreferences prefs = context.getSharedPreferences("numero_pulsos_por_volta", MODE_PRIVATE);
        return prefs.getInt("pulsosPorVolta", 1); // will return 1 if no  value is saved
    }

    void setNumDePulsosPorVolta(Integer numDePulsosPorVolta) {
        SharedPreferences.Editor editor = context.getSharedPreferences("numero_pulsos_por_volta", MODE_PRIVATE).edit();
        editor.putInt("pulsosPorVolta", numDePulsosPorVolta);
        editor.apply();

    }

    boolean isID_OPERADOR() {
        SharedPreferences prefs = context.getSharedPreferences("identificador_operador", MODE_PRIVATE);
        return prefs.getBoolean("idop", true); // will return 1 if no  value is saved
    }

    void setID_OPERADOR(boolean ID_OPERADOR) {
        SharedPreferences.Editor editor = context.getSharedPreferences("identificador_operador", MODE_PRIVATE).edit();
        editor.putBoolean("idop", ID_OPERADOR);// 1 = usar tela de identicicação do operador
        editor.apply();
    }

    int getAlarmeTemperatura() {
        SharedPreferences prefs = context.getSharedPreferences("alarme_Temperatura", MODE_PRIVATE);
        return prefs.getInt("alarmeTemperatura", 1); // will return 1 if no  value is saved
    }

    void setAlarmeTemperatura(Integer setPointTemperatura) {
        SharedPreferences.Editor editor = context.getSharedPreferences("alarme_Temperatura", MODE_PRIVATE).edit();
        editor.putInt("alarmeTemperatura", setPointTemperatura);
        editor.apply();

    }

    float getSetPointAcelerometro() {
        SharedPreferences prefs = context.getSharedPreferences("setPoint_Acell", MODE_PRIVATE);
        return prefs.getFloat("setPointAcell", 1); // will return 1 if no  value is saved
    }

    void setSetPointAcelerometro(float setPointAcelerometro) {
        SharedPreferences.Editor editor = context.getSharedPreferences("setPoint_Acell", MODE_PRIVATE).edit();
        editor.putFloat("setPointAcell", setPointAcelerometro);
        editor.apply();

    }
    int getMaquinaId() {
        SharedPreferences prefs = context.getSharedPreferences("maquinaIdDatabase", MODE_PRIVATE);
        return prefs.getInt("maquina_Id_Database", -1); // will return -1 if no  value is saved
    }

    void setMaquinaId(int maquinaIdDb) {
        SharedPreferences.Editor editor = context.getSharedPreferences("maquinaIdDatabase", MODE_PRIVATE).edit();
        editor.putInt("maquina_Id_Database", maquinaIdDb);
        editor.apply();
    }

    int getVelMaxGauge() {
        SharedPreferences prefs = context.getSharedPreferences("velMaxGauge", MODE_PRIVATE);
        return prefs.getInt("vel_Max_Gauge", -1); // will return -1 if no  value is saved
    }

    void setVelMaxGauge(int myVelMaxGauge) {
        SharedPreferences.Editor editor = context.getSharedPreferences("velMaxGauge", MODE_PRIVATE).edit();
        editor.putInt("vel_Max_Gauge", myVelMaxGauge);
        editor.apply();
    }

    int getIntervaloTx() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloTX", MODE_PRIVATE);
        return prefs.getInt("intervalo_TX", 120000); // will return -1 if no  value is saved
    }

    void setIntervaloTx(int intervalo_tx) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloTX", MODE_PRIVATE).edit();
        editor.putInt("intervalo_TX", intervalo_tx);
        editor.apply();
    }

    int getOperadorTimeOut() {
        SharedPreferences prefs = context.getSharedPreferences("operadorTimeOut", MODE_PRIVATE);
        return prefs.getInt("operador_timeOut", 30); // will return -1 if no  value is saved
    }

    void setOperadorTimeOut(int operador_timeout) {
        SharedPreferences.Editor editor = context.getSharedPreferences("operadorTimeOut", MODE_PRIVATE).edit();
        editor.putInt("operador_timeOut", operador_timeout);
        editor.apply();
    }

    String getDataTxServer() {
        SharedPreferences prefs = context.getSharedPreferences("dataTransmissao", MODE_PRIVATE);
        return prefs.getString("data_Transmissao", "data");
    }

    void setDataTxServer(String dataTxServer) {
        SharedPreferences.Editor editor = context.getSharedPreferences("dataTransmissao", MODE_PRIVATE).edit();
        editor.putString("data_Transmissao", dataTxServer);
        editor.apply();
    }

    String getHoraTxServer() {
        SharedPreferences prefs = context.getSharedPreferences("horaTransmissao", MODE_PRIVATE);
        return prefs.getString("hora_Transmissao", "data");

    }

    void setHoraTxServer(String horaTxServer) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horaTransmissao", MODE_PRIVATE).edit();
        editor.putString("hora_Transmissao", horaTxServer);
        editor.apply();
    }

    int getIntervaloTxDesligado() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloTxDesligado", MODE_PRIVATE);
        return prefs.getInt("intervalo_Tx_Desligado", 3600000); // will return 3600000 if no  value is saved
    }

    void setIntervaloTxDesligado(int intervaloTxDesligado) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloTxDesligado", MODE_PRIVATE).edit();
        editor.putInt("intervalo_Tx_Desligado", intervaloTxDesligado);
        editor.apply();
    }

    int getTimeoutPoschave() {
        SharedPreferences prefs = context.getSharedPreferences("timeoutPoschave", MODE_PRIVATE);
        return prefs.getInt("timeout_Poschave", 3600000); // will return 3600000 if no  value is saved
    }

    void setTimeoutPoschave(int mTimeoutPoschave) {
        SharedPreferences.Editor editor = context.getSharedPreferences("timeoutPoschave", MODE_PRIVATE).edit();
        editor.putInt("timeout_Poschave", mTimeoutPoschave);
        editor.apply();
    }


    String getLastLatitude() {
        SharedPreferences prefs = context.getSharedPreferences("gpsLastLatitude", MODE_PRIVATE);
        return prefs.getString("gps_LastLatitude", "0.0");
    }

    void setLastLatitude(String lastLatitude) {
        SharedPreferences.Editor editor = context.getSharedPreferences("gpsLastLatitude", MODE_PRIVATE).edit();
        editor.putString("gps_LastLatitude", lastLatitude);
        editor.apply();
    }

    String getLastLongitude() {
        SharedPreferences prefs = context.getSharedPreferences("gpsLastLongitude", MODE_PRIVATE);
        return prefs.getString("gps_LastLongitude", "0.0");
    }

    void setLastLongitude(String lastLongitude) {
        SharedPreferences.Editor editor = context.getSharedPreferences("gpsLastLongitude", MODE_PRIVATE).edit();
        editor.putString("gps_LastLongitude", lastLongitude);
        editor.apply();
    }

//    String getDataExameMedico() {
//        SharedPreferences prefs = context.getSharedPreferences("dataExameMedico", MODE_PRIVATE);
//        return prefs.getString("data_ExameMedico", "data");
//    }
//
//    public void setDataExameMedico(String dataEameMedico) {
//        SharedPreferences.Editor editor = context.getSharedPreferences("dataExameMedico", MODE_PRIVATE).edit();
//        editor.putString("data_ExameMedico", dataEameMedico);
//        editor.apply();
//    }
//
//    public String getDataTreinamento() {
//        SharedPreferences prefs = context.getSharedPreferences("dataTreinamento", MODE_PRIVATE);
//        return prefs.getString("data_Treinamento", "data");
//    }
//
//    public void setDataTreinamento(String dataTreinamento) {
//        SharedPreferences.Editor editor = context.getSharedPreferences("dataTreinamento", MODE_PRIVATE).edit();
//        editor.putString("data_Treinamento", dataTreinamento);
//        editor.apply();
//    }

    String getDataParadaManutenção() {
        SharedPreferences prefs = context.getSharedPreferences("dataParadaManutenção", MODE_PRIVATE);
        return prefs.getString("data_ParadaManutenção", "data");
    }

    void setDataParadaManutenção(String dataParadaManutenção) {
        SharedPreferences.Editor editor = context.getSharedPreferences("dataParadaManutenção", MODE_PRIVATE).edit();
        editor.putString("data_ParadaManutenção", dataParadaManutenção);
        editor.apply();
    }

    String getHoraParadaManutenção() {
        SharedPreferences prefs = context.getSharedPreferences("horaParadaManutenção", MODE_PRIVATE);
        return prefs.getString("hora_ParadaManutenção", "data");
    }

    void setHoraParadaManutenção(String horaParadaManutenção) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horaParadaManutenção", MODE_PRIVATE).edit();
        editor.putString("hora_ParadaManutenção", horaParadaManutenção);
        editor.apply();
    }

    String getDataManutençãoExecutada() {
        SharedPreferences prefs = context.getSharedPreferences("dataManutençãoExecutada", MODE_PRIVATE);
        return prefs.getString("data_Manutenção_Executada", "data");
    }

    void setDataManutençãoExecutada(String dataManutençãoExecutada) {
        SharedPreferences.Editor editor = context.getSharedPreferences("dataManutençãoExecutada", MODE_PRIVATE).edit();
        editor.putString("data_Manutenção_Executada", dataManutençãoExecutada);
        editor.apply();
    }

    String getHoraManutençãoExecutada() {
        SharedPreferences prefs = context.getSharedPreferences("horaManutençãoExecutada", MODE_PRIVATE);
        return prefs.getString("hora_Manutenção_Exxecutada", "data");
    }

    void setHoraManutençãoExecutada(String horaManutençãoExecutada) {
        SharedPreferences.Editor editor = context.getSharedPreferences("horaManutençãoExecutada", MODE_PRIVATE).edit();
        editor.putString("hora_Manutenção_Exxecutada", horaManutençãoExecutada);
        editor.apply();
    }

    long getTimeStampManutençãoAberta() {
        SharedPreferences prefs = context.getSharedPreferences("timeStampManutençãoAberta", MODE_PRIVATE);
        return prefs.getLong("timeStamp_Manutenção_Aberta", -1);
    }

    void setTimeStampManutençãoAberta(long timeStampManutençãoAberta) {
        SharedPreferences.Editor editor = context.getSharedPreferences("timeStampManutençãoAberta", MODE_PRIVATE).edit();
        editor.putLong("timeStamp_Manutenção_Aberta", timeStampManutençãoAberta);
        editor.apply();
    }

    long getTimeStampManutençãoExecutada() {
        SharedPreferences prefs = context.getSharedPreferences("timeStampManutençãoExecutada", MODE_PRIVATE);
        return prefs.getLong("timeStamp_Manutenção_Executada", -1);
    }

    void setTimeStampManutençãoExecutada(long timeStampManutençãoExecutada) {
        SharedPreferences.Editor editor = context.getSharedPreferences("timeStampManutençãoExecutada", MODE_PRIVATE).edit();
        editor.putLong("timeStamp_Manutenção_Executada", timeStampManutençãoExecutada);
        editor.apply();
    }

    long getTimeStampAbastecimento() {
        SharedPreferences prefs = context.getSharedPreferences("timeStampAbastecimento", MODE_PRIVATE);
        return prefs.getLong("timeStamp_Abastecimento", -1);
    }

    void setTimeStampAbastecimento(long timeStampAbastecimento) {
        SharedPreferences.Editor editor = context.getSharedPreferences("timeStampAbastecimento", MODE_PRIVATE).edit();
        editor.putLong("timeStamp_Abastecimento", timeStampAbastecimento);
        editor.apply();
    }

    int getGpsMinTime() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloGpsMinTime",MODE_PRIVATE);
        return prefs.getInt("intervalo_Gps_Min_Time",0);
    }

    void setGpsMinTime(int gpsMinTime) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloGpsMinTime", MODE_PRIVATE).edit();
        editor.putInt("intervalo_Gps_Min_Time", gpsMinTime);
        editor.apply();
    }

    int getGpsMinDistance() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloGpsMinDistance",MODE_PRIVATE);
        return prefs.getInt("intervalo_Gps_Min_Distance",0);
    }

    void setGpsMinDistance(int gpsMinDistance) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloGpsMinDistance", MODE_PRIVATE).edit();
        editor.putInt("intervalo_Gps_Min_Distance", gpsMinDistance);
        editor.apply();
    }

    int getDelayHoraEfetiva() {
        SharedPreferences prefs = context.getSharedPreferences("delayhoraefetiva",MODE_PRIVATE);
        return prefs.getInt("delay_hora_efetiva",15);
    }

    void setDelayHoraEfetiva(int delayHoraEfetiva) {
        SharedPreferences.Editor editor = context.getSharedPreferences("delayhoraefetiva", MODE_PRIVATE).edit();
        editor.putInt("delay_hora_efetiva", delayHoraEfetiva);
        editor.apply();
    }

    boolean isMyPermissions() {
        SharedPreferences prefs = context.getSharedPreferences("my_permissions", MODE_PRIVATE);
        return prefs.getBoolean("mypermissions",false);
    }

    void setMyPermisions(boolean myPermissions) {
        SharedPreferences.Editor editor = context.getSharedPreferences("my_permissions", MODE_PRIVATE).edit();
        editor.putBoolean("mypermissions", myPermissions);//
        editor.apply();
    }

    boolean isCintoAlarme() {
        SharedPreferences prefs = context.getSharedPreferences("cinto_alarme", MODE_PRIVATE);
        return prefs.getBoolean("cintoalarme",false);
    }

    void setCintoAlarme(boolean myCintoAlarme) {
        SharedPreferences.Editor editor = context.getSharedPreferences("cinto_alarme", MODE_PRIVATE).edit();
        editor.putBoolean("cintoalarme", myCintoAlarme);//
        editor.apply();
    }

    boolean isLimitadorVel() {
        SharedPreferences prefs = context.getSharedPreferences("limitador_vel", MODE_PRIVATE);
        return prefs.getBoolean("limitadorVel",false);
    }

    void setLimitadorVel(boolean myLimitadorVel) {
        SharedPreferences.Editor editor = context.getSharedPreferences("limitador_vel", MODE_PRIVATE).edit();
        editor.putBoolean("limitadorVel", myLimitadorVel);//
        editor.apply();
    }
    String getStatusManutenção() {
        SharedPreferences prefs = context.getSharedPreferences("statusManutenção", MODE_PRIVATE);
        return prefs.getString("status_Manutenção", "vazio");
    }

    void setStatusManutenção(String statusManut) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statusManutenção", MODE_PRIVATE).edit();
        editor.putString("status_Manutenção", statusManut);
        editor.apply();
    }

    int getHardwareTimeout() {
        SharedPreferences prefs = context.getSharedPreferences("hardwaretimeout",MODE_PRIVATE);
        return prefs.getInt("hardware_timeout",3);
    }

    void setHardwareTimeout(int hardwareTimeout) {
        SharedPreferences.Editor editor = context.getSharedPreferences("hardwaretimeout", MODE_PRIVATE).edit();
        editor.putInt("hardware_timeout", hardwareTimeout);
        editor.apply();
    }

    boolean isHardwareTurnOff() {
        SharedPreferences prefs = context.getSharedPreferences("hardware_turn_off", MODE_PRIVATE);
        return prefs.getBoolean("hardwareTurnOff",false);
    }

    void setHardwareTurnOff(boolean myhto) {
        SharedPreferences.Editor editor = context.getSharedPreferences("hardware_turn_off", MODE_PRIVATE).edit();
        editor.putBoolean("hardwareTurnOff", myhto);//
        editor.apply();
    }

    int getIntervaloUserName() {
        SharedPreferences prefs = context.getSharedPreferences("intervaloUserName", MODE_PRIVATE);
        return prefs.getInt("intervalo_UserName", -1); // will return -1 if no  value is saved
    }

    void setIntervaloUserName(int mIntervaloUser) {
        SharedPreferences.Editor editor = context.getSharedPreferences("intervaloUserName", MODE_PRIVATE).edit();
        editor.putInt("intervalo_UserName", mIntervaloUser);
        editor.apply();
    }

    boolean isValidUserName() {
        SharedPreferences prefs = context.getSharedPreferences("valid_userName", MODE_PRIVATE);
        return prefs.getBoolean("validUserName",false);
    }

    void setValidUserName(boolean validUserName) {
        SharedPreferences.Editor editor = context.getSharedPreferences("valid_userName", MODE_PRIVATE).edit();
        editor.putBoolean("validUserName", validUserName);//
        editor.apply();
    }

    String getLastUserName() {
        SharedPreferences prefs = context.getSharedPreferences("gpsLastUserName", MODE_PRIVATE);
        return prefs.getString("gps_LastUserName", "0.0");
    }

    void setLastUserName(String lastUserName) {
        SharedPreferences.Editor editor = context.getSharedPreferences("gpsLastUserName", MODE_PRIVATE).edit();
        editor.putString("gps_LastUserName", lastUserName);
        editor.apply();
    }
}
