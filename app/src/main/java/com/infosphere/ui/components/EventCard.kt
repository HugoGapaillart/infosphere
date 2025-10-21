package com.infosphere.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Deux images carrées en colonne à gauche
            Column {
                AsyncImage(
                    model = event.photoUrls.getOrNull(0) ?: R.drawable.image_placeholder,
                    contentDescription = "Photo principale de l'événement",
                    modifier = Modifier
                        .size(imageSize)
                        .clip(cardShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = event.photoUrls.getOrNull(1) ?: R.drawable.image_placeholder,
                    contentDescription = "Photo secondaire de l'événement",
                    modifier = Modifier
                        .size(imageSize)
                        .clip(cardShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Détails à droite
            Column(
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                val dateFormat = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
                Text(
                    text = dateFormat.format(event.date.toDate()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location
                Text(
                    text = "${event.location} • ${event.cityName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onClick = { },
                            label = { Text(typeName) }
                        )
                    }
                }
            }
        }
    }
}