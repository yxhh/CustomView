package com.ruolian.yw

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.ruolian.yw.R.layout.activity_main
import com.ruolian.yw.coustomview.UIUrlTextView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        textview.setLinkClickListener(object : UIUrlTextView.OnLinkClickListener {
            override fun onClick(linkText: CharSequence?) {
                Toast.makeText(this@MainActivity, linkText, Toast.LENGTH_SHORT).show();
            }

        })
    }
}
