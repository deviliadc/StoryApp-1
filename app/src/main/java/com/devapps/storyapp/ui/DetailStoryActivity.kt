package com.devapps.storyapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.devapps.storyapp.data.model.Story
import com.devapps.storyapp.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    companion object{
        const val EXTRA_DETAIL = "extra_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater )
        setContentView(binding.root)
        supportActionBar?.hide()

        setupView()
    }

    private fun setupView() {
        val detail = intent.getParcelableExtra<Story>(EXTRA_DETAIL)

        binding.apply {
            tvNameDetail.text = detail?.name
            tvDesc.text = detail?.description
        }
        Glide.with(this)
            .load(detail?.photoUrl)
            .into(binding.imgStoryDetail)
    }
}