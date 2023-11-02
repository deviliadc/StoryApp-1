package com.devapps.storyapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.devapps.storyapp.R
import com.devapps.storyapp.adapter.StoryAdapter
import com.devapps.storyapp.data.Resource
import com.devapps.storyapp.data.pref.UserPreferences
import com.devapps.storyapp.databinding.ActivityMainBinding
import com.devapps.storyapp.viewmodel.MainViewModel
import com.devapps.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupView()
        setupClickListeners()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                handleLogout()
                true
            }
            R.id.menu_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun handleLogout() {
        mainViewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun setupView() {
        storyAdapter = StoryAdapter()

        mainViewModel.getUserToken().observe(this){ token ->
            if (token.isNotEmpty()){
                mainViewModel.stories.observe(this) {
                    when (it) {
                        is Resource.Success -> {
                            it.data?.let { stories -> storyAdapter.setData(stories) }
                            showLoad(false)
                        }
                        is Resource.Loading -> showLoad(true)
                        is Resource.Error -> {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    mainViewModel.getStories()
                }
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        with(binding.rvStory) {
            setHasFixedSize(true)
            adapter = storyAdapter
        }

        mainViewModel.logoutResult.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                is Resource.Loading -> {
                    showLoad(true)
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    showLoad(false)
                }
            }
        }
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        val viewModelFactory = ViewModelFactory(pref)

        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    private fun showLoad(isLoad: Boolean) {
        if (isLoad){
            binding.progressBar.visibility = View.VISIBLE
        }
        else {
            binding.progressBar.visibility = View.GONE
        }
    }
}