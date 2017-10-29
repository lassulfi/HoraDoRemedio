package application.meusprojetos.com.horadoremedio.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by LuisDaniel on 17/09/2017.
 */

public class Alarms {

    //Atributos
    private Context context;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private String nomeMedicamento;


    //Métodos especiais
    public Alarms(Context c) {
        this.context = c;
    }

    //Métodos
    /**
     * Método para definir uma hora de alarme
     * @param h hora em inteiro
     * @param m minuto em inteiro
     * @param i intervalo em horas
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setAlarm(int h, int m, int i){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Despertador.class);
        intent.putExtra("nome",nomeMedicamento);
        alarmIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //Cálculo do intervalo em milisegundos
        long interval = i * 1000 * 60 * 60;

        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY,h);
        c.set(Calendar.MINUTE,m);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),interval,alarmIntent);
        Log.i("ALARMMANAGER","Alarme definido para " + c.getTime().toString());

    }

    public void cancelAlarm(){
        if (alarmManager != null){
            alarmManager.cancel(alarmIntent);
        }
    }

    public void getNomeMedicamento(String n){
        this.nomeMedicamento = n;
    }

}
