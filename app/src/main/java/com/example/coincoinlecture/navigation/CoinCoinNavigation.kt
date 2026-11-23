package com.example.coincoinlecture.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.ui.view.LoginView
import com.example.coincoinlecture.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

object Routes {
    const val TASK_LIST = "tasks"
    const val TASK_DETAIL = "tasks/{taskId}"

    fun taskDetail(id: String) = "tasks/$id"
}

/**
 * Racine de l'interface. Un ViewModel par domaine, créés ici avec leur factory.
 * Personne de connecté : écran de connexion Google. Sinon : l'application.
 */
@Composable
fun CoinCoinNavigation(
    authRepository: AuthRepository
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
    val userId by authViewModel.userId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (userId == null) {
        LoginView(
            onSignIn = {
                authViewModel.signIn(context) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = authViewModel::signOut) {
                Text("Déconnexion (debug)")
            }
        }
//        SignedInContent(
//            taskViewModel = taskViewModel,
//            onSignOut = authViewModel::signOut
//        )
    }
}

/**
 * Navigation de l'application elle-même. Elle n'existe que pendant qu'un utilisateur
 * est connecté : à la déconnexion elle disparaît, et la suivante repart de la liste.
 */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun SignedInContent(
//    taskViewModel: TaskViewModel,
//    onSignOut: () -> Unit
//) {
//    val snackbarHostState = remember { SnackbarHostState() }
//    val navController = rememberNavController()
//    val state by taskViewModel.uiState.collectAsStateWithLifecycle()
//
//    ObserveEvents(
//        events = taskViewModel.events,
//        snackbarHostState = snackbarHostState,
//        onCloseScreen = {
//            if (navController.currentDestination?.route == Routes.TASK_DETAIL) {
//                navController.popBackStack()
//            }
//        }
//    )
//
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val isDetail = backStackEntry?.destination?.route == Routes.TASK_DETAIL
//    val detailId = backStackEntry?.arguments?.getString("taskId")
//
//    val title = if (isDetail) {
//        detailId?.let { state.taskById(it)?.title } ?: "Détail de la tâche"
//    } else {
//        "Mes tâches"
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(title) },
//                navigationIcon = {
//                    if (isDetail) {
//                        TextButton(onClick = { navController.popBackStack() }) {
//                            Text("←")
//                        }
//                    }
//                },
//                actions = {
//                    if (!isDetail) {
//                        TextButton(onClick = onSignOut) {
//                            Text("Déconnexion")
//                        }
//                    }
//                }
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = Routes.TASK_LIST,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(Routes.TASK_LIST) {
//                TaskListScreen(
//                    state = state,
//                    onAddTask = taskViewModel::addTask,
//                    onToggleTask = taskViewModel::toggleTask,
//                    onTaskClick = { id -> navController.navigate(Routes.taskDetail(id)) }
//                )
//            }
//
//            composable(
//                route = Routes.TASK_DETAIL,
//                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
//            ) { entry ->
//                val id = entry.arguments?.getString("taskId").orEmpty()
//                TaskDetailScreen(
//                    task = state.taskById(id),
//                    isLoading = state.isLoading,
//                    onToggle = { taskViewModel.toggleTask(id) },
//                    // La fermeture de l'écran vient de l'événement du ViewModel.
//                    onDelete = { taskViewModel.deleteTask(id) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun ObserveEvents(
//    events: Flow<UiEvent>,
//    snackbarHostState: SnackbarHostState,
//    onCloseScreen: () -> Unit = {}
//) {
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val currentOnCloseScreen by rememberUpdatedState(onCloseScreen)
//
//    LaunchedEffect(events, lifecycleOwner) {
//        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            events.collect { event ->
//                if (event.closeScreen) {
//                    currentOnCloseScreen()
//                }
//                event.message?.let { message ->
//                    // Lancé à part : un message affiché ne bloque pas les événements suivants.
//                    launch {
//                        snackbarHostState.currentSnackbarData?.dismiss()
//                        snackbarHostState.showSnackbar(message)
//                    }
//                }
//            }
//        }
//    }
//}