package com.infosphere.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.infosphere.R
import com.infosphere.models.Event
import com.infosphere.models.EventType
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventCard(
    event: Event,
    eventTypes: List<EventType>,
    onClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageSize = 100.dp
    val cardPadding = 16.dp
    val cardElevation = 4.dp
    val cardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = cardPadding, vertical = cardPadding / 2)
            .clickable { onClick(event) },
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column (modifier = Modifier.padding(cardPadding,0.dp, 0.dp, 0.dp)){
                AsyncImage(
                    model = event.photoUrls.getOrNull(0) ?: R.drawable.image_placeholder,
                    contentDescription = "Photo principale de l'événement",
                    modifier = Modifier
                        .size(imageSize)
                        .clip(cardShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = event.title,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                val dateFormat = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
                Text(
                    text = "Le ${dateFormat.format(event.date.toDate())} à ${event.cityName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Event Types
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    event.eventTypes.take(3).forEach { typeId ->
                        val typeName = eventTypes.find { it.id == typeId }?.name ?: typeId
                        AssistChip(
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White,
                            ),
                            onClick = { },
                            label = { Text(typeName, modifier = Modifier.background(color = Color.White)) }
                        )
                    }
                }
            }
        }
    }
}