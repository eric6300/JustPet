package com.taiwan.justvet.justpet

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.taiwan.justvet.justpet.databinding.ActivityMainBinding
import com.taiwan.justvet.justpet.databinding.NavDrawerHeaderBinding

const val PHOTO_FROM_GALLERY = 1
const val PHOTO_FROM_CAMERA = 2
const val RC_SIGN_IN = 101
const val USERS = "users"
const val PETS = "pets"
const val EVENTS = "events"
const val TAGS = "tags"
const val UID = "uid"
const val TAG = "testEric"
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
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
        binding.viewModel = viewModel

        setupBottomNav()
        setupDrawer()
        setupFAB()

//        requestPermission()

        setupFirebaseAuth()

        setUserManager()

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

        // Set up header of drawer ui using data binding
        val bindingNavHeader = NavDrawerHeaderBinding.inflate(
            LayoutInflater.from(this), binding.drawerNavView, false
        )

        bindingNavHeader.lifecycleOwner = this
        bindingNavHeader.viewModel = viewModel
        binding.drawerNavView.addHeaderView(bindingNavHeader.root)
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
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_petProfileDialogFragment)
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_sign_out -> {
                signOut()
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

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), 0
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                Toast.makeText(
                    applicationContext,
                    response?.error?.errorCode.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupFirebaseAuth() {
        val authProvider: List<AuthUI.IdpConfig> = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

//        val customLayout = AuthMethodPickerLayout
//            .Builder(R.layout.your_custom_layout_xml)
//            .setGoogleButtonId(R.profileId.bar)
//            .build()

        val authListener: FirebaseAuth.AuthStateListener =
            FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
                val user: FirebaseUser? = auth.currentUser
                if (user == null) {
                    val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(authProvider)
                        .setAlwaysShowSignInMethodScreen(true)
                        .setIsSmartLockEnabled(false)
//                        .setAuthMethodPickerLayout(customLayout)
                        .build()
                    startActivityForResult(intent, RC_SIGN_IN)
                } else {
                    UserManager.getFirebaseUser(user)
                }
            }

        FirebaseAuth.getInstance()
            .addAuthStateListener(authListener)
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "已登出", Toast.LENGTH_SHORT).show()
                UserManager.clear()
            }
    }

    private fun setUserManager() {
        UserManager.getFirebaseUserCompleted.observe(this, Observer {
            if (it == true) {
                UserManager.userProfile.value?.let { userProfile ->
                    viewModel.checkUserProfile(userProfile)
                    viewModel.setupDrawerUser(userProfile)
                    UserManager.userProfileCompleted()
                }
            }
        })
    }
}
