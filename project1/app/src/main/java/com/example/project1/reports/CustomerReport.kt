//package com.example.project1.reports
//
//import android.content.Context
//import android.os.Environment
//import com.example.project1.data.local.entities.Customer
//import com.example.project1.data.local.entities.Payment
//import com.example.project1.ui.util.formatDate
//import org.apache.poi.ss.usermodel.BorderStyle
//import org.apache.poi.ss.usermodel.FillPatternType
//import org.apache.poi.ss.usermodel.HorizontalAlignment
//import org.apache.poi.ss.usermodel.IndexedColors
//import org.apache.poi.xssf.usermodel.XSSFWorkbook
//import java.io.File
//import java.io.FileOutputStream
//
//fun exportToExcel(
//    context: Context,
//    sales: List<Sale>,
//    customers: List<Customer>,
//    allPayments: List<Payment>
//): File? {
//    val workbook = XSSFWorkbook()
//    val sheet = workbook.createSheet("Reporte General de Cobranza")
//
//    // 1. ESTILOS
//    val headerFont = workbook.createFont().apply {
//        setBold(true)
//        color = IndexedColors.WHITE.getIndex()
//    }
//
//    val headerStyle = workbook.createCellStyle().apply {
//        setFont(headerFont)
//        alignment = HorizontalAlignment.CENTER
//        fillForegroundColor = IndexedColors.DARK_GREEN.getIndex()
//        fillPattern = FillPatternType.SOLID_FOREGROUND
//        borderBottom = BorderStyle.MEDIUM
//    }
//
//    val currencyStyle = workbook.createCellStyle().apply {
//        val format = workbook.createDataFormat()
//        dataFormat = format.getFormat("$#,##0.00")
//        alignment = HorizontalAlignment.RIGHT
//    }
//
//    // 2. ENCABEZADOS (Añadimos "Total Abonado" y "Detalle de Pagos")
//    val headers = listOf(
//        "Cliente",
//        "Inicio Deuda",
//        "Monto Original",
//        "Total Abonado",
//        "Saldo Pendiente",
//        "Estado",
//        "Historial de Pagos (Fecha: Monto)"
//    )
//
//    val headerRow = sheet.createRow(0)
//    headers.forEachIndexed { i, title ->
//        headerRow.createCell(i).apply {
//            setCellValue(title)
//            cellStyle = headerStyle
//        }
//    }
//
//    // 3. AGRUPAR PAGOS
//    val paymentsMap = allPayments.groupBy { it.saleId }
//
//    // 4. LLENAR DATOS
//    sales.forEachIndexed { index, sale ->
//        val row = sheet.createRow(index + 1)
//        val customer = customers.find { it.id == sale.customerId }
//        val salePayments = paymentsMap[sale.id]?.sortedBy { it.date } ?: emptyList()
//
//        val totalAbonado = salePayments.sumOf { it.amount }
//
//        // Columna 0: Cliente
//        row.createCell(0).setCellValue(customer?.name ?: "ID: ${sale.customerId}")
//
//        // Columna 1: Fecha Inicio
//        row.createCell(1).setCellValue(formatDate(sale.date))
//
//        // Columna 2: Monto Original
//        row.createCell(2).apply {
//            setCellValue(sale.saleTotal)
//            cellStyle = currencyStyle
//        }
//
//        // Columna 3: Total Abonado (Calculado)
//        row.createCell(3).apply {
//            setCellValue(totalAbonado)
//            cellStyle = currencyStyle
//        }
//
//        // Columna 4: Saldo Pendiente
//        row.createCell(4).apply {
//            setCellValue(sale.remainingBalance)
//            cellStyle = currencyStyle
//        }
//
//        // Columna 5: Estado Visual
//        val statusText = if (sale.remainingBalance <= 0) "LIQUIDADO" else "PENDIENTE"
//        row.createCell(5).setCellValue(statusText)
//
//        // Columna 6: HISTORIAL DETALLADO (Aquí concatenamos todos los abonos)
//        // Ejemplo: "01/05: $10.00 | 05/05: $20.00"
//        val historyText = salePayments.joinToString(" | ") { p ->
//            "${formatDate(p.date)}: $${String.format("%.2f", p.amount)}"
//        }
//        row.createCell(6).setCellValue(historyText.ifEmpty { "Sin movimientos" })
//    }
//
//    // 5. AJUSTE DE COLUMNAS
//    sheet.setColumnWidth(0, 25 * 256) // Cliente
//    sheet.setColumnWidth(1, 15 * 256) // Fecha
//    sheet.setColumnWidth(2, 15 * 256) // Monto Original
//    sheet.setColumnWidth(3, 15 * 256) // Total Abonado
//    sheet.setColumnWidth(4, 15 * 256) // Saldo
//    sheet.setColumnWidth(5, 12 * 256) // Estado
//    sheet.setColumnWidth(6, 60 * 256) // Historial (Más ancho para que quepan los textos)
//
//    // 6. GUARDAR
//    try {
//        val fileName = "Reporte_Cobranza_${System.currentTimeMillis()}.xlsx"
//        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val file = File(downloadsDir, fileName)
//
//        FileOutputStream(file).use { workbook.write(it) }
//        return file // <--- DEVOLVEMOS EL ARCHIVO
//    } catch (e: Exception) {
//        return null
//    } finally {
//        workbook.close()
//    }
//}