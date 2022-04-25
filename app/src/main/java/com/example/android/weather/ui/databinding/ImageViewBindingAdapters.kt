package com.example.android.weather.ui.databinding

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.weather.R
import java.io.File

@BindingAdapter("imageResource")
fun setImage(imageView: ImageView, imageUri: Uri?) {
    Glide.with(imageView.rootView.context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )
        .load(imageUri)
        .into(imageView)
}

@BindingAdapter("imageResource")
fun setImage(imageView: ImageView, imageUrl: String?) {
    Glide.with(imageView.rootView.context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )
        .load(imageUrl)
        .into(imageView)
}

@BindingAdapter("imageResource")
fun setImage(imageView: ImageView, imageFile: File?) {
    Glide.with(imageView.rootView.context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )
        .load(imageFile)
        .into(imageView)
}

@BindingAdapter("imageResource")
fun setImage(imageView: ImageView, imageResourceId: Int) {
    Glide.with(imageView.rootView.context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )
        .load(imageResourceId)
        .into(imageView)
}

@BindingAdapter("imageResource")
fun setImage(imageView: ImageView, imageDrawable: Drawable?) {
    Glide.with(imageView.rootView.context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )
        .load(imageDrawable)
        .into(imageView)
}
