package com.infosphere.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun CompactEventCard(
    event: Event,
    eventTypes: List<EventType>,
    onClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageSize = 72.dp
    val cardPadding = 8.dp
    val cardElevation = 2.dp
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
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Image carrée à gauche, 30% de la width
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = event.photoUrls.firstOrNull() ?: R.drawable.image_placeholder,
                    contentDescription = "Photo de l'événement",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(cardShape),
                    contentScale = ContentScale.Crop
                )
            }
            // Infos event centrées, 70% de la width
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(cardPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val dateFormat = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
                    Text(
                        text = dateFormat.format(event.date.toDate()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${event.location} • ${event.cityName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
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
}