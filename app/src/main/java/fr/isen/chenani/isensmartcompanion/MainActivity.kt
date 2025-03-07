package fr.isen.chenani.isensmartcompanion

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import fr.isen.chenani.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import androidx.compose.ui.tooling.preview.Preview

// Application class for Hilt
@HiltAndroidApp
class MyApp : Application()

// Main Activity with Hilt support
@AndroidEntryPoint
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

// Data model for an Event
data class Event(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

// Retrofit API interface
interface EventApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}

// Retrofit client
object EventRetrofitClient {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"
    val apiService: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }
}

// ViewModel to fetch and store events
class EventViewModel : ViewModel() {
    var events by mutableStateOf<List<Event>>(emptyList())
        private set

    fun fetchEvents() {
        EventRetrofitClient.apiService.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    events = response.body() ?: emptyList()
                    Log.d("EventViewModel", "Événements chargés : ${events.size}")
                } else {
                    Log.e("EventViewModel", "Erreur de réponse : ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e("EventViewModel", "Erreur réseau : ${t.message}")
            }
        })
    }
}

// Composable function to display the list of events
@Composable
fun EventsScreen(navController: NavHostController, viewModel: EventViewModel = hiltViewModel()) {
    val events = viewModel.events
    val gson = Gson()
    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Événements à venir",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (events.isEmpty()) {
            Text(text = "Aucun événement disponible", color = Color.Gray)
        } else {
            events.forEach { event ->
                EventItem(event = event) {
                    if (!event.id.isNullOrEmpty()) {
                        val eventJson = gson.toJson(event)
                        navController.navigate("eventDetails/$eventJson")
                    }
                }
            }
        }
    }
}

// Composable function to display a single event
@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text(text = event.title ?: "Sans titre", style = MaterialTheme.typography.headlineSmall)
    }
}

// Event details screen
@Composable
fun EventDetailsScreen(eventJson: String) {
    val gson = Gson()
    val event = gson.fromJson(eventJson, Event::class.java)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ID : ${event.id}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Titre : ${event.title}")
        Text(text = "Description : ${event.description}")
        Text(text = "Date : ${event.date}")
        Text(text = "Lieu : ${event.location}")
    }
}

// Gemini AI Interaction
@Composable
fun GeminiScreen() {
    var userInput by remember { mutableStateOf("") }
    var responses by remember { mutableStateOf(listOf<String>()) }
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyAJh_yW6voF2ixCuBuG7CRJSOLBV2hh754"
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        TextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Entrez du texte à analyser") }
        )
        Button(onClick = {
            scope.launch {
                val response = model.generateContent(prompt = "Content for: $userInput")
                response?.text?.let { responseText ->
                    responses = responses + responseText
                } ?: Log.e("GeminiAI", "La réponse n'a pas de texte valide.")
            }
        }) {
            Text("Analyser avec Gemini")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Réponses de Gemini AI :")
        responses.forEach {
            Text(text = it, modifier = Modifier.padding(4.dp))
        }
    }
}

// Navigation setup
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            Modifier.padding(innerPadding)
        ) {
            composable("main") { MainScreen(navController) }
            composable("events") { EventsScreen(navController) }
            composable("eventDetails/{eventJson}") { backStackEntry ->
                val eventJson = backStackEntry.arguments?.getString("eventJson")
                if (eventJson != null) {
                    EventDetailsScreen(eventJson)
                }
            }
            composable("gemini") { GeminiScreen() }
        }
    }
}

// Bottom navigation bar
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") },
            selected = navController.currentDestination?.route == "main",
            onClick = { navController.navigate("main") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Événements") },
            label = { Text("Événements") },
            selected = navController.currentDestination?.route == "events",
            onClick = { navController.navigate("events") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Paramètres") },
            label = { Text("Paramètres") },
            selected = navController.currentDestination?.route == "gemini",
            onClick = { navController.navigate("gemini") }
        )
    }
}


// Main screen with navigation
@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo4), // Remplace 'logo4' par le nom de ton image
            contentDescription = "Logo de l'application",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )
        Button(onClick = { navController.navigate("events") }) {
            Text(text = "Voir les événements")
        }
        Button(onClick = { navController.navigate("gemini") }) {
            Text(text = "Analyser avec Gemini AI")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ISENSmartCompanionTheme {
        AppNavigation()
    }
}

