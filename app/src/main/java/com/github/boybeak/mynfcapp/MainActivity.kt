package com.github.boybeak.mynfcapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.github.boybeak.mynfcapp.ui.theme.MyNfcAppTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val nfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private val nfcIntent by lazy {
        Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
    private val pendingIt by lazy {
        PendingIntent.getActivity(this, 0, nfcIntent, 0)
    }

    private var ndefTech: Ndef? = null

    private val techListState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNfcAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainPanel(techListState.value, writeClick = {
                        ndefTech?.run {
                            val rcd = NdefRecord.createUri("")
                            val ndefMsg = NdefMessage(rcd)
                            writeNdefMessage(ndefMsg)
                        }
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, pendingIt, null,  null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        when(intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                techListState.value = tag.techList.joinToString()
                if (tag.techList.contains(Ndef::class.java.name)) {
                    ndefTech = Ndef.get(tag)
                }
                Log.d(TAG, "onNewIntent ")
            }
        }
    }

}

@Composable
fun MainPanel(techList: String, writeClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = techList, textAlign = TextAlign.Center)
        Button(onClick = writeClick) {
            Text(text = "writeMessage")
        }
    }
}