package com.example.project1.ui.report
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.project1.data.local.entities.Customer
import com.example.project1.data.local.entities.Payment
import com.example.project1.data.local.entities.Sale
import com.example.project1.ui.util.formatDate
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportToExcel(
    context: Context,
    sales: List<Sale>,
    customers: List<Customer>,
    allPayments: List<Payment> // Cambiado de Map a List para que coincida con tu pantalla
) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Reporte de Deudas")

    // Agrupamos los pagos por saleId internamente para facilitar la búsqueda
    val paymentsMap = allPayments.groupBy { it.saleId }

    // Estilo de encabezado
    val headerFont = workbook.createFont().apply {
        setBold(true)
    }

    val headerStyle = workbook.createCellStyle().apply {
        setFont(headerFont)
        alignment = HorizontalAlignment.CENTER
        borderBottom = BorderStyle.THIN
    }

    // Crear encabezados
    val headers = listOf("Cliente", "Fecha Venta", "Total", "Saldo Pendiente", "Fecha Último Abono", "Monto Último Abono")
    val headerRow = sheet.createRow(0)
    headers.forEachIndexed { i, title ->
        headerRow.createCell(i).apply {
            setCellValue(title)
            cellStyle = headerStyle
        }
    }

    // Llenar datos
    sales.forEachIndexed { index, sale ->
        val row = sheet.createRow(index + 1)
        val customerName = customers.find { it.id == sale.customerId }?.name ?: "ID: ${sale.customerId}"

        // Buscamos los abonos de esta venta en el mapa que creamos arriba
        // Ordenamos por fecha para obtener el más reciente
        val salePayments = paymentsMap[sale.id]?.sortedByDescending { it.date } ?: emptyList()
        val lastP = salePayments.firstOrNull()

        row.createCell(0).setCellValue(customerName)
        row.createCell(1).setCellValue(formatDate(sale.date))
        row.createCell(2).setCellValue(sale.saleTotal)
        row.createCell(3).setCellValue(sale.remainingBalance)
        row.createCell(4).setCellValue(lastP?.let { formatDate(it.date) } ?: "Sin abonos")
        row.createCell(5).setCellValue(lastP?.amount ?: 0.0)
    }

    sheet.setColumnWidth(0, 20 * 256) // Cliente (20 caracteres)
    sheet.setColumnWidth(1, 15 * 256) // Fecha
    sheet.setColumnWidth(2, 12 * 256) // Total
    sheet.setColumnWidth(3, 12 * 256) // Saldo
    sheet.setColumnWidth(4, 20 * 256) // Último Abono
    sheet.setColumnWidth(5, 12 * 256) // Monto


    // Guardar archivo
    try {
        val fileName = "Reporte_Cobranza_${System.currentTimeMillis()}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).use { workbook.write(it) }
        Toast.makeText(context, "Reporte guardado con éxito", Toast.LENGTH_LONG).show()
        Log.d("EXCEL", "Archivo guardado en: ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("EXCEL_ERROR", "Error al guardar: ${e.message}")
        Toast.makeText(context, "Error al generar reporte", Toast.LENGTH_SHORT).show()
    } finally {
        workbook.close()
    }
}