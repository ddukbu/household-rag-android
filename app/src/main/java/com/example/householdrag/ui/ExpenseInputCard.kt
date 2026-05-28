package com.example.householdrag.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.householdrag.api.ExpenseIn
import com.example.householdrag.api.FixedExpenseItem
import com.example.householdrag.api.FixedIncomeItem
import com.example.householdrag.api.IncomeIn
import com.example.householdrag.ui.theme.CommonTextField
import com.example.householdrag.ui.theme.LemonDeep
import com.example.householdrag.ui.theme.formatAmount
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputCard(
    editId: String?,
    date: String, onDateChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    paymentMethod: String, onPaymentChange: (String) -> Unit,
    place: String, onPlaceChange: (String) -> Unit,
    memo: String, onMemoChange: (String) -> Unit,

    fixedIncomeList: List<FixedIncomeItem>,
    fixedExpenseList: List<FixedExpenseItem>,
    onFixedItemSelect: (category: String, amount: String, memo: String) -> Unit,

    //onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    onSaveExpense: (ExpenseIn) -> Unit, // 지출 저장 콜백
    onSaveIncome: (IncomeIn) -> Unit        // 수입 저장 콜백
) {

    val context = LocalContext.current
    var isExpenseMode by remember { mutableStateOf(true) } // 지출/수입 모드 전환
    var isFixed by remember { mutableStateOf(false) }     // 고정/변동 스위치

    // 드롭다운의 펼침 상태를 관리하는 변수들
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    val isFixedListAvailable =
        if (isExpenseMode) fixedExpenseList.isNotEmpty() else fixedIncomeList.isNotEmpty()

//    // 드롭다운 선택지 리스트
//    val categoryOptions = if (isExpenseMode) {
//        listOf("식비", "교통비", "쇼핑", "여가", "생활", "의료", "월세", "보험료", "기타")
//    } else {
//        listOf("월급", "연금", "부수입", "용돈", "상여", "기타")
//    }

    val methodOptions = if (isExpenseMode) {
        listOf("카드", "현금", "계좌이체", "기타")
    } else {
        listOf("현금", "계좌이체", "기타")
    }
    val methodLabel = if (isExpenseMode) "결제수단" else "입금방법"
    val placeLabel = if (isExpenseMode) "사용처" else "입금처"

    // 날짜
    val calendar = Calendar.getInstance()
    val interactionSourceDate = remember { MutableInteractionSource() }
    val isDatePressed by interactionSourceDate.collectIsPressedAsState()

    // 시간 상태 추가
    var selectedTime by remember { mutableStateOf("12:00") }
    val interactionSourceTime = remember { MutableInteractionSource() }
    val isTimePressed by interactionSourceTime.collectIsPressedAsState()

    val isAmountValid = (amount.toIntOrNull() ?: 0) > 0
    val isInputComplete = date.isNotBlank() &&
            selectedTime.isNotBlank() &&
            category.isNotBlank() &&
            place.isNotBlank() &&
            paymentMethod.isNotBlank() &&
            isAmountValid

    // 달력 다이얼로그를 띄우는 함수
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // 시간 선택 다이얼로그
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hour, minute -> selectedTime = String.format("%02d:%02d", hour, minute) },
        12, 0, true
    )

    androidx.compose.runtime.LaunchedEffect(isDatePressed) {
        if (isDatePressed) {
            datePickerDialog.show()
        }
    }

    androidx.compose.runtime.LaunchedEffect(isTimePressed) {
        if (isTimePressed) {
            timePickerDialog.show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = Color.White
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editId == null) "가계부 입력" else "가계부 수정",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF3F3F3), // 연한 회색 배경
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        TypeSelectionChip(
                            text = "지출",
                            isSelected = isExpenseMode,
                            onClick = {
                                isExpenseMode = true
                                isFixed = false
                            }
                        )
                        TypeSelectionChip(
                            text = "수입",
                            isSelected = !isExpenseMode,
                            onClick = {
                                isExpenseMode = false
                                isFixed = false
                            }
                        )
                    }
                }
            }

            // 고정 여부
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.Checkbox(
                    checked = isFixed,
                    onCheckedChange = { isFixed = it })
                Text(
                    text = if (isExpenseMode) "고정 지출 내역에서 가져오기" else "고정 수입 내역에서 가져오기",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Normal
                )
            }

            // 날짜 입력 (직접 입력 대신 클릭 시 달력 호출)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 클릭하면 달력이 뜨는 Box
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    CommonTextField(
                        value = date,
                        onValueChange = {},
                        label = "날짜 선택 *",
                        readOnly = true,
                        // 아이콘만 깔끔하게 표시
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                        },
                        interactionSource = interactionSourceDate
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 시간
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                ) {
                    CommonTextField(
                        value = selectedTime,
                        onValueChange = {},
                        label = "시간 *",
                        readOnly = true,
                        interactionSource = interactionSourceTime
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isFixed) "등록된 고정 항목 선택 *" else "카테고리 *") },
                    placeholder = { Text(if (isFixed && !isFixedListAvailable) "예산 탭에 등록된 고정 내역이 없어요" else "선택해주세요") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (isFixed) {
                        // 고정 스위치가 켜진 상태 -> 등록된 예산 리스트 출력
                        if (isExpenseMode) {
                            fixedExpenseList.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${item.category} (${formatAmount(item.amount)}원) · ${item.memo.ifBlank { "메모없음" }}",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        onFixedItemSelect(
                                            item.category,
                                            item.amount.toString(),
                                            item.memo
                                        )
                                        categoryExpanded = false
                                    }
                                )
                            }
                        } else {
                            fixedIncomeList.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${item.category} (${formatAmount(item.amount)}원) · ${item.memo.ifBlank { "메모없음" }}",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        onFixedItemSelect(
                                            item.category,
                                            item.amount.toString(),
                                            item.memo
                                        )
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    } else {
                        // 고정 스위치가 꺼진 상태 -> 원래 쓰던 일반 기본 템플릿 제공
                        val standardOptions = if (isExpenseMode) {
                            listOf("식비", "교통비", "쇼핑", "여가", "생활", "의료", "월세", "보험료", "기타")
                        } else {
                            listOf("월급", "연금", "부수입", "용돈", "상여", "기타")
                        }
                        standardOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onCategoryChange(option)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 금액 입력
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("금액 *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black
                )
            )

            // 결제수단 드롭다운
            ExposedDropdownMenuBox(
                expanded = paymentExpanded,
                onExpandedChange = { paymentExpanded = !paymentExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("$methodLabel *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
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
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    methodOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onPaymentChange(option); paymentExpanded = false }
                        )
                    }
                }
            }

            // 사용처 및 메모 입력
            CommonTextField(value = place, onValueChange = onPlaceChange, label = "$placeLabel *")
            CommonTextField(value = memo, onValueChange = onMemoChange, label = "메모")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "* 필수 항목 입력 필요",
                    fontSize = 11.sp,
                    color = if (isInputComplete) Color.LightGray else Color(0xFFE53935),
                    fontWeight = FontWeight.Medium
                )

                Row {
                    TextButton(onClick = onResetClick) {
                        Text("초기화", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))




                    Button(
                        enabled = isInputComplete,
                        onClick = {
                            val amountInt = amount.toIntOrNull() ?: 0
                            if (isExpenseMode) {
                                val expenseData = ExpenseIn(
                                    date = date,
                                    time = selectedTime,
                                    category = category,
                                    amount = amountInt,
                                    payment_method = paymentMethod,
                                    place = place,
                                    memo = memo,
                                    is_fixed_expense = isFixed
                                )
                                onSaveExpense(expenseData)
                            } else {
                                val incomeData = IncomeIn(
                                    date = date,
                                    time = selectedTime,
                                    is_fixed_income = isFixed,
                                    category = category,
                                    amount = amountInt,
                                    deposit_method = paymentMethod,
                                    deposit_source = place,
                                    memo = memo
                                )
                                onSaveIncome(incomeData)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text(if (editId == null) "저장" else "수정", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}

@Composable
fun TypeSelectionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) LemonDeep else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}

//@Preview(showBackground = true, name = "가계부 입력 카드 미리보기")
//@Composable
//fun ExpenseInputCardPreview() {
//    com.example.householdrag.ui.theme.HouseholdRAGTheme {
//        Box(modifier = Modifier.padding(16.dp)) {
//            ExpenseInputCard(
//                editId = null,
//                date = "2026-05-10",
//                onDateChange = {},
//                category = "식비",
//                onCategoryChange = {},
//                amount = "10000",
//                onAmountChange = {},
//                paymentMethod = "카드",
//                onPaymentChange = {},
//                place = "스타벅스",
//                onPlaceChange = {},
//                memo = "아메리카노",
//                onMemoChange = {},
//                onResetClick = {},
//                onSaveExpense = {},
//                onSaveIncome = {}
//            )
//        }
//    }
//}