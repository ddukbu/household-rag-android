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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.*
import com.example.householdrag.api.ApiClient
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
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(Unit) { refreshExpenses() }

    // --- UI 조립 ---
    Scaffold(
        topBar = { TopAppBar(title = { Text("HouseHold RAG") }) },
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
// 미리보기
@Preview(showSystemUi = true, name = "앱 화면 (기기 형태)")
@Composable
fun FullAppPreview() {
    MaterialTheme {
        HouseholdApp()
    }
}