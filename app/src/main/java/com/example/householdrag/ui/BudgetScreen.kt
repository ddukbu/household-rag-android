package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.model.FixedExpenseItem
import com.example.householdrag.model.FixedIncomeItem
import com.example.householdrag.ui.theme.MonthSelector
import com.example.householdrag.ui.theme.formatAmount

@Composable
fun BudgetScreen(
    currentYM: String,
    budgetData: BudgetOut?,
    fixedIncomeTotal: Int,
    fixedExpenseTotal: Int,
    fixedIncomeList: List<FixedIncomeItem>,  // 고정 수입 원본 리스트 수신
    fixedExpenseList: List<FixedExpenseItem>,// 고정 지출 원본 리스트 수신
    onIncomeRowClick: () -> Unit,
    onExpenseRowClick: () -> Unit,
    onAddFixedIncome: (String, Int, String) -> Unit, // 고정 수입 추가 콜백
    onAddFixedExpense: (String, Int, String) -> Unit,// 고정 지출 추가 콜백
    onUpdateFixedIncome: (String, String, Int, String) -> Unit, // id, category, amount, memo
    onDeleteFixedIncome: (String) -> Unit,
    onUpdateFixedExpense: (String, String, Int, String) -> Unit, // id, category, amount, memo
    onDeleteFixedExpense: (String) -> Unit,
    onMonthChange: (String) -> Unit
) {
    // 서버 데이터가 오기 전에는 로딩 UI를 보여주거나 빈 화면
    if (budgetData == null) {
        LoadingOverlay()
        return
    }

    // 월이 바뀔 때마다 실행됨
    LaunchedEffect(currentYM) {
        onMonthChange(currentYM)
    }

    // 팝업 가시성을 제어하는 로컬 스위치 상태
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
    ) {
        MonthSelector(currentYM = currentYM, onMonthChange = onMonthChange)

        Spacer(modifier = Modifier.height(10.dp))

        // 메인 요약 카드 호출
        BudgetSummaryCard(
            totalBudget = budgetData.total_budget,
            saving = budgetData.saving,
            state = budgetData.state,
            fixedIncome = fixedIncomeTotal,
            fixedExpense = fixedExpenseTotal,
            onIncomeRowClick = {
                onIncomeRowClick()
                showIncomeDialog = true
            },
            onExpenseRowClick = {
                onExpenseRowClick()
                showExpenseDialog = true
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "카테고리별 자산 현황",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        budgetData.budget_details.forEach { (category, limit) ->
            val remaining = budgetData.remaining_budget_details[category] ?: 0
            val spent = limit - remaining

            BudgetProgressItem(
                category = category,
                budget = limit,
                spent = spent,
                remaining = remaining,
                onClick = { /* TODO: 클릭 시 수정 API 호출 */ }
            )
        }
    }

    // 고정 수익 리스트
    if (showIncomeDialog) {
        var editingId by remember { mutableStateOf<String?>(null) }
        var inputCategory by remember { mutableStateOf("") }
        var inputAmount by remember { mutableStateOf("") }
        var inputMemo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = {
                Text(
                    if (editingId == null) "고정 수익 내역 관리" else "고정 수익 항목 수정",
                    fontWeight = FontWeight.Bold
                )
            }, text = {
                Column {
                    if (editingId == null) {
                        Text(
                            "이번 달 등록된 고정 수익 목록입니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 목록 표시 구역
                        LazyColumn(modifier = Modifier.height(130.dp)) {
                            items(fixedIncomeList) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.category, fontWeight = FontWeight.Medium)
                                        Text(
                                            "+ ${formatAmount(item.amount)}원",
                                            color = Color(0xFF4CAF50),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    IconButton(onClick = {
                                        editingId = item.id
                                        inputCategory = item.category
                                        inputAmount = item.amount.toString()
                                        inputMemo = item.memo
                                    }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "수정",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteFixedIncome(item.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        if (editingId == null) "새 고정 수익 항목 추가" else "선택한 항목 내용 변경",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("카테고리 (예: 월급, 용돈)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("금액") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputMemo,
                        onValueChange = { inputMemo = it },
                        label = { Text("메모") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = inputAmount.toIntOrNull() ?: 0
                        if (inputCategory.isNotBlank() && amt > 0) {
                            val currentId = editingId
                            if (currentId == null) {
                                onAddFixedIncome(inputCategory, amt, inputMemo)
                            } else {
                                onUpdateFixedIncome(currentId, inputCategory, amt, inputMemo)
                                editingId = null // 수정 완료 후 초기화
                            }
                            inputCategory = ""; inputAmount = ""; inputMemo = ""
                            if (currentId != null) showIncomeDialog = false

                        }
                    }
                ) { Text(if (editingId == null) "추가" else "수정 완료") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (editingId != null) {
                        editingId = null // 수정 모드 취소
                        inputCategory = ""; inputAmount = ""; inputMemo = ""
                    } else {
                        showIncomeDialog = false
                    }
                }) { Text(if (editingId == null) "닫기" else "취소", color = Color.Gray) }
            }
        )
    }

    // 고정 지출 리스트
    if (showExpenseDialog) {
        var editingId by remember { mutableStateOf<String?>(null) } // 수정 모드 추적용 변수
        var inputCategory by remember { mutableStateOf("") }
        var inputAmount by remember { mutableStateOf("") }
        var inputMemo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = {
                Text(
                    if (editingId == null) "고정 지출 내역 관리" else "고정 지출 항목 수정",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (editingId == null) {
                        Text(
                            "이번 달 등록된 고정 지출 목록입니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.height(130.dp)) {
                            items(fixedExpenseList) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.category, fontWeight = FontWeight.Medium)
                                        Text(
                                            "- ${formatAmount(item.amount)}원",
                                            color = Color(0xFFE53935),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    IconButton(onClick = {
                                        editingId = item.id
                                        inputCategory = item.category
                                        inputAmount = item.amount.toString()
                                        inputMemo = item.memo
                                    }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "수정",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteFixedExpense(item.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Text(
                        if (editingId == null) "새 고정 지출 항목 추가" else "선택한 항목 내용 변경",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("카테고리 (예: 월세, 보험료)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("금액") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputMemo,
                        onValueChange = { inputMemo = it },
                        label = { Text("메모 (선택사항)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = inputAmount.toIntOrNull() ?: 0
                        if (inputCategory.isNotBlank() && amt > 0) {
                            val currentId = editingId
                            if (currentId == null) {
                                onAddFixedExpense(inputCategory, amt, inputMemo)
                            } else {
                                onUpdateFixedExpense(currentId, inputCategory, amt, inputMemo)
                                editingId = null
                            }
                            inputCategory = ""; inputAmount = ""; inputMemo = ""
                            if (currentId != null) showExpenseDialog = false
                        }
                    }
                ) { Text(if (editingId == null) "추가" else "수정 완료") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (editingId != null) {
                        editingId = null // 수정 모드 취소
                        inputCategory = ""; inputAmount = ""; inputMemo = ""
                    } else {
                        showExpenseDialog = false
                    }
                }) { Text(if (editingId == null) "닫기" else "취소", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun BudgetSummaryCard(
    totalBudget: Int,
    saving: Int,
    state: String,
    fixedIncome: Int,
    fixedExpense: Int,
    onIncomeRowClick: () -> Unit, // 이벤트 리스너 추가
    onExpenseRowClick: () -> Unit  // 이벤트 리스너 추가
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (state == "good") MaterialTheme.colorScheme.primary else Color(
                0xFFFFCDD2
            )
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("사용 가능한 여유 자금", style = MaterialTheme.typography.labelMedium)
            Text(
                "${formatAmount(totalBudget)}원",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.Black.copy(alpha = 0.1f)
            )

            Column {
                // 고정 수익 행 클릭 가능 영역 변환
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onIncomeRowClick() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고정 수익", style = MaterialTheme.typography.bodyMedium)
                    Text("+ ${formatAmount(fixedIncome)}원", fontWeight = FontWeight.Bold)
                }
                // 고정 지출 행 클릭 가능 영역 변환
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onExpenseRowClick() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고정 지출", style = MaterialTheme.typography.bodyMedium)
                    Text("- ${formatAmount(fixedExpense)}원", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BudgetProgressItem(
    category: String,
    budget: Int,
    spent: Int,
    remaining: Int,
    onClick: () -> Unit
) {
    val progressValue = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "남은 돈 ${formatAmount(remaining)}원 / 총 ${formatAmount(budget)}원",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}