package com.example.householdrag.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.ApiClient
import com.example.householdrag.api.AskRequest
import com.example.householdrag.api.Expense
import com.example.householdrag.auth.AuthRepository
import com.example.householdrag.auth.AuthTokenStore
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import kotlinx.coroutines.launch

enum class Screen { LOGIN, SIGNUP, LIST, CALENDAR, ADD, ASK, BUDGET }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        setContent {
            HouseholdRAGTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HouseholdApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdApp() {
    val TAG = "HouseholdApp"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isAuthenticated by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.ASK) } // 기본은 LOGIN 테스트 시 변경
    var isLoading by remember { mutableStateOf(false) }

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

    var currentYearMonth by remember { mutableStateOf(
        java.time.YearMonth.now().toString() // 기본값: 현재 연-월 ("2026-05")
    )}
    var budgetData by remember { mutableStateOf<BudgetOut?>(null) }

    // --- 로직 함수 ---
    fun clearForm() {
        editId = null; date = ""; category = ""; amount = ""; paymentMethod = ""; place = ""; memo =
            ""
    }

    // todo 현제 날짜?
    // 예산 데이터를 서버에서 가져오는 함수
    fun refreshBudget(yearMonth: String) {
        scope.launch {
            try {
                val response = ApiClient.api.getBudget(yearMonth)
                budgetData = response
            } catch (e: Exception) {
                statusMessage = "예산 데이터를 가져오지 못했습니다."
            }
        }
    }

    suspend fun refreshExpenses() {
        isLoading = true
        try {
            expenses = ApiClient.api.getExpenses()
            statusMessage = "목록 업데이트 성공"
        } catch (e: Exception) {
            statusMessage = "불러오기 실패"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshExpenses() }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                TopAppBar(
                    title = { Text("HouseHold RAG") },
                    actions = {
                        TextButton(onClick = {
                            AuthTokenStore.clear(context)
                            currentScreen = Screen.LOGIN
                            statusMessage = "로그아웃 되었습니다."
                        }) {
                            Text("로그아웃", color = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { scope.launch { refreshExpenses() } }) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.LIST) {
                FloatingActionButton(
                    onClick = { clearForm(); currentScreen = Screen.ADD },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가")
                }
            }
        },
        bottomBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.BUDGET,
                            onClick = { currentScreen = Screen.BUDGET },
                            label = { Text("예산") },
                            icon = { Icon(Icons.Default.Build, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.LIST,
                            onClick = { currentScreen = Screen.LIST },
                            label = { Text("목록") },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.ASK,
                            onClick = { currentScreen = Screen.ASK },
                            label = { Text("분석") },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen(
                    onLoginClick = { email, pw ->
                        isLoading = true
                        AuthRepository.loginAndSetFirebaseIdToken(
                            context,
                            email.trim(),
                            pw
                        ) { success, error ->
                            scope.launch {
                                isLoading = false
                                if (success) {
                                    currentScreen = Screen.LIST; refreshExpenses()
                                } else {
                                    statusMessage = error ?: "로그인 실패"
                                }
                            }
                        }
                    },
                    onSignUpClick = { currentScreen = Screen.SIGNUP }
                )

                Screen.SIGNUP -> SignUpScreen(
                    onSignUpClick = { email, pw ->
                        isLoading = true
                        AuthRepository.signUpAndInitProfile(
                            context,
                            email.trim(),
                            pw
                        ) { success, error ->
                            scope.launch {
                                isLoading = false
                                if (success) currentScreen = Screen.LOGIN
                                else statusMessage = error ?: "회원가입 실패"
                            }
                        }
                    },
                    onBackToLogin = { currentScreen = Screen.LOGIN }
                )

                Screen.LIST -> {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)) {
                        Text("가계부 목록", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(expenses) { expense ->
                                ExpenseItemCard(
                                    expense = expense,
                                    onEditClick = {
                                        editId = expense.id; date = expense.date; category =
                                        expense.category
                                        amount = expense.amount.toString(); paymentMethod =
                                        expense.payment_method
                                        place = expense.place; memo = expense.memo
                                        currentScreen = Screen.ADD
                                    },
                                    onDeleteClick = {
                                        scope.launch {
                                            ApiClient.api.deleteExpense(
                                                expense.id
                                            ); refreshExpenses()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Screen.ADD -> {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)) {
                        ExpenseInputCard(
                            editId = editId, date = date, onDateChange = { date = it },
                            category = category, onCategoryChange = { category = it },
                            amount = amount, onAmountChange = { amount = it },
                            paymentMethod = paymentMethod, onPaymentChange = { paymentMethod = it },
                            place = place, onPlaceChange = { place = it },
                            memo = memo, onMemoChange = { memo = it },
                            onResetClick = { clearForm(); currentScreen = Screen.LIST },

                            // 지출 저장 로직 (ExpenseRequest 처리)
                            onSaveExpense = { expenseReq ->
                                scope.launch {
                                    try {
                                        if (editId == null) {
                                            ApiClient.api.createExpense(expenseReq) // 신규 지출
                                        } else {
                                            editId?.let { ApiClient.api.updateExpense(it, expenseReq) } // 지출 수정
                                        }
                                        clearForm()
                                        refreshExpenses()
                                        currentScreen = Screen.LIST
                                    } catch (e: Exception) {
                                        statusMessage = "지출 저장 실패"
                                    }
                                }
                            },

                            // 수입 저장 로직 (IncomeIn 처리)
                            onSaveIncome = { incomeReq ->
                                scope.launch {
                                    try {
                                        // 수입은 IncomeApiService를 사용해야 합니다
                                        if (editId == null) {
                                            ApiClient.api.createIncome(incomeReq) // 신규 수입
                                        } else {
                                            editId?.let { ApiClient.api.updateIncome(it, incomeReq) } // 수입 수정
                                        }
                                        clearForm()
                                        refreshExpenses() // 목록 새로고침 (수입도 포함되게 API 확인 필요!)
                                        currentScreen = Screen.LIST
                                    } catch (e: Exception) {
                                        statusMessage = "수입 저장 실패"
                                    }
                                }
                            }
                        )
                    }
                }

                Screen.ASK -> {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)) {
                        AskSectionCard(
                            question = question, onQuestionChange = { question = it },
                            answer = answer,
                            onAskClick = {
                                if (question.isBlank()) return@AskSectionCard
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val res = ApiClient.api.ask(AskRequest(question))
                                        Log.d(TAG, "AI 응답 수신 성공")

                                        val rSecText = res.retrieval_seconds?.toString().orEmpty()
                                        val gSecText = res.generation_seconds?.toString().orEmpty()
                                        val tSecText = res.total_seconds?.toString().orEmpty()

                                        answer = buildString {
                                            append(res.answer.orEmpty())
                                            if (rSecText.isNotEmpty() || gSecText.isNotEmpty() || tSecText.isNotEmpty()) {
                                                append("\n\n시간: r=")
                                                append(rSecText.ifEmpty { "-" })
                                                append(", g=")
                                                append(gSecText.ifEmpty { "-" })
                                                append(", t=")
                                                append(tSecText.ifEmpty { "-" })
                                            }
                                            if (res.references.isNotEmpty()) {
                                                append("\n\n참고: ")
                                                append(res.references.joinToString(", "))
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "질문 API 호출 에러: ${e.message}", e)
                                        answer = "죄송해요, 답변을 가져오지 못했어요."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                }

                Screen.BUDGET -> {
                    LaunchedEffect(currentYearMonth) {
                        refreshBudget(currentYearMonth)
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        BudgetScreen(
                            currentYM = currentYearMonth,
                            budgetData = budgetData,
                            onMonthChange = { newYM ->
                                currentYearMonth = newYM // 화살표 클릭 시 상태 업데이트
                            }
                        )
                    }
                }

                Screen.CALENDAR -> CalendarScreen(expenses = expenses)
            }

            if (isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.23f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showSystemUi = true, name = "앱 화면 (기기 형태)")
@Composable
fun FullAppPreview() {
    HouseholdRAGTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HouseholdApp()
        }
    }
}