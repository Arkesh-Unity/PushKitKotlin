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
package com.huawei.pushkitkotlin.dialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import com.huawei.pushkitkotlin.R
import com.huawei.pushkitkotlin.listener.OnDialogClickListener
import kotlinx.android.synthetic.main.dialog_add_topic.view.*
class TopicDialog(context: Context, isAdd: Boolean) : Dialog(context),View.OnClickListener {
    private var view: View? = null
    private lateinit var onDialogClickListener: OnDialogClickListener
    private lateinit var edTopic: EditText
   init {
      view = View.inflate(context, R.layout.dialog_add_topic, null)
      initView(isAdd, context)
    }

    private fun initView(
        isAdd: Boolean,
        context: Context
    ) {
        view?.tv_cancel?.setOnClickListener(this)
        view?.tv_confirm?.setOnClickListener(this)
        edTopic = view?.ed_topic!!
        edTopic.setHint(if (isAdd) R.string.add_topic else R.string.delete_topic)
        edTopic.setOnEditorActionListener(OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //
                Log.i("Dialog", "testView {$textView} keyEvent {$keyEvent}" )
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window?.decorView?.windowToken, 0)
                return@OnEditorActionListener true
            }
            false
        })
        setCanceledOnTouchOutside(false)
        setContentView(view!!)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_cancel -> onDialogClickListener.let {
                onDialogClickListener.onCancelClick()
            }
            R.id.tv_confirm -> onDialogClickListener.let {
                onDialogClickListener.onConfirmClick(edTopic.text.toString())
            }
        }
    }

    fun setOnDialogClickListener(onDialogClickListener: OnDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener
    }
}
