package com.cyberprotect.privacyanalyzer.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.cyberprotect.privacyanalyzer.data.RiskCalculator
import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.model.DeviceSnapshot
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders a self-contained PDF security report: logo, summary, risk score,
 * a simple risk-distribution bar, the top risky apps with their permissions,
 * and recommendations. Uses android.graphics.pdf directly — no external
 * PDF library required.
 */
object PdfReportGenerator {

    private const val PAGE_W = 595 // A4 @ 72dpi
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    fun generate(context: Context, snapshot: DeviceSnapshot, apps: List<AppInfo>): File {
        val document = PdfDocument()
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        var canvas = page.canvas
        var y = MARGIN

        val titlePaint = Paint().apply { color = Color.parseColor("#0C1525"); textSize = 22f; isFakeBoldText = true }
        val accentPaint = Paint().apply { color = Color.parseColor("#00A8C8"); textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { color = Color.parseColor("#333333"); textSize = 11f }
        val mutedPaint = Paint().apply { color = Color.parseColor("#7A97B0"); textSize = 10f }
        val sectionPaint = Paint().apply { color = Color.parseColor("#0C1525"); textSize = 14f; isFakeBoldText = true }

        // Header / "logo"
        val badgePaint = Paint().apply { color = Color.parseColor("#00A8C8") }
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + 28f, y + 28f), 8f, 8f, badgePaint)
        val shieldPaint = Paint().apply { color = Color.WHITE; textSize = 16f; textAlign = Paint.Align.CENTER }
        canvas.drawText("P", MARGIN + 14f, y + 20f, shieldPaint)
        canvas.drawText("Privacy Analyzer", MARGIN + 38f, y + 20f, titlePaint)
        y += 44f
        canvas.drawText("Security Report", MARGIN, y, sectionPaint)
        val dateStr = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(snapshot.lastScanTime))
        canvas.drawText("Generated $dateStr", MARGIN, y + 16f, mutedPaint)
        y += 40f

        // Summary block
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, Paint().apply { color = Color.parseColor("#E0E6EC") })
        y += 24f
        canvas.drawText("Overall Privacy Score", MARGIN, y, sectionPaint)
        y += 22f
        val scorePaint = Paint().apply {
            color = colorForLevel(snapshot.riskLevel.name)
            textSize = 34f
            isFakeBoldText = true
        }
        canvas.drawText("${snapshot.overallScore} / 100", MARGIN, y, scorePaint)
        canvas.drawText("Grade ${snapshot.grade}  ·  ${snapshot.riskLevel.label} Risk", MARGIN + 140f, y, bodyPaint)
        y += 30f

        val stats = listOf(
            "Apps Scanned" to snapshot.appsScanned.toString(),
            "Apps At Risk" to snapshot.appsAtRisk.toString(),
            "Critical Apps" to snapshot.criticalApps.toString(),
            "Dangerous Permissions" to snapshot.dangerousPermissionCount.toString()
        )
        var sx = MARGIN
        for ((label, value) in stats) {
            canvas.drawText(value, sx, y, Paint().apply { color = Color.parseColor("#0C1525"); textSize = 16f; isFakeBoldText = true })
            canvas.drawText(label, sx, y + 14f, mutedPaint)
            sx += 130f
        }
        y += 40f

        // Risky apps section
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, Paint().apply { color = Color.parseColor("#E0E6EC") })
        y += 24f
        canvas.drawText("Highest-Risk Applications", MARGIN, y, sectionPaint)
        y += 20f

        val topRisky = apps.sortedByDescending { it.riskScore }.take(12)
        for (app in topRisky) {
            if (y > PAGE_H - 100f) {
                document.finishPage(page)
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, document.pages.size + 1).create())
                canvas = page.canvas
                y = MARGIN
            }

            val levelColor = colorForLevel(app.riskLevel.name)
            canvas.drawRoundRect(RectF(MARGIN, y - 12f, MARGIN + 8f, y + 4f), 2f, 2f, Paint().apply { color = levelColor })
            canvas.drawText("${app.appName}  (${app.riskScore}%, ${app.riskLevel.label})", MARGIN + 16f, y, bodyPaint)
            y += 14f
            canvas.drawText(app.packageName, MARGIN + 16f, y, mutedPaint)
            y += 12f
            val permNames = app.dangerousGranted.mapNotNull {
                com.cyberprotect.privacyanalyzer.model.PermissionCatalog.metaFor(it.permissionId)?.displayName
            }.distinct()
            if (permNames.isNotEmpty()) {
                canvas.drawText("Permissions: ${permNames.joinToString(", ")}", MARGIN + 16f, y, mutedPaint)
                y += 12f
            }
            y += 8f
        }

        // Recommendations page
        document.finishPage(page)
        page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, document.pages.size + 1).create())
        canvas = page.canvas
        y = MARGIN
        canvas.drawText("Recommendations", MARGIN, y, sectionPaint)
        y += 22f

        val criticalApps = apps.filter { it.riskLevel.name == "CRITICAL" || it.riskLevel.name == "HIGH" }.take(8)
        if (criticalApps.isEmpty()) {
            canvas.drawText("No high-risk apps found. Your device looks healthy.", MARGIN, y, bodyPaint)
        } else {
            for (app in criticalApps) {
                val recs = RiskCalculator.recommendationsFor(app)
                for (rec in recs.take(1)) {
                    canvas.drawText("• $rec", MARGIN, y, bodyPaint)
                    y += 16f
                }
            }
        }

        document.finishPage(page)

        val fileName = "PrivacyAnalyzer_Report_${System.currentTimeMillis()}.pdf"
        val outDir = context.getExternalFilesDir(null) ?: context.filesDir
        val outFile = File(outDir, fileName)
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()
        return outFile
    }

    private fun colorForLevel(levelName: String): Int = when (levelName) {
        "CRITICAL" -> Color.parseColor("#FF4560")
        "HIGH" -> Color.parseColor("#FF8C42")
        "MEDIUM" -> Color.parseColor("#FFD166")
        "LOW" -> Color.parseColor("#00A8A0")
        else -> Color.parseColor("#22B07D")
    }
}
