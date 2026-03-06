package com.simats.databaseoddiseasestatus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsResult>(NotificationsResult.Idle)
    val notificationsState: StateFlow<NotificationsResult> = _notificationsState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun fetchNotifications() {
        _notificationsState.value = NotificationsResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getNotifications().enqueue(object : Callback<List<Notification>> {
                override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                    if (response.isSuccessful && response.body() != null) {
                        _notificationsState.value = NotificationsResult.Success(response.body()!!)
                        globalNotifications.clear()
                        globalNotifications.addAll(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to fetch notifications."
                        _notificationsState.value = NotificationsResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    _notificationsState.value = NotificationsResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            ApiClient.instance.getUnreadCount().enqueue(object : Callback<Map<String, Int>> {
                override fun onResponse(call: Call<Map<String, Int>>, response: Response<Map<String, Int>>) {
                    if (response.isSuccessful && response.body() != null) {
                        _unreadCount.value = response.body()!!["unread_count"] ?: 0
                    }
                }

                override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {
                    Log.e("NotificationViewModel", "Failed to fetch unread count: ${t.message}")
                }
            })
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            ApiClient.instance.markAsRead(notificationId).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        fetchUnreadCount()
                        // Update local state
                        val index = globalNotifications.indexOfFirst { it.id == notificationId }
                        if (index != -1) {
                            globalNotifications[index] = globalNotifications[index].copy(isRead = true)
                        }
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("NotificationViewModel", "Failed to mark as read: ${t.message}")
                }
            })
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            ApiClient.instance.deleteNotification(notificationId).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        globalNotifications.removeAll { it.id == notificationId }
                        fetchUnreadCount()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("NotificationViewModel", "Failed to delete notification: ${t.message}")
                }
            })
        }
    }
}

sealed class NotificationsResult {
    object Idle : NotificationsResult()
    object Loading : NotificationsResult()
    data class Success(val notifications: List<Notification>) : NotificationsResult()
    data class Error(val message: String) : NotificationsResult()
}
