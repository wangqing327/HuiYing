package com.example.liuguangtv.settingactivitys

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.liuguangtv.databinding.ActivityJlScBinding
import com.example.liuguangtv.settingactivitys.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout

class JlScActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJlScBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJlScBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        viewPager.currentItem = intent.getIntExtra("index", 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}