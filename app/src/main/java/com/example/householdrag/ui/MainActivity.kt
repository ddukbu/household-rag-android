package com.example.householdrag.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.*
import com.example.householdrag.api.ApiClient
import com.example.householdrag.auth.AuthRepository
import com.example.householdrag.auth.AuthTokenStore
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import kotlinx.coroutines.launch

// 화면의 종류 정의
enum class Screen { LOGIN, SIGNUP, LIST, ADD, ASK }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        setContent {
            HouseholdRAGTheme {
                // 이 안에서 실행되는 모든 화면(HouseholdApp)이 흰색 배경이 됩니다!
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // 테마에 설정한 흰색을 가져옴
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
    val TAG = "HouseholdApp" // 로그 식별자

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isAuthenticated by remember { mutableStateOf(false) }

    // --- 내비게이션 및 로딩 상태 변수 ---
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
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

    // --- 로직 함수 ---
    fun clearForm() {
        editId = null; date = ""; category = ""; amount = ""; paymentMethod = ""; place = ""; memo =
            ""
    }

    // 목록 새로고침 함수
    suspend fun refreshExpenses() {
        isLoading = true // 로딩 켜기
        try {
            expenses = ApiClient.api.getExpenses()
            statusMessage = "목록 업데이트 성공"
        } catch (e: Exception) {
            statusMessage = "불러오기 실패"
            //println("LogTest: 에러 발생 -> ${e.message}") // 에러 내용 확인용
        } finally {
            isLoading = false // 로딩 끄기
        }
    }

    LaunchedEffect(Unit) { refreshExpenses() }

    // --- UI 조립 ---
    Scaffold(
        topBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                TopAppBar(
                    title = { Text("HouseHold RAG") },
                    actions = {
                        TextButton(onClick = {
                            AuthTokenStore.clear(context)
                            // isAuthenticated = false
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
        // 화면 우측 하단에 떠 있는 '추가' 버튼
        floatingActionButton = {
            if (currentScreen == Screen.LIST) {
                FloatingActionButton(
                    onClick = {
                        clearForm();
                        currentScreen = Screen.ADD
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가")
                }
            }
        },
        bottomBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                Column {
                    // 상단과 구분해주는 얇은 노란색 선
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )

                    // 하단 바의 색상을 직접 지정
                    NavigationBar(
                        containerColor = Color.White, // 배경은 하얗게
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.LIST,
                            onClick = { currentScreen = Screen.LIST },
                            label = { Text("목록") },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            // 선택되었을 때의 노란색 포인트 설정
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary, // 선택시 노란색
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // 아이콘 뒤에 생기는 동그라미도 연한 노란색으로!
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.ASK,
                            onClick = { currentScreen = Screen.ASK },
                            label = { Text("분석") },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
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

            Box(
                modifier = Modifier
                    //.padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
            ) {
                when (currentScreen) {
                    Screen.LOGIN -> {
                        LoginScreen(
                            onLoginClick = { email, pw ->
                                isLoading = true
                                AuthRepository.loginAndSetFirebaseIdToken( context, email.trim(), pw) { success, error ->
                                    scope.launch {
                                        isLoading = false
                                        if (success) {
                                            isAuthenticated = true
                                            currentScreen = Screen.LIST
                                            refreshExpenses()
                                        } else {
                                            // 실패하면 왜 실패했는지 statusMessage
                                            statusMessage = error ?: "로그인 실패: 정보를 확인하세요."
                                        }
                                    }
                                }
                                // currentScreen = Screen.LIST
                            },
                            onSignUpClick = { currentScreen = Screen.SIGNUP }
                        )
                    }

                    Screen.SIGNUP -> {
                        SignUpScreen(
                            onSignUpClick = { email, pw ->
                                isLoading = true
                                AuthRepository.signUpAndInitProfile( context, email.trim(), pw) { success, error ->
                                    scope.launch {
                                        isLoading = false
                                        if (success) {
                                            currentScreen = Screen.LOGIN
                                        } else {
                                            // 에러 표시
                                            statusMessage = error ?: "회원가입 실패"
                                        }
                                    }
                                }
                                // currentScreen = Screen.LOGIN    // 회원가입 후 로그인 화면으로 이동
                            },
                            onBackToLogin = { currentScreen = Screen.LOGIN }
                        )
                    }

                    Screen.LIST -> {
                        // [목록 탭]
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
//                                Text(
//                                    "상태: $statusMessage",
//                                    style = MaterialTheme.typography.bodySmall
//                                )
                            }
                            item { Text("가계부 목록", style = MaterialTheme.typography.headlineSmall) }
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
                                            ApiClient.api.deleteExpense(expense.id)
                                            refreshExpenses()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Screen.ADD -> {
                        // [추가/수정 화면]
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
                                    try {
                                        val req = ExpenseRequest(
                                            date,
                                            category,
                                            amount.toIntOrNull() ?: 0,
                                            paymentMethod,
                                            place,
                                            memo
                                        )
                                        if (editId == null) ApiClient.api.createExpense(req)
                                        else editId?.let { ApiClient.api.updateExpense(it, req) }
                                        clearForm()
                                        refreshExpenses()
                                        currentScreen = Screen.LIST // 저장 후 목록으로 자동 이동
                                    } catch (e: Exception) {
                                        statusMessage = "저장 실패"
                                    }
                                }
                            },
                            onResetClick = {
                                clearForm()
                                currentScreen = Screen.LIST // 취소 느낌으로 목록 이동
                            }
                        )
                    }

                    Screen.ASK -> {
                        // [분석 탭]
                        AskSectionCard(
                            question = question,
                            onQuestionChange = { question = it },
                            answer = answer,
                            onAskClick = {
                                if (question.isBlank()) return@AskSectionCard // 질문이 비었으면 무시

                                scope.launch {
                                    isLoading = true
                                    Log.d(TAG, "AI 질문 전송: $question")
                                    try {
                                        val res = ApiClient.api.ask(AskRequest(question))
                                        Log.d(TAG, "AI 응답 수신 성공")
                                        answer =
                                            "${res.answer}\n\n참고: ${res.references.joinToString(", ")}"
                                    } catch (e: Exception) {
                                        Log.e(TAG, "질문 API 호출 에러: ${e.message}", e)
                                        // statusMessage = "질문 실패"
                                        answer = "죄송해요, 답변을 가져오지 못했어요."
                                    }finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                }
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
            .background(Color.Black.copy(alpha = 0.23f)), // 배경을 어둡게
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator() // 빙글빙글 돌아가는 아이콘
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