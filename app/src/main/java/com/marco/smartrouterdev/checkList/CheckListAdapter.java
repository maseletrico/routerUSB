package com.marco.smartrouterdev.checkList;

import android.graphics.Color;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marco.smartrouterdev.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import android.graphics.drawable.Drawable;
//import android.support.annotation.IdRes;
//import android.util.Log;
//import android.util.Log;
//import android.widget.Toast;
//import com.marco.smartrouterdev.MainActivity;
//import java.io.UnsupportedEncodingException;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder>{

    private static final String TAG = "CheckListAdapter";

    //private List<Object> checkListData= Collections.emptyList();
    private List<CheckListData> checkListData= Collections.emptyList();

    // cria json checkList criticos de envio para servidor
    public static JSONObject jsonObjectCheckListEnvioCritico = new JSONObject();
    // cria json checkList geral de envio para servidor
    public static JSONObject jsonObjectCheckListEnvioGeral= new JSONObject();
    //Cor do texto do CheckList
    private static String textColor;

    public interface OnItemClickListener {
        void onClick(View view, int position);

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView tvCkeckItem;


        ViewHolder(View v)  {
            super(v);
            tvCkeckItem = (TextView) v.findViewById(R.id.tv_check_item);
            tvCkeckItem.setTextColor(Color.RED);



        }
    }
    // Provide a suitable constructor
    public CheckListAdapter(ArrayList myDataset) {
        checkListData = myDataset;

    }

    @Override
    public CheckListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.check_list_body, parent, false);

        // set the view's size, margins, paddings and layout parameters
        //...

        return new ViewHolder( v);
    }

    @Override
    public void onBindViewHolder(final CheckListAdapter.ViewHolder holder, final int position) {

        CheckListData current=checkListData.get(position);

        if(holder!=null){
            //verifica se o item é vazio
            //converte inteiro em string para cor do texto
            if(holder.tvCkeckItem.getCurrentTextColor()== Color.GRAY){
                textColor="NA";
            }else if(holder.tvCkeckItem.getCurrentTextColor()== Color.BLUE){
                textColor="Sim";
            }else if(holder.tvCkeckItem.getCurrentTextColor()== Color.RED){
                textColor="Nao";
            }
            if(!current.getCheckListItem().isEmpty()) {
                //verifica se o item é crítico, se for crítico tem "@" no final
                if (current.getCheckListItem().substring(current.getCheckListItem().length() - 1).equals("@")) {
                    //Seta o TextView para Negrito itálico
                    holder.tvCkeckItem.setTypeface(null, Typeface.BOLD_ITALIC);
                    //seta textView para a cor de fundo do text view
                    holder.tvCkeckItem.setBackgroundColor(Color.WHITE);
                    //Tira o símbolo "@" do fim sa string
                    holder.tvCkeckItem.setText(current.getCheckListItem().substring(0, current.getCheckListItem().length() - 1));
                } else {//se a última letra da string não for "@"
                    //seta textView para a cor de fundo do text view
                    holder.tvCkeckItem.setBackgroundColor(Color.LTGRAY);
                    holder.tvCkeckItem.setText(current.getCheckListItem());
                }
            }
        }else{
            //Log.d(TAG,"HOLDER NULL");
        }
        assert holder != null;
        holder.tvCkeckItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(holder.tvCkeckItem.getCurrentTextColor()== Color.BLUE){
                    holder.tvCkeckItem.setTextColor(Color.GRAY);
                    textColor="NA";
                }else if(holder.tvCkeckItem.getCurrentTextColor()== Color.RED){
                    holder.tvCkeckItem.setTextColor(Color.BLUE);
                    textColor="Sim";
                }else if(holder.tvCkeckItem.getCurrentTextColor()== Color.GRAY){
                    holder.tvCkeckItem.setTextColor(Color.RED);
                    textColor="Nao";
                }

                try {
                    String itemAcentuado = holder.tvCkeckItem.getText().toString();
                    if(position>=jsonObjectCheckListEnvioCritico.length()){
                    jsonObjectCheckListEnvioGeral.put(URLEncoder.encode(itemAcentuado),textColor);
                    }else{
                        jsonObjectCheckListEnvioCritico.put(URLEncoder.encode(itemAcentuado),textColor);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if(checkListData!=null){
            return checkListData.size();
        }else{
            return 0;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
