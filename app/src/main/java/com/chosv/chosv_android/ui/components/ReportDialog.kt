package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chosv.chosv_android.data.model.ReportEntityType

/**
 * Dialog để người dùng nhập lý do báo cáo
 *
 * @param entityType Loại đối tượng đang báo cáo (User, Product, Comment)
 * @param entityName Tên hiển thị của đối tượng (tên người dùng, tên sản phẩm, ...)
 * @param isLoading Trạng thái đang gửi báo cáo
 * @param onDismiss Callback khi đóng dialog
 * @param onSubmit Callback khi gửi báo cáo, truyền lý do báo cáo
 */
@Composable
fun ReportDialog(
    entityType: ReportEntityType,
    entityName: String,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    val title = when (entityType) {
        ReportEntityType.User -> "Báo cáo người dùng"
        ReportEntityType.Product -> "Báo cáo sản phẩm"
        ReportEntityType.Comment -> "Báo cáo bình luận"
    }

    val description = when (entityType) {
        ReportEntityType.User -> "Bạn đang báo cáo người dùng \"$entityName\""
        ReportEntityType.Product -> "Bạn đang báo cáo sản phẩm \"$entityName\""
        ReportEntityType.Comment -> "Bạn đang báo cáo bình luận của \"$entityName\""
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lý do báo cáo") },
                    placeholder = { Text("Nhập lý do báo cáo...") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason) },
                enabled = reason.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi báo cáo")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}

