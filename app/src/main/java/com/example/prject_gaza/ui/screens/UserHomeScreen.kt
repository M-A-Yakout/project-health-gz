package com.example.prject_gaza.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prject_gaza.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// Color scheme consistent with previous screens
private val PrimaryColor = Color(0xFF0288D1) // Vibrant Blue
private val SecondaryColor = Color(0xFF4FC3F7) // Light Blue
private val AccentColor = Color(0xFFFFA726) // Warm Orange
private val SurfaceColor = Color(0xFFFFFFFF) // Pure White
private val ErrorColor = Color(0xFFE57373) // Light Red for errors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val TAG = "UserHomeScreen"

    // Form fields
    var ptName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var idNo by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var natureOfCase by remember { mutableStateOf(emptyList<String>()) }
    var jointsAffected by remember { mutableStateOf(emptyList<String>()) }
    var indexJoint by remember { mutableStateOf("") }
    var side by remember { mutableStateOf("Left") }
    var rrVs1 by remember { mutableStateOf("") }
    var rrVs2 by remember { mutableStateOf("") }
    var rrVs3 by remember { mutableStateOf("") }
    var bpVs1 by remember { mutableStateOf("") }
    var bpVs2 by remember { mutableStateOf("") }
    var bpVs3 by remember { mutableStateOf("") }
    var prVs1 by remember { mutableStateOf("") }
    var prVs2 by remember { mutableStateOf("") }
    var prVs3 by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var spO2 by remember { mutableStateOf("") }
    var bs by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf(emptyList<String>()) }
    var chiefComplaint by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var pdfFileUri by remember { mutableStateOf<Uri?>(null) }
    var showPdfActions by remember { mutableStateOf(false) }

    // Real-time user status
    var userStatus by remember { mutableStateOf("pending") }
    val userListener = remember { mutableStateOf<ListenerRegistration?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            userListener.value = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed for user status: ${e.message}", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        userStatus = snapshot.getString("status") ?: "pending"
                        if (userStatus == "accepted") {
                            scope.launch {
                                snackbarHostState.showSnackbar("Account approved! You can now submit cases / تمت الموافقة على الحساب!")
                            }
                        }
                    }
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            userListener.value?.remove()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "User Home / الصفحة الرئيسية للمستخدم",
                        color = SurfaceColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor),
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
        containerColor = Color(0xFFF5F7FA)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (userStatus == "accepted") {
                        // Form Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceColor)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Patient Information
                            Text(
                                text = "Patient Information / معلومات المريض",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = ptName,
                                onValueChange = { ptName = it },
                                label = { Text("PT Name / اسم المريض", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address / العنوان", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = phoneNo,
                                onValueChange = { phoneNo = it },
                                label = { Text("Phone No / رقم الهاتف", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age / العمر", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = idNo,
                                onValueChange = { idNo = it },
                                label = { Text("ID No / رقم الهوية", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (gender == "Male"),
                                        onClick = { gender = "Male" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Text("Male / ذكر", color = Color.Black)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (gender == "Female"),
                                        onClick = { gender = "Female" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Text("Female / أنثى", color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Nature of Case
                            Text(
                                text = "Nature of Case / طبيعة الحالة",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CheckboxRow("Medical / طبي", natureOfCase) { checked ->
                                natureOfCase = if (checked) natureOfCase + "Medical" else natureOfCase - "Medical"
                            }
                            CheckboxRow("Trauma / صدمة", natureOfCase) { checked ->
                                natureOfCase = if (checked) natureOfCase + "Trauma" else natureOfCase - "Trauma"
                            }
                            CheckboxRow("Psychologist / نفسي", natureOfCase) { checked ->
                                natureOfCase = if (checked) natureOfCase + "Psychologist" else natureOfCase - "Psychologist"
                            }
                            CheckboxRow("Delivery / ولادة", natureOfCase) { checked ->
                                natureOfCase = if (checked) natureOfCase + "Delivery" else natureOfCase - "Delivery"
                            }
                            CheckboxRow("Pediatric / طب الأطفال", natureOfCase) { checked ->
                                natureOfCase = if (checked) natureOfCase + "Pediatric" else natureOfCase - "Pediatric"
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Joints Affected
                            Text(
                                text = "Joints Affected / المفاصل المصابة",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CheckboxRow("Hip / ورك", jointsAffected) { checked ->
                                jointsAffected = if (checked) jointsAffected + "Hip" else jointsAffected - "Hip"
                            }
                            CheckboxRow("Knee / ركبة", jointsAffected) { checked ->
                                jointsAffected = if (checked) jointsAffected + "Knee" else jointsAffected - "Knee"
                            }
                            CheckboxRow("Shoulder / كتف", jointsAffected) { checked ->
                                jointsAffected = if (checked) jointsAffected + "Shoulder" else jointsAffected - "Shoulder"
                            }
                            CheckboxRow("Wrist / معصم", jointsAffected) { checked ->
                                jointsAffected = if (checked) jointsAffected + "Wrist" else jointsAffected - "Wrist"
                            }
                            CheckboxRow("Neck / رقبة", jointsAffected) { checked ->
                                jointsAffected = if (checked) jointsAffected + "Neck" else jointsAffected - "Neck"
                            }

                            OutlinedTextField(
                                value = indexJoint,
                                onValueChange = { indexJoint = it },
                                label = { Text("Index Joint / المفصل المؤشر", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (side == "Left"),
                                        onClick = { side = "Left" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Text("Left / يسار", color = Color.Black)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (side == "Right"),
                                        onClick = { side = "Right" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Text("Right / يمين", color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Vital Signs
                            Text(
                                text = "Vital Signs / العلامات الحيوية",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            VitalSignsRow("RR", rrVs1, rrVs2, rrVs3, { rrVs1 = it }, { rrVs2 = it }, { rrVs3 = it }, isLoading)
                            VitalSignsRow("BP", bpVs1, bpVs2, bpVs3, { bpVs1 = it }, { bpVs2 = it }, { bpVs3 = it }, isLoading)
                            VitalSignsRow("PR", prVs1, prVs2, prVs3, { prVs1 = it }, { prVs2 = it }, { prVs3 = it }, isLoading)

                            OutlinedTextField(
                                value = temp,
                                onValueChange = { temp = it },
                                label = { Text("Temp / درجة الحرارة", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = spO2,
                                onValueChange = { spO2 = it },
                                label = { Text("SpO2 / تشبع الأكسجين", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = bs,
                                onValueChange = { bs = it },
                                label = { Text("BS / السكر في الدم", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Medical History
                            Text(
                                text = "Medical History / التاريخ الطبي",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CheckboxRow("Diabetes / سكري", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Diabetes" else medicalHistory - "Diabetes"
                            }
                            CheckboxRow("COPD / مرض الانسداد الرئوي", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "COPD" else medicalHistory - "COPD"
                            }
                            CheckboxRow("Cardiac / قلبي", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Cardiac" else medicalHistory - "Cardiac"
                            }
                            CheckboxRow("Seizure / نوبة", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Seizure" else medicalHistory - "Seizure"
                            }
                            CheckboxRow("Hypertension / ارتفاع ضغط الدم", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Hypertension" else medicalHistory - "Hypertension"
                            }
                            CheckboxRow("Hypotension / انخفاض ضغط الدم", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Hypotension" else medicalHistory - "Hypotension"
                            }
                            CheckboxRow("Cancer / سرطان", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Cancer" else medicalHistory - "Cancer"
                            }
                            CheckboxRow("Covid19 / كوفيد-19", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Covid19" else medicalHistory - "Covid19"
                            }
                            CheckboxRow("Other / أخرى", medicalHistory) { checked ->
                                medicalHistory = if (checked) medicalHistory + "Other" else medicalHistory - "Other"
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Chief Complaint
                            Text(
                                text = "Chief Complaint / الشكوى الرئيسية",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = chiefComplaint,
                                onValueChange = { chiefComplaint = it },
                                label = { Text("Chief Complaint / الشكوى الرئيسية", color = PrimaryColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                maxLines = 3,
                                colors = textFieldColors()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val caseData = mapOf(
                                                "ptName" to (ptName.takeIf { it.isNotEmpty() } ?: ""),
                                                "address" to (address.takeIf { it.isNotEmpty() } ?: ""),
                                                "phoneNo" to (phoneNo.takeIf { it.isNotEmpty() } ?: ""),
                                                "age" to (age.takeIf { it.isNotEmpty() } ?: ""),
                                                "idNo" to (idNo.takeIf { it.isNotEmpty() } ?: ""),
                                                "gender" to gender,
                                                "natureOfCase" to natureOfCase,
                                                "jointsAffected" to jointsAffected,
                                                "indexJoint" to (indexJoint.takeIf { it.isNotEmpty() } ?: ""),
                                                "side" to side,
                                                "rrVs1" to (rrVs1.takeIf { it.isNotEmpty() } ?: ""),
                                                "rrVs2" to (rrVs2.takeIf { it.isNotEmpty() } ?: ""),
                                                "rrVs3" to (rrVs3.takeIf { it.isNotEmpty() } ?: ""),
                                                "bpVs1" to (bpVs1.takeIf { it.isNotEmpty() } ?: ""),
                                                "bpVs2" to (bpVs2.takeIf { it.isNotEmpty() } ?: ""),
                                                "bpVs3" to (bpVs3.takeIf { it.isNotEmpty() } ?: ""),
                                                "prVs1" to (prVs1.takeIf { it.isNotEmpty() } ?: ""),
                                                "prVs2" to (prVs2.takeIf { it.isNotEmpty() } ?: ""),
                                                "prVs3" to (prVs3.takeIf { it.isNotEmpty() } ?: ""),
                                                "temp" to (temp.takeIf { it.isNotEmpty() } ?: ""),
                                                "spO2" to (spO2.takeIf { it.isNotEmpty() } ?: ""),
                                                "bs" to (bs.takeIf { it.isNotEmpty() } ?: ""),
                                                "medicalHistory" to medicalHistory,
                                                "chiefComplaint" to (chiefComplaint.takeIf { it.isNotEmpty() } ?: ""),
                                                "userId" to (currentUser?.uid ?: ""),
                                                "timestamp" to System.currentTimeMillis(),
                                                "status" to "pending"
                                            )
                                            val documentRef = FirebaseFirestore.getInstance()
                                                .collection("cases")
                                                .add(caseData)
                                                .await()
                                            val caseId = documentRef.id
                                            authRepository.submitCase(caseData).getOrThrow()
                                            pdfFileUri = withContext(Dispatchers.IO) {
                                                saveAsPdf(context, caseData, caseId)
                                            }
                                            showPdfActions = true
                                            snackbarHostState.showSnackbar("Case submitted successfully / تم تقديم الحالة بنجاح")
                                            // Reset form
                                            ptName = ""; address = ""; phoneNo = ""; age = ""; idNo = ""
                                            gender = "Male"; natureOfCase = emptyList(); jointsAffected = emptyList()
                                            indexJoint = ""; side = "Left"; rrVs1 = ""; rrVs2 = ""; rrVs3 = ""
                                            bpVs1 = ""; bpVs2 = ""; bpVs3 = ""; prVs1 = ""; prVs2 = ""; prVs3 = ""
                                            temp = ""; spO2 = ""; bs = ""; medicalHistory = emptyList(); chiefComplaint = ""
                                        } catch (e: Exception) {
                                            error = "Failed to submit case: ${e.message} / فشل في تقديم الحالة: ${e.message}"
                                            Log.e(TAG, "Submission error: ${e.message}", e)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceColor)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = "Submit", tint = SurfaceColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Case / تقديم الحالة", color = SurfaceColor, fontSize = 18.sp)
                                }
                            }
                        }

                        // PDF Actions
                        AnimatedVisibility(
                            visible = showPdfActions && pdfFileUri != null,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceColor)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Submission Successful / تم التقديم بنجاح",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { pdfFileUri?.let { openPdf(context, it) } },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
                                ) {
                                    Icon(Icons.Default.Attachment, contentDescription = "Save as PDF", tint = SurfaceColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save as PDF", color = SurfaceColor, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { pdfFileUri?.let { sendEmail(context, it) } },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Send to Email", tint = SurfaceColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send to Email", color = SurfaceColor, fontSize = 18.sp)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceColor)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your account is $userStatus. Please wait for admin approval / حسابك $userStatus، انتظر موافقة الإدارة",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ErrorColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Error Message
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        error?.let {
                            Text(
                                text = it,
                                color = ErrorColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout Button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                    ) {
                        Text("Logout / تسجيل الخروج", color = SurfaceColor, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

// Reusable TextField Colors
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryColor,
    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
    cursorColor = PrimaryColor,
    focusedLabelColor = PrimaryColor,
    unfocusedTextColor = Color.Black
)

// Reusable Checkbox Row
@Composable
private fun CheckboxRow(
    label: String,
    checkedList: List<String>,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = label.split(" / ")[0] in checkedList,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

// Reusable Vital Signs Row
@Composable
private fun VitalSignsRow(
    label: String,
    vs1: String,
    vs2: String,
    vs3: String,
    onVs1Change: (String) -> Unit,
    onVs2Change: (String) -> Unit,
    onVs3Change: (String) -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = Color.Black,
            fontSize = 16.sp
        )
        OutlinedTextField(
            value = vs1,
            onValueChange = onVs1Change,
            label = { Text("VS1", color = PrimaryColor.copy(alpha = 0.7f)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = vs2,
            onValueChange = onVs2Change,
            label = { Text("VS2", color = PrimaryColor.copy(alpha = 0.7f)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = vs3,
            onValueChange = onVs3Change,
            label = { Text("VS3", color = PrimaryColor.copy(alpha = 0.7f)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors()
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

// PDF Saving Function
private fun saveAsPdf(context: Context, caseData: Map<String, Any>, caseId: String): Uri? {
    return try {
        val fileName = "Case_$caseId.pdf"
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { contentResolver.openOutputStream(it) }?.use { outputStream ->
                PdfWriter(outputStream).use { writer ->
                    PdfDocument(writer).use { pdfDocument ->
                        Document(pdfDocument).use { document ->
                            document.add(Paragraph("Patient Case Report / تقرير حالة المريض"))
                            document.add(Paragraph("Case ID: $caseId"))
                            document.add(Paragraph("Patient Name: ${caseData["ptName"]}"))
                            document.add(Paragraph("Address: ${caseData["address"]}"))
                            document.add(Paragraph("Phone No: ${caseData["phoneNo"]}"))
                            document.add(Paragraph("Age: ${caseData["age"]}"))
                            document.add(Paragraph("ID No: ${caseData["idNo"]}"))
                            document.add(Paragraph("Gender: ${caseData["gender"]}"))
                            document.add(Paragraph("Nature of Case: ${caseData["natureOfCase"]}"))
                            document.add(Paragraph("Joints Affected: ${caseData["jointsAffected"]}"))
                            document.add(Paragraph("Index Joint: ${caseData["indexJoint"]}"))
                            document.add(Paragraph("Side: ${caseData["side"]}"))
                            document.add(Paragraph("RR: ${caseData["rrVs1"]}, ${caseData["rrVs2"]}, ${caseData["rrVs3"]}"))
                            document.add(Paragraph("BP: ${caseData["bpVs1"]}, ${caseData["bpVs2"]}, ${caseData["bpVs3"]}"))
                            document.add(Paragraph("PR: ${caseData["prVs1"]}, ${caseData["prVs2"]}, ${caseData["prVs3"]}"))
                            document.add(Paragraph("Temp: ${caseData["temp"]}"))
                            document.add(Paragraph("SpO2: ${caseData["spO2"]}"))
                            document.add(Paragraph("BS: ${caseData["bs"]}"))
                            document.add(Paragraph("Medical History: ${caseData["medicalHistory"]}"))
                            document.add(Paragraph("Chief Complaint: ${caseData["chiefComplaint"]}"))
                        }
                    }
                }
            }
            uri
        } else {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            FileOutputStream(file).use { outputStream ->
                PdfWriter(outputStream).use { writer ->
                    PdfDocument(writer).use { pdfDocument ->
                        Document(pdfDocument).use { document ->
                            document.add(Paragraph("Patient Case Report / تقرير حالة المريض"))
                            document.add(Paragraph("Case ID: $caseId"))
                            document.add(Paragraph("Patient Name: ${caseData["ptName"]}"))
                            document.add(Paragraph("Address: ${caseData["address"]}"))
                            document.add(Paragraph("Phone No: ${caseData["phoneNo"]}"))
                            document.add(Paragraph("Age: ${caseData["age"]}"))
                            document.add(Paragraph("ID No: ${caseData["idNo"]}"))
                            document.add(Paragraph("Gender: ${caseData["gender"]}"))
                            document.add(Paragraph("Nature of Case: ${caseData["natureOfCase"]}"))
                            document.add(Paragraph("Joints Affected: ${caseData["jointsAffected"]}"))
                            document.add(Paragraph("Index Joint: ${caseData["indexJoint"]}"))
                            document.add(Paragraph("Side: ${caseData["side"]}"))
                            document.add(Paragraph("RR: ${caseData["rrVs1"]}, ${caseData["rrVs2"]}, ${caseData["rrVs3"]}"))
                            document.add(Paragraph("BP: ${caseData["bpVs1"]}, ${caseData["bpVs2"]}, ${caseData["bpVs3"]}"))
                            document.add(Paragraph("PR: ${caseData["prVs1"]}, ${caseData["prVs2"]}, ${caseData["prVs3"]}"))
                            document.add(Paragraph("Temp: ${caseData["temp"]}"))
                            document.add(Paragraph("SpO2: ${caseData["spO2"]}"))
                            document.add(Paragraph("BS: ${caseData["bs"]}"))
                            document.add(Paragraph("Medical History: ${caseData["medicalHistory"]}"))
                            document.add(Paragraph("Chief Complaint: ${caseData["chiefComplaint"]}"))
                        }
                    }
                }
            }
            Uri.fromFile(file)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Utility functions to open and send PDF
private fun openPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF with"))
}

private fun sendEmail(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Patient Case Report")
        putExtra(Intent.EXTRA_TEXT, "Please find the attached patient case report.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Send email with"))
}