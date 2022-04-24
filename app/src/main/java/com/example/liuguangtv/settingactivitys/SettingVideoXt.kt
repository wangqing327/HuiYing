package com.example.liuguangtv.settingactivitys


import android.os.Bundle
import com.example.liuguangtv.databinding.ActivitySettingVideoXtBinding
import com.example.liuguangtv.utils.ShareUtils

/**
 * 视频地址嗅探设置
 */
class SettingVideoXt : ActivityParent() {
    private lateinit var binding: ActivitySettingVideoXtBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingVideoXtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.settingview.setOnCheckedChanged { _, b ->
            ShareUtils.writeBoolean(this,ShareUtils.VideoXt,b)
        }

        binding.settingview.setButtonSelected(
            ShareUtils.getBoolean(this,ShareUtils.VideoXt,true))
        super.onCreate(savedInstanceState)
    }
}