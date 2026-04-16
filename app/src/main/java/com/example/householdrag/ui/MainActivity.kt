package com.example.householdrag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.*
import com.example.householdrag.api.ApiClient
import kotlinx.coroutines.launch

// 화면의 종류 정의
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
    val scope = rememberCoroutineScope()

    // --- 내비게이션 및 로딩 상태 변수 ---
    var currentScreen by remember { mutableStateOf(Screen.LIST) }
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
            TopAppBar(
                title = { Text("HouseHold RAG") },
                // 상단바 우측에 새로고침 아이콘 추가
                actions = {
                    IconButton(onClick = { scope.launch { refreshExpenses() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        },
        // 화면 우측 하단에 떠 있는 '추가' 버튼
        floatingActionButton = {
            if (currentScreen != Screen.ADD) {
                FloatingActionButton(
                    onClick = {
                        clearForm() // 새 데이터를 쓰기 위해 폼 초기화
                        currentScreen = Screen.ADD
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    // 글씨 없이 플러스 아이콘만 넣기
                    Icon(Icons.Default.Add, contentDescription = "추가")
                }
            }
        },
        bottomBar = {
            // 하단 바는 '목록'과 '분석' 2개 탭으로 구성
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.LIST,
                    onClick = { currentScreen = Screen.LIST },
                    label = { Text("목록") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Box(
                modifier = Modifier
                    //.padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 10.dp)
            ) {
                when (currentScreen) {
                    Screen.LIST -> {
                        // [목록 탭]
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text(
                                    "상태: $statusMessage",
                                    style = MaterialTheme.typography.bodySmall
                                )
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
                                scope.launch {
                                    try {
                                        val res = ApiClient.api.ask(AskRequest(question))
                                        answer =
                                            "${res.answer}\n\n참고: ${res.references.joinToString(", ")}"
                                    } catch (e: Exception) {
                                        statusMessage = "질문 실패"
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
    MaterialTheme {
        HouseholdApp()
    }
}