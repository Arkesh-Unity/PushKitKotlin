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
package com.huawei.pushkitkotlin.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.pushkitkotlin.R
import com.huawei.pushkitkotlin.dialog.TopicDialog
import com.huawei.pushkitkotlin.listener.OnDialogClickListener
import com.huawei.pushkitkotlin.receiver.MyReceiver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.log_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Huawei developer
 * you can learn how to get your Push notification by this demo.
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val GET_AAID = 1
    private val deleteAaid = 2
    private lateinit var pushToken: String
    private lateinit var receiver: MyReceiver
    companion object {
        private const val TAG = "MainActivity"
        private const val CODELABS_ACTION = "com.huawei.pushkitkotlin.action"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSetPush.setOnClickListener(this)
        btnGetAaid.setOnClickListener(this)
        btnSetAutoInit.setOnClickListener(this)
        btn_get_token.setOnClickListener(this)
        btn_delete_token.setOnClickListener(this)
        btn_action.setOnClickListener(this)
        btn_generate_intent.setOnClickListener(this)
        btn_is_autoInit_enabled.setOnClickListener(this)
        btn_add_topic.setOnClickListener(this)
        btn_delete_topic.setOnClickListener(this)
        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(CODELABS_ACTION)
        registerReceiver(receiver, filter)
    }
    override fun onStart() {
        super.onStart()

    }

    /**
     * click listener here!
     * Base on Click you can perform many actions
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnGetAaid -> {
                setAAID(btnGetAaid?.text == getString(R.string.get_aaid))
            }
            R.id.btn_get_token -> getToken()
            R.id.btn_delete_token -> deleteToken()
            R.id.btnSetPush -> setReceiveNotifyMsg(
                btnSetPush?.text == getString(R.string.set_push_enable)
            )
            R.id.btn_add_topic -> addTopic()
            R.id.btn_delete_topic ->deleteTopic()
            R.id.btn_action -> openActivityByAction()
            R.id.btn_generate_intent -> generateIntentUri()
            R.id.btn_is_autoInit_enabled -> isAutoInitEnabled()
            R.id.btnSetAutoInit -> {
                setAutoInitEnabled(btnSetAutoInit?.text == getString(R.string.autoInitEnabled))
            }
        }
    }

    /**
     * Set up enable or disable the display of notification messages.
     * @param enable enabled or not
     */
    private fun setReceiveNotifyMsg(enable: Boolean) {
        showLog("Control the display of notification messages:begin")
        if (enable) {
            HmsMessaging.getInstance(this).turnOnPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLog("turnOnPush Complete")
                    btnSetPush?.setText(R.string.set_push_unable)
                } else {
                    showLog("turnOnPush failed: cause=" + task.exception.message)
                }
            }
        } else {
            HmsMessaging.getInstance(this).turnOffPush().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLog("turnOffPush Complete")
                    btnSetPush?.text=getString(R.string.set_push_enable)
                } else {
                    showLog("turnOffPush  failed: cause =" + task.exception.message)
                }
            }
        }
    }
    /**
     * getAAID(), This method is used to obtain an AAID in asynchronous mode. You need to add a listener to listen to the operation result.
     * deleteAAID(), delete a local AAID and its generation timestamp.
     * @param isGet getAAID or deleteAAID
     */
    private fun setAAID(isGet: Boolean) {
        if (isGet) {
            val idResult =
                HmsInstanceId.getInstance(this).aaid
                idResult.addOnSuccessListener { aaidResult ->
                val aaId = aaidResult.id
                Log.i(TAG, "getAAID success:$aaId")
                showLog("getAAID success:$aaId")
                handler.sendEmptyMessage(deleteAaid)
            }.addOnFailureListener { e ->
                Log.e(TAG, "getAAID failed:$e")
                showLog("getAAID failed.$e")
            }
        } else {
            GlobalScope.launch {
                    try {
                        HmsInstanceId.getInstance(this@MainActivity).deleteAAID()
                        showLog("delete aaid and its generation timestamp success.")
                        handler.sendEmptyMessage(GET_AAID)
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "deleteAAID failed. $e")
                        showLog("deleteAAID failed.$e")
                    }
                }
        }
    }
    /**
     * getToken(String appId, String scope), This method is used to obtain a token required for accessing HUAWEI Push Kit.
     * If there is no local AAID, this method will automatically generate an AAID when it is called because the Huawei Push server needs to generate a token based on the AAID.
     * This method is a synchronous method, and you cannot call it in the main thread. Otherwise, the main thread may be blocked.
     */
   private fun getToken(){
        GlobalScope.launch() {
            try {
                // read from agconnect-services.json
                val appId = AGConnectServicesConfig.fromContext(this@MainActivity)
                    .getString("client/app_id")
                pushToken = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                if (pushToken.isNotEmpty()) {
                    Log.i(TAG, "get token:$pushToken")
                    showLog(pushToken)
                }
            } catch (e: Exception) {
                Log.i(TAG, "getToken failed, $e")
            }
            println("running from lambda: ${Thread.currentThread()}")
        }
    }
    /**
     * void deleteToken(String appId, String scope) throws ApiException
     * This method is used to obtain a token. After a token is deleted, the corresponding AAID will not be deleted.
     * This method is a synchronous method. Do not call it in the main thread. Otherwise, the main thread may be blocked.
     */
    private fun deleteToken() {
        showLog("deleteToken:begin")
        GlobalScope.launch() {
                try {
                    // read from agconnect-services.json
                    val appId =
                        AGConnectServicesConfig.fromContext(this@MainActivity)
                            .getString("client/app_id")
                    HmsInstanceId.getInstance(this@MainActivity).deleteToken(appId, "HCM")
                    Log.i(TAG, "deleteToken success.")
                    showLog("deleteToken success")
                } catch (e: ApiException) {
                    Log.e(TAG, "deleteToken failed.$e")
                    showLog("deleteToken failed.$e")
                }
            }
        }



    /**
     * This method is user for log nd data in text View
     */
    private fun showLog(pushToken: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            if (tv_log is TextView) {
                tv_log?.text=pushToken
                Toast.makeText(this@MainActivity, pushToken, Toast.LENGTH_SHORT).show()
            }
        }

    }

    @SuppressLint("HandlerLeak")
    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_AAID -> btnGetAaid?.text=getString(R.string.get_aaid)
                deleteAaid -> btnGetAaid?.text=getString(R.string.delete_aaid)

            }
        }
    }
    /**
     * In Opening a Specified Page of an App, how to Generate Intent parameters.
     */
    private fun generateIntentUri() {
        val intent = Intent(Intent.ACTION_VIEW)
        // You can add parameters in either of the following ways:
        // Define a scheme protocol, for example, pushscheme://com.huawei.pushkitkotlin/deeplink?.
        // way 1 start: Use ampersands (&) to separate key-value pairs. The following is an example:
        intent.data = Uri.parse("pushscheme://com.huawei.pushkitkotlin/deeplink?name=abc&age=180")
        // way 1 end. In this example, name=abc and age=180 are two key-value pairs separated by an ampersand (&).
        // way 2 start: Directly add parameters to the Intent.
        // intent.setData(Uri.parse("pushscheme://com.huawei.pushkitkotlin/deeplink?"));
        // intent.putExtra("name", "abc");
        // intent.putExtra("age", 180);
        // way 2 end.
        // The following flag is mandatory. If it is not added, duplicate messages may be displayed.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val intentUri = intent.toUri(Intent.URI_INTENT_SCHEME)
        // The value of intentUri will be assigned to the intent parameter in the message to be sent.
        Log.d("intentUri", intentUri)
        showLog(intentUri)

        // You can start the deep link activity with the following code.
        //intent.setClass(this, DeepLinkActivity.class);
        //startActivity(intent);
    }

    /**
     * Simulate pulling up the application custom page by action.
     */
    private fun openActivityByAction() {
        val intent = Intent("com.huawei.pushkitkotlin.intent.action.test")

        // You can start the deep link activity with the following code.
        intent.setClass(this, DeepLinkActivity::class.java)
        startActivity(intent)
    }

    private fun isAutoInitEnabled() {
        Log.i(TAG, "isAutoInitEnabled:" + HmsMessaging.getInstance(this).isAutoInitEnabled)
        showLog("isAutoInitEnabled:" + HmsMessaging.getInstance(this).isAutoInitEnabled)
    }

    /**
     * Set Auto Init Enable Here!
     */
    private fun setAutoInitEnabled(enable: Boolean) {
        if (enable) {
            HmsMessaging.getInstance(this).isAutoInitEnabled = true
            showLog("setAutoInitEnabled: true")
            btnSetAutoInit?.text=getString(R.string.autoInitDisabled)
        } else {
            HmsMessaging.getInstance(this).isAutoInitEnabled = false
            showLog("setAutoInitEnabled: false")
            btnSetAutoInit?.text=getString(R.string.autoInitEnabled)
        }
    }

    /**
     * to subscribe to topics in asynchronous mode.
     */
    private fun addTopic() {
        val topicDialog = TopicDialog(this, true)
        topicDialog.setOnDialogClickListener(object :
            OnDialogClickListener {
            override fun onConfirmClick(msg: String?) {
                topicDialog.dismiss()
                try {
                    HmsMessaging.getInstance(this@MainActivity)
                        .subscribe(msg)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "subscribe Complete")
                                showLog("subscribe Complete")
                            } else {
                                showLog("subscribe failed: ret=" + task.exception.message)
                            }
                        }
                } catch (e: java.lang.Exception) {
                    showLog("subscribe failed: exception=" + e.message)
                }
            }

            override fun onCancelClick() {
                topicDialog.dismiss()
            }
        })
        topicDialog.show()
    }

    /**
     * to unsubscribe to topics in asynchronous mode.
     */
    private fun deleteTopic() {
        val topicDialog =
            TopicDialog(this, false)
        topicDialog.setOnDialogClickListener(object :
            OnDialogClickListener {
            override fun onConfirmClick(msg: String?) {
                topicDialog.dismiss()
                try {
                    HmsMessaging.getInstance(this@MainActivity)
                        .unsubscribe(msg)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showLog("unsubscribe Complete")
                            } else {
                                showLog("unsubscribe failed: ret=" + task.exception.message)
                            }
                        }
                } catch (e: java.lang.Exception) {
                    showLog("unsubscribe failed: exception=" + e.message)
                }
            }

            override fun onCancelClick() {
                topicDialog.dismiss()
            }
        })
        topicDialog.show()
    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(receiver)
    }

}
