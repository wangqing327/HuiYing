package com.example.liuguangtv.settingactivitys


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * 设置类窗口的父类，主要将supportActionBar图标设置出来
 * 注意将super.onCreate(savedInstanceState)代码到setContentView(binding.root)后执行
 */
abstract class ActivityParent:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}