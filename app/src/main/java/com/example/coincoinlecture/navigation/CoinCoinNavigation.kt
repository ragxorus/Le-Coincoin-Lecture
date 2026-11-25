package com.example.coincoinlecture.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.coincoinlecture.model.GroupLoan
import com.example.coincoinlecture.ui.view.AccountCreationView
import com.example.coincoinlecture.ui.view.AccountView
import com.example.coincoinlecture.ui.view.BookFormView
import com.example.coincoinlecture.ui.view.BookLoanView
import com.example.coincoinlecture.ui.view.BookView
import com.example.coincoinlecture.ui.view.GroupFormView
import com.example.coincoinlecture.ui.view.GroupView
import com.example.coincoinlecture.ui.view.JoinGroupView
import com.example.coincoinlecture.ui.view.LoginView
import com.example.coincoinlecture.ui.view.NewLoanDialog
import com.example.coincoinlecture.viewmodel.AuthViewModel
import com.example.coincoinlecture.viewmodel.FireBaseViewModel

private const val BOOK = "book"
private const val BOOK_FORM = "book_form"
private const val BOOK_LOAN = "book/{id}"
private const val SCAN = "scan"
private const val GROUP = "group"
private const val GROUP_FORM = "group_form"
private const val ACCOUNT = "account"


@Composable
fun CoinCoinNavigation(
    authRepository: AuthRepository,
    dbRepository: FireBaseRepository,
    pendingGroupId: String?,
    onJoinHandled: () -> Unit
) {

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
    val userId by authViewModel.userId.collectAsStateWithLifecycle()

    val fireBaseViewModel: FireBaseViewModel =
        viewModel(factory = FireBaseViewModel.factory(authRepository, dbRepository))
    val userExist by fireBaseViewModel.userExist.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val uid = userId

    when {
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
        else -> MainApp(uid, authViewModel, fireBaseViewModel, pendingGroupId, onJoinHandled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    userId: String,
    authViewModel: AuthViewModel,
    fireBaseViewModel: FireBaseViewModel,
    pendingGroupId: String?,
    onJoinHandled: () -> Unit
){
    val context = LocalContext.current
    val nav = rememberNavController()
    val navStart by nav.currentBackStackEntryAsState()
    val route = navStart?.destination?.route

    val account by fireBaseViewModel.accountState.collectAsStateWithLifecycle()
    val groups by fireBaseViewModel.groups.collectAsStateWithLifecycle()
    val groupLoans by fireBaseViewModel.groupLoans.collectAsStateWithLifecycle()
    val borrowedLoans by fireBaseViewModel.borrowedLoans.collectAsStateWithLifecycle()

    // prêts dont l'utilisateur n'a pas encore été averti (on retient ceux déjà vus sur l'appareil)
    val prefs = remember { context.getSharedPreferences("seen_loans", Context.MODE_PRIVATE) }
    var newLoans by remember { mutableStateOf<List<GroupLoan>>(emptyList()) }
    LaunchedEffect(borrowedLoans) {
        val seen = prefs.getStringSet("keys", emptySet()) ?: emptySet()
        newLoans = borrowedLoans.filter { it.key !in seen }
    }
    if (newLoans.isNotEmpty()) {
        NewLoanDialog(
            loans = newLoans,
            onDismiss = {
                val seen = (prefs.getStringSet("keys", emptySet()) ?: emptySet()).toMutableSet()
                seen.addAll(newLoans.map { it.key })
                prefs.edit().putStringSet("keys", seen).apply()
                newLoans = emptyList()
            }
        )
    }

    // ouvre l'écran d'invitation quand on arrive via un lien
    LaunchedEffect(pendingGroupId) {
        if (pendingGroupId != null) {
            nav.navigate("join/$pendingGroupId")
            onJoinHandled()
        }
    }

    fun navigateTo(destination: String) {
        nav.navigate(destination) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBar = route in setOf(BOOK, SCAN, GROUP)

    Scaffold(
        topBar = {
            if (showBar) {
                CenterAlignedTopAppBar(
                    title = { Text("") },
                    actions = {
                        IconButton(
                            onClick = { navigateTo(ACCOUNT) },
                            modifier = Modifier.size(76.dp)          // zone cliquable
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profil",
                                modifier = Modifier.size(40.dp)      // taille de l'icône
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBar) {
                NavigationBar {
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
        ) {
            composable(SCAN) {

            }
            composable(GROUP) {
                GroupView(
                    groups = groups,
                    loans = groupLoans,
                    currentUid = userId,
                    onCreateGroup = { navigateTo(GROUP_FORM) },
                    onAddMember = fireBaseViewModel::addMember,
                    onRemoveMember = fireBaseViewModel::removeMember,
                    onDeleteGroup = fireBaseViewModel::deleteGroup,
                    onClaim = { groupId, memberId ->
                        fireBaseViewModel.claimMember(groupId, memberId) { error ->
                            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        }
                    },
                    onJoinWithCode = { code -> nav.navigate("join/$code") }
                )
            }
            composable(
                route = "join/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { entry ->
                val groupId = entry.arguments?.getString("groupId").orEmpty()
                val joinState by fireBaseViewModel.join.collectAsStateWithLifecycle()
                LaunchedEffect(groupId) { fireBaseViewModel.loadJoin(groupId) }

                JoinGroupView(
                    state = joinState,
                    currentUid = userId,
                    onClaim = { memberId ->
                        fireBaseViewModel.claimMember(groupId, memberId) { error ->
                            if (error == null) navigateTo(GROUP)
                            else Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    },
                    onCancel = { navigateTo(GROUP) }
                )
            }
            composable(GROUP_FORM) {
                GroupFormView(
                    onCreate = fireBaseViewModel::newGroup,
                    onDone = { navigateTo(GROUP) }
                )
            }
            composable(BOOK) {
                BookView(
                    accountState = account,
                    groups = groups,
                    currentUid = userId,
                    borrowedLoans = borrowedLoans,
                    onAdd = { navigateTo(BOOK_FORM) },
                    onDeleteBook = fireBaseViewModel::deleteBook,
                    onLend = fireBaseViewModel::lendBook,
                    onLendToUser = { id -> navigateTo("book/$id") },
                    onReturn = fireBaseViewModel::returnBook,
                    updateBookLoan = fireBaseViewModel::updateBookLoan
                )
            }
            composable(BOOK_FORM) {
                BookFormView(
                    fireBaseViewModel::newBook,
                    { navigateTo(BOOK) }
                )
            }
            composable(ACCOUNT) {
                AccountView(
                    account,
                    authViewModel::signOut
                )
            }
            composable(
                route = BOOK_LOAN,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { input ->
                val id = input.arguments?.getString("id")
                if (id != null) {
                    val pseudoTaken by fireBaseViewModel.pseudoTaken.collectAsStateWithLifecycle()
                    BookLoanView(
                        id,
                        pseudoTaken,
                        account,
                        fireBaseViewModel::updateBookLoan,
                        fireBaseViewModel::checkPseudo,
                        { navigateTo(BOOK) }
                    )
                }
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