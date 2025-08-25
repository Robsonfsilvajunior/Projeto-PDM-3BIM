package com.example.robsonprojeto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textViewResult: TextView
    private val REQUEST_IMAGE_PICK = 1

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textViewResult = findViewById(R.id.respostaTexto)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("pt", "BR")
            }
        }

        val btnSelectImage = findViewById<Button>(R.id.botaoEnvio)
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // <- todos os tipos
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)

                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                       textViewResult.text = visionText.text.trim()

                        val textoExtraido = visionText.text
                        textViewResult.text = textoExtraido
                        tts.speak(textoExtraido, TextToSpeech.QUEUE_FLUSH, null, null)

                    }
                    .addOnFailureListener { e ->
                        textViewResult.text = "Erro: ${e.message}"
                    }
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}