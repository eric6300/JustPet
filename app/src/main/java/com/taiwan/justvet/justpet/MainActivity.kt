package com.taiwan.justvet.justpet

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.taiwan.justvet.justpet.data.Invite
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.ActivityMainBinding
import com.taiwan.justvet.justpet.util.CurrentFragmentType

const val PHOTO_FROM_GALLERY = 1
const val PHOTO_FROM_CAMERA = 2
const val RC_SIGN_IN = 101
const val USERS = "users"
const val PETS = "pets"
const val EVENTS = "events"
const val TAGS = "tags"
const val UID = "uid"
const val ERIC = "testEric"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private val onNavigationItemSelectedListener =
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
                    if (viewModel.petsSizeNotZero.value == true) {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_chartFragment)
                        return@OnNavigationItemSelectedListener true
                    } else {
                        Toast.makeText(this, "新增寵物後才能查看圖表", Toast.LENGTH_LONG).show()
                        return@OnNavigationItemSelectedListener false
                    }
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        setupBottomNav()
        setupNavController()
        setupFAB()

        setupFirebaseAuth()

        setUserManager()

        UserManager.userProfile.observe(this, Observer {
            it?.let {
                viewModel.petsSizeNotZero(it.pets?.size != 0)
            }
        })

        viewModel.inviteList.observe(this, Observer { inviteList ->
            inviteList?.let {
                val invite = it[0]

                val dialog = this.let {
                    AlertDialog.Builder(it)
                        .setTitle("邀請通知")
                        .setMessage("${invite.inviterName} ( ${invite.inviterEmail} ) \n邀請你一起紀錄 ${invite.petName} 的生活")
                        .setPositiveButton("接受") { _, _ ->

                            viewModel.confirmInvite(invite)

                            val newList = mutableListOf<Invite>()
                            newList.addAll(inviteList)
                            newList.removeAt(0)
                            viewModel.showInvite(newList)
                        }
                        .setNeutralButton("再想想") { _, _ ->
                            val newList = mutableListOf<Invite>()
                            newList.addAll(inviteList)
                            newList.removeAt(0)
                            viewModel.showInvite(newList)
                        }.create()

                }

                dialog?.show()
            }
        })

        viewModel.navigateToHome.observe(this, Observer {
            it?.let {
                if (it) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_homeFragment)
                    viewModel.navigateToHomeCompleted()
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_pet -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_petProfileDialogFragment)
            }
            R.id.check_invite -> {
                viewModel.checkInvite()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBottomNav() {
        binding.navBottomView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun setupNavController() {
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.homeFragment -> CurrentFragmentType.HOME
                R.id.calendarFragment -> CurrentFragmentType.CALENDAR
                R.id.chartFragment -> CurrentFragmentType.CHART
                R.id.toolFragment -> CurrentFragmentType.TOOL
                R.id.eventDetailFragment -> CurrentFragmentType.EVENT
                R.id.breathFragment -> CurrentFragmentType.BREATH
                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun setupFAB() {
        binding.floatingActionButton.setOnClickListener {
            if (viewModel.petsSizeNotZero.value == true) {
                findNavController(R.id.nav_host_fragment).navigate(
                    NavGraphDirections.navigateToTagDialog(PetEvent())
                )
            } else {
                Toast.makeText(this, "新增寵物後才能替寵物記錄生活", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                finishAndRemoveTask()
            }
        }
    }

    private fun setupFirebaseAuth() {
        val authProvider: List<AuthUI.IdpConfig> = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.custom_login_layout)
            .setGoogleButtonId(R.id.button_google_sign_in)
            .build()

        val authListener: FirebaseAuth.AuthStateListener =
            FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
                val user = auth.currentUser
                if (user == null) {
                    val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(authProvider)
                        .setAlwaysShowSignInMethodScreen(true)
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.LoginTheme)
                        .setAuthMethodPickerLayout(customLayout)
                        .build()
                    startActivityForResult(intent, RC_SIGN_IN)
                } else {
                    UserManager.getFirebaseUser(user)
                }
            }

        FirebaseAuth.getInstance()
            .addAuthStateListener(authListener)
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
