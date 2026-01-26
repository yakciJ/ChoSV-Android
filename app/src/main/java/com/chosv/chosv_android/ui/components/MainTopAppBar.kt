package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.data.model.NotificationItem
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    onNavigationIconClick: () -> Unit,
    onSearchActionClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as ChoSVApplication
    val notificationRepository = application.container.notificationRepository
    val notificationHubService = application.container.notificationHubService

    val scope = rememberCoroutineScope()

    var unreadCount by remember { mutableIntStateOf(0) }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var showNotificationDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Lắng nghe thông báo realtime từ SignalR
    val incomingNotification by notificationHubService.incomingNotifications.collectAsState(initial = null)

    // Kết nối SignalR và lấy số lượng thông báo chưa đọc khi khởi tạo
    LaunchedEffect(Unit) {
        // Kết nối SignalR
        try {
            notificationHubService.connect()
        } catch (_: Exception) {
            // Ignore connection errors
        }

        // Lấy số lượng thông báo chưa đọc
        try {
            unreadCount = notificationRepository.getUnreadCount()
        } catch (_: Exception) {
            unreadCount = 0
        }
    }

    // Khi nhận được thông báo mới từ SignalR, tăng unreadCount
    LaunchedEffect(incomingNotification) {
        incomingNotification?.let {
            unreadCount++
            // Thêm vào đầu danh sách nếu dropdown đang mở
            if (showNotificationDropdown) {
                notifications = listOf(it) + notifications
            }
        }
    }

    TopAppBar(
        title = { Text("ChoSV") },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Drawer")
            }
        },
        actions = {
            IconButton(onClick = onSearchActionClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            // Notification Bell Icon with Badge
            Box {
                IconButton(onClick = {
                    showNotificationDropdown = !showNotificationDropdown
                    if (showNotificationDropdown) {
                        // Load notifications when dropdown opens
                        scope.launch {
                            isLoading = true
                            try {
                                val response = notificationRepository.getNotifications(pageSize = 50)
                                notifications = response.items
                                // Mark all as read
                                notificationRepository.markAllAsRead(notifications)
                                unreadCount = 0
                            } catch (_: Exception) {
                                // Ignore errors
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }

                // Badge showing unread count
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notification Dropdown
                DropdownMenu(
                    expanded = showNotificationDropdown,
                    onDismissRequest = { showNotificationDropdown = false },
                    modifier = Modifier
                        .width(320.dp)
                        .heightIn(max = 400.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Thông báo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có thông báo nào",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 350.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                notifications.forEach { notification ->
                                    NotificationItemRow(
                                        notification = notification,
                                        onDeleteClick = {
                                            scope.launch {
                                                try {
                                                    notificationRepository.deleteNotification(notification.notificationId)
                                                    notifications = notifications.filter {
                                                        it.notificationId != notification.notificationId
                                                    }
                                                } catch (_: Exception) {
                                                    // Ignore errors
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun NotificationItemRow(
    notification: NotificationItem,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message.ifEmpty { "Thông báo mới" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa thông báo",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Format thời gian thông báo thành dạng dễ đọc
 */
private fun formatNotificationTime(createdAt: String): String {
    return try {
        // Parse ISO 8601 format (e.g., "2025-10-31T17:33:45.653338Z")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

        // Remove microseconds and timezone suffix for parsing
        val cleanedDate = createdAt.substringBefore(".").let {
            if (it.length == createdAt.length) createdAt.substringBefore("Z") else it
        }

        val date = inputFormat.parse(cleanedDate) ?: return createdAt
        val now = Date()

        val diffMillis = now.time - date.time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        createdAt
    }
}

// --- KHỐI CODE PREVIEW ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopAppBarPreview() {
    ChoSVAndroidTheme {
        // Preview không hoạt động đầy đủ do cần ChoSVApplication
        TopAppBar(
            title = { Text("ChoSV") },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Menu, contentDescription = "Toggle Drawer")
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                Box {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "5",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}