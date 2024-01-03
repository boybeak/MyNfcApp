package com.github.boybeak.mynfcapp

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.github.boybeak.mynfcapp.ui.theme.MyNfcAppTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

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

    private var tagTechState = mutableStateOf<TagTechnology?>(null)

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
                    MainPanel(tagTechState.value, techListState.value)
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
        nfcAdapter.enableReaderMode(this, { TODO("Not yet implemented") }, 0, Bundle())
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        when(intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                techListState.value = tag.techList.joinToString()
                if (tag.techList.contains(NfcA::class.java.name)) {
                    tagTechState.value = NfcA.get(tag)
                } else if (tag.techList.contains(Ndef::class.java.name)) {
                    tagTechState.value = Ndef.get(tag)
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun selectMainDir(nfcA: NfcA) {
//    val response = transceive(nfcA)
//    Log.d(TAG, "selectMainDir response(${response.size})=[${response.joinToString { it.toHexString() }}]")
}

@OptIn(ExperimentalStdlibApi::class)
fun writeNfcA(nfcA: NfcA) {
    val response = transceive(nfcA, byteArrayOf(0xA2.toByte(), 0x02, 0x00, 0x00, 0xFF.toByte(), 0xFF.toByte()))
    Log.d(TAG, "writeNfcA response(${response.size})=[${response.joinToString { it.toHexString() }}]")
}

@OptIn(ExperimentalStdlibApi::class)
fun readNfcA(nfcA: NfcA) {
    val response = transceive(nfcA, byteArrayOf(0x30, 0x10))
    Log.d(TAG, "readNfcA response(${response.size})=[${response.joinToString { it.toHexString() }}]")
}

private fun transceive(nfcA: NfcA, data: ByteArray): ByteArray {
    nfcA.connect()
    nfcA.timeout = 2000
    val response = nfcA.transceive(data)
    nfcA.close()
    return response
}

@Composable
fun MainPanel(tech: TagTechnology?, techList: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = techList, textAlign = TextAlign.Center)

        when(tech) {
            is NfcA -> {
                val nfcA = tech as NfcA
                Button(onClick = {
                    selectMainDir(nfcA)
                }) {
                    Text(text = "selectMainDir")
                }
                Button(onClick = {
                    writeNfcA(nfcA)
                }) {
                    Text(text = "write")
                }
                Button(onClick = {
                    readNfcA(nfcA)
                }) {
                    Text(text = "read")
                }
            }
            is Ndef -> {
                Button(onClick = {
                    val ndef = tech as Ndef
                    ndef.run {
                        connect()
                        val rcd = NdefRecord.createUri("https://www.youtube.com")
                        val ndefMsg = NdefMessage(rcd)
                        writeNdefMessage(ndefMsg)
                        close()
                    }
                }) {
                    Text(text = "writeMessage")
                }
            }
        }
    }
}