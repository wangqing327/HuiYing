package com.example.liuguangtv.settingactivitys

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.liuguangtv.R
import com.example.liuguangtv.databinding.ActivitySettingAdblockBinding
import com.example.liuguangtv.utils.ShareUtils

class SettingAdblockActivity : ActivityParent() {
    private lateinit var binding: ActivitySettingAdblockBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingAdblockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.button.text = String.format(
            getString(R.string.tiao),
            ShareUtils.getInt(this, ShareUtils.AdblockCount)
        )

        binding.checkBox.setOnCheckedChangeListener { _, boolean ->
            ShareUtils.writeBoolean(this, ShareUtils.AdblockIsOpen, boolean)
            if (boolean) {
                binding.adbState.setText(R.string.adbon)
            } else {
                binding.adbState.setText(R.string.adboff)
            }
        }

        binding.checkBox2.setOnCheckedChangeListener { _, boolean ->
            ShareUtils.writeBoolean(this, ShareUtils.AdblockTipState, boolean)
            if (boolean) {
                binding.adbState2.setText(R.string.adbon)
            } else {
                binding.adbState2.setText(R.string.adboff)
            }
        }
        var isChecked = ShareUtils.getBoolean(this, ShareUtils.AdblockIsOpen, true)
        binding.checkBox.isChecked = isChecked
        //adbState1和adbstate2默认的文本是打开，所以只判断假的状态
        if (!isChecked) {
            binding.adbState.setText(R.string.adboff)
        }
        isChecked = ShareUtils.getBoolean(this, ShareUtils.AdblockTipState, true)
        binding.checkBox2.isChecked = isChecked
        if (!isChecked) {
            binding.adbState2.setText(R.string.adboff)
        }

        binding.button.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.AlertTitle)
                .setMessage(
                    String.format(
                        getString(R.string.xwsfql),
                        ShareUtils.getInt(this@SettingAdblockActivity,
                            ShareUtils.AdblockCount)
                    )
                )
                .setCancelable(true)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    binding.button.text = String.format(
                        getString(R.string.tiao), 0
                    )
                    ShareUtils.writeInt(
                        this@SettingAdblockActivity,
                        ShareUtils.AdblockCount,
                        0
                    )
                }
                .create()
                .show()
        }
    }
}