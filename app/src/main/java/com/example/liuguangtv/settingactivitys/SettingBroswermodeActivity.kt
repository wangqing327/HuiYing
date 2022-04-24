package com.example.liuguangtv.settingactivitys

import android.os.Bundle
import com.example.liuguangtv.databinding.ActivitySettingBroswermodeBinding
import com.example.liuguangtv.utils.ShareUtils

/**
 * 设置浏览器的模式，PC or Android
 */
class SettingBroswermodeActivity : ActivityParent() {
    private lateinit var binding: ActivitySettingBroswermodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingBroswermodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        binding.settingview.setButtonSelected(
            ShareUtils.getBoolean(this, ShareUtils.WebMode, true)
        )

        binding.settingview.setOnCheckedChanged { _, p1 ->
            ShareUtils.writeBoolean(this, ShareUtils.WebMode, p1)
        }
    }
}