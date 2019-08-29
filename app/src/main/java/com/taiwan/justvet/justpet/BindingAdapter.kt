package com.taiwan.justvet.justpet

import android.widget.ImageView
import androidx.cardview.widget.CardView
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

@BindingAdapter("eventBackground")
fun bindEventBackground (cardView: CardView, type: Int) {
    type.let {
        when (it) {
            0 -> {
                cardView.setCardBackgroundColor(JustPetApplication.appContext.getColor(R.color.colorDiary))
            }
            1 -> {
                cardView.setCardBackgroundColor(JustPetApplication.appContext.getColor(R.color.colorTreatment))
            }
            2 -> {
                cardView.setCardBackgroundColor(JustPetApplication.appContext.getColor(R.color.colorSyndrome))
            }
        }
    }
}

@BindingAdapter("eventTagIcon")
fun bindEventTagIcon (imageView: ImageView, tag: Int) {
    tag.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_dog))
            }
            1 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_cake))
            }
            2 -> {
                imageView.setImageDrawable(JustPetApplication.appContext.getDrawable(R.drawable.ic_cat))
            }
        }
    }
}