package com.taiwan.justvet.justpet

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
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
import com.taiwan.justvet.justpet.ext.getVmFactory
import com.taiwan.justvet.justpet.util.CurrentFragmentType
import kotlinx.android.synthetic.main.activity_main.*

const val PHOTO_FROM_GALLERY = 1
const val RC_SIGN_IN = 101
const val ERIC = "testEric"
const val UID = "uid"
const val EMAIL = "email"
const val USERS = "users"
const val PETS = "pets"
const val EVENTS = "events"
const val TAGS = "tags"
const val INVITES = "invites"
const val SLASH = "/"
const val COLON = ":"
const val EMPTY_STRING = ""

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> { getVmFactory() }

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
                    if (UserManager.userHasPets()) {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_chartFragment)
                        return@OnNavigationItemSelectedListener true
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.text_chart_not_available),
                            Toast.LENGTH_LONG
                        ).show()
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
        setupFab()

        setupFirebaseAuth()

        setUserManager()

        UserManager.refreshUserProfileCompleted.observe(this, Observer {
            it?.let {
                if (it) {
                    UserManager.userProfile.value?.let { userProfile ->
                        nav_bottom_view.selectedItemId = R.id.nav_bottom_home
                        UserManager.refreshUserProfileCompleted()
                    }
                }
            }
        })

        viewModel.invitationList.observe(this, Observer { invitationList ->
            invitationList?.let {
                val invitation = it[0]

                val dialog = this.let {
                    AlertDialog.Builder(it)
                        .setTitle(getString(R.string.title_invitation_dialog))
                        .setMessage("${invitation.inviterName} ( ${invitation.inviterEmail} ) \n邀請你一起紀錄 ${invitation.petName} 的生活")
                        .setPositiveButton(getString(R.string.text_receive)) { _, _ ->

                            viewModel.confirmInvite(invitation)

                            showNextInvitation(invitationList)

                        }
                        .setNeutralButton(getString(R.string.text_consider)) { _, _ ->

                            showNextInvitation(invitationList)

                        }
                        .setNegativeButton(getString(R.string.text_refuse)) { _, _ ->

                            viewModel.deleteInvitation(invitation)

                            showNextInvitation(invitationList)

                        }.create()

                }

                dialog?.show()
            }
        })

    }

    private fun showNextInvitation(invitationList: List<Invite>) {
        val newList = mutableListOf<Invite>()
        newList.addAll(invitationList)
        newList.removeAt(0)
        viewModel.showInvite(newList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_pet -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigate_to_addNewPetDialog)
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
                R.id.eventFragment -> CurrentFragmentType.EVENT
                R.id.breathFragment -> CurrentFragmentType.BREATH
                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun setupFab() {
        binding.floatingActionButton.setOnClickListener {
            if (UserManager.userHasPets()) {
                findNavController(R.id.nav_host_fragment).navigate(
                    NavGraphDirections.navigateToTagDialog(PetEvent())
                )
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.text_fab_not_available),
                    Toast.LENGTH_LONG
                ).show()
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
                when (val user = auth.currentUser) {
                    null -> {
                        val intent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(authProvider)
                            .setAlwaysShowSignInMethodScreen(true)
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.LoginTheme)
                            .setAuthMethodPickerLayout(customLayout)
                            .build()
                        startActivityForResult(intent, RC_SIGN_IN)
                    }
                    else -> {
                        UserManager.getFirebaseUser(user)
                    }
                }

            }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun setUserManager() {
        UserManager.getFirebaseUserCompleted.observe(this, Observer {
            it?.let {
                if (it) {
                    UserManager.userProfile.value?.let { userProfile ->
                        viewModel.checkUserProfile(userProfile)
                        UserManager.userProfileCompleted()
                    }
                }
            }
        })
    }
}
