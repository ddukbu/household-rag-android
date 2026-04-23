package com.example.householdrag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.*
import com.example.householdrag.api.ApiClient
import com.example.householdrag.auth.AuthRepository
import com.example.householdrag.auth.AuthSessionEvents
import com.example.householdrag.auth.AuthTokenStore
import com.example.householdrag.auth.FirebaseAuthManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// 화면의 종류를 정의 (탭 메뉴)
enum class Screen { LIST, ADD, ASK }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        setContent { MaterialTheme { HouseholdApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isAuthenticated by remember { mutableStateOf(AuthTokenStore.hasAccessToken(context)) }

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var authLoading by remember { mutableStateOf(false) }
    var authMessage by remember { mutableStateOf<String?>(null) }

    // --- 내비게이션 상태 변수 ---
    var currentScreen by remember { mutableStateOf(Screen.LIST) }

    // --- 데이터 상태 변수 ---
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var statusMessage by remember { mutableStateOf("준비됨") }
    var editId by remember { mutableStateOf<String?>(null) }

    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    // --- 로직 함수 ---
    fun clearForm() {
        editId = null; date = ""; category = ""; amount = ""; paymentMethod = ""; place = ""; memo = ""
    }

    suspend fun refreshExpenses() {
        try {
            expenses = ApiClient.api.getExpenses()
            statusMessage = "목록 업데이트 성공"
        } catch (e: Exception) { statusMessage = "불러오기 실패" }
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            refreshExpenses()
        } else {
            expenses = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        AuthSessionEvents.sessionExpired.collectLatest {
            AuthTokenStore.clear(context)
            FirebaseAuthManager.logout()
            isAuthenticated = false
            authMessage = "세션이 만료되었습니다. 다시 로그인해 주세요."
            statusMessage = "세션 만료"
        }
    }

    fun resetAuthForm() {
        email = ""
        password = ""
        confirmPassword = ""
    }

    fun handleAuthAction() {
        if (email.isBlank() || password.isBlank()) {
            authMessage = "이메일과 비밀번호를 입력해 주세요."
            return
        }
        if (!isLoginMode && password != confirmPassword) {
            authMessage = "비밀번호 확인이 일치하지 않습니다."
            return
        }

        authLoading = true
        authMessage = null

        if (isLoginMode) {
            AuthRepository.loginAndSetFirebaseIdToken(context, email.trim(), password) { success, error ->
                scope.launch {
                    authLoading = false
                    if (success) {
                        isAuthenticated = true
                        authMessage = null
                        resetAuthForm()
                    } else {
                        authMessage = error ?: "로그인에 실패했습니다."
                    }
                }
            }
        } else {
            AuthRepository.signUpAndInitProfile(context, email.trim(), password) { success, error ->
                scope.launch {
                    authLoading = false
                    if (success) {
                        isAuthenticated = true
                        authMessage = error
                        resetAuthForm()
                    } else {
                        authMessage = error ?: "회원가입에 실패했습니다."
                    }
                }
            }
        }
    }

    if (!isAuthenticated) {
        AuthForm(
            isLoginMode = isLoginMode,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            loading = authLoading,
            message = authMessage,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onToggleMode = {
                isLoginMode = !isLoginMode
                authMessage = null
            },
            onSubmit = { handleAuthAction() }
        )
        return
    }

    // --- UI 조립 ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HouseHold RAG") },
                actions = {
                    TextButton(
                        onClick = {
                            AuthTokenStore.clear(context)
                            FirebaseAuthManager.logout()
                            isAuthenticated = false
                            currentScreen = Screen.LIST
                            clearForm()
                            answer = ""
                            statusMessage = "로그아웃됨"
                        }
                    ) {
                        Text("로그아웃")
                    }
                }
            )
        },
        bottomBar = {
            // 하단 탭 메뉴 바
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.LIST,
                    onClick = { currentScreen = Screen.LIST },
                    label = { Text("목록") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.ADD,
                    onClick = { currentScreen = Screen.ADD },
                    label = { Text("추가") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.ASK,
                    onClick = { currentScreen = Screen.ASK },
                    label = { Text("분석") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        // 현재 선택된 탭(currentScreen)에 따라 화면 전환
        Box(modifier = Modifier.padding(innerPadding).padding(12.dp)) {
            when (currentScreen) {
                Screen.LIST -> {
                    // [목록 탭]
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { Text("상태: $statusMessage", style = MaterialTheme.typography.bodySmall) }
                        item { Text("가계부 목록", style = MaterialTheme.typography.headlineSmall) }
                        items(expenses) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                onEditClick = {
                                    // 수정 모드 셋팅 후 '추가' 탭으로 이동
                                    editId = expense.id; date = expense.date; category = expense.category
                                    amount = expense.amount.toString(); paymentMethod = expense.payment_method
                                    place = expense.place; memo = expense.memo
                                    currentScreen = Screen.ADD
                                },
                                onDeleteClick = {
                                    scope.launch {
                                        ApiClient.api.deleteExpense(expense.id)
                                        refreshExpenses()
                                    }
                                }
                            )
                        }
                    }
                }
                Screen.ADD -> {
                    // [추가/수정 탭]
                    ExpenseInputCard(
                        editId = editId,
                        date = date, onDateChange = { date = it },
                        category = category, onCategoryChange = { category = it },
                        amount = amount, onAmountChange = { amount = it },
                        paymentMethod = paymentMethod, onPaymentChange = { paymentMethod = it },
                        place = place, onPlaceChange = { place = it },
                        memo = memo, onMemoChange = { memo = it },
                        onSaveClick = {
                            scope.launch {
                                val req = ExpenseRequest(date, category, amount.toIntOrNull() ?: 0, paymentMethod, place, memo)
                                if (editId == null) ApiClient.api.createExpense(req)
                                else editId?.let { ApiClient.api.updateExpense(it, req) }
                                clearForm()
                                refreshExpenses()
                                currentScreen = Screen.LIST // 저장 후 목록으로 이동
                            }
                        },
                        onResetClick = { clearForm() }
                    )
                }
                Screen.ASK -> {
                    // [분석 탭]
                    AskSectionCard(
                        question = question,
                        onQuestionChange = { question = it },
                        answer = answer,
                        onAskClick = {
                            scope.launch {
                                val res = ApiClient.api.ask(AskRequest(question))
                                answer = "${res.answer}\n\n참고: ${res.references.joinToString(", ")}"
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthForm(
    isLoginMode: Boolean,
    email: String,
    password: String,
    confirmPassword: String,
    loading: Boolean,
    message: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isLoginMode) "로그인" else "회원가입",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("이메일") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("비밀번호 확인") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onSubmit,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "처리 중..." else if (isLoginMode) "로그인" else "회원가입")
                }

                TextButton(
                    onClick = onToggleMode,
                    enabled = !loading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isLoginMode) "계정이 없나요? 회원가입" else "이미 계정이 있나요? 로그인")
                }
            }
        }
    }
}
// 미리보기
@Preview(showSystemUi = true, name = "앱 화면 (기기 형태)")
@Composable
fun FullAppPreview() {
    MaterialTheme {
        HouseholdApp()
    }
}