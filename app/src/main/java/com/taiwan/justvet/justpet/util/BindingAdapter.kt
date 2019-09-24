package com.taiwan.justvet.justpet.util

import android.net.Uri
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.taiwan.justvet.justpet.GlideApp
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.calendar.CalendarEventAdapter
import com.taiwan.justvet.justpet.chart.ChartPetAvatarAdapter
import com.taiwan.justvet.justpet.data.EventNotification
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.event.EditEventTagAdapter
import com.taiwan.justvet.justpet.family.FamilyAdapter
import com.taiwan.justvet.justpet.home.EventNotificationAdapter
import com.taiwan.justvet.justpet.tag.PetAvatarAdapter
import com.taiwan.justvet.justpet.tag.TagListAdapter

@BindingAdapter("iconSpecies")
fun bindSpeciesIcon(imageView: ImageView, species: Long) {
    species.let {
        when (it) {
            0L -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_cat
                    ))
            }
            1L -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_dog
                    ))
            }
        }
    }
}


@BindingAdapter("notificationBackground")
fun bindEventBackground (cardView: CardView, eventType: Int) {
    eventType.let {
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

@BindingAdapter("notificationType")
fun bindEventTagIcon (imageView: ImageView, type: Int) {
    type.let {
        when (it) {
            // normal
            0 -> {
                imageView.setImageDrawable(
                    Util.getDrawable(R.drawable.ic_others))
            }
            // medicine
            1 -> {
                imageView.setImageDrawable(
                    Util.getDrawable(R.drawable.ic_medicine))
            }
            // warning
            2 -> {
                imageView.setImageDrawable(
                    Util.getDrawable(R.drawable.ic_warning))
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
                        R.drawable.ic_expand_less
                    ))
            }
            else -> {
                imageView.setImageDrawable(
                    JustPetApplication.appContext.getDrawable(
                        R.drawable.ic_expand_more
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
                is EditEventTagAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("listOfEvents")
fun bindRecyclerViewWithListOfPetEvents(recyclerView: RecyclerView, list: List<PetEvent>?) {
    list?.let {
        recyclerView.adapter?.apply {
            when (this) {
                is CalendarEventAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("listOfNotification")
fun bindRecyclerViewWithListOfNotification(recyclerView: RecyclerView, list: List<EventNotification>?) {
    list?.let {
        recyclerView.adapter?.apply {
            when (this) {
                is EventNotificationAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("listOfProfile")
fun bindRecyclerViewWithListOfProfile(recyclerView: RecyclerView, list: List<PetProfile>?) {
    list?.let {
        recyclerView.adapter?.apply {
            when (this) {
                is PetAvatarAdapter -> {
                    when (itemCount) {
                        0 -> submitList(it)
                        it.size -> notifyDataSetChanged()
                        else -> submitList(it)
                    }
                }
                is ChartPetAvatarAdapter -> {
                    when (itemCount) {
                        0 -> submitList(it)
                        it.size -> notifyDataSetChanged()
                        else -> submitList(it)
                    }
                }
            }
        }
    }
}

@BindingAdapter("listOfFamily")
fun bindRecyclerViewWithFamily(recyclerView: RecyclerView, list: List<String>?) {
    list?.let {
        recyclerView.adapter?.apply {
            when (this) {
                is FamilyAdapter -> {
                    submitList(it)
                }
            }
        }
    }
}

/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("userPhotoUrl")
fun bindImage(imgView: ImageView, imgUrl: Uri?) {
    imgUrl?.let {
        val imgUri = it.buildUpon().build()
        GlideApp.with(imgView.context)
            .load(imgUri)
            .circleCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
            )
            .into(imgView)
    }
}

@BindingAdapter("imageUrl")
fun bindImageWithUrlString(imgView: ImageView, imgUrl: String?) {
    if (imgUrl != null) {
        val imgUri = imgUrl.toUri().buildUpon().build()
        GlideApp.with(imgView.context)
            .load(imgUri)
//            .apply(
//                RequestOptions()
//                    .placeholder(R.drawable.image_cat)
//            )
            .into(imgView)
    } else {
        GlideApp.with(imgView.context)
            .clear(imgView)
    }
}

@BindingAdapter("isSelected")
fun bindTagBackground(layout: ConstraintLayout, eventTag: EventTag) {
    if (eventTag.isSelected != true) {
        layout.background = JustPetApplication.appContext.getDrawable(R.drawable.background_white)
    } else {
        when (eventTag.type) {
            TagType.DIARY.value -> {
                layout.background = JustPetApplication.appContext.getDrawable(R.drawable.selected_icon_tag_green)
            }
            TagType.SYNDROME.value -> {
                layout.background = JustPetApplication.appContext.getDrawable(R.drawable.selected_icon_tag_red)
            }
            TagType.TREATMENT.value -> {
                layout.background = JustPetApplication.appContext.getDrawable(R.drawable.selected_icon_tag_yellow)
            }

        }
    }
}