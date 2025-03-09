package com.example.prject_gaza.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prject_gaza.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// نفس الألوان المستخدمة في الشاشات الأخرى
private val PrimaryColor = Color(0xFF0288D1) // أزرق نابض
private val SecondaryColor = Color(0xFF4FC3F7) // أزرق فاتح
private val AccentColor = Color(0xFFFFA726) // برتقالي دافئ
private val SurfaceColor = Color(0xFFFFFFFF) // أبيض نقي
private val ErrorColor = Color(0xFFE57373) // أحمر فاتح للأخطاء

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val TAG = "AdminPanelScreen"

    // الحالة للمستخدمين والحالات مع مؤشرات التحميل
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var cases by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // تحميل البيانات مرة واحدة عند تهيئة الشاشة
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val usersSnapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("status", "pending")
                    .limit(10)
                    .get()
                    .await()
                users = usersSnapshot.documents.mapNotNull { doc ->
                    doc.data?.plus("uid" to doc.id)
                }

                val casesSnapshot = FirebaseFirestore.getInstance()
                    .collection("cases")
                    .whereEqualTo("status", "pending")
                    .limit(10)
                    .get()
                    .await()
                cases = casesSnapshot.documents.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data: ${e.message}", e)
                errorMessage = "Failed to load data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Panel / لوحة الإدارة",
                        color = SurfaceColor,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor
                ),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(
                            "Logout / تسجيل الخروج",
                            color = AccentColor,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F7FA) // خلفية الشاشة
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryColor.copy(alpha = 0.15f),
                            SecondaryColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp),
                        color = PrimaryColor
                    )
                }
                errorMessage != null -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ErrorColor,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // قسم المستخدمين المعلقين
                        item {
                            Text(
                                text = "Pending Users / المستخدمون المعلقون",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (users.isEmpty()) {
                            item {
                                Text(
                                    text = "No pending users / لا يوجد مستخدمون معلقون",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        items(users, key = { it["uid"] as String }) { user ->
                            UserCard(
                                user = user,
                                onApprove = {
                                    scope.launch {
                                        try {
                                            authRepository.updateUserStatus(user["uid"] as String, "accepted")
                                            users = users.filter { it["uid"] != user["uid"] }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error approving user: ${e.message}", e)
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        try {
                                            authRepository.updateUserStatus(user["uid"] as String, "rejected")
                                            users = users.filter { it["uid"] != user["uid"] }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error rejecting user: ${e.message}", e)
                                        }
                                    }
                                }
                            )
                        }

                        // قسم الحالات المعلقة
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Pending Cases / الحالات المعلقة",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (cases.isEmpty()) {
                            item {
                                Text(
                                    text = "No pending cases / لا يوجد حالات معلقة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        items(cases, key = { it["id"] as String }) { case ->
                            CaseCard(
                                case = case,
                                onApprove = {
                                    scope.launch {
                                        try {
                                            val caseId = case["id"] as String
                                            FirebaseFirestore.getInstance()
                                                .collection("cases")
                                                .document(caseId)
                                                .update("status", "approved")
                                                .await()
                                            cases = cases.filter { it["id"] != case["id"] }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error approving case: ${e.message}", e)
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        try {
                                            val caseId = case["id"] as String
                                            FirebaseFirestore.getInstance()
                                                .collection("cases")
                                                .document(caseId)
                                                .update("status", "rejected")
                                                .await()
                                            cases = cases.filter { it["id"] != case["id"] }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error rejecting case: ${e.message}", e)
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

@Composable
fun UserCard(
    user: Map<String, Any>,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Full Name: ${user["fullName"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Text(
                text = "Email: ${user["email"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "Phone Number: ${user["phoneNumber"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "UID: ${user["uid"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        "Approve / موافقة",
                        color = SurfaceColor,
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text(
                        "Reject / رفض",
                        color = SurfaceColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CaseCard(
    case: Map<String, Any>,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Patient: ${case["ptName"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Text(
                text = "Case ID: ${case["id"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "Complaint: ${case["chiefComplaint"] ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        "Approve / موافقة",
                        color = SurfaceColor,
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text(
                        "Reject / رفض",
                        color = SurfaceColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}