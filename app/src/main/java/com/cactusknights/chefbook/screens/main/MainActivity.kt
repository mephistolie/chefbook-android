package com.cactusknights.chefbook.screens.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.cactusknights.chefbook.R
import com.cactusknights.chefbook.databinding.ActivityMainBinding
import com.cactusknights.chefbook.dialogs.SettingsDialog
import com.cactusknights.chefbook.fragments.CategoriesFragment
import com.cactusknights.chefbook.fragments.ShoppingListFragment
import com.cactusknights.chefbook.screens.auth.AuthActivity
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val viewModel: MainActivityViewModel by viewModels()
//    val userViewModel: UuuserViewModel by viewModels()
    private val fragmentManager: FragmentManager = supportFragmentManager
    private val innerFragments = arrayListOf("Favourite", "Categories", "Recipes in Category")
    private lateinit var sp: SharedPreferences

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startLoginActivity()

        if (savedInstanceState != null) {
            val savedFragment = supportFragmentManager.getFragment(savedInstanceState, "savedFragment")
            if (savedFragment != null) {
                val savedSectionName = savedInstanceState.getString("sectionName", resources.getString(R.string.recipes))
                val tag = savedFragment.tag!!
                when (savedFragment.tag) {
                    "Favourite" -> { setTopMenu(resources.getString(R.string.favourite), true) }
                    "Categories" -> { setTopMenu(resources.getString(R.string.categories), true) }
                    "Recipes in Category" -> { setTopMenu(savedSectionName, true) }
                }
                setFragment(savedFragment, R.anim.zoom_in_show, R.anim.zoom_in_hide, tag)
            } else { setStartFragment() }
        } else { setStartFragment() }

        val navigation = NavigationBarView.OnItemSelectedListener { item ->
            val shoppingListFragment = fragmentManager.findFragmentByTag("Shopping List")
            val recipesFragment = fragmentManager.findFragmentByTag("Recipes")
            if (item.itemId == R.id.shopping_list && (shoppingListFragment == null || !shoppingListFragment.isVisible)) {
                setTopMenu(resources.getString(R.string.shopping_list))
                setFragment(ShoppingListFragment(), R.anim.swipe_right_show, R.anim.swipe_right_hide, "Shopping List")
            } else if (item.itemId == R.id.recipes && (recipesFragment == null || !recipesFragment.isVisible)) {
                setTopMenu()
                if (shoppingListFragment != null && shoppingListFragment.isVisible) setFragment(
                    DashboardFragment(), R.anim.swipe_left_show, R.anim.swipe_left_hide, "Recipes")
                else setFragment(DashboardFragment(), R.anim.zoom_out_show, R.anim.zoom_out_hide, "Recipes")
            }
            true
        }
        binding.nvNavigation.setOnItemSelectedListener(navigation)
        binding.btnLeft.setOnClickListener { onBackPressed() }
        binding.btnRight.setOnClickListener { onRightPressed() }

        // SQLite Migration
        val legacyDatabase = File(filesDir, "../databases/ChefBookDB")
//        if (File(filesDir, "../databases/ChefBookDB").exists())
//            FirebaseAuthRepository.migrateToFirebase(legacyDatabase, this)
    }

    override fun onStart() {
        super.onStart()
    }

    private suspend fun listenToUser() {
//        startLoginActivity()
//        userViewModel.listenToUser().collect { user: User? ->
//            if (user == null) {
//                startLoginActivity()
//            } else {
//                if (user.isPremium)
//                    binding.avBanner.visibility = View.GONE
//                else {
//                    MobileAds.initialize(this)
//                    val adRequest: AdRequest = AdRequest.Builder().build()
//                    binding.avBanner.loadAd(adRequest)
//                    binding.avBanner.visibility = View.VISIBLE
//                }
//            }
//        }
    }

//    override fun onDestroy() {
////        userViewModel.stopListeningToUpdates()
//        super.onDestroy()
//    }

//    override fun onSaveInstanceState(outState: Bundle) {
////        for (fragment in fragmentManager.fragments) {
////            if (fragment != null && fragment.isVisible) {
////                fragmentManager.putFragment(outState, "savedFragment", fragment)
////                outState.putString("sectionName", binding.textSection.text.toString())
////            }
////        }
//        super.onSaveInstanceState(outState)
//    }

    private fun setStartFragment() {
//        if (!sp.getBoolean("shoppingListIsDefault", false)) {
//            setFragment(DashboardFragment(), R.anim.zoom_in_show, R.anim.zoom_in_hide,"Recipes")
//            binding.nvNavigation.selectedItemId = R.id.recipes
//        } else {
//            setTopMenu(resources.getString(R.string.shopping_list))
//            setFragment(ShoppingListFragment(), R.anim.zoom_in_show, R.anim.zoom_in_hide,"Shopping List")
//        }
    }

    fun setFragment(fragment: Fragment, startAnimation: Int, endAnimation: Int, tag: String = "") {
        fragmentManager.beginTransaction()
            .setCustomAnimations(startAnimation, endAnimation)
            .replace(R.id.cv_content, fragment, tag)
            .commit()
    }

    fun setTopMenu(title: String = resources.getString(R.string.recipes),
                           isLeftVisible: Boolean = false) {
        binding.textSection.text = title
        if (isLeftVisible) {
            binding.btnLeft.visibility = View.VISIBLE
        } else {
            binding.btnLeft.visibility = View.INVISIBLE
        }
    }

    private fun onRightPressed() { SettingsDialog().show(fragmentManager, "Settings") }

    override fun onBackPressed() {
        for (fragment in innerFragments) {
            val currentFragment = fragmentManager.findFragmentByTag(fragment)
            if (currentFragment != null && currentFragment.isVisible) {
                if (fragment == "Recipes in Category") {
                    setTopMenu(resources.getString(R.string.categories), true)
                    setFragment(CategoriesFragment(), R.anim.zoom_out_show, R.anim.zoom_out_hide, "Categories")
                } else {
                    setTopMenu()
                    setFragment(DashboardFragment(), R.anim.zoom_out_show, R.anim.zoom_out_hide, "Recipes")
                }
                return
            }
        }
        super.onBackPressed()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, AuthActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}