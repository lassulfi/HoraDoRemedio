package application.meusprojetos.com.horadoremedio.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import application.meusprojetos.com.horadoremedio.MainActivity;
import application.meusprojetos.com.horadoremedio.R;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by LuisDaniel on 24/09/2017.
 */

/**
 * Classe que cria o despertador e exibe notificação da tela quando o despetador tocar
 */
public class Despertador extends WakefulBroadcastReceiver {

    //Atributos
    private Context context;
    private String nomeRemedio;

    //Métodos especiais


    public Despertador()  {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Log para verificao se o reciever está funcionando
            Log.e("DESPERTADOR", "O despertador está sendo acessado");

            //Recupera o contexto atual
            this.context = context;

            //Recupera o nome do remédio
            this.nomeRemedio = intent.getStringExtra("nome");

            //Toca o alarme
            tocarAlarme();

            //Exibe notificação
            notificacao();
        }
    }

    //Metodos
    private void tocarAlarme(){
        Log.i("DESPERTADOR","Despertador foi acionado");
        Uri alamrUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alamrUri == null){
            alamrUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alamrUri);
        ringtone.play();
    }

    private void notificacao(){

        String nomeMedicamento = "Está na hora do medicamento " + nomeRemedio;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.capsule_icon)
                .setContentTitle("Está na hora do remérdio")
                .setContentText(nomeMedicamento);

        //Criando uma intent para abrir o app
        Intent mainActivityIntent = new Intent(context, MainActivity.class);

        //Instancia do objeto stack builder que contém uma back stack  artifical para a activity
        //que inicia o App.
        //Isso garante que ao navegar para trás na Activity na tela inicial
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //Adiciona a back stack para a intent
        stackBuilder.addParentStack(MainActivity.class);
        //Adiciona a Intent que inicia a atividade no topo da stack
        stackBuilder.addNextIntent(mainActivityIntent);
        PendingIntent mainAcitivityPendingIntent = stackBuilder
                .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(mainAcitivityPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0,mBuilder.build());

    }

}
