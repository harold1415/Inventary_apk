package com.example.myinventarioapp.ui.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinventarioapp.ui.model.Local
import com.example.myinventarioapp.ui.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


data class ProductoGrouped(
    val modeloCod: String,
    val nombre: String,
    val tipo: String,
    val stock: Int,
    val costo: Double,
    val precio: Double,
    val local: String
)

class ReportViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales = _locales.asStateFlow()

    // VENTAS FILTRADAS
    private val _ventas = MutableStateFlow<List<Venta>>(emptyList())
    val ventas = _ventas.asStateFlow()

    //GENERAR EXCEL
    private val _excelGenerated = MutableStateFlow<String?>(null)
    val excelGenerated = _excelGenerated.asStateFlow()

    //GRUPO PARA EL REPORTE POR INVENTARIO
    private val _inventoryReport = MutableStateFlow<List<ProductoGrouped>>(emptyList())
    val inventoryReport = _inventoryReport.asStateFlow()

    init {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Local::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            _locales.value = lista
        }
    }

    /** ---------------------------------------------------------------------
     *     FUNCIONES DE FECHAS (CONVERSIÓN A TIMESTAMP)
     *  --------------------------------------------------------------------*/
    private fun getTimestampRange(start: Calendar, end: Calendar): Pair<Timestamp, Timestamp> {
        val startTs = Timestamp(start.time)
        val endTs = Timestamp(end.time)
        return startTs to endTs
    }

    /** ----------------------  REPORTES ---------------------- **/

    fun exportInventoryToExcel(context: Context, local: String) {
        var query: Query = db.collection("productos")
        if (local.isNotEmpty()) {
            query = query.whereEqualTo("local", local)
        }

        query.get().addOnSuccessListener { snapshot ->
            val productos =
                snapshot?.documents?.mapNotNull { it.toObject(Producto::class.java) } ?: emptyList()

            viewModelScope.launch(Dispatchers.IO) {
                val timeStamp = System.currentTimeMillis()
                val safeLocal = if (local.isBlank()) "Todos" else local.replace(" ", "_")
                val fileName = "Inventario_${safeLocal}_$timeStamp.xlsx"

                saveExcelWithColorDetails(context, fileName, productos)

            }
        }
    }
    private val ORDEN_TALLAS_TEXTO = listOf(
        "XS",
        "S",
        "M",
        "L",
        "XL",
        "2XL",
        "3XL",
        "4XL"
    )

    private fun normalizarTalla(talla: String?): String =
        talla?.trim()?.uppercase() ?: ""

    // Detecta si la talla es numérica (pantalones)
    private fun esTallaNumerica(talla: String): Boolean =
        talla.toIntOrNull() != null
    fun saveExcelWithColorDetails(
        context: Context,
        fileName: String,
        productos: List<Producto>
    ) {
        Log.d("EXCEL_DEBUG", "Iniciando Excel: $fileName")
        Log.d("EXCEL_DEBUG", "Total productos recibidos: ${productos.size}")
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        file.parentFile?.mkdirs()
        //AQUÍ SE ARMA EL EXCEL
        try {
            FileOutputStream(file).use { fos ->

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Inventario")

                // --- ESTILO ENCABEZADOS ---
                val headerStyle = workbook.createCellStyle().apply {
                    alignment = HorizontalAlignment.CENTER
                    verticalAlignment = VerticalAlignment.CENTER
                    wrapText= true
                    setBorderBottom(BorderStyle.THIN)
                    setBorderTop(BorderStyle.THIN)
                    setBorderLeft(BorderStyle.THIN)
                    setBorderRight(BorderStyle.THIN)
                }

                val boldFont = workbook.createFont().apply {
                    bold = true
                }
                headerStyle.setFont(boldFont)
                // AJUSTAR ANCHO DE COLUMNAS (en caracteres)
                sheet.setColumnWidth(0, 20 * 256) // Modelo
                sheet.setColumnWidth(1, 25 * 256) // Nombre
                sheet.setColumnWidth(2, 15 * 256) // Color
                // AGRUPAR POR MODELO
                val groupByModel = productos.groupBy { it.modeloCod }

                var rowIndex = 0

                val maxTallas = groupByModel.values.maxOf { lista ->
                    lista.map { normalizarTalla(it.talla) }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .size
                }
                val startTallaCol = 3
                val endTallaColGlobal = startTallaCol + maxTallas - 1
                val colStockGlobal = endTallaColGlobal + 1
                val colCostoGlobal = endTallaColGlobal + 2

                // TALLAS
                for (i in startTallaCol..endTallaColGlobal) {
                    sheet.setColumnWidth(i, 10 * 256)
                }

                // STOCK y COSTO
                sheet.setColumnWidth(colStockGlobal, 10 * 256)
                sheet.setColumnWidth(colCostoGlobal, 18 * 256)

                for ((modelo, listaProductos) in groupByModel) {
                    Log.d(
                        "EXCEL_DEBUG",
                        "Modelo: $modelo | Items: ${listaProductos.size}"
                    )
                    // 🔹 TALLAS ÚNICAS NORMALIZADAS
                    val tallasUnicas = listaProductos
                        .map { normalizarTalla(it.talla) }
                        .filter { it.isNotBlank() }
                        .distinct()

                    val tallas = when {
                        // 🟦 PANTALONES → 26,27,...42
                        tallasUnicas.all { esTallaNumerica(it) } -> {
                            tallasUnicas
                                .map { it.toInt() }
                                .sorted()
                                .map { it.toString() }
                        }

                        // 🟩 ROPA → XS,S,M,L,XL...
                        else -> {
                            ORDEN_TALLAS_TEXTO.filter { it in tallasUnicas }
                        }
                    }

                    // 🔒 Sin talla
                    val tallasFinales = if (tallas.isNotEmpty()) tallas else listOf("ÚNICA")

                    Log.d("EXCEL_DEBUG", "Tallas finales: $tallasFinales")

                    Log.d("EXCEL_DEBUG", "Tallas ordenadas: $tallas")
                    Log.d("EXCEL_DEBUG", "1")

                    // --- ENCABEZADO NIVEL 1 ---
                    val headerRow1 = sheet.createRow(rowIndex)
                    val headerRow2 = sheet.createRow(rowIndex + 1)

                    headerRow1.createCell(0).apply {
                        setCellValue("MODELO")
                        cellStyle = headerStyle
                    }
                    sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex + 1, 0, 0))

                    headerRow1.createCell(1).apply {
                        setCellValue("NOMBRE")
                        cellStyle = headerStyle
                    }
                    sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex + 1, 1, 1))

                    headerRow1.createCell(2).apply {
                        setCellValue("COLOR")
                        cellStyle = headerStyle
                    }
                    sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex + 1, 2, 2))

                    // --- TÍTULO "TALLAS" ---
                    Log.d("EXCEL_DEBUG", "2")
                    val startTallaCol = 3
                    val tallaCount = tallasFinales.size
                    val endTallaCol = startTallaCol + tallaCount - 1

                    val colStock = endTallaCol + 1
                    val colCosto = endTallaCol + 2

                    headerRow1.createCell(startTallaCol).apply {
                        setCellValue("TALLAS")
                        cellStyle = headerStyle
                    }
                    // 🔒 SOLO unir si hay más de una talla
                    if(tallaCount > 1) {
                        sheet.addMergedRegion(
                            CellRangeAddress(
                                rowIndex,
                                rowIndex,
                                startTallaCol,
                                endTallaCol
                            )
                        )
                    }

                    // --- ENCABEZADO DE TALLAS ---
                    Log.d("EXCEL_DEBUG", "3")
                    tallas.forEachIndexed { index, talla ->
                        headerRow2.createCell(startTallaCol + index).apply {
                            setCellValue(talla)
                            cellStyle = headerStyle
                        }
                    }

                    // --- COLUMNAS STOCK Y COSTO ---
                    Log.d("EXCEL_DEBUG", "4")
