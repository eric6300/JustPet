package com.taiwan.justvet.justpet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taiwan.justvet.justpet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_bottom_home -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_bottom_calendar -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_calendarFragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_bottom_chart -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_chartFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_bottom_tool -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_toolFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupBottomNav()
        setupDrawer()
        setupFAB()

        requestPermission()
    }

    private fun setupBottomNav() {
        binding.navBottomView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun setupDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        val navController = this.findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        NavigationUI.setupWithNavController(binding.drawerNavView, navController)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set drawer Navigation itemSelectedListener
        binding.drawerNavView.setNavigationItemSelectedListener(this)

        // TODO : detail of drawer
    }

    fun setupFAB() {
        binding.floatingActionButton.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_tagDialog)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
    }

    companion object {
        val PHOTO_FROM_GALLERY = 1
        val PHOTO_FROM_CAMERA = 2
    }
}
