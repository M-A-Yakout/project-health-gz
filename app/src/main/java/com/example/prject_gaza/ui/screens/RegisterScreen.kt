package com.example.prject_gaza.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prject_gaza.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// نفس الألوان المستخدمة في شاشة تسجيل الدخول
private val PrimaryColor = Color(0xFF0288D1) // أزرق نابض
private val SecondaryColor = Color(0xFF4FC3F7) // أزرق فاتح
private val AccentColor = Color(0xFFFFA726) // برتقالي دافئ
private val SurfaceColor = Color(0xFFFFFFFF) // أبيض نقي
private val ErrorColor = Color(0xFFE57373) // أحمر فاتح للأخطاء

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val TAG = "RegisterScreen"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrimaryColor.copy(alpha = 0.15f),
                        SecondaryColor.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight() // السماح للعمود بالتمدد ليشمل كل العناصر
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // تغيير الترتيب إلى الأعلى لضمان ظهور كل شيء
        ) {
            // العنوان
            Text(
                text = "Create Account / إنشاء حساب",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = PrimaryColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // حقل الاسم الكامل
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name / الاسم الكامل", color = PrimaryColor.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
                    cursorColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // حقل رقم الهاتف
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number / رقم الهاتف", color = PrimaryColor.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
                    cursorColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // حقل البريد الإلكتروني
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email / البريد الإلكتروني", color = PrimaryColor.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
                    cursorColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // حقل كلمة المرور
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password / كلمة المرور", color = PrimaryColor.copy(alpha = 0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
                    cursorColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // حقل تأكيد كلمة المرور
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password / تأكيد كلمة المرور", color = PrimaryColor.copy(alpha = 0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = SecondaryColor.copy(alpha = 0.5f),
                    cursorColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedTextColor = Color.Black
                )
            )

            // رسائل الخطأ والنجاح مع الحركة
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                errorMessage?.let {
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

            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                successMessage?.let {
                    Text(
                        text = it,
                        color = PrimaryColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // زر التسجيل
            Button(
                onClick = {
                    when {
                        fullName.isEmpty() -> {
                            errorMessage = "Full name is required / الاسم الكامل مطلوب"
                            Log.w(TAG, "Registration failed: Full name is empty")
                        }
                        phoneNumber.isEmpty() -> {
                            errorMessage = "Phone number is required / رقم الهاتف مطلوب"
                            Log.w(TAG, "Registration failed: Phone number is empty")
                        }
                        email.isEmpty() -> {
                            errorMessage = "Email is required / البريد الإلكتروني مطلوب"
                            Log.w(TAG, "Registration failed: Email is empty")
                        }
                        email == "admin" -> {
                            errorMessage = "Cannot register with admin username / لا يمكن التسجيل باسم المستخدم admin"
                            Log.w(TAG, "Registration failed: Attempted to register as admin")
                        }
                        password.isEmpty() -> {
                            errorMessage = "Password is required / كلمة المرور مطلوبة"
                            Log.w(TAG, "Registration failed: Password is empty")
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match / كلمات المرور غير متطابقة"
                            Log.w(TAG, "Registration failed: Passwords do not match")
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters / يجب أن تكون كلمة المرور 6 أحرف على الأقل"
                            Log.w(TAG, "Registration failed: Password too short")
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            scope.launch {
                                try {
                                    Log.d(TAG, "Attempting registration for email: $email")
                                    val result = authRepository.registerUserWithDetails(email, password, fullName, phoneNumber)
                                    isLoading = false

                                    if (result.isSuccess && result.getOrDefault(false)) {
                                        successMessage = "Registration successful! Please wait for approval. / تم التسجيل بنجاح! يرجى انتظار الموافقة."
                                        Log.i(TAG, "Registration successful for $email")
                                        delay(1500)
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = "Registration failed. Please try again. / فشل التسجيل. يرجى المحاولة مرة أخرى."
                                        Log.w(TAG, "Registration failed for $email: Unknown error")
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    val errorMsg = when {
                                        e.message?.contains("email-already-in-use") == true ->
                                            "This email is already registered / هذا البريد الإلكتروني مسجل بالفعل"
                                        e.message?.contains("invalid-email") == true ->
                                            "Invalid email format / تنسيق البريد الإلكتروني غير صالح"
                                        e.message?.contains("weak-password") == true ->
                                            "Password is too weak / كلمة المرور ضعيفة جدًا"
                                        else -> "Registration failed: ${e.message} / فشل التسجيل: ${e.message}"
                                    }
                                    errorMessage = errorMsg
                                    Log.e(TAG, "Registration error for $email: ${e.message}", e)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = PrimaryColor.copy(alpha = 0.6f),
                    contentColor = SurfaceColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SurfaceColor
                    )
                } else {
                    Text(
                        "Register / تسجيل",
                        fontSize = 18.sp,
                        color = SurfaceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // رابط تسجيل الدخول مع تحسينات للظهور
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // ارتفاع ثابت لضمان الظهور
                    .background(Color.Transparent, RoundedCornerShape(8.dp)) // خلفية شفافة مع زوايا مستديرة
                    .padding(vertical = 8.dp), // حشوة عمودية لتجنب التقاطع
                contentPadding = PaddingValues(horizontal = 8.dp) // حشوة أفقية
            ) {
                Text(
                    text = "Already have an account? Login / لديك حساب؟ تسجيل الدخول",
                    color = AccentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}