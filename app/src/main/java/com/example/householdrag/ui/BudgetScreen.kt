package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.sp
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.model.FixedExpenseItem
import com.example.householdrag.model.FixedIncomeItem
import com.example.householdrag.ui.theme.MonthSelector
import com.example.householdrag.ui.theme.formatAmount

fun getCategoryColor(category: String): Color {
    return when (category) {
        "식비" -> Color(0xFFFF6B6B) // 연빨강
        "교통비" -> Color(0xFFFF9F43) // 연주황
        "쇼핑" -> Color(0xFFFECA57) // 연노랑
        "여가" -> Color(0xFF1DD1A1) // 연초록
        "생활" -> Color(0xFF54A0FF) // 연파랑
        "의료" -> Color(0xFF9B5DE5) // 연보라
        else -> Color(0xFFBCAAA4) // 기타
    }
}


@OptIn(ExperimentalMaterial3Api::class)
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
    onMonthChange: (String) -> Unit,
    onUpdateCategoryBudget: (category: String, newLimit: Int) -> Unit,
    onUpdateSaving: (newSaving: Int) -> Unit
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
    val scrollState = androidx.compose.foundation.rememberScrollState()

    var showIncomeSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }

    var showSavingEditDialog by remember { mutableStateOf(false) }
    var inputNewSaving by remember { mutableStateOf("") }

    var incomeCategoryExpanded by remember { mutableStateOf(false) }
    var expenseCategoryExpanded by remember { mutableStateOf(false) }

    val fixedIncomeOptions = listOf("월급", "부수입", "용돈", "상여", "연금", "기타")
    val fixedExpenseOptions = listOf("월세", "보험료", "식비", "교통비", "쇼핑", "여가", "생활", "의료", "기타")

    var showLimitEditDialog by remember { mutableStateOf(false) }
    var selectedCategoryToEdit by remember { mutableStateOf("") }
    var inputNewLimit by remember { mutableStateOf("") }

    var currentRemainingAmount by remember { mutableStateOf(0) }
    var currentTotalLimitAmount by remember { mutableStateOf(0) }

    // 예산안 고정 항목 수정/삭제 경고 팝업
    var showDeleteWarningDialog by remember { mutableStateOf(false) }
    var showUpdateWarningDialog by remember { mutableStateOf(false) }
    var pendingActionId by remember { mutableStateOf("") }
    var pendingActionType by remember { mutableStateOf("") }

    // 수정 대기 데이터 보관함
    var pendingUpdateCategory by remember { mutableStateOf("") }
    var pendingUpdateAmount by remember { mutableStateOf(0) }
    var pendingUpdateMemo by remember { mutableStateOf("") }

    var showBudgetResetWarningDialog by remember { mutableStateOf(false) }

    var pendingAddExpenseCategory by remember { mutableStateOf("") }
    var pendingAddExpenseAmount by remember { mutableStateOf(0) }
    var pendingAddExpenseMemo by remember { mutableStateOf("") }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 30.dp)
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
                showIncomeSheet = true
            },
            onExpenseRowClick = {
                onExpenseRowClick()
                showExpenseSheet = true
            },
            onSavingRowClick = {
                inputNewSaving = budgetData.saving.toString()
                showSavingEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이번 달 예산 이용량",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val isAiGenerated = budgetData.created_by.lowercase() == "ai"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isAiGenerated) Color(0xFFE8F5E9) else Color(0xFFF1F3F5)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAiGenerated) "🤖 AI 추천" else "👤 나의 설정",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAiGenerated) Color(0xFF2E7D32) else Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val overallTotalBudget = budgetData.budget_details.values.sum()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 상단 퍼센트 지표 계산
                var totalSpentSum = 0
                budgetData.budget_details.forEach { (category, limit) ->
                    val remaining = budgetData.remaining_budget_details[category] ?: 0
                    val spent = (limit - remaining).coerceAtLeast(0)
                    totalSpentSum += spent
                }
                val totalProgressPercent =
                    if (overallTotalBudget > 0) (totalSpentSum.toFloat() / overallTotalBudget.toFloat() * 100).toInt() else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "총 지출액 ${formatAmount(totalSpentSum)}원",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "$totalProgressPercent%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (totalProgressPercent > 100) Color.Red else Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9ECEF))
                ) {
                    if (overallTotalBudget > 0) {
                        budgetData.budget_details.forEach { (category, limit) ->
                            val remaining = budgetData.remaining_budget_details[category] ?: 0
                            val spent = (limit - remaining).coerceAtLeast(0)
                            val weightValue = spent.toFloat() / overallTotalBudget.toFloat()

                            if (weightValue > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(weightValue)
                                        .background(getCategoryColor(category))
                                )
                            }
                        }
                        val remainWeight =
                            ((overallTotalBudget - totalSpentSum).coerceAtLeast(0)).toFloat() / overallTotalBudget.toFloat()
                        if (remainWeight > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(remainWeight)
                                    .background(Color(0xFFE9ECEF))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    budgetData.budget_details.keys.take(4).forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(cat))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

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
                onClick = {
                    selectedCategoryToEdit = category
                    inputNewLimit = limit.toString()

                    currentRemainingAmount = remaining
                    currentTotalLimitAmount = limit

                    showLimitEditDialog = true
                }
            )
        }
    }

    // 목표 저축액 조절 팝업
    if (showSavingEditDialog) {
        val maxSavingSliderValue =
            if (fixedIncomeTotal > 0) fixedIncomeTotal.toFloat() else 3000000f

        var sliderPosition by remember {
            val currentSaving = inputNewSaving.toIntOrNull() ?: budgetData.saving
            mutableStateOf(currentSaving.toFloat().coerceIn(0f, maxSavingSliderValue))
        }

        AlertDialog(
            onDismissRequest = { showSavingEditDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "이번 달 저축 목표 변경",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "이번 달 내 고정 수익 한도 내에서 목표를 설정합니다.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "현재 모으기로 다짐한 금액",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatAmount(sliderPosition.toInt())}원",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp)
                    ) {
                        androidx.compose.material3.Slider(
                            value = sliderPosition,
                            onValueChange = {
                                val snappedValue =
                                    (kotlin.math.round(it / 10000f) * 10000).coerceIn(
                                        0f,
                                        maxSavingSliderValue
                                    )
                                sliderPosition = snappedValue
                                inputNewSaving = snappedValue.toInt().toString()
                            },
                            valueRange = 0f..maxSavingSliderValue,
                            // steps = if (maxSavingSliderValue >= 10000f) (maxSavingSliderValue.toInt() / 10000) - 1 else 0,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = Color(0xFF2E7D32),
                                activeTrackColor = Color(0xFF2E7D32),
                                inactiveTrackColor = Color(0xFFE9ECEF),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0원", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                "최대 ${formatAmount(maxSavingSliderValue.toInt())}원",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickIncrements =
                            listOf(10000 to "+1만", 50000 to "+5만", 100000 to "+10만")
                        quickIncrements.forEach { (value, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F5))
                                    .clickable {
                                        sliderPosition =
                                            (sliderPosition + value).coerceAtMost(
                                                maxSavingSliderValue
                                            )
                                        inputNewSaving = sliderPosition.toInt().toString()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE))
                                .clickable {
                                    sliderPosition = 0f
                                    inputNewSaving = "0"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val newSavingAmt = inputNewSaving.toIntOrNull() ?: 0
                        if (newSavingAmt >= 0) {
                            onUpdateSaving(newSavingAmt) // 🌟 상위 API 매핑 엔진 발사!
                            showSavingEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50.dp)
                ) { Text("변경 완료", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSavingEditDialog = false }) {
                    Text(
                        "취소",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // 카테고리 예산 변경
    if (showLimitEditDialog) {
        val otherCategoriesTotalSpent = budgetData.budget_details
            .filter { it.key != selectedCategoryToEdit } // 현재 수정 중인 카테고리는 제외!
            .values
            .sum()

        val realMaxLimit = (budgetData.total_budget - otherCategoriesTotalSpent).coerceAtLeast(0)

        val maxSliderValue = realMaxLimit.toFloat()

        var sliderPosition by remember {
            val currentAmt = inputNewLimit.toIntOrNull() ?: 0
            mutableStateOf(
                currentAmt.toFloat().coerceIn(0f, if (maxSliderValue > 0f) maxSliderValue else 1f)
            )
        }

        AlertDialog(
            onDismissRequest = { showLimitEditDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "[$selectedCategoryToEdit] 예산 한도 수정",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("현재 카테고리에 남은 돈", fontSize = 13.sp, color = Color.Gray)
                                Text(
                                    text = "${formatAmount(currentRemainingAmount)}원",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentRemainingAmount < 0) Color(0xFFE53935) else Color(
                                        0xFF2E7D32
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("기존 목표 예산", fontSize = 13.sp, color = Color.Gray)
                                Text(
                                    "${formatAmount(currentTotalLimitAmount)}원",
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "설정할 목표 금액", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (maxSliderValue <= 0f) "0원" else "${
                                formatAmount(
                                    sliderPosition.toInt()
                                )
                            }원", fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp)
                    ) {
                        androidx.compose.material3.Slider(
                            value = if (maxSliderValue <= 0f) 0f else sliderPosition,
                            onValueChange = {
                                if (maxSliderValue > 0f) {
                                    val snappedValue =
                                        (kotlin.math.round(it / 10000f) * 10000).coerceIn(
                                            0f,
                                            maxSliderValue
                                        )

                                    sliderPosition = snappedValue
                                    inputNewLimit = snappedValue.toInt().toString()
                                }
                            },
                            valueRange = 0f..(if (maxSliderValue > 0f) maxSliderValue else 1f),
                            enabled = maxSliderValue > 0f,
                            // steps = if (maxSliderValue >= 10000f) (maxSliderValue.toInt() / 10000) - 1 else 0,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color(0xFFE9ECEF),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0원", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                "최대 ${formatAmount(maxSliderValue.toInt())}원",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickIncrements =
                            listOf(10000 to "+1만", 50000 to "+5만", 100000 to "+10만")
                        quickIncrements.forEach { (value, label) ->
                            val isBtnEnabled =
                                maxSliderValue > 0f && (sliderPosition + value) <= maxSliderValue
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBtnEnabled) Color(0xFFF1F3F5) else Color(
                                            0xFFE0E0E0
                                        ).copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        sliderPosition =
                                            (sliderPosition + value).coerceAtMost(maxSliderValue)
                                        inputNewLimit = sliderPosition.toInt().toString()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE))
                                .clickable {
                                    sliderPosition = 0f
                                    inputNewLimit = "0"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val finalAmount = sliderPosition.toInt()
                        onUpdateCategoryBudget(selectedCategoryToEdit, finalAmount)
                        showLimitEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("변경 완료", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitEditDialog = false }) {
                    Text("취소", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 삭제 확인 주의 문구 팝업
    if (showDeleteWarningDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWarningDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "⚠️ 예산안 내역 삭제 경고",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
            },
            text = {
                Column {
                    Text(
                        text = "이 고정 항목을 삭제하면,\n이번 달 가계부에 이미 입력되어 있던\n연동 가계부 데이터까지 한꺼번에 동시 삭제됩니다.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "정말 삭제하시겠습니까?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pendingActionType == "INCOME") onDeleteFixedIncome(pendingActionId) else onDeleteFixedExpense(
                            pendingActionId
                        )
                        showDeleteWarningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) { Text("위험 감수하고 삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarningDialog = false }) {
                    Text(
                        "취소",
                        color = Color.Gray
                    )
                }
            }
        )
    }

    // 수정 확인 주의 문구 팝업
    if (showUpdateWarningDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateWarningDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "⚠️ 예산안 내역 수정 경고",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9F43)
                )
            },
            text = {
                Column {
                    Text(
                        text = "고정 내역의 세부 사항을 수정하면,\n기존에 가계부에 등록되어 연동 중이던\n수입/지출 원본 금액 정보가 초기화되거나\n의도치 않게 삭제될 수 있습니다.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "정말 변경하시겠습니까?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9F43)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pendingActionType == "INCOME") {
                            onUpdateFixedIncome(
                                pendingActionId,
                                pendingUpdateCategory,
                                pendingUpdateAmount,
                                pendingUpdateMemo
                            )
                        } else {
                            onUpdateFixedExpense(
                                pendingActionId,
                                pendingUpdateCategory,
                                pendingUpdateAmount,
                                pendingUpdateMemo
                            )
                        }
                        showUpdateWarningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9F43),
                        contentColor = Color.Black
                    )
                ) { Text("수정 진행") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateWarningDialog = false }) {
                    Text(
                        "취소",
                        color = Color.Gray
                    )
                }
            }
        )
    }


    // 고정 수익 리스트
    if (showIncomeSheet) {
        var editingId by remember { mutableStateOf<String?>(null) }
        var inputCategory by remember { mutableStateOf("") }
        var inputAmount by remember { mutableStateOf("") }
        var inputMemo by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = {
                showIncomeSheet = false
                incomeCategoryExpanded = false
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
            ) {
                Text(
                    if (editingId == null) "고정 수익 내역 관리" else "고정 수익 항목 수정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (editingId == null) {
                    Text("이번 달 등록된 고정 수익 목록입니다.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(fixedIncomeList) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        // is_recorded 조건식 체크표시 UI
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (item.is_recorded) Color(0xFFE8F5E9) else Color(
                                                        0xFFECEFF1
                                                    )
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.is_recorded) "☑ 반영됨" else "☐ 미반영",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.is_recorded) Color(0xFF2E7D32) else Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = item.category,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )

                                        if (item.memo.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "·  ${item.memo}",
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "+ ${formatAmount(item.amount)}원",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        editingId = item.id; inputCategory =
                                        item.category; inputAmount =
                                        item.amount.toString(); inputMemo = item.memo
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "수정",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        pendingActionId = item.id
                                        pendingActionType = "INCOME"
                                        showDeleteWarningDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = Color(0xFFF1F3F5),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )
                }

                Text(
                    if (editingId == null) "새 고정 수익 항목 추가" else "선택한 항목 내용 변경",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = incomeCategoryExpanded,
                    onExpandedChange = { incomeCategoryExpanded = !incomeCategoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("카테고리 선택") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = incomeCategoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = incomeCategoryExpanded,
                        onDismissRequest = { incomeCategoryExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        fixedIncomeOptions.forEach { option ->
                            DropdownMenuItem(text = {
                                Text(
                                    option,
                                    fontWeight = FontWeight.Medium
                                )
                            }, onClick = { inputCategory = option; incomeCategoryExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("금액") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputMemo,
                    onValueChange = { inputMemo = it },
                    label = { Text("메모") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        if (editingId != null) {
                            editingId = null; inputCategory = ""; inputAmount = ""; inputMemo = ""
                        } else showIncomeSheet = false
                    }) {
                        Text(
                            if (editingId == null) "닫기" else "취소",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = inputAmount.toIntOrNull() ?: 0
                            if (inputCategory.isNotBlank() && amt > 0) {
                                val currentId = editingId
                                if (currentId == null) {
                                    onAddFixedIncome(inputCategory, amt, inputMemo)
                                    inputCategory = ""; inputAmount = ""; inputMemo = ""
                                } else {
                                    //onUpdateFixedIncome(currentId, inputCategory, amt, inputMemo)
                                    pendingActionId = currentId
                                    pendingActionType = "INCOME"
                                    pendingUpdateCategory = inputCategory
                                    pendingUpdateAmount = amt
                                    pendingUpdateMemo = inputMemo
                                    showUpdateWarningDialog = true

                                    inputCategory = ""; inputAmount = ""; inputMemo =
                                        ""; editingId = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (editingId == null) "추가하기" else "수정 완료",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }


    // 고정 지출 리스트
    if (showExpenseSheet) {
        var editingId by remember { mutableStateOf<String?>(null) }
        var inputCategory by remember { mutableStateOf("") }
        var inputAmount by remember { mutableStateOf("") }
        var inputMemo by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showExpenseSheet = false; expenseCategoryExpanded = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
            ) {
                Text(
                    if (editingId == null) "고정 지출 내역 관리" else "고정 지출 항목 수정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (editingId == null) {
                    Text("이번 달 등록된 고정 지출 목록입니다.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(fixedExpenseList) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        // is_recorded 조건식 체크표시 UI
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (item.is_recorded) Color(0xFFE8F5E9) else Color(
                                                        0xFFECEFF1
                                                    )
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.is_recorded) "☑ 반영됨" else "☐ 미반영",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.is_recorded) Color(0xFF2E7D32) else Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            item.category,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )

                                        if (item.memo.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "·  ${item.memo}",
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "- ${formatAmount(item.amount)}원",
                                        color = Color(0xFFC62828),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        editingId = item.id; inputCategory =
                                        item.category; inputAmount =
                                        item.amount.toString(); inputMemo = item.memo
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "수정",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        pendingActionId = item.id
                                        pendingActionType = "EXPENSE"
                                        showDeleteWarningDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = Color(0xFFF1F3F5),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )
                }

                Text(
                    if (editingId == null) "새 고정 지출 항목 추가" else "선택한 항목 내용 변경",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expenseCategoryExpanded,
                    onExpandedChange = { expenseCategoryExpanded = !expenseCategoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("카테고리 선택") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expenseCategoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expenseCategoryExpanded,
                        onDismissRequest = { expenseCategoryExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        fixedExpenseOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    inputCategory = option; expenseCategoryExpanded = false
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("금액") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputMemo,
                    onValueChange = { inputMemo = it },
                    label = { Text("메모") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        if (editingId != null) {
                            editingId = null; inputCategory = ""; inputAmount = ""; inputMemo = ""
                        } else showExpenseSheet = false
                    }) {
                        Text(
                            if (editingId == null) "닫기" else "취소",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = inputAmount.toIntOrNull() ?: 0
                            if (inputCategory.isNotBlank() && amt > 0) {
                                val currentId = editingId
                                if (currentId == null) {
                                    val currentTotalCategoryBudget =
                                        budgetData.budget_details.values.sum()
                                    val pureRemainingMoney =
                                        budgetData.total_budget - currentTotalCategoryBudget

                                    if (amt > pureRemainingMoney) {
                                        // 임시 주머니에 보관하고 경고창을 띄워 제동을 겁니다!
                                        pendingAddExpenseCategory = inputCategory
                                        pendingAddExpenseAmount = amt
                                        pendingAddExpenseMemo = inputMemo
                                        showBudgetResetWarningDialog = true
                                    } else {
                                        onAddFixedExpense(inputCategory, amt, inputMemo)
                                        inputCategory = ""; inputAmount = ""; inputMemo = ""
                                        showExpenseSheet = false
                                    }

                                } else {
                                    pendingActionId = currentId
                                    pendingActionType = "EXPENSE"
                                    pendingUpdateCategory = inputCategory
                                    pendingUpdateAmount = amt
                                    pendingUpdateMemo = inputMemo
                                    showUpdateWarningDialog = true

                                    inputCategory = ""; inputAmount = ""; inputMemo =
                                        ""; editingId = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (editingId == null) "추가하기" else "수정 완료",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showBudgetResetWarningDialog) {
            AlertDialog(
                onDismissRequest = { showBudgetResetWarningDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text(
                        "🚨 예산안 강제 초기화 경고",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "새로 추가하려는 고정 지출 금액(${formatAmount(pendingAddExpenseAmount)}원)이\n현재 남은 여유 자금보다 더 큽니다.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚠️ 이 항목을 추가할 경우,\n기존 카테고리별 목표 예산 전체가\n[0원]으로 강제 초기화됩니다!",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC62828),
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "기존 예산안이 전부 날아가도 진행하시겠습니까?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddFixedExpense(
                                pendingAddExpenseCategory,
                                pendingAddExpenseAmount,
                                pendingAddExpenseMemo
                            )

                            pendingAddExpenseCategory = ""; pendingAddExpenseAmount =
                            0; pendingAddExpenseMemo = ""
                            showBudgetResetWarningDialog = false
                            showExpenseSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        )
                    ) { Text("네, 초기화하고 추가") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showBudgetResetWarningDialog = false
                        }
                    ) { Text("취소", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            )
        }
    }
}


@Composable
fun BudgetSummaryCard(
    totalBudget: Int,
    saving: Int,
    state: String,
    fixedIncome: Int,
    fixedExpense: Int,
    onIncomeRowClick: () -> Unit,
    onExpenseRowClick: () -> Unit,
    onSavingRowClick: () -> Unit
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
            Text(
                "사용 가능한 여유 자금",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Text(
                "${formatAmount(totalBudget)}원",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.Black.copy(alpha = 0.1f)
            )

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onIncomeRowClick() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "고정 수익",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "+ ${formatAmount(fixedIncome)}원",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onExpenseRowClick() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "고정 지출",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "- ${formatAmount(fixedExpense)}원",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSavingRowClick() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "목표 저축액",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${formatAmount(saving)}원",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
    val isOverBudget = remaining < 0

    val progressColor = if (isOverBudget) Color(0xFFE53935) else getCategoryColor(category)
    val remainingTextColor = if (isOverBudget) Color(0xFFE53935) else Color.Gray
    val remainingFontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Normal

    val progressValue = if (budget > 0) {
        if (isOverBudget) 0f else remaining.toFloat() / budget.toFloat()
    } else {
        0f
    }

    val singleSolidColor = when {
        isOverBudget -> Color(0xFF990000)
        progressValue <= 0.2f -> {
            Color(0xFFE53935)
        }

        progressValue <= 0.6f -> {
            Color(0xFFFFB300)
        }

        else -> {
            Color(0xFF81C784)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = "남은 돈 ${formatAmount(remaining)}원 / 총 ${formatAmount(budget)}원",
                style = MaterialTheme.typography.bodySmall,
                color = remainingTextColor,
                fontWeight = remainingFontWeight
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

//        LinearProgressIndicator(
//            progress = { progressValue },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(8.dp)
//                .clip(CircleShape),
//            color = progressColor,
//            trackColor = Color(0xFFF0F0F0)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0))
        ) {
            if (progressValue > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressValue)
                        .fillMaxHeight()
                        .background(color = singleSolidColor)
                )
            }
        }
    }
}