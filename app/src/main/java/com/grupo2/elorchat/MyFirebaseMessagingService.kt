package com.grupo2.elorchat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessaginService"
    //Identificador unico asociado a la notificacion que se va ha mostrar
    private val CHANNEL_ID = "MyNotificationChannel"

    //FUNCIO QUE SE EJECUTA AUTOMATICAMENTE CUANDO SE GENERA UN
    //TOKEN NUEVO EN ESTA APP
    override fun onNewToken(token: String) {
        //Solo se llama la primera vez que se llama desde un dispositivo
        Log.d(TAG,"Nuevo Token $token")
        //guardar token en el UserPrederences

        //Se puede enviar el token al server aqui de ser necesario
    }

    //Codigo que se ejecuta cuando recivimos notificaciones de Firebase
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensaje recivido de: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "notification: ${it.title}")
            Log.d(TAG, "notification: ${it.body}")
            showNotification(it.title ?: "Title", it.body ?: "body")
        }

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "data: ${remoteMessage.data}")
        }


    }

    private fun showNotification(title: String, body: String) {
        Log.d(TAG,"showNotification")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Crear un canal de notificación (Requerido en Android oreo y posteriores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal de notificación",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        //Crear la notificación (objeto)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.circle_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        //Mostrar la notificación
        notificationManager.notify(1,builder.build())
    }

}