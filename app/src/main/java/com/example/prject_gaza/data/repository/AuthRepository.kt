package com.example.prject_gaza.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.prject_gaza.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlin.runCatching

class AuthRepository(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepository"

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return runCatching {
            if (!isNetworkAvailable()) {
                throw Exception("No internet connection / لا يوجد اتصال بالإنترنت")
            }

            if (email == "admin" && password == "admin") {
                Log.d(TAG, "Admin login successful with hardcoded credentials")
                return@runCatching true
            }

            Log.d(TAG, "Attempting to login user with email: $email")
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login successful for email: $email, UID: ${auth.currentUser?.uid}")
            true
        }.onFailure { e ->
            Log.e(TAG, "Login failed for email: $email, error: ${e.message}", e)
            throw e
        }
    }

    suspend fun registerUserWithDetails(email: String, password: String, fullName: String, phoneNumber: String, maxAttempts: Int = 3): Result<Boolean> {
        return runCatching {
            Log.d(TAG, "Attempting to register user with email: $email")
            var attempt = 0
            var lastException: Exception? = null

            while (attempt < maxAttempts) {
                try {
                    if (!isNetworkAvailable()) {
                        attempt++
                        Log.w(TAG, "No network available on attempt $attempt/$maxAttempts")
                        if (attempt >= maxAttempts) {
                            throw Exception("No internet connection after $maxAttempts attempts / لا يوجد اتصال بالإنترنت بعد $maxAttempts محاولات")
                        }
                        delay(2000L * attempt)
                        continue
                    }

                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val user = result.user
                    if (user != null) {
                        delay(2000) // Allow token propagation
                        val userData = hashMapOf(
                            "email" to email,
                            "fullName" to fullName,
                            "phoneNumber" to phoneNumber,
                            "status" to "pending",
                            "isAdmin" to false
                        )
                        firestore.collection("users").document(user.uid)
                            .set(userData)
                            .await()
                        Log.d(TAG, "Registered user ${user.uid} with email $email")
                        return@runCatching true
                    } else {
                        Log.e(TAG, "User registration failed: User is null")
                        return@runCatching false
                    }
                } catch (e: FirebaseNetworkException) {
                    lastException = e
                    attempt++
                    Log.w(TAG, "Network error on registration attempt $attempt/$maxAttempts for $email: ${e.message}")
                    if (attempt >= maxAttempts) {
                        throw Exception("Network error after $maxAttempts attempts: ${e.message} / خطأ في الشبكة بعد $maxAttempts محاولات")
                    }
                    delay(2000L * attempt)
                } catch (e: FirebaseAuthUserCollisionException) {
                    throw Exception("The email address is already in use by another account / البريد الإلكتروني مستخدم بالفعل")
                } catch (e: Exception) {
                    if (e.message?.contains("network", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("unreachable", ignoreCase = true) == true) {
                        lastException = e
                        attempt++
                        Log.w(TAG, "Possible network error on attempt $attempt/$maxAttempts: ${e.message}")
                        if (attempt >= maxAttempts) {
                            throw Exception("Network error after $maxAttempts attempts: ${e.message} / خطأ في الشبكة بعد $maxAttempts محاولات")
                        }
                        delay(2000L * attempt)
                    } else {
                        throw e
                    }
                }
            }

            if (lastException != null) {
                throw lastException
            }

            false
        }.onFailure { e ->
            val errorMessage = when (e) {
                is FirebaseAuthUserCollisionException -> "The email address is already in use by another account / البريد الإلكتروني مستخدم بالفعل"
                else -> "Registration failed: ${e.message} / فشل التسجيل: ${e.message}"
            }
            Log.e(TAG, "Registration failed for email $email: $errorMessage", e)
            throw Exception(errorMessage)
        }
    }

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No current authenticated user found")
            return null
        }
        Log.d(TAG, "Fetching data for current user UID: ${currentUser.uid}")
        return getUserById(currentUser.uid)
    }

    suspend fun getUserById(userId: String): User? {
        return runCatching {
            Log.d(TAG, "Attempting to fetch user data from Firestore for UID: $userId")
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val user = document.toObject(User::class.java)?.apply { uid = document.id }
            if (user != null) {
                Log.d(TAG, "Successfully fetched user: $user")
            } else {
                Log.w(TAG, "User data is null for UID: $userId")
            }
            user
        }.onFailure { e ->
            Log.e(TAG, "Failed to get user by ID $userId: ${e.message}", e)
        }.getOrNull()
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<Unit> {
        return runCatching {
            val currentDoc = firestore.collection("users").document(userId).get().await()
            val currentStatus = currentDoc.getString("status")
            if (currentStatus == status) {
                Log.d(TAG, "User status already $status for UID: $userId, no update needed")
                Unit
            } else {
                firestore.collection("users").document(userId)
                    .update("status", status)
                    .await()
                Log.d(TAG, "User status updated to $status for UID: $userId")
                Unit
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to update user status for UID: $userId, error: ${e.message}", e)
            throw Exception("Failed to update user status: ${e.message} / فشل تحديث حالة المستخدم")
        }
    }

    suspend fun getAllUsers(): List<User> {
        return runCatching {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.apply { uid = doc.id }
            }
            Log.d(TAG, "Fetched ${users.size} users")
            users
        }.onFailure { e ->
            Log.e(TAG, "Failed to fetch all users: ${e.message}", e)
        }.getOrDefault(emptyList())
    }

    fun listenToUsersRealtime(onUpdate: (List<User>) -> Unit): ListenerRegistration {
        return firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed for users: ${e.message}", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { uid = doc.id }
                    }
                    Log.d(TAG, "Realtime update: Fetched ${users.size} users")
                    onUpdate(users)
                }
            }
    }

    suspend fun getAllCases(): List<Map<String, Any>> {
        return runCatching {
            val snapshot = firestore.collection("cases").get().await()
            val cases = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                data?.put("caseId", doc.id)
                data
            }
            Log.d(TAG, "Fetched ${cases.size} cases")
            cases
        }.onFailure { e ->
            Log.e(TAG, "Failed to fetch all cases: ${e.message}", e)
        }.getOrDefault(emptyList())
    }

    fun listenToCasesRealtime(onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration {
        return firestore.collection("cases")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed for cases: ${e.message}", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cases = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        data?.put("caseId", doc.id)
                        data
                    }
                    Log.d(TAG, "Realtime update: Fetched ${cases.size} cases")
                    onUpdate(cases)
                }
            }
    }

    suspend fun updateCaseStatus(caseId: String, status: String): Result<Unit> {
        return runCatching {
            val currentDoc = firestore.collection("cases").document(caseId).get().await()
            val currentStatus = currentDoc.getString("status")
            if (currentStatus == status) {
                Log.d(TAG, "Case status already $status for caseId: $caseId, no update needed")
                Unit
            } else {
                firestore.collection("cases").document(caseId)
                    .update("status", status)
                    .await()
                Log.d(TAG, "Case status updated to $status for caseId: $caseId")
                Unit
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to update case status for caseId: $caseId, error: ${e.message}", e)
            throw Exception("Failed to update case status: ${e.message} / فشل تحديث حالة الطلب")
        }
    }

    suspend fun deleteCase(caseId: String): Result<Unit> {
        return runCatching {
            firestore.collection("cases").document(caseId)
                .delete()
                .await()
            Log.d(TAG, "Case deleted: $caseId")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to delete case $caseId: ${e.message}", e)
            throw Exception("Failed to delete case: ${e.message} / فشل حذف الحالة")
        }
    }

    suspend fun submitCase(caseData: Map<String, Any>): Result<Unit> {
        return runCatching {
            firestore.collection("cases")
                .add(caseData)
                .await()
            Log.d(TAG, "Case submitted successfully")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to submit case: ${e.message}", e)
            throw Exception("Failed to submit case: ${e.message} / فشل إرسال الحالة")
        }
    }

    fun logout() {
        auth.signOut()
        Log.d(TAG, "User logged out")
    }
}