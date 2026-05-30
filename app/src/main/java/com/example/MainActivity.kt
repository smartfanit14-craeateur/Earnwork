package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PaidworkApp()
            }
        }
    }
}

@Composable
fun PaidworkApp(viewModel: AppViewModel = viewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"
    
    // Support Arabic + English localized directionality
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isLoggedIn) {
                    LoginScreen(viewModel)
                } else {
                    MainAppLayout(viewModel)
                }
                
                // Real-time full-screen Ad playback simulation overlay
                val adOverlayState by viewModel.adOverlay.collectAsState()
                adOverlayState?.let { state ->
                    AdPlayerOverlay(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// LOGIN SCREEN (Arabic + English)
// ==========================================
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val errorType by viewModel.loginError.collectAsState()
    val context = LocalContext.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val titleText = Translations.getString("login_title", language)
    val subText = Translations.getString("login_sub", language)
    val emailText = Translations.getString("email", language)
    val passwordText = Translations.getString("password", language)
    val loginBtnText = Translations.getString("login_btn", language)
    val googleLoginText = Translations.getString("google_login", language)
    val guestModeText = Translations.getString("guest_mode", language)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        if (isSystemInDarkTheme()) Color(0xFF031409) else Color(0xFFE8F8EE)
                    )
                )
            )
            .padding(20.dp)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Language Toggle at Top Right/Left of screen (Uses text directly, no extended icon dependency)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    viewModel.changeLanguage(if (language == "en") "ar" else "en") 
                },
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .width(96.dp)
                    .height(36.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (language == "en") "العربية" else "English",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Animated Coin / Paidwork logo drawn using Canvas
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GreenPrimary.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            radius = size.width * 0.7f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    drawCircle(
                        color = GreenPrimary,
                        radius = size.width / 2f,
                        style = Stroke(width = 8f)
                    )
                    drawCircle(
                        color = CashGold,
                        radius = size.width / 3f
                    )
                    val strokeWidth = 10f
                    val topCircleRadius = size.width / 7f
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    
                    drawLine(
                        color = Color.Black,
                        start = Offset(centerX - strokeWidth, centerY - topCircleRadius),
                        end = Offset(centerX - strokeWidth, centerY + topCircleRadius * 1.5f),
                        strokeWidth = strokeWidth
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = topCircleRadius,
                        center = Offset(centerX + strokeWidth * 0.1f, centerY - topCircleRadius * 0.2f),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Error Message
            errorType?.let { err ->
                val localizedError = if (err == "empty_error") {
                    Translations.getString("error_empty", language)
                } else {
                    Translations.getString("invalid_credentials", language)
                }
                Text(
                    text = localizedError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .testTag("error_banner"),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Credentials Card panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = emailText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        placeholder = { Text("e.g. name@domain.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Text(
                        text = passwordText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Text(
                                    text = if (isPasswordVisible) "HIDE" else "SHOW",
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            val ok = viewModel.login(email, password)
                            if (ok) {
                                Toast.makeText(context, "Logged in successful!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = loginBtnText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Google Login Mock Button
            OutlinedButton(
                onClick = { 
                    viewModel.loginAsGuest()
                    Toast.makeText(context, "Mock Google Login Successful", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        drawCircle(color = SoftRed, radius = size.width / 2f)
                        drawArc(
                            color = CashGold,
                            startAngle = 45f,
                            sweepAngle = 90f,
                            useCenter = true
                        )
                        drawArc(
                            color = SoftBlue,
                            startAngle = 135f,
                            sweepAngle = 90f,
                            useCenter = true
                        )
                        drawArc(
                            color = GreenPrimary,
                            startAngle = 225f,
                            sweepAngle = 90f,
                            useCenter = true
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = googleLoginText,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    viewModel.loginAsGuest()
                    Toast.makeText(context, "Logged in as Guest!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("guest_login")
            ) {
                Text(
                    text = "⚡ $guestModeText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// ==========================================
// MAIN APP NAVIGATION LAYOUT
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: AppViewModel) {
    val selectedTab by viewModel.currentTab.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val coins by viewModel.coins.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Translations.getString("app_title", language),
                        fontWeight = FontWeight.Black,
                        color = GreenPrimary
                    )
                },
                actions = {
                    // Golden coins pill capsule in TopBar
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clickable { viewModel.setTab("wallet") }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "coin_spin")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .drawBehind {
                                    drawCircle(color = CashGold)
                                    drawCircle(
                                        color = Color.White,
                                        radius = size.width / 4f,
                                        center = Offset(size.width / 2f, size.height / 2f)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GreenPrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = coins.toString(),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = Translations.getString("coins", language),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Localization toggle (Text based to prevent extended icons crash)
                    TextButton(
                        onClick = { 
                            viewModel.changeLanguage(if (language == "en") "ar" else "en") 
                        }
                    ) {
                        Text(
                            text = if (language == "en") "العربية" else "English",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = GreenPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs_def = listOf(
                    Triple("dashboard", Icons.Default.Home, Translations.getString("dashboard", language)),
                    Triple("tasks", Icons.Default.PlayArrow, Translations.getString("tasks", language)),
                    Triple("wallet", Icons.Default.CheckCircle, Translations.getString("wallet", language)),
                    Triple("settings", Icons.Default.Settings, Translations.getString("settings", language))
                )
                
                tabs_def.forEach { (tabId, icon, label) ->
                    val isActive = selectedTab == tabId
                    NavigationBarItem(
                        selected = isActive,
                        onClick = { viewModel.setTab(tabId) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isActive) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_$tabId")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "tasks" -> TasksScreen(viewModel = viewModel)
                "wallet" -> WalletScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val currencyVal by viewModel.appCurrency.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "${Translations.getString("login_title", language)},",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userEmail.split("@").firstOrNull() ?: "Guest User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("earnings_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GreenPrimary, GreenPrimaryDark)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("current_balance", language).uppercase(Locale.US),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = coins.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.getString("coins", language),
                                style = MaterialTheme.typography.headlineSmall,
                                color = CashGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Translations.getString("equivalent", language),
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = viewModel.getCoinsValue(coins, currencyVal),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currencyVal,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            var checkInDone by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !checkInDone) {
                        viewModel.dailyCheckIn()
                        checkInDone = true
                        Toast.makeText(context, "+50 Coins Credited!", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (checkInDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (checkInDone) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else GreenPrimary.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (checkInDone) MaterialTheme.colorScheme.surface else GreenPrimary.copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (checkInDone) Icons.Default.Check else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (checkInDone) Color.Gray else GreenPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(14.dp))
                        
                        Column {
                            Text(
                                text = Translations.getString("daily_target", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (checkInDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Translations.getString("daily_sub", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (checkInDone) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else GreenPrimary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (checkInDone) "CLAIMED" else "+50",
                            color = if (checkInDone) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
        
        item {
            Text(
                text = Translations.getString("recent_activity", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No activities registered yet.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == "ar") log.descriptionAr else log.descriptionEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "+${log.coinsAdded}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. EARN TASKS PAGE WITH "WATCH AD"
// ==========================================
@Composable
fun TasksScreen(viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val mediationDetails = remember { AdMediationManager.getMediationDetails() }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = Translations.getString("watch_video_ad", language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.getString("reward_descr", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GreenPrimary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = Translations.getString("test_ads_enabled", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        item {
            Text(
                text = Translations.getString("choose_network", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        items(mediationDetails) { network ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("network_card_${network.name.lowercase(Locale.US).replace(" ", "_")}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(GreenPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = network.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+100 Coins",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${network.testKeyType}: ${network.testKeyVal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.watchRewardedAd(network.name, network.testKeyVal) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("watch_btn_${network.name.lowercase(Locale.US).replace(" ", "_")}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.getString("watch_video_ad", language),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. WALLET PAGE WITH BALANCE
// ==========================================
@Composable
fun WalletScreen(viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val currencyVal by viewModel.appCurrency.collectAsState()
    val context = LocalContext.current
    
    val withdrawMethods = when (currencyVal) {
        "EGP" -> listOf(
            Translations.getString("local_egypt", language),
            Translations.getString("paypal_instapay", language)
        )
        "SAR" -> listOf(
            Translations.getString("local_saudi", language),
            Translations.getString("paypal_instapay", language)
        )
        else -> listOf(
            Translations.getString("paypal_instapay", language),
            "Direct International Wire Bank Transfer"
        )
    }
    
    var selectedMethodIndex by remember { mutableStateOf(0) }
    val payoutProgress = (coins.toFloat() / 1000f).coerceIn(0f, 1f)
    var withdrawDetailsInput by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = Translations.getString("wallet", language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.getString("payout_placeholder", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Translations.getString("current_balance", language),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = coins.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = GreenPrimary
                            )
                            Text(
                                text = Translations.getString("coins", language),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = viewModel.getCoinsValue(coins, currencyVal),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = Translations.getString("equivalent", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("payout_progress", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(payoutProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { payoutProgress },
                            color = GreenPrimary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
        
        item {
            Text(
                text = Translations.getString("payout_method", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(withdrawMethods.size) { index ->
            val isSelected = selectedMethodIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMethodIndex = index }
                    .testTag("payout_method_$index"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = withdrawMethods[index],
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedMethodIndex = index },
                        colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.getString("payout_options", language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = withdrawDetailsInput,
                        onValueChange = { withdrawDetailsInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_input"),
                        placeholder = { Text("e.g. PayPal Email or Mobile wallet number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
        
        item {
            Button(
                onClick = {
                    if (coins < 1000) {
                        val neededCoins = 1000 - coins
                        val feedback = if (language == "ar") {
                            "عذراً، تحتاج إلى $neededCoins عملة إضافية للوصول للحد الأدنى!"
                        } else {
                            "You need $neededCoins more coins to withdraw!"
                        }
                        Toast.makeText(context, feedback, Toast.LENGTH_LONG).show()
                    } else if (withdrawDetailsInput.isBlank()) {
                        Toast.makeText(context, "Please enter withdrawal info first!", Toast.LENGTH_SHORT).show()
                    } else {
                        val feedback = if (language == "ar") {
                            "تم إرسال طلب السحب الخاص بك بنجاح وجاري المراجعة."
                        } else {
                            "Withdrawal request submitted successfully!"
                        }
                        Toast.makeText(context, feedback, Toast.LENGTH_LONG).show()
                        withdrawDetailsInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("withdraw_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (coins >= 1000) GreenPrimary else Color.Gray
                )
            ) {
                Text(
                    text = Translations.getString("payout_req", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ==========================================
// 4. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val currencyVal by viewModel.appCurrency.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Text(
                text = Translations.getString("settings", language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(GreenPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${Translations.getString("current_balance", language)}: $coins ${Translations.getString("coins", language)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = Translations.getString("language", language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            val activeLang = language
                            listOf("en", "ar").forEach { code ->
                                val active = activeLang == code
                                val label = if (code == "en") "English" else "العربية"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (active) GreenPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.changeLanguage(code) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Column {
                        Text(
                            text = Translations.getString("currency", language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            val systemCurrencies = listOf("USD", "EGP", "SAR")
                            systemCurrencies.forEach { cur ->
                                val active = currencyVal == cur
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (active) GreenPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.changeCurrency(cur) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cur,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🛠️ " + Translations.getString("dev_info", language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Translations.getString("ads_med_descr", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "AdMob ID" to AdMediationManager.ADMOB_REWARDED_TEST_UNIT_ID,
                            "StartApp AppId" to AdMediationManager.STARTAPP_TEST_APP_ID,
                            "Unity GameId" to AdMediationManager.UNITY_ADS_TEST_GAME_ID,
                            "AppLovin SDKKey" to AdMediationManager.APPLOVIN_TEST_SDK_KEY
                        ).forEach { (network, param) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = network, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = param,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(160.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = { 
                    viewModel.logout() 
                    Toast.makeText(context, "Log Out Successful", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("logout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SoftRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.getString("logout", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// DETAILED REWARDED VIDEO AD GRAPHIC OVERLAY
// ==========================================
@Composable
fun AdPlayerOverlay(state: AdOverlayState, viewModel: AppViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    val context = LocalContext.current
    var isConfirmingSkip by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = {
            if (!state.isCompleted) {
                isConfirmingSkip = true
            } else {
                viewModel.dismissAdOverlay()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .systemBarsPadding()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "tech_spin")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing)),
                label = "angle"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val stroke = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawCircle(
                            color = GreenPrimary.copy(alpha = 0.15f),
                            radius = size.width * 0.6f,
                            style = stroke
                        )
                        drawCircle(
                            color = GreenPrimary.copy(alpha = 0.25f),
                            radius = size.width * 0.3f,
                            style = stroke
                        )
                    }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.TopCenter)
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        text = "TEST AD MEDIATION",
                        color = GreenPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = state.networkName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                IconButton(
                    onClick = {
                        if (!state.isCompleted) {
                            isConfirmingSkip = true
                        } else {
                            viewModel.dismissAdOverlay()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .size(36.dp)
                        .testTag("ad_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Ad",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFF0D1611), RoundedCornerShape(24.dp))
                        .border(1.dp, GreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (!state.isCompleted) {
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.92f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    tween(1000, easing = FastOutSlowInEasing),
                                    RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = GreenPrimary.copy(alpha = 0.15f),
                                            radius = (size.width / 2f) * scale
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = Translations.getString("playing_video_from", language) + " " + state.networkName,
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Test Unit ID: " + state.testId,
                                color = GreenPrimary.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .drawBehind {
                                        drawCircle(color = CashGold)
                                        drawCircle(
                                            color = Color.White,
                                            radius = size.width / 4f
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = Translations.getString("ad_completed", language),
                                color = GreenPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = Translations.getString("ad_completed_desc", language),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                if (!state.isCompleted) {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { state.remainingSeconds / 15f },
                            color = GreenPrimary,
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = state.remainingSeconds.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (language == "ar") "شاهد للحصول على 100 عملة" else "Watch until end for 100 coins",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Button(
                        onClick = { 
                            viewModel.claimRewards() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("claim_rewards_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text(
                            text = if (language == "ar") "استلام المكافأة (+100 🪙)" else "Claim coins (+100 🪙)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
            
            if (isConfirmingSkip) {
                AlertDialog(
                    onDismissRequest = { isConfirmingSkip = false },
                    title = {
                        Text(
                            text = if (language == "ar") "إغلاق الإعلان؟" else "Skip Video?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = if (language == "ar") "إذا أغلقت الإعلان قبل النهاية فلن تحصل على الـ 100 عملة." else "Closing the video early will void your 100 coins reward. Are you sure?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isConfirmingSkip = false
                                viewModel.dismissAdOverlay()
                                Toast.makeText(context, "Ad Skipped. No reward granted.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("confirm_skip_button")
                        ) {
                            Text(
                                text = if (language == "ar") "نعم، إغلاق" else "Yes, skip",
                                color = SoftRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isConfirmingSkip = false },
                            modifier = Modifier.testTag("cancel_skip_button")
                        ) {
                            Text(
                                text = if (language == "ar") "إلغاء المتابعة" else "Keep watching",
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }
    }
}
