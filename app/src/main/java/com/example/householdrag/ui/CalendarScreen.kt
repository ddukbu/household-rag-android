package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.householdrag.api.Expense
import com.example.householdrag.ui.theme.LemonMain
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(expenses: List<Expense>) {
    // 1. 날짜 상태 관리
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(50) }
    val endMonth = remember { currentMonth.plusMonths(50) }
    val daysOfWeek = remember { daysOfWeek() }

    // 2. 지출이 있는 날짜들만 모으기 (중복 제거를 위해 Set 사용)
    val spentDates = remember(expenses) {
        expenses.mapNotNull {
            try { LocalDate.parse(it.date) } catch (e: Exception) { null }
        }.toSet()
    }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // [상단] 캘린더 헤더 (연도/월 표시)
        val visibleMonth = remember { derivedStateOf { state.firstVisibleMonth } }
        Text(
            text = "${visibleMonth.value.yearMonth.year}년 ${visibleMonth.value.yearMonth.monthValue}월",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // [중앙] 요일 표시 (일~토)
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.name.take(1), // 'M', 'T', 'W'...
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        // [본체] 라이브러리 캘린더
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayElement(
                    day = day,
                    isSelected = selectedDate == day.date,
                    hasExpense = spentDates.contains(day.date), // 여기서 지출 여부 확인!
                    onClick = { selectedDate = it.date }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

        // [하단] 선택된 날짜의 지출 리스트
        val filteredExpenses = expenses.filter {
            try { LocalDate.parse(it.date) == selectedDate } catch (e: Exception) { false }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "${selectedDate.dayOfMonth}일 지출 내역",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            if (filteredExpenses.isEmpty()) {
                item { Text("지출 내역이 없어요.", color = Color.Gray, fontSize = 14.sp) }
            } else {
                items(filteredExpenses) { expense ->
                    ExpenseItemCard(expense = expense, onEditClick = {}, onDeleteClick = {})
                }
            }
        }
    }
}

@Composable
fun DayElement(day: CalendarDay, isSelected: Boolean, hasExpense: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 정사각형
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LemonMain else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate) { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    day.position != DayPosition.MonthDate -> Color.LightGray // 이전/다음 달 날짜
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Black
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // [포인트!] 지출이 있으면 날짜 아래에 작은 점을 찍습니다.
            if (hasExpense && day.position == DayPosition.MonthDate) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.Red) // 지출은 빨간 점으로 강조!
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "캘린더 화면 미리보기")
@Composable
fun CalendarScreenPreview() {
    // 미리보기용 가짜 데이터
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
            category = "생활",
            amount = 3000,
            payment_method = "현금",
            place = "편의점",
            memo = "우유"
        ),
        // 어제 날짜에도 점이 찍히는지 확인하기 위한 데이터
        Expense(
            id = "3",
            date = LocalDate.now().minusDays(1).toString(),
            category = "교통",
            amount = 1250,
            payment_method = "카드",
            place = "지하철",
            memo = ""
        )
    )

    MaterialTheme {
        CalendarScreen(expenses = demoExpenses)
    }
}