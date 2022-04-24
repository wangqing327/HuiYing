package com.example.liuguangtv

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import com.example.liuguangtv.databinding.ActivityWebbroswerBinding
import com.example.liuguangtv.utils.Utils


class VideoJieXi : BrosWerAndPlayerParent() {

    lateinit var binding: ActivityWebbroswerBinding

    /**
     * 返回按钮上次单击时间
     */
    private var lastTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWebbroswerBinding.inflate(layoutInflater)
        initRoot(binding.root)
        super.onCreate(savedInstanceState)
        //视频解析被单击
        openJieXiClick = View.OnClickListener {
            createpw()
        }
        //StatService.start(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isFullScreen) {
                //当前是全屏状态，先退出全屏
                return true
            }
            return backCalled()
        }
        return super.onKeyDown(keyCode, event)
    }


    /**
     * 用户点击返回按钮时的处理程序
     */
    private fun backCalled(): Boolean {
        if (System.currentTimeMillis() - lastTime <= 2000) {
            finish()
        } else {
            lastTime = System.currentTimeMillis()
            Utils.showToast(this, R.string.exitVideo)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            返回上一层
            android.R.id.home -> {
                backCalled()
            }

            R.id.openMenu -> {
                createMenu()
            }

            R.id.openjiexi -> {
                createpw()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}