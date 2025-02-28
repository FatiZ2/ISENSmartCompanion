package fr.isen.chenani.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.chenani.isensmartcompanion.R.drawable.logo4
import fr.isen.chenani.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompanionTheme {
                AppNavigation()
            }
        }
    }
}

data class GPTRequest(
    val model: String = "text-davinci-003",
    val prompt: String,
    val max_tokens: Int = 100
)

data class GPTResponse(
    val choices: List<Choice>
)

data class Choice(
    val text: String
)

interface OpenAIApiService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${BuildConfig.OPENAI_API_KEY}" // Utilisation correcte
    )
    @POST("v1/completions")
    suspend fun getCompletion(@Body request: GPTRequest): Response<GPTResponse>
}



object RetrofitClient {
    private const val BASE_URL = "https://api.openai.com/"

    val apiService: OpenAIApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApiService::class.java)
    }
}

class ChatViewModel : ViewModel() {
    var responseText by mutableStateOf("")
        private set

    fun getAIResponse(prompt: String) {
        viewModelScope.launch {
            try {
                val request = GPTRequest(prompt = prompt)
                val response = RetrofitClient.apiService.getCompletion(request)
                if (response.isSuccessful) {
                    responseText = response.body()?.choices?.get(0)?.text ?: "Pas de réponse"
                } else {
                    responseText = "Erreur de réponse"
                }
            } catch (e: Exception) {
                responseText = "Erreur : ${e.message}"
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    var inputText by remember { mutableStateOf("") }
    val responseText = viewModel.responseText

    LaunchedEffect(responseText) {
        // Déclencher une action à chaque fois que la réponse change
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Assistant IA", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = responseText)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(text = "Demander à l'IA...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.getAIResponse(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Envoyer"
                )
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") { MainScreen() }
        composable("events") { EventsScreen() }
        composable("history") { HistoryScreen() }
        composable("assistant") { ChatScreen() }
    }
}

@Composable
fun MainScreen() {
    Greeting()
}

@Composable
fun EventsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Événements à venir",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Historique des événements",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    var messages by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        DisplayImage()
        Spacer(modifier = Modifier.weight(1f))
        MessageList(messages)
        InputBar { newMessage ->
            messages = messages + newMessage
            messages = messages + "Merci pour ton message"
        }
    }
}

@Composable
fun DisplayImage() {
    Image(
        painter = painterResource(id = logo4),
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .padding(top = 8.dp)
    )
}

@Composable
fun MessageList(messages: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        messages.forEach { message ->
            Text(
                text = message,
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(onMessageSent: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = Color(0xFFE9EBF8),
                shape = RoundedCornerShape(20.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(text = "Écrire ici...") },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onMessageSent(text)
                    text = "" // Réinitialise le champ après l'envoi
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Envoyer",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = navController.currentDestination?.route == "main",
            onClick = {
                navController.navigate("main") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Events") },
            label = { Text("Events") },
            selected = navController.currentDestination?.route == "events",
            onClick = {
                navController.navigate("events") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "History") },
            label = { Text("History") },
            selected = navController.currentDestination?.route == "history",
            onClick = {
                navController.navigate("history") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // Onglet pour l'Assistant IA
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Assistant IA") },
            label = { Text("Assistant IA") },
            selected = navController.currentDestination?.route == "assistant",
            onClick = {
                navController.navigate("assistant") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ISENSmartCompanionTheme {
        AppNavigation()
    }
}