//                    val colStock = endTallaCol + 1
//                    val colCosto = endTallaCol + 2



                    headerRow1.createCell(colStock).apply {
                        setCellValue("STOCK")
                        cellStyle = headerStyle
                    }
                    sheet.addMergedRegion(
                        CellRangeAddress(
                            rowIndex,
                            rowIndex + 1,
                            colStock,
                            colStock
                        )
                    )

                    headerRow1.createCell(colCosto).apply {
                        setCellValue("COSTO\nTOTAL")

                        cellStyle = headerStyle
                    }

                    sheet.addMergedRegion(
                        CellRangeAddress(
                            rowIndex,
                            rowIndex + 1,
                            colCosto,
                            colCosto
                        )
                    )

                    rowIndex += 2

                    // --- AGRUPAR POR COLOR ---
                    val productosPorColor = listaProductos.groupBy { it.color }

                    val nombreProducto = listaProductos.first().nombre
                        .trim()                           // quita espacios al inicio y final
                        .split("\\s+".toRegex())  // divide por 1 o MÁS espacios
                        .filter { it.isNotBlank() }       // elimina palabras vacías
                        .take(3)                      // toma máximo 3 palabras
                        .joinToString(" ")     // vuelve a unirlas

                    for ((color, itemsColor) in productosPorColor) {
                        Log.d(
                            "EXCEL_DEBUG",
                            "Modelo: $modelo | Color: $color | Registros: ${itemsColor.size}"
                        )

                        itemsColor.forEach {
                            Log.d("EXCEL_DEBUG", "Producto: $it")
                        }
                        val row = sheet.createRow(rowIndex)

                        row.createCell(0).setCellValue(modelo)
                        row.createCell(1).setCellValue(nombreProducto)
                        row.createCell(2).setCellValue(color)

                        var totalStockColor = 0

                        tallas.forEachIndexed { i, talla ->
                            val cantidad = itemsColor.firstOrNull { it.talla == talla }?.stock ?: 0
                            totalStockColor += cantidad

                            row.createCell(startTallaCol + i)
                                .setCellValue(cantidad.toDouble())
                        }

                        val costoTotalColor = itemsColor.sumOf {
//                        it.stock * it.costo
                            val stock = it.stock ?: 0
                            val costo = it.costo ?: 0.0
                            stock * costo

                        }

                        row.createCell(colStock).setCellValue(totalStockColor.toDouble())
                        row.createCell(colCosto).setCellValue(costoTotalColor)

                        rowIndex++
                    }

                    rowIndex++ // espacio entre modelos
                }
                Log.d("EXCEL_DEBUG", "Escribiendo archivo Excel...")
                workbook.write(fos)
                Log.d("EXCEL_DEBUG", "Excel escrito correctamente")
                workbook.close()
                viewModelScope.launch(Dispatchers.Main) {
                    _excelGenerated.value = file.absolutePath
                }
            }
        }catch (e: Exception){
            Log.e("EXCEL_ERROR", "Error generando Excel", e)
        }
    }


    //PARA QUE LA UI DETECTE CUANDO EL ARCHIVO ESTE DISPONBILE
    fun resetExcelGenerated() {
        _excelGenerated.value = null
    }

    fun generateExcelReport(productos: List<ProductoGrouped>, filePath: String) {
        // Crear libro de Excel
        Log.d("EXCEL", "Creando workbook…")
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        Log.d("EXCEL", "Workbook creado")
        val sheet = workbook.createSheet("Inventario")

        // Fila de encabezados
        val headerRow = sheet.createRow(0)
        val headers = listOf("Modelo", "Nombre", "Tipo", "Stock", "Precio Promedio", "Sucursal")
        headers.forEachIndexed { index, title ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(title)
        }

        // Llenar filas con datos
        productos.forEachIndexed { rowIndex, producto ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(producto.modeloCod)
            row.createCell(1).setCellValue(producto.nombre)
            row.createCell(2).setCellValue(producto.tipo)
            row.createCell(3).setCellValue(producto.stock.toDouble())
            row.createCell(4).setCellValue(producto.precio)
            row.createCell(5).setCellValue(producto.local)
        }

        // Autoajustar columnas
//        for (i in headers.indices) sheet.autoSizeColumn(i)
        val file = java.io.File(filePath)
        file.parentFile?.mkdirs()   // <-- crea las carpetas si no existen

        // Guardar archivo en almacenamiento
        val fileOut = java.io.FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()
        workbook.close()
    }

    // HOY
    fun getReportToday(sucursal: String) {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val (startTs, endTs) = getTimestampRange(start, end)
        runQuery(startTs, endTs, sucursal)
    }

    // AYER
    fun getReportYesterday(sucursal: String) {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val (startTs, endTs) = getTimestampRange(start, end)
        runQuery(startTs, endTs, sucursal)
    }

    // ÚLTIMOS 7 DÍAS
    fun getReportLast7Days(sucursal: String) {
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val (startTs, endTs) = getTimestampRange(start, end)
        runQuery(startTs, endTs, sucursal)
    }

    // SEMANA PASADA (lunes a domingo)
    fun getReportLastWeek(sucursal: String) {
        val today = Calendar.getInstance()
        // Calcular lunes de la semana pasada
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val diffToLastMonday = (dayOfWeek + 5) % 7 + 7 // días hasta lunes anterior
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -diffToLastMonday)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            time = start.time
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val (startTs, endTs) = getTimestampRange(start, end)
        runQuery(startTs, endTs, sucursal)
    }

    /** ---------------------------------------------------------------------
     *      CONSULTA A FIRESTORE
     *  --------------------------------------------------------------------*/
    private fun runQuery(start: Timestamp, end: Timestamp, sucursal: String) {
        var query = db.collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", start)
            .whereLessThanOrEqualTo("fecha", end)

        if (sucursal.isNotEmpty()) {
            query = query.whereEqualTo("sucursal", sucursal)
        }

        query.get().addOnSuccessListener { snap ->
            val lista = snap.documents.mapNotNull { it.toObject(Venta::class.java) }
            _ventas.value = lista
        }
    }
}