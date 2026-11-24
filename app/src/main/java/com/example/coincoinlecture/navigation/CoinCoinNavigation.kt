package com.example.coincoinlecture.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.data.FireBaseRepository
import com.example.coincoinlecture.ui.view.AccountCreationView
import com.example.coincoinlecture.ui.view.LoginView
import com.example.coincoinlecture.viewmodel.AuthViewModel
import com.example.coincoinlecture.viewmodel.FireBaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.navigation.NavHost
import com.example.coincoinlecture.ui.view.BookFormView
import com.example.coincoinlecture.ui.view.BookView

private const val BOOK = "book"
private const val BOOK_FORM = "book_form"
private const val SCAN = "scan"
private const val GROUP = "group"


@Composable
fun CoinCoinNavigation(
    authRepository: AuthRepository,
    dbRepository: FireBaseRepository
) {

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
    val userId by authViewModel.userId.collectAsStateWithLifecycle()

    val fireBaseViewModel: FireBaseViewModel =
        viewModel(factory = FireBaseViewModel.factory(authRepository, dbRepository))
    val userExist by fireBaseViewModel.userExist.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val uid = userId

    val target = when {
        uid == null -> LoginView(
            onSignIn = {
                authViewModel.signIn(context) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        )
        userExist == null -> LoadingScreen()
        userExist == false -> {
            val pseudoTaken by fireBaseViewModel.pseudoTaken.collectAsStateWithLifecycle()
            AccountCreationView(
                pseudoTaken,
                fireBaseViewModel::checkPseudo,
                fireBaseViewModel::newUser
            )
        }
        else -> MainApp(uid,authViewModel,fireBaseViewModel)
    }
}

@Composable
fun MainApp(
    userId: String,
    authViewModel: AuthViewModel,
    fireBaseViewModel: FireBaseViewModel
){
    val nav = rememberNavController()
    val navStart by nav.currentBackStackEntryAsState()
    val route = navStart?.destination?.route

    val books by fireBaseViewModel.books.collectAsStateWithLifecycle()

    fun navigateTo(destination: String) {
        nav.navigate(destination) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar = route in setOf(BOOK,SCAN,GROUP)

    Scaffold(
        bottomBar = {
            if(showBottomBar) {
                NavigationBar() {
                    NavigationBarItem(
                        selected = route == SCAN,
                        onClick = { navigateTo(SCAN) },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                        label = { Text("Scan ISBN") }
                    )
                    NavigationBarItem(
                        selected = route == BOOK,
                        onClick = { navigateTo(BOOK) },
                        icon = { Icon(Icons.Default.Book, contentDescription = null) },
                        label = { Text("Livres") }
                    )
                    NavigationBarItem(
                        selected = route == GROUP,
                        onClick = { navigateTo(GROUP) },
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text("Groupe") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = BOOK,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(SCAN){

            }
            composable(GROUP){

            }
            composable(BOOK){
                BookView(
                    books,
                    {navigateTo(BOOK_FORM)},
                    fireBaseViewModel::deleteBook,
                    {}
                )
            }
            composable(BOOK_FORM){
                BookFormView(
                    fireBaseViewModel::newBook,
                    {navigateTo(BOOK)}
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        CircularProgressIndicator()
    }
}

@Composable
fun debugDisconnect(authViewModel : AuthViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = authViewModel::signOut) {
            Text("Déconnexion (debug)")
        }
    }
}


//
//        LaunchedEffect(target) {
//            if (nav.currentDestination?.route != target) {
//                navigateTo(target)
//            }
//        }
//    }

//    if (userId == null) {
//        LoginView(
//            onSignIn = {
//                authViewModel.signIn(context) { message ->
//                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//                }
//            }
//        )
//    } else {
//        when (userExist) {
//            null -> CircularProgressIndicator()
//            false -> AccountCreationView(fireBaseViewModel::newUser)
//            true -> Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("$userId")
//                Button(onClick = authViewModel::signOut) {
//                    Text("Déconnexion (debug)")
//                }
//            }
//        }
//    }


//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("$userId")
//            Button(onClick = authViewModel::signOut) {
//                Text("Déconnexion (debug)")
//            }
//        }
//        SignedInContent(
//            taskViewModel = taskViewModel,
//            onSignOut = authViewModel::signOut
//        )


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