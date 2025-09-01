package com.example.fa100221_practicai

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



// ====== Helpers de SharedPreferences ======
private fun prefs(context: Context) =
    context.getSharedPreferences("usuario", Context.MODE_PRIVATE)

private fun loadTheme(context: Context): String =
    prefs(context).getString("tema", "light") ?: "light"

private fun saveTheme(context: Context, value: String) {
    prefs(context).edit().putString("tema", value).apply()
}

private fun clearUserData(context: Context) {
    prefs(context).edit().apply {
        remove("nombre")
        remove("correo")
        remove("telefono")
        remove("fechaRegistro")
        // OJO: NO borramos "tema" para respetar la preferencia del usuario
        apply()
    }
}

// ====== Validaciones ======
private fun isEmailValid(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

private fun isPhoneValid(telefono: String): Boolean =
    telefono.all { it.isDigit() } && telefono.length in 8..9

// ====== Tema de la app ======
@Composable
fun AppTheme(isDark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = scheme, content = content)
}

// ====== Punto de entrada ======
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    var isDark by remember { mutableStateOf(loadTheme(context) == "dark") }

    AppTheme(isDark = isDark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            RegistroAppConMenu(
                isDark = isDark,
                onToggleTheme = { checked ->
                    isDark = checked
                    saveTheme(
                        context,
                        if (checked) "dark" else "light"
                    )
                }
            )
        }
    }
}

/** Pantallas disponibles en el menú */
enum class Screen { Registrar, Usuario, AcercaDe }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAppConMenu(
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf(Screen.Registrar) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado simple
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Menú", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    // Toggle de tema
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tema oscuro", modifier = Modifier.weight(1f))
                        Switch(
                            checked = isDark,
                            onCheckedChange = { onToggleTheme(it) }
                        )
                    }
                }
                Divider()

                // Ítems de navegación
                NavigationDrawerItem(
                    label = { Text("Registrar") },
                    selected = currentScreen == Screen.Registrar,
                    onClick = {
                        currentScreen = Screen.Registrar
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Usuario") },
                    selected = currentScreen == Screen.Usuario,
                    onClick = {
                        currentScreen = Screen.Usuario
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Acerca de") },
                    selected = currentScreen == Screen.AcercaDe,
                    onClick = {
                        currentScreen = Screen.AcercaDe
                        scope.launch { drawerState.close() }
                    }
                )

                Divider(Modifier.padding(vertical = 8.dp))

                // Acción: Borrar datos
                NavigationDrawerItem(
                    label = { Text("Borrar datos") },
                    selected = false,
                    onClick = {
                        clearUserData(context)
                        Toast.makeText(context, "Datos borrados", Toast.LENGTH_SHORT).show()
                        currentScreen = Screen.Usuario
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registro de Usuario") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.Registrar -> RegistroUsuarioForm(onSaved = {
                        // Al guardar, pasamos a "Usuario"
                        currentScreen = Screen.Usuario
                    })
                    Screen.Usuario -> MostrarUsuarioGuardado()
                    Screen.AcercaDe -> AcercaDeScreen(autor = "Saraí Flores")
                }
            }
        }
    }
}

@Composable
fun RegistroUsuarioForm(onSaved: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            when {
                nombre.isBlank() || correo.isBlank() || telefono.isBlank() ->
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()

                !isEmailValid(correo) ->
                    Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()

                !isPhoneValid(telefono) ->
                    Toast.makeText(context, "Teléfono inválido (usa 8–9 dígitos)", Toast.LENGTH_SHORT).show()

                else -> {
                    // Guardar en SharedPreferences
                    val fecha = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                    val p = prefs(context)
                    p.edit().apply {
                        putString("nombre", nombre.trim())
                        putString("correo", correo.trim())
                        putString("telefono", telefono.trim())
                        putString("fechaRegistro", fecha)
                        apply()
                    }

                    // Limpiar campos y avisar
                    nombre = ""
                    correo = ""
                    telefono = ""
                    Toast.makeText(context, "Usuario guardado", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
            }
        }) {
            Text("Registrar")
        }
    }
}

@Composable
fun MostrarUsuarioGuardado() {
    val context = LocalContext.current
    val p = prefs(context)
    val nombre = p.getString("nombre", "") ?: ""
    val correo = p.getString("correo", "") ?: ""
    val telefono = p.getString("telefono", "") ?: ""
    val fecha = p.getString("fechaRegistro", "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Name: ${nombre.ifBlank { "—" }}")
        Text("Email: ${correo.ifBlank { "—" }}")
        Text("Phone: ${telefono.ifBlank { "—" }}")
        Text("Registrado: ${fecha.ifBlank { "—" }}")
    }
}

@Composable
fun AcercaDeScreen(autor: String) {
    val context = LocalContext.current
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    val versionName = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "1.0"
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(appName, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Autor: $autor")
        Text("Versión: $versionName")
    }
}
