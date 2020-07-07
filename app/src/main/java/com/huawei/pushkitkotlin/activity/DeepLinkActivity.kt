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
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.pushkitkotlin.R

class DeepLinkActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "DeepLinkActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink2)
        getIntentData(intent)
    }

    private fun getIntentData(intent: Intent?) {
        if (null != intent) {
            //
            val msgId = intent.getStringExtra("_push_msg_id")
            val cmdType = intent.getStringExtra("_push_cmd_type")
            val notifyId = intent.getIntExtra("_push_notify_id", -1)
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    val content = bundle.getString(key)
                    Log.i(TAG, "receive data from push, key = $key, content = $content"
                    )
                }
            }
            Log.i(TAG, "receive data from push, msgId = $msgId, cmd = $cmdType, notifyId = $notifyId"
            )
        } else {
            Log.i(TAG, "intent = null")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }
}