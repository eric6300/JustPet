package com.taiwan.justvet.justpet.util

import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.tag.TagListAdapter

@BindingAdapter("iconSpecies")
fun bindSpeciesIcon(imageView: ImageView, species: Int) {
    species.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_cat
                    ))
            }
            1 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_dog
                    ))
            }
        }
    }
}

@BindingAdapter("iconGender")
fun bindGenderIcon(imageView: ImageView, gender: Int) {
    gender.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_female
                    ))
            }
            1 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_male
                    ))
            }
        }
    }
}

@BindingAdapter("eventBackground")
fun bindEventBackground (cardView: CardView, type: Int) {
    type.let {
        when (it) {
            0 -> {
                cardView.setCardBackgroundColor(
                    JustPetApplication.appContext.getColor(
                        R.color.colorDiary
                    ))
            }
            1 -> {
                cardView.setCardBackgroundColor(
                    JustPetApplication.appContext.getColor(
                        R.color.colorTreatment
                    ))
            }
            2 -> {
                cardView.setCardBackgroundColor(
                    JustPetApplication.appContext.getColor(
                        R.color.colorSyndrome
                    ))
            }
        }
    }
}

@BindingAdapter("eventTagIcon")
fun bindEventTagIcon (imageView: ImageView, tag: Int) {
    tag.let {
        when (it) {
            0 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_dog
                    ))
            }
            1 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_cake
                    ))
            }
            2 -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_cat
                    ))
            }
        }
    }
}

@BindingAdapter("expandIcon")
fun bindExpandIcon (imageView: ImageView, status: Boolean) {
    status.let {
        when (it) {
            true -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_expand_more
                    ))
            }
            else -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_expand_less
                    ))
            }
        }
    }
}

@BindingAdapter("listOfTags")
fun bindRecyclerViewWithListOfTags(recyclerView: RecyclerView, list: List<EventTag>?) {
    list?.let {
        recyclerView.adapter?.apply {
            when (this) {
                is TagListAdapter -> submitList(it)
            }
        }
    }
}