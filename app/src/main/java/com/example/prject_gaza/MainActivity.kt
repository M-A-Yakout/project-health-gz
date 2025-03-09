package com.example.prject_gaza

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prject_gaza.data.repository.AuthRepository
import com.example.prject_gaza.ui.screens.AdminPanelScreen
import com.example.prject_gaza.ui.screens.LoginScreen
import com.example.prject_gaza.ui.screens.RegisterScreen
import com.example.prject_gaza.ui.screens.UserHomeScreen
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val PLAY_SERVICES_REQUEST = 9000
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w(TAG, "Storage permission denied")
            Toast.makeText(this, "Storage permission required for PDF generation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkGooglePlayServices()) {
            return
        }

        requestStoragePermissionIfNeeded()

        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }

    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return when {
            resultCode == ConnectionResult.SUCCESS -> true
            googleApiAvailability.isUserResolvableError(resultCode) -> {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST)?.show()
                false
            }
            else -> {
                Toast.makeText(this, "Google Play Services is not available", Toast.LENGTH_LONG).show()
                finish()
                false
            }
        }
    }

    private fun requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    @Composable
    private fun AppNavigation(
        navController: NavHostController = rememberNavController(),
        authRepository: AuthRepository = AuthRepository(LocalContext.current.applicationContext)
    ) {
        val context = LocalContext.current
        val firebaseAuth = FirebaseAuth.getInstance()

        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                val scope = rememberCoroutineScope()
                LoginScreen(
                    onLoginSuccess = { email ->
                        scope.launch {
                            try {
                                Log.d(TAG, "Attempting to fetch user data for email: $email")
                                val user = authRepository.getCurrentUser()
                                if (user != null && user.isAdmin) { // Fixed: Compare as Boolean
                                    navController.navigate("adminPanel") {
                                        popUpTo("login") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    Log.d(TAG, "User fetched: ${user?.uid ?: "Unknown UID"}, Status: ${user?.status ?: "Unknown Status"}")
                                    when {
                                        user?.status == "accepted" || user?.status == "approved" -> {
                                            Log.d(TAG, "Navigating to UserHomeScreen for email: $email")
                                            navController.navigate("userHome") {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                        user?.status == "pending" -> {
                                            Toast.makeText(
                                                context,
                                                "Your account is pending approval / حسابك قيد الموافقة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Log.d(TAG, "Account pending for email: $email")
                                        }
                                        user?.status == "rejected" -> {
                                            Toast.makeText(
                                                context,
                                                "Your account has been rejected / تم رفض حسابك",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Log.d(TAG, "Account rejected for email: $email")
                                        }
                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Unknown account status / حالة الحساب غير معروفة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Log.e(TAG, "Unknown status: ${user?.status ?: "Unknown"} for email: $email")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Login failed / فشل تسجيل الدخول",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(TAG, "Login error: ${e.message}", e)
                            }
                        }
                    },
                    authRepository = authRepository,
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToAdmin = {
                        navController.navigate("adminPanel") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        Toast.makeText(context, "Registration successful / تم التسجيل بنجاح", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    authRepository = authRepository
                )
            }

            composable("userHome") {
                UserHomeScreen(
                    authRepository = authRepository, // Pass authRepository here
                    onLogout = {
                        firebaseAuth.signOut()
                        Toast.makeText(context, "Logged out successfully / تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("adminPanel") {
                AdminPanelScreen(
                    authRepository = authRepository,
                    onLogout = {
                        firebaseAuth.signOut()
                        Toast.makeText(context, "Logged out successfully / تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun AppPreview() {
        MaterialTheme {
            Surface {
                AppNavigation()
            }
        }
    }
}