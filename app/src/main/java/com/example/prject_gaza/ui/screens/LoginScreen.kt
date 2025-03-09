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
import kotlinx.coroutines.launch

// تعريف الألوان المخصصة
private val PrimaryColor = Color(0xFF0288D1) // أزرق نابض
private val SecondaryColor = Color(0xFF4FC3F7) // أزرق فاتح
private val AccentColor = Color(0xFFFFA726) // برتقالي دافئ
private val SurfaceColor = Color(0xFFFFFFFF) // أبيض نقي
private val ErrorColor = Color(0xFFE57373) // أحمر فاتح للأخطاء

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    authRepository: AuthRepository,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val TAG = "LoginScreen"

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
                .fillMaxHeight(fraction = 0.75f)
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // العنوان مع تأثير
            Text(
                text = "Login / تسجيل الدخول",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = PrimaryColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // حقل البريد الإلكتروني
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email / البريد الإلكتروني", color = PrimaryColor.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            // رسالة الخطأ مع حركة
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

            Spacer(modifier = Modifier.height(32.dp))

            // زر تسجيل الدخول
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        error = "Email and password are required / البريد الإلكتروني وكلمة المرور مطلوبان"
                        return@Button
                    }

                    if (email == "admin" && password == "admin") {
                        onNavigateToAdmin()
                        return@Button
                    }

                    isLoading = true
                    error = null

                    scope.launch {
                        try {
                            val result = authRepository.loginUser(email, password)
                            if (result.isSuccess && result.getOrDefault(false)) {
                                onLoginSuccess(email)
                            } else {
                                error = "Login failed / فشل تسجيل الدخول"
                            }
                        } catch (e: Exception) {
                            error = e.message
                            Log.e(TAG, "Login error: ${e.message}", e)
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
                        "Login / تسجيل الدخول",
                        fontSize = 18.sp,
                        color = SurfaceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // رابط التسجيل
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Don't have an account? Register / ليس لديك حساب؟ سجل الآن",
                    color = AccentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// لتطبيق الألوان في الثيم
@Composable
fun ProjectGazaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            tertiary = AccentColor,
            surface = SurfaceColor,
            error = ErrorColor,
            background = Color(0xFFF5F7FA)
        ),
        typography = Typography(),
        content = content
    )
}