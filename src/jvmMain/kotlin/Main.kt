// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import Jama.Matrix
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
import java.math.RoundingMode
import java.text.DecimalFormat

@Composable
@Preview
fun App() {
    var fieldText by remember { mutableStateOf("") }
    var isAlert by remember { mutableStateOf(false) }
    var w by remember { mutableStateOf("null") }
    var lambdaMax by remember { mutableStateOf("null") }
    var ci by remember { mutableStateOf("null") }
    var cr by remember { mutableStateOf("null") }
    val ri = arrayOf(0.0, 0.0, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49, 1.52)
    val format = DecimalFormat("#.####")
    format.roundingMode = RoundingMode.HALF_UP

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
                        val strA = fieldText
                        val strAList = strA.split('\n')
                        val length = strAList.size

                        // 计算输入矩阵A
                        val a = Array(length){FloatArray(length)}
                        val aD = Array(length){DoubleArray(length)}

                        for (index in 0 until length) {
                            val temp = strAList[index].split(' ')
                            if (temp.size != length) {
                                isAlert = true
                                break
                            }
                            for (i in temp.indices) {
                                if (temp[i].isEmpty()) {
                                    isAlert = true
                                    break
                                }
                                val numList = temp[i].split('/')
                                if (numList.size > 1) {
                                    a[index][i] = numList[0].toFloat() / numList[1].toFloat()
                                    aD[index][i] = a[index][i].toDouble()
                                } else {
                                    a[index][i] = temp[i].toFloat()
                                    aD[index][i] = temp[i].toDouble()
                                }
                            }
                        }
                        if (isAlert) {
                            return@Button
                        }

                        // 矩阵A每列元素相加
                        val sumRow = FloatArray(length)
                        for (i in 0 until length) {
                            for (j in 0 until length) {
                                sumRow[i] += a[j][i]
                            }
                        }

                        // A / sumRow
                        val b = Array(length){FloatArray(length)}
                        for (i in 0 until length) {
                            for (j in 0 until length) {
                                b[j][i] = a[j][i] / sumRow[i]
                            }
                        }

                        // 矩阵B每行元素相加
                        val sumCol = FloatArray(length)
                        for (i in 0 until length) {
                            for (j in 0 until length) {
                                sumCol[i] += b[i][j]
                            }
                        }

                        // 计算权重矩阵
                        val wArray = FloatArray(length)
                        val wArrayD = Array(length) { DoubleArray(1) }
                        for (i in 0 until length) {
                            wArray[i] += sumCol[i] / sumCol.sum()
                            wArrayD[i][0] += sumCol[i].toDouble() / sumCol.sum().toDouble()
                        }
                        w = "["
                        for (i in 0 until length) {
                            w += format.format(wArray[i]).toString()
                            if (i != length - 1) {
                                w += ", "
                            }
                        }
                        w += "]"


                        // C dot W
                        val cw = Matrix(aD).times(Matrix(wArrayD)).array

                        // 计算最大特征根
                        var lm = 0.0
                        for (i in 0 until length) {
                            lm += (cw[i][0].toFloat() / wArray[i])
                        }
                        lm /= length.toFloat()
                        lambdaMax = format.format(lm).toString()

                        // ci
                        val ciN = (lm - length) / (length - 1)
                        ci = format.format(ciN).toString()

                        // cr
                        cr = format.format(ciN / ri[length - 1]).toString()
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
                    text = "权重矩阵W:$w",
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
                        text = "λm:$lambdaMax",
                        modifier = Modifier
                            .width(172.dp)
                    )
                    Text(
                        text = "CI:$ci",
                        modifier = Modifier
                            .width(172.dp)
                    )
                    Text(
                        text = "CR:$cr",
                        modifier = Modifier
                            .width(172.dp)
                    )
                }
            }
        }
        if (isAlert) {
            Dialog(
                onCloseRequest = { isAlert = false },
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
                            onClick = { isAlert = false },
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
