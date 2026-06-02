package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.model.ChatHistoryDto
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import com.example.householdrag.ui.theme.LemonDeep

@Composable
fun AskSectionCard(
    chatHistory: List<ChatHistoryDto>, // 서버에서 가져온 대화 목록
    currentQuestion: String,           // 지금 치고 있는 글자
    chatMode: ChatMode,
    isWaitingAnswer: Boolean,
    hasPendingBudgetDraft: Boolean = false,
    onQuestionChange: (String) -> Unit,
    onAskClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onBudgetModeClick: () -> Unit,
    onBudgetToneClick: (String) -> Unit,
    onBudgetApplyClick: () -> Unit,
    onBudgetCancelClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val actionsEnabled = !isWaitingAnswer

    LaunchedEffect(chatHistory.size, isWaitingAnswer) {
        if (chatHistory.isNotEmpty()) {
            val targetIndex = chatHistory.lastIndex + if (isWaitingAnswer) 1 else 0
            listState.animateScrollToItem(targetIndex)
        }
    }

    // [포인트] 이제 카드가 아니라 화면 전체를 쓰는 구조가 더 예뻐요!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // 연한 회색 배경
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (chatMode == ChatMode.BUDGET) "채팅 : 예산안 모드" else "채팅 : 일반모드",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }

        // 1. 대화 리스트 영역
        LazyColumn(
            modifier = Modifier
                .weight(1f) // 남은 공간 다 차지
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(chatHistory) { history ->
                // 내 질문 (오른쪽)
                ChatBubble(text = history.question, isFromUser = true)
                // AI 답변 (왼쪽)
                ChatBubble(text = history.answer, isFromUser = false)
            }

            if (isWaitingAnswer) {
                item {
                    ChatBubble(text = "AI가 답변 중...", isFromUser = false)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatMode == ChatMode.GENERAL) {
                Button(onClick = onBudgetModeClick, enabled = actionsEnabled) {
                    Text("예산안")
                }
                Button(onClick = onAnalysisClick, enabled = actionsEnabled) {
                    Text("분석")
                }
            } else {
                Button(
                    onClick = { onBudgetToneClick("balanced") },
                    enabled = actionsEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F),
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFFFFD54F),
                        disabledContentColor = Color.Black
                    )
                ) {
                    Text("균형")
                }
                Button(
                    onClick = { onBudgetToneClick("saving") },
                    enabled = actionsEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF66BB6A),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF66BB6A),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("절약", color = Color.White)
                }
                Button(
                    onClick = { onBudgetToneClick("relaxed") },
                    enabled = actionsEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A5F5),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF42A5F5),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("여유", color = Color.White)
                }
                Button(
                    onClick = onBudgetApplyClick,
                    enabled = actionsEnabled && hasPendingBudgetDraft
                ) {
                    Text("확인")
                }
                Button(
                    enabled = actionsEnabled && hasPendingBudgetDraft,
                    onClick = onBudgetCancelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDEDED))
                ) {
                    Text("취소", color = Color.Black)
                }
            }
        }

        // 2. 하단 입력바 영역 (여기가 혜림 님이 말한 '>' 버튼 있는 곳!)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .navigationBarsPadding(), // 키보드 위로 띄우기
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentQuestion,
                    onValueChange = onQuestionChange,
                    placeholder = { Text(if (chatMode == ChatMode.BUDGET) "예산안 관련 메시지를 입력하세요..." else "AI에게 분석을 요청하세요...") },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LemonDeep,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                if (chatMode == ChatMode.GENERAL) {
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onAskClick,
                        enabled = actionsEnabled,
                        modifier = Modifier
                            .size(48.dp)
                            .background(LemonDeep, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isFromUser: Boolean) {
    val alignment = if (isFromUser) Alignment.End else Alignment.Start

    val color = if (isFromUser) LemonDeep else Color(0xFFF1F1F1)

    val shape = if (isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}

// 질문만 입력했을 때 (답변 대기 중)
@Preview(showBackground = true, name = "1. 질문 입력 상태")
@Composable
fun AskSectionOnlyQuestionPreview() {
    HouseholdRAGTheme {
        AskSectionCard(
            chatHistory = listOf(
                ChatHistoryDto(
                    id = "1",
                    mode = "ask",
                    question = "이번달 식비가 어떻게 되니?",
                    answer = "데이터를 분석 중입니다...",
                    created_at = "2026-05-13"
                )
            ),
            currentQuestion = "",
            chatMode = ChatMode.GENERAL,
            isWaitingAnswer = false,
            onQuestionChange = {},
            onAskClick = {},
            onAnalysisClick = {},
            onBudgetModeClick = {},
            onBudgetToneClick = {},
            onBudgetApplyClick = {},
            onBudgetCancelClick = {}
        )
    }
}


// AI 답변까지 완료되었을 때 (완전한 대화 형태)
@Preview(showBackground = true, name = "2. AI 답변 완료 상태")
@Composable
fun AskSectionFullChatPreview() {
    HouseholdRAGTheme {
        AskSectionCard(
            chatHistory = listOf(
                ChatHistoryDto(
                    id="2",
                    mode="ask",
                    question="저번 달보다 지출이 늘었어?",
                    answer="네, 저번 달 대비 배달 음식 지출이 15% 증가했습니다. 🍔",
                    created_at="2026-05-13"
                )
            ),
            currentQuestion = "",
            chatMode = ChatMode.BUDGET,
            isWaitingAnswer = true,
            onQuestionChange = {},
            onAskClick = {},
            onAnalysisClick = {},
            onBudgetModeClick = {},
            onBudgetToneClick = {},
            onBudgetApplyClick = {},
            onBudgetCancelClick = {}
        )
    }
}
