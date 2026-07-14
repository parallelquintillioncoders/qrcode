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
    var useGradient by remember { mutableStateOf(false) }
    
    val primaryBrush = if (useGradient) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF2196F3), Color(0xFFE040FB))
        )
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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Shape selector
        Text(
            text = "Pattern Shape:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        
        // Gradient Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Use Color Gradient", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = useGradient,
                onCheckedChange = { useGradient = it }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // QR Code Container
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            QRCodeView(
                content = payload.ifEmpty { " " },
                modifier = Modifier.fillMaxSize(),
                primaryColor = if (useGradient) Color.Unspecified else Color.Black,
                primaryBrush = primaryBrush,
                backgroundColor = Color.White,
                shape = selectedShape
            )
        }
    }
}