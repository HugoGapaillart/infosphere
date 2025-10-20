package com.infosphere.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.infosphere.R
import com.infosphere.models.Event
import com.infosphere.models.EventType
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(
    private val onEventClick: (Event) -> Unit,
    private var eventTypes: MutableList<EventType> = mutableListOf()
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view, onEventClick, eventTypes)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateEventTypes(newEventTypes: List<EventType>) {
        eventTypes.clear()
        eventTypes.addAll(newEventTypes)
        notifyDataSetChanged() // Reload all items to reflect new types
    }
}

class EventViewHolder(
    itemView: View,
    private val onEventClick: (Event) -> Unit,
    private val eventTypes: List<EventType>
) : RecyclerView.ViewHolder(itemView) {
    
    private val ivEventPhoto: ImageView = itemView.findViewById(R.id.iv_event_photo)
    private val tvEventTitle: TextView = itemView.findViewById(R.id.tv_event_title)
    private val tvEventDate: TextView = itemView.findViewById(R.id.tv_event_date)
    private val tvEventLocation: TextView = itemView.findViewById(R.id.tv_event_location)
    private val tvEventDescription: TextView = itemView.findViewById(R.id.tv_event_description)
    private val chipGroupTypes: ChipGroup = itemView.findViewById(R.id.chip_group_types)

    fun bind(event: Event) {
        tvEventTitle.text = event.title
        tvEventDescription.text = event.description
        tvEventLocation.text = "${event.location} • ${event.cityName}"
        
        // Format date
        val dateFormat = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
        tvEventDate.text = dateFormat.format(event.date.toDate())
        
        // Load first photo or placeholder
        if (event.photoUrls.isNotEmpty()) {
            ivEventPhoto.load(event.photoUrls.first()) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.color.darker_gray)
            }
        } else {
            ivEventPhoto.setImageResource(android.R.color.darker_gray)
        }
        
        // Display event types
        chipGroupTypes.removeAllViews()
        event.eventTypes.forEach { typeId ->
            val typeName = eventTypes.find { it.id == typeId }?.name ?: typeId
            val chip = Chip(itemView.context).apply {
                text = typeName
                isClickable = false
                isCheckable = false
            }
            chipGroupTypes.addView(chip)
        }
        
        itemView.setOnClickListener { onEventClick(event) }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
