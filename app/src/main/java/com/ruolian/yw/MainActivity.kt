package com.ruolian.yw

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.ruolian.yw.R.layout.activity_main
import com.ruolian.yw.coustomview.UIUrlTextView
import com.ruolian.yw.coustomview.chart.LineChart
import com.ruolian.yw.coustomview.chart.chartdata.CircleChartData
import com.ruolian.yw.coustomview.chart.chartdata.LineChartData
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    private lateinit var circleDatas: ArrayList<CircleChartData>
    private lateinit var lineDatas: ArrayList<LineChartData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        initData()

        initListener()


    }


    private fun initData() {
        circleDatas = ArrayList<CircleChartData>()
        circleDatas.add(CircleChartData("公司高管 2015.3-2009.3 杭州航天信息科技公司", 15, "#FF0000"))
        circleDatas.add(CircleChartData("akldh;w;kajdl;fk", 9, "#00ff00"))
        circleDatas.add(CircleChartData("akldh;lajdfl;kajdl;fk", 16, "#FF0000"))
        circleDatas.add(CircleChartData("我们", 6, "#00ff00"))
        circleDatas.add(CircleChartData("我们", 11, "#FF0000"))
        circleDatas.add(CircleChartData("我们", 15, "#00ff00"))
        circleDatas.add(CircleChartData("我们", 8, "#FF0000"))
        circleDatas.add(CircleChartData("我们我们我", 50, "#00ff00"))
        circleDatas.add(CircleChartData("集团CEO 2001.3-2009.3 北华航天信息科技公司", 33, "#FF0000"))
        circleDatas.add(CircleChartData("我们", 12, "#00ff00"))


        lineDatas = ArrayList<LineChartData>()
        lineDatas.add(LineChartData(2010, 5000))
        lineDatas.add(LineChartData(2011, 8000))
        lineDatas.add(LineChartData(2012, 6000))
        lineDatas.add(LineChartData(2013, 12000))
        lineDatas.add(LineChartData(2014, 19000))
        lineDatas.add(LineChartData(2015, 20000))
        lineDatas.add(LineChartData(2016, 21000))
        lineDatas.add(LineChartData(2017, 22000))
        lineDatas.add(LineChartData(2018, 30000))

    }

    private fun initListener() {
        textview.setLinkClickListener(object : UIUrlTextView.OnLinkClickListener {
            override fun onClick(linkText: CharSequence?) {
                Toast.makeText(this@MainActivity, linkText, Toast.LENGTH_SHORT).show()
            }
        })
        btn_circle.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                circle_chart.setDatas(circleDatas)

                line_chart.setLineData(lineDatas)
            }
        })
        line_chart.setOnTouchChartListener(object : LineChart.OnTouchChartListener {
            override fun onTouch(event: MotionEvent?) {
                //TODO 此处需要处理滑动冲突的事件

            }
        })
    }
}
