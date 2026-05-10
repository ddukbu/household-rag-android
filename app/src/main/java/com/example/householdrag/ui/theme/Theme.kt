package com.example.householdrag.ui.theme

import android.app.Activity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

private val LightColorScheme = lightColorScheme(
    primary = LemonDeep,                    // 버튼 등 주요 포인트 색상
    onPrimary = Color.Black,                // 버튼 위 글자색
    background = Color.White,
    surface = Color.White,                  // 카드나 입력창은 흰색으로 두면 대비가 예뻐요
    onBackground = Color(0xFF1C1B1F),       // 일반 글자색
    onSurface = Color(0xFF1C1B1F),          // 카드 위 글자색
    surfaceVariant = Color.White
)

@Composable
fun HouseholdRAGTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun CommonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.Black,
            focusedLeadingIconColor = Color.Black
        )
    )
}

// 숫자를 받아서 "1,234,567" 형식의 문자열로 바꿔주는 함수입니다.
fun formatAmount(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(amount)
}

// 화살표를 누를떄마다 달을 바꿔줌
@Composable
fun MonthSelector(
    currentYM: String, // "2026-05" 형식
    onMonthChange: (String) -> Unit
) {
    // 문자열을 날짜 객체로 변환
    val yearMonth = try {
        YearMonth.parse(currentYM)
    } catch (e: Exception) {
        YearMonth.now() // 에러 시 현재 날짜로 방어 로직
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        //horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [<] 이전 달 버튼
        IconButton(
            onClick = {
                val prevMonth = yearMonth.minusMonths(1).toString()
                onMonthChange(prevMonth)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "이전 달",
                tint = Color.Gray
            )
        }

        // 중앙 날짜 표시 (예: 2026년 05월)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
//            Text(
//                text = "${yearMonth.year}년",
//                style = MaterialTheme.typography.labelSmall,
//                color = Color.Gray
//            )
            Text(
                text = "${yearMonth.monthValue}월",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // [>] 다음 달 버튼
        IconButton(
            onClick = {
                val nextMonth = yearMonth.plusMonths(1).toString()
                onMonthChange(nextMonth)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "다음 달",
                tint = Color.Gray
            )
        }
    }
}