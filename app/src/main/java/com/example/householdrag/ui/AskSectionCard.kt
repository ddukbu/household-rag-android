package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AskSectionCard(
    question: String,
    onQuestionChange: (String) -> Unit,
    answer: String,
    onAskClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("소비 분석 질문", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                label = { Text("질문 입력") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = onAskClick, modifier = Modifier.padding(top = 8.dp)) {
                Text("질문하기")
            }

            if (answer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("답변", style = MaterialTheme.typography.titleMedium)
                Text(text = answer)
            }
        }
    }
}