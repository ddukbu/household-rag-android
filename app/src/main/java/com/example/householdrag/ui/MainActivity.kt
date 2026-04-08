package com.example.householdrag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.ApiClient
import com.example.householdrag.api.AskRequest
import com.example.householdrag.api.Expense
import com.example.householdrag.api.ExpenseRequest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HouseholdApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun HouseholdApp() {
    val scope = rememberCoroutineScope()

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

    fun clearForm() {
        editId = null
        date = ""
        category = ""
        amount = ""
        paymentMethod = ""
        place = ""
        memo = ""
    }

    suspend fun refreshExpenses() {
        try {
            val result = ApiClient.api.getExpenses()
            expenses = result
            statusMessage = "가계부 목록 불러오기 성공: ${result.size}개"
        } catch (e: Exception) {
            statusMessage = "목록 불러오기 실패: ${e.javaClass.simpleName} / ${e.message}"
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        refreshExpenses()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("HouseHold RAG") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("상태: $statusMessage")
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (editId == null) "가계부 입력" else "가계부 수정 (ID: $editId)",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("날짜 (예: 2026-03-05)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("카테고리 (예: 식비)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("금액") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            label = { Text("결제수단 (예: 카드)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = place,
                            onValueChange = { place = it },
                            label = { Text("사용처") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            label = { Text("메모") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val request = ExpenseRequest(
                                                date = date,
                                                category = category,
                                                amount = amount.toIntOrNull() ?: 0,
                                                payment_method = paymentMethod,
                                                place = place,
                                                memo = memo
                                            )

                                            if (editId == null) {
                                                ApiClient.api.createExpense(request)
                                                statusMessage = "저장 성공"
                                            } else {
                                                editId?.let {
                                                    ApiClient.api.updateExpense(it, request)
                                                    statusMessage = "수정 성공"
                                                } ?: run {
                                                    statusMessage = "수정 실패: editId가 null"
                                                }
                                            }

                                            clearForm()
                                            refreshExpenses()
                                        } catch (e: Exception) {
                                            statusMessage = "저장/수정 실패: ${e.javaClass.simpleName} / ${e.message}"
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Text(if (editId == null) "저장" else "수정")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(onClick = { clearForm() }) {
                                Text("초기화")
                            }
                        }
                    }
                }
            }

            item {
                Text("가계부 목록", style = MaterialTheme.typography.titleMedium)
            }

            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${expense.date} / ${expense.category} / ${expense.amount}원")
                        Text("결제수단: ${expense.payment_method}")
                        Text("사용처: ${expense.place}")
                        Text("메모: ${expense.memo}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Button(
                                onClick = {
                                    editId = expense.id
                                    date = expense.date
                                    category = expense.category
                                    amount = expense.amount.toString()
                                    paymentMethod = expense.payment_method
                                    place = expense.place
                                    memo = expense.memo
                                    statusMessage = "수정할 항목을 불러왔습니다. ID: ${expense.id}"
                                }
                            ) {
                                Text("수정")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            ApiClient.api.deleteExpense(expense.id)
                                            statusMessage = "삭제 성공"
                                            refreshExpenses()
                                        } catch (e: Exception) {
                                            statusMessage = "삭제 실패: ${e.javaClass.simpleName} / ${e.message}"
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Text("삭제")
                            }
                        }
                    }
                }
            }

            item {
                Divider()
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("소비 분석 질문", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            label = { Text("질문 입력") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val response = ApiClient.api.ask(AskRequest(question))
                                        answer = response.answer + "\n\n참고: " +
                                                response.references.joinToString(", ")
                                        statusMessage = "질문 처리 성공"
                                    } catch (e: Exception) {
                                        answer = ""
                                        statusMessage = "질문 처리 실패: ${e.javaClass.simpleName} / ${e.message}"
                                        e.printStackTrace()
                                    }
                                }
                            }
                        ) {
                            Text("질문하기")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("답변", style = MaterialTheme.typography.titleMedium)
                        Text(answer.ifBlank { "아직 답변이 없습니다." })
                    }
                }
            }
        }
    }
}