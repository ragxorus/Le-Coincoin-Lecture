package com.example.coincoinlecture.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
@Composable
fun IsbnScannerScreen(
    onIsbnDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    var status by remember {
        mutableStateOf("Initialisation du scanner...")
    }
    val context = LocalContext.current

    // Vérification de la permission caméra
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Demande de permission à Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Bouton retour Android
    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Scanner un livre",
                style = MaterialTheme.typography.titleLarge
            )


        }

        if (hasCameraPermission) {

            CameraPreview(
                onIsbnDetected = onIsbnDetected,
                onStatusChanged = { status = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Text(
                text = "Place le code-barres au centre de la caméra.",
                modifier = Modifier.padding(12.dp)
            )

        } else {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("L'accès à la caméra est nécessaire pour scanner un livre.")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Autoriser la caméra")
                }
            }
        }

        // Saisie manuelle, disponible même sans permission caméra
        ManualIsbnEntry(
            onIsbnDetected = onIsbnDetected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// Affichage et gestion de la caméra
@Composable
private fun CameraPreview(
    onIsbnDetected: (String) -> Unit,
    onStatusChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnStatusChanged by rememberUpdatedState(onStatusChanged)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Évite d'utiliser une ancienne version du callback
    val currentOnIsbnDetected by rememberUpdatedState(onIsbnDetected)

    // Configuration ML Kit : uniquement les codes EAN-13
    val scannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .build()
    }

    val scanner = remember {
        BarcodeScanning.getClient(scannerOptions)
    }

    // Thread dédié à l'analyse des images
    val analysisExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    // Vue qui affiche le flux vidéo
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(lifecycleOwner, previewView) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var cameraProvider: ProcessCameraProvider? = null

        val disposed = AtomicBoolean(false)

        cameraProviderFuture.addListener({

            if (!disposed.get()) {

                try {

                    cameraProvider = cameraProviderFuture.get()

                    // Configuration de l'aperçu caméra
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    // Configuration de l'analyse des images
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                    // On branche notre analyseur ML Kit
                    imageAnalysis.setAnalyzer(
                        analysisExecutor,
                        IsbnAnalyzer(
                            scanner,
                            onIsbnDetected = { isbn ->
                                currentOnIsbnDetected(isbn)
                            },
                            onStatusChanged = { status ->
                                currentOnStatusChanged(status)
                            }
                        )
                    )

                    // Activation de la caméra arrière
                    cameraProvider?.unbindAll()

                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )

                } catch (e: Exception) {

                    Log.e("IsbnScanner", "Erreur caméra", e)

                    cameraError = e.localizedMessage
                        ?: "Impossible de démarrer la caméra."
                }
            }

        }, ContextCompat.getMainExecutor(context))

        onDispose {

            disposed.set(true)

            cameraProvider?.unbindAll()

            scanner.close()

            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        cameraError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}


// Analyse d'une image de la caméra
private class IsbnAnalyzer(
    private val scanner: BarcodeScanner,
    private val onIsbnDetected: (String) -> Unit,
    private val onStatusChanged: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Empêche de détecter plusieurs fois le même livre
    private val alreadyDetected = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        if (alreadyDetected.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.getImage()

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Conversion de l'image CameraX vers une image ML Kit
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        try {
            scanner.process(image)
                .addOnSuccessListener { barcodes ->

                    if (barcodes.isEmpty()) {
                        onStatusChanged("Aucun code-barres détecté")
                    }

                    for (barcode in barcodes) {

                        val value = barcode.rawValue ?: continue

                        Log.d("IsbnScanner", "Code détecté : $value")

                        onStatusChanged("Code détecté : $value")

                        val isIsbn =
                            barcode.format == Barcode.FORMAT_EAN_13 &&
                                    value.length == 13 &&
                                    (value.startsWith("978") || value.startsWith("979"))

                        if (isIsbn && alreadyDetected.compareAndSet(false, true)) {
                            onStatusChanged("ISBN trouvé : $value")
                            onIsbnDetected(value)
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("IsbnScanner", "Erreur ML Kit", e)
                    onStatusChanged("Erreur ML Kit : ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }

        } catch (e: Exception) {

            Log.e("IsbnScanner", "Erreur pendant l'analyse", e)

            imageProxy.close()
        }
    }
}

@Composable
private fun ManualIsbnEntry(
    onIsbnDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isbn by rememberSaveable { mutableStateOf("") }

    // Nettoyage de l'ISBN avant validation
    val cleanIsbn = isbn
        .filter { it.isDigit() || it == 'X' || it == 'x' }
        .uppercase()

    val validIsbn = cleanIsbn.length == 10 || cleanIsbn.length == 13

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ou saisir un ISBN manuellement")

        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            label = { Text("ISBN") },
            placeholder = { Text("Ex : 9782841727919") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii
            )
        )

        Button(
            onClick = {
                onIsbnDetected(cleanIsbn)
            },
            enabled = validIsbn,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rechercher ce livre")
        }
    }
}
