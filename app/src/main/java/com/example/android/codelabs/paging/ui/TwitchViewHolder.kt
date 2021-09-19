/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.codelabs.paging.R
import com.example.android.codelabs.paging.model.TwitchItem
import com.squareup.picasso.Picasso


class TwitchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.twitch_name)
    private val game_logo: ImageView = view.findViewById(R.id.game_logo)


    fun bind(twitchItem: TwitchItem?) {
        if (twitchItem == null) {
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
        } else {
            showTwitchData(twitchItem)
        }
    }

    private fun showTwitchData(twitchItem: TwitchItem) {
        name.text = twitchItem.name
        Picasso.get().load(twitchItem.box_art_url.replace("{width}", "100").replace("{height}","150")).into(game_logo)

    }

    companion object {
        fun create(parent: ViewGroup): TwitchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.twitch_view_item, parent, false)
            return TwitchViewHolder(view)
        }
    }
}
