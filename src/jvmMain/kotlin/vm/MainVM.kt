package vm

import Jama.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.RoundingMode
import java.text.DecimalFormat

class MainVM {
    var isAlert by mutableStateOf(false)
    var w by mutableStateOf("null")
    var lambdaMax by mutableStateOf("null")
    var ci by mutableStateOf("null")
    var cr by mutableStateOf("null")
    private val ri = arrayOf(0.0, 0.0, 0.52, 0.89, 1.12, 1.26, 1.36, 1.41, 1.46, 1.49, 1.52, 1.54, 1.56, 1.58, 1.59)
    private val format = DecimalFormat("#.####")

    fun  calculateAHP(str: String) {
        format.roundingMode = RoundingMode.HALF_UP
        val strAList = str.split('\n')
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
            return
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
    }
}