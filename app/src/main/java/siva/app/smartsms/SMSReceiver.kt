package siva.app.smartsms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsMessage
import androidx.core.app.NotificationCompat


class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val bundle = intent.extras
            val msgs: Array<SmsMessage?>?
            var msg_from: String
            if (bundle != null) {
                try {
                    val pdus = bundle["pdus"] as Array<*>?
                    msgs = arrayOfNulls(pdus!!.size)
                    for (i in msgs.indices) {
                        msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        msg_from = msgs[i]!!.originatingAddress.toString()
                        val msgBody: String = msgs[i]!!.messageBody
                        sendNotification(msg_from, msgBody, context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendNotification(msg_from: String?, msgBody: String?, context: Context?) {

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("Notif", "1")
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val channelId = "siva.app.smartsms"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context!!, channelId)
                .setSmallIcon(R.drawable.ic_sms)
                .setContentTitle(msg_from)
                .setContentText(msgBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager!!.createNotificationChannel(channel)
        }
        notificationManager!!.notify(0, notificationBuilder.build())
    }
}