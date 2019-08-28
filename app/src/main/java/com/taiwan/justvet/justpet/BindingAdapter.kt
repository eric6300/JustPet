package com.taiwan.justvet.justpet

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("iconSpecies")
fun bindSpeciesIcon(imageView: ImageView, species: Int) {
    species.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_cat))
            }
            1 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_dog))
            }
        }
    }
}

@BindingAdapter("iconGender")
fun bindGenderIcon(imageView: ImageView, gender: Int) {
    gender.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_female))
            }
            1 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_male))
            }
        }
    }
}