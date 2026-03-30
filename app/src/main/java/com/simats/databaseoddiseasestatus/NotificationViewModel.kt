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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

class NotificationViewModel : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsResult>(NotificationsResult.Idle)
    val notificationsState: StateFlow<NotificationsResult> = _notificationsState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun fetchNotifications() {
        _notificationsState.value = NotificationsResult.Loading
        viewModelScope.launch {
            try {
                ApiClient.instance.getNotifications().enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val type = object : TypeToken<List<Notification>>() {}.type
                                val serverNotifications: List<Notification> = Gson().fromJson(jsonString, type)
                                
                                serverNotifications.forEach { sn ->
                                    if (globalNotifications.none { it.id == sn.id }) {
                                        globalNotifications.add(sn)
                                    }
                                }
                                _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                            } catch (e: Exception) {
                                Log.e("NotificationViewModel", "Parsing error", e)
                                // Fallback to local notifications
                                _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                            }
                        } else {
                            // Fallback to local notifications on server error
                            _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("NotificationViewModel", "Network error, showing local notifications", t)
                        // Fallback to local notifications on network error
                        _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                    }
                })
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Fetch notifications launch error", e)
                _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            try {
                ApiClient.instance.getUnreadCount().enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val type = object : TypeToken<Map<String, Int>>() {}.type
                                val result: Map<String, Int> = Gson().fromJson(jsonString, type)
                                _unreadCount.value = result["unread_count"] ?: 0
                            } catch (e: Exception) {
                                Log.e("NotificationViewModel", "Parsing error", e)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("NotificationViewModel", "Failed to fetch unread count: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Unread count launch error", e)
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        // If it's a local notification, remove from list to erase from screen
        if (notificationId > 1000000) {
            globalNotifications.removeAll { it.id == notificationId }
            _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
            return
        }

        viewModelScope.launch {
            try {
                ApiClient.instance.markAsRead(notificationId).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            fetchUnreadCount()
                            // Erase from screen after marking as read on server
                            globalNotifications.removeAll { it.id == notificationId }
                            _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("NotificationViewModel", "Failed to mark as read: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Mark as read launch error", e)
            }
        }
    }

    fun deleteNotification(notificationId: Int) {
        if (notificationId > 1000000) {
            globalNotifications.removeAll { it.id == notificationId }
            _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
            return
        }

        viewModelScope.launch {
            try {
                ApiClient.instance.deleteNotification(notificationId).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            globalNotifications.removeAll { it.id == notificationId }
                            _notificationsState.value = NotificationsResult.Success(globalNotifications.toList())
                            fetchUnreadCount()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("NotificationViewModel", "Failed to delete notification: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Delete notification launch error", e)
            }
        }
    }
}

sealed class NotificationsResult {
    object Idle : NotificationsResult()
    object Loading : NotificationsResult()
    data class Success(val notifications: List<Notification>) : NotificationsResult()
    data class Error(val message: String) : NotificationsResult()
}
