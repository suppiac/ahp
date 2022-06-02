// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import vm.MainVM

@Composable
@Preview
fun App() {
    var fieldText by remember { mutableStateOf("") }
    val mainVM = MainVM()

    MaterialTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .size(width = 640.dp, height = 480.dp)
            ) {
                // 输入框
                OutlinedTextField(
                    value = fieldText,
                    onValueChange = { fieldText = it },
                    textStyle = TextStyle(fontSize = 16.sp),
                    label = { Text("输入矩阵：") },
                    maxLines = 10,
                    modifier = Modifier
                        .size(width = 516.dp, height = 240.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    placeholder = {
                        Text("e.g.:\n1 1 3\n1 1 3\n1/3 1/3 1")
                    }
                )

                // 按钮
                Button(
                    modifier = Modifier
                        .padding(vertical = 15.dp)
                        .size(width = 516.dp, height = 40.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        mainVM.calculateAHP(fieldText)
                    },
                ) {
                    Text("计算")
                }

                // 分隔线
                Divider(
                    modifier = Modifier
                        .size(width = 516.dp, height = 1.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )

                // 权重矩阵
                Text(
                    text = "权重矩阵W:${mainVM.w}",
                    modifier = Modifier
                        .padding(vertical = 15.dp)
                        .width(516.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )

                // lambda_max CI CR
                Row(
                    modifier = Modifier
                        .padding(vertical = 15.dp)
                        .width(516.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "λm:${mainVM.lambdaMax}",
                        modifier = Modifier
                            .width(172.dp)
                    )
                    Text(
                        text = "CI:${mainVM.ci}",
                        modifier = Modifier
                            .width(172.dp)
                    )
                    Text(
                        text = "CR:${mainVM.cr}",
                        modifier = Modifier
                            .width(172.dp)
                    )
                }
            }
        }
        if (mainVM.isAlert) {
            Dialog(
                onCloseRequest = { mainVM.isAlert = false },
                title = "错误",
                state = rememberDialogState(
                    width = 240.dp,
                    height = 180.dp,
                    position = WindowPosition(Alignment.Center)
                ),
                resizable = false,
            ) {
                Column(
                    modifier = Modifier
                        .size(width = 270.dp, height = 180.dp)
                ) {
                    Text(
                        text = "输入数据不是矩阵！",
                        modifier = Modifier
                            .width(210.dp)
                            .height(90.dp)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(210.dp)
                    ) {
                        Button(
                            onClick = { mainVM.isAlert = false },
                            modifier = Modifier.align(alignment = Alignment.BottomEnd)
                        ) {
                            Text("确认")
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "层次分析计算",
        state = rememberWindowState(
            width = 640.dp,
            height = 480.dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = false,
    ) {
        App()
    }
}
