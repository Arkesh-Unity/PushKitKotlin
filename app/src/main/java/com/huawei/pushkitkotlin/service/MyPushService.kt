/**
 * Copyright 2020 Huawei Technologies co, Ltd All
 * Rights reserved
 * Licenced under the Apache License,Version 2.0(the "License");
 * you may not use this file except in compliance with license
 * you may obtain a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by application law or agreed to in writing software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permission and
 * limitations under the License
 */
package com.huawei.pushkitkotlin.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.huawei.hms.push.SendException
import com.huawei.pushkitkotlin.R
import com.huawei.pushkitkotlin.activity.DeepLinkActivity
import com.huawei.pushkitkotlin.utils.NotificationUtils
import java.util.*

class MyPushService : HmsMessageService() {
    companion object {
        private const val CODELABS_ACTION = "com.huawei.pushkitkotlin.action"
        private const val TAG = "MyPushService"
    }
    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.i(TAG, "received refresh token:{$token}")
        // send the token to your app server.
        if (token.isNotEmpty()) {
            refreshedTokenToServer(token)
        }

        sendBroadcast(Intent().apply {
            action = CODELABS_ACTION
            putExtra("method", "onNewToken")
            putExtra("msg", "onNewToken called, token: $token")
        })

    }// [End on_new_token]

    private fun refreshedTokenToServer(token: String) {
        Log.i(TAG, "sending token to server. token:$token")
    }
    /**
     * This method is used to receive downstream data messages.
     * This method callback must be completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * @param message RemoteMessage
     */
    // [START receive_message]
    override fun onMessageReceived(message: RemoteMessage?) {

        Log.i(TAG, "onMessageReceived is called")

        // getCollapseKey() Obtains the classification identifier (collapse key) of a message.
        // getData() Obtains valid content data of a message.
        // getMessageId() Obtains the ID of a message.
        // getMessageType() Obtains the type of a message.
        // getNotification() Obtains the notification data instance from a message.
        // getOriginalUrgency() Obtains the original priority of a message.
        // getSentTime() Obtains the time when a message is sent from the server.
        // getTo() Obtains the recipient of a message.
        message?.let {
            Log.i(
                TAG, """collapseKey: ${it.collapseKey}
               data: ${it.data}
               from: ${it.from}
               to: ${it.to}
               messageId: ${it.messageId}
               originalUrgency: ${it.originalUrgency}
               urgency: ${it.urgency}
               sendTime: ${it.sentTime}
               messageType: ${it.messageType}
               ttl: ${it.ttl}"""
            )

            // getBody() Obtains the displayed content of a message
            // getTitle() Obtains the title of a message
            // getTitleLocalizationKey() Obtains the key of the displayed title of a notification message
            // getTitleLocalizationArgs() Obtains variable parameters of the displayed title of a message
            // getBodyLocalizationKey() Obtains the key of the displayed content of a message
            // getBodyLocalizationArgs() Obtains variable parameters of the displayed content of a message
            // getIcon() Obtains icons from a message
            // getSound() Obtains the sound from a message
            // getTag() Obtains the tag from a message for message overwriting
            // getColor() Obtains the colors of icons in a message
            // getClickAction() Obtains actions triggered by message tapping
            // getChannelId() Obtains IDs of channels that support the display of messages
            // getImageUrl() Obtains the image URL from a message
            // getLink() Obtains the URL to be accessed from a message
            // getNotifyId() Obtains the unique ID of a message
            val notification = message.notification
            notification?.let {
                showNotificationWithButton(notification.title)

                Log.i(
                    TAG,
                    """getImageUrl: ${notification.imageUrl} 
                        getTitle: ${notification.title}
                        getTitleLocalizationKey: ${notification.titleLocalizationKey}
                        getTitleLocalizationArgs: ${Arrays.toString(notification.titleLocalizationArgs)}
                        getBody: ${notification.body}
                        getBodyLocalizationKey: ${notification.bodyLocalizationKey}
                        getBodyLocalizationArgs: ${Arrays.toString(notification.bodyLocalizationArgs)}
                        getIcon: ${notification.icon}
                        getSound: ${notification.sound}
                         getTag: ${notification.tag}
                         getColor: ${notification.color}
                         getClickAction: ${notification.clickAction}
                         getChannelId: ${notification.channelId}
                         getLink: ${notification.link}
                         getNotifyId: ${notification.notifyId}"""
                )
            }
            sendBroadcast(Intent().apply {
                action = CODELABS_ACTION
                putExtra("method", "onMessageReceived2342423")
                putExtra("msg", "onMessageReceived called, message id:" + message.messageId + ", payload data:" + message.data)
            })

            // If the messages are not processed in 10 seconds, the app needs to use WorkManager for processing.
            // Process message within 10s
            processWithin10s(message)
        }
    }
    // [END receive_message]

    private fun processWithin10s(message: RemoteMessage) {
        Log.d(TAG, "Processing now. $message")
    }

    override fun onMessageSent(msgId: String) {
        Log.i(TAG, "onMessageSent called, Message id:$msgId")
        sendBroadcast(Intent().apply {
            action = CODELABS_ACTION
            putExtra("method", "onMessageSent")
            putExtra("msg", "onMessageSent called, Message id:$msgId")
        })

    }

    override fun onSendError(msgId: String, exception: Exception) {
        Log.i(
            TAG, "onSendError called, message id:" + msgId + ", ErrCode:"
                    + (exception as SendException).errorCode + ", description:" + exception.message
        )
        sendBroadcast(Intent().apply {
            action = CODELABS_ACTION
            putExtra("method", "onSendError")
            putExtra(
                "msg", "onSendError called, message id:" + msgId + ", ErrCode:"
                        + exception.errorCode + ", description:" + exception.message
            )
        })
    }

    /**
     * Create and show a simple notification containing the received from HMS.
     * And also with Action Button
     * @param messageBody HMS message body received.
     */
   private fun showNotificationWithButton(messageBody: String?) {
        val intent = Intent(this, DeepLinkActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val channelId = getString(R.string.default_notification_channel_id)
        val action =
            NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "BUTTON", pIntent).build()
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.hms_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(action) // #0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}