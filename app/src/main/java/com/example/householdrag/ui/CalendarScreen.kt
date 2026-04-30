package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.Expense
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(expenses: List<Expense>) {
    // val locale = java.util.Locale.getDefault()

    // 1. 선택된 날짜 관리
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 2. 화면에 항상 떠 있는 달력 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli()
    )

    // 달력에서 날짜를 클릭할 때마다 selectedDate를 업데이트합니다.
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            selectedDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    // 3. 지출 데이터 필터링
    val filteredExpenses = expenses.filter {
        try {
            LocalDate.parse(it.date) == selectedDate
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // [상단] 캘린더 영역 (팝업이 아니라 화면의 일부로 그립니다!)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "달력 지출 조회",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
                // [핵심!] DatePicker를 다이얼로그 없이 바로 호출합니다.
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false, // 달력 모양 고정
                    title = null,
                    headline = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        // 오늘 날짜나 선택된 날짜의 동그라미 색상
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        // 선택된 날짜의 글자 색상 (검정색 추천)
                        selectedDayContentColor = Color.Black,
                        // 오늘 날짜 표시 색상
                        todayContentColor = MaterialTheme.colorScheme.primary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        // 상단 연도/월 선택 글자 색상
                        titleContentColor = Color.Black,
                        headlineContentColor = Color.Black
                    )
                )
            }
        }

        // [하단] 리스트 영역
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 지출",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredExpenses.isEmpty()) {
                    item {
                        Text(
                            "지출 내역이 없습니다.",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                } else {
                    items(filteredExpenses) { expense ->
                        ExpenseItemCard(expense = expense, onEditClick = {}, onDeleteClick = {})
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, name = "달력 화면 미리보기")
@Composable
fun CalendarScreenPreview() {
    HouseholdRAGTheme {
        val demoExpenses = listOf(
            Expense(
                id = "1",
                date = LocalDate.now().toString(),
                category = "식비",
                amount = 5500,
                payment_method = "카드",
                place = "스타벅스",
                memo = "아메리카노"
            ),
            Expense(
                id = "2",
                date = LocalDate.now().toString(),
                category = "식비",
                amount = 3000,
                payment_method = "현금",
                place = "편의점",
                memo = "우유"
            )
        )
        CalendarScreen(expenses = demoExpenses)
    }
}