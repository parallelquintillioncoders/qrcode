package org.pqc.qrcode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.pqc.qrcode.qrkit.PermissionStatus
import org.pqc.qrcode.qrkit.QRCodeShape
import org.pqc.qrcode.qrkit.QRCodeView
import org.pqc.qrcode.qrkit.QRScannerView
import org.pqc.qrcode.qrkit.rememberCameraPermissionHandler
import org.jetbrains.compose.resources.painterResource
import qrcode.shared.generated.resources.Res
import qrcode.shared.generated.resources.compose_multiplatform

fun parseHexColor(hex: String, fallback: Color): Color {
    val clean = hex.trim().removePrefix("#")
    return try {
        if (clean.length == 6) {
            Color(
                red = clean.substring(0, 2).toInt(16),
                green = clean.substring(2, 4).toInt(16),
                blue = clean.substring(4, 6).toInt(16)
            )
        } else if (clean.length == 8) {
            Color(
                alpha = clean.substring(0, 2).toInt(16),
                red = clean.substring(2, 4).toInt(16),
                green = clean.substring(4, 6).toInt(16),
                blue = clean.substring(6, 8).toInt(16)
            )
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF64B5F6),
            onPrimary = Color.Black,
            surface = Color(0xFF121212),
            background = Color(0xFF1E1E1E),
            onSurface = Color.White
        )
    ) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Scan QR", "Generate QR")
        
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("QR Kit Dashboard", style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedTab == 0) {
                        ScanScreen()
                    } else {
                        GenerateScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun ScanScreen() {
    val permissionHandler = rememberCameraPermissionHandler()
    var permissionStatus by remember { mutableStateOf(permissionHandler.getStatus()) }
    val scope = rememberCoroutineScope()
    
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var flashlightEnabled by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(org.pqc.qrcode.qrkit.LensFacing.BACK) }
    var selectedResolution by remember { mutableStateOf<org.pqc.qrcode.qrkit.ScannerResolution?>(null) }

    LaunchedEffect(Unit) {
        permissionStatus = permissionHandler.getStatus()
    }

    if (scannedResult != null) {
        AlertDialog(
            onDismissRequest = { scannedResult = null },
            title = { Text("Scanned QR Code") },
            text = { Text(scannedResult ?: "") },
            confirmButton = {
                Button(onClick = { scannedResult = null }) {
                    Text("OK")
                }
            }
        )
    }

    when (permissionStatus) {
        PermissionStatus.GRANTED -> {
            Box(modifier = Modifier.fillMaxSize()) {
                QRScannerView(
                    modifier = Modifier.fillMaxSize(),
                    lensFacing = lensFacing,
                    targetResolution = selectedResolution,
                    flashlightEnabled = flashlightEnabled,
                    scanWindowEnabled = true,
                    onQrCodeDetected = { result ->
                        if (scannedResult == null) {
                            scannedResult = result
                        }
                    }
                )
                
                // Overlay controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                lensFacing = if (lensFacing == org.pqc.qrcode.qrkit.LensFacing.BACK) {
                                    org.pqc.qrcode.qrkit.LensFacing.FRONT
                                } else {
                                    org.pqc.qrcode.qrkit.LensFacing.BACK
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Camera: ${lensFacing.name}")
                        }

                        Button(
                            onClick = { flashlightEnabled = !flashlightEnabled },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (flashlightEnabled) MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                        ) {
                            Text(if (flashlightEnabled) "Flash ON" else "Flash OFF")
                        }
                    }

                    // Resolution presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val resolutions = listOf(
                            "Default" to null,
                            "SD (480p)" to org.pqc.qrcode.qrkit.ScannerResolution(640, 480),
                            "HD (720p)" to org.pqc.qrcode.qrkit.ScannerResolution(1280, 720),
                            "FHD (1080p)" to org.pqc.qrcode.qrkit.ScannerResolution(1920, 1080)
                        )

                        resolutions.forEach { (label, res) ->
                            val isSelected = selectedResolution == res
                            ElevatedButton(
                                onClick = { selectedResolution = res },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant camera access to scan QR codes with your device's camera.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            permissionStatus = permissionHandler.requestPermission()
                        }
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun GenerateScreen() {
    var payload by remember { mutableStateOf("https://github.com/parallelquintillioncoders/qrcode") }
    var selectedShape by remember { mutableStateOf(QRCodeShape.Squares) }
    var selectedGradientPreset by remember { mutableStateOf(0) }
    var embedLogo by remember { mutableStateOf(false) }
    var qrSize by remember { mutableStateOf(240f) }
    var sizeInputText by remember { mutableStateOf("240") }
    
    var customStartHex by remember { mutableStateOf("#FF5722") }
    var customEndHex by remember { mutableStateOf("#E91E63") }
    
    val gradientColors = when (selectedGradientPreset) {
        1 -> listOf(Color(0xFFFF5722), Color(0xFFE91E63)) // Sunset
        2 -> listOf(Color(0xFF00E676), Color(0xFF2979FF)) // Ocean
        3 -> listOf(Color(0xFF2196F3), Color(0xFFE040FB)) // Neon
        4 -> {
            val start = parseHexColor(customStartHex, Color.Black)
            val end = parseHexColor(customEndHex, Color.Black)
            listOf(start, end)
        }
        else -> null
    }

    val primaryBrush = if (gradientColors != null) {
        Brush.linearGradient(colors = gradientColors)
    } else {
        null
    }

    val logoPainter = if (embedLogo) {
        painterResource(Res.drawable.compose_multiplatform)
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = payload,
            onValueChange = { payload = it },
            label = { Text("QR Code Payload") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Shape selector
        Text(
            text = "Pattern Shape:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QRCodeShape.values().forEach { shape ->
                val isSelected = selectedShape == shape
                ElevatedButton(
                    onClick = { selectedShape = shape },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(shape.name, maxLines = 1)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gradient presets selector
        Text(
            text = "Color / Gradient Style:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf("Solid Black", "Sunset", "Ocean", "Neon", "Custom")
            presets.forEachIndexed { index, label ->
                val isSelected = selectedGradientPreset == index
                ElevatedButton(
                    onClick = { selectedGradientPreset = index },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Hex inputs if "Custom" is selected
        if (selectedGradientPreset == 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customStartHex,
                    onValueChange = { customStartHex = it },
                    label = { Text("Start Color (Hex)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = customEndHex,
                    onValueChange = { customEndHex = it },
                    label = { Text("End Color (Hex)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Logo Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Embed Center Logo Overlay", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = embedLogo,
                onCheckedChange = { embedLogo = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Size slider and Custom Size input field
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QR Code Size (dp)", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = sizeInputText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            sizeInputText = newValue
                            newValue.toIntOrNull()?.let { parsed ->
                                if (parsed in 100..400) {
                                    qrSize = parsed.toFloat()
                                }
                            }
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )
            }
            Slider(
                value = qrSize,
                onValueChange = { 
                    qrSize = it
                    sizeInputText = it.toInt().toString()
                },
                valueRange = 100f..400f,
                steps = 6
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // QR Code Container
        Box(
            modifier = Modifier
                .size(qrSize.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            QRCodeView(
                content = payload.ifEmpty { " " },
                modifier = Modifier.fillMaxSize(),
                primaryColor = if (gradientColors != null) Color.Unspecified else Color.Black,
                primaryBrush = primaryBrush,
                backgroundColor = Color.White,
                shape = selectedShape,
                logo = logoPainter,
                logoScale = 0.22f
            )
        }
    }
}