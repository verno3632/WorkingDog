package jp.developer.retia.credential

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.*
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CredentialActivity : ComponentActivity() {
    private val credentialManager = CredentialManager.create(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CredentialSampleScreen(onClickSave = { id, password ->
                lifecycleScope.launch {
                    val request = CreatePasswordRequest(id, password)
                    try {
                        credentialManager.executeCreateCredential(request, this@CredentialActivity)
                    } catch (e: CreateCredentialCancellationException) {
                        Toast.makeText(this@CredentialActivity, "キャンセルされました", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }, onClickLoad = {
                lifecycleScope.launch {
                    val text = try {
                        val request = GetCredentialRequest(listOf(GetPasswordOption()))
                        val response =
                            credentialManager.executeGetCredential(request, this@CredentialActivity)
                        val credential = response.credential
                        val id = credential.data.getString(PasswordCredential.BUNDLE_KEY_ID, "")
                        val password =
                            credential.data.getString(PasswordCredential.BUNDLE_KEY_PASSWORD, "")
                        "id: $id pass: $password"
                    } catch (e: GetCredentialCancellationException) {
                        "キャンセルされました"
                    } catch (e: GetCredentialInterruptedException) {
                        "エラーが発生しました"
                    }
                    Toast.makeText(this@CredentialActivity, text, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}

@Preview
@Composable
fun CredentialSampleScreen(
    onClickSave: (String, String) -> Unit = { _, _ -> },
    onClickLoad: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var id by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text("id")
        TextField(value = id, onValueChange = { id = it })
        Text("パスワード")
        TextField(value = password, onValueChange = { password = it })
        Button(
            onClick = {
                onClickSave.invoke(id, password)
            }, enabled = id.isNotEmpty() && password.isNotEmpty()
        ) {
            Text("保存")
        }
        Button(onClick = {
            onClickLoad.invoke()
        }) {
            Text("読み込み")
        }
    }
}

