package com.torneos.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.torneos.dto.EstadisticaResponse;
import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TablaPosicionesDTO;

@Service
public class ReporteService {

    // ─── PDF: Estadísticas ───────────────────────────────────────────────────

    public ByteArrayInputStream generarPdfEstadisticas(String torneoNombre,
                                                        List<EstadisticaResponse> datos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(new PdfDocument(new PdfWriter(out)));

            document.add(new Paragraph("Tabla de Estadísticas")
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Torneo: " + torneoNombre)
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            String[] headers = {"Jugador", "Equipo", "Goles", "Asistencias",
                    "P. Jugados", "Amarillas", "Rojas"};
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1, 1, 1, 1, 1}));
            table.setWidth(UnitValue.createPercentValue(100));
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            for (EstadisticaResponse e : datos) {
                table.addCell(e.jugador());
                table.addCell(e.equipo());
                table.addCell(centrada(e.goles()));
                table.addCell(centrada(e.asistencias()));
                table.addCell(centrada(e.partidosJugados()));
                table.addCell(centrada(e.tarjetasAmarillas()));
                table.addCell(centrada(e.tarjetasRojas()));
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de estadísticas", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── PDF: Fixture ────────────────────────────────────────────────────────

    public ByteArrayInputStream generarPdfFixture(String torneoNombre,
                                                   List<PartidoResponse> partidos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(new PdfDocument(new PdfWriter(out)));

            document.add(new Paragraph("Fixture — " + torneoNombre)
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));

            String[] headers = {"Fecha", "Hora", "Local", "Visitante", "Cancha", "Estado"};
            Table table = new Table(UnitValue.createPercentArray(new float[]{1.5f, 1, 2, 2, 2, 1.5f}));
            table.setWidth(UnitValue.createPercentValue(100));
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            for (PartidoResponse p : partidos) {
                table.addCell(p.fecha().toString());
                table.addCell(p.hora().toString());
                table.addCell(p.equipoLocal());
                table.addCell(p.equipoVisitante());
                table.addCell(p.cancha());
                table.addCell(new Cell().add(new Paragraph(p.estado())
                        .setTextAlignment(TextAlignment.CENTER)));
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de fixture", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── PDF: Tabla de Posiciones ────────────────────────────────────────────

    public ByteArrayInputStream generarPdfTablaPosiciones(String torneoNombre,
                                                           List<TablaPosicionesDTO> datos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(new PdfDocument(new PdfWriter(out)));

            document.add(new Paragraph("Tabla de Posiciones — " + torneoNombre)
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            String[] headers = {"Pos", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "PTS"};
            Table table = new Table(UnitValue.createPercentArray(
                    new float[]{0.5f, 2.5f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.7f, 0.7f}));
            table.setWidth(UnitValue.createPercentValue(100));
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            for (TablaPosicionesDTO t : datos) {
                table.addCell(centrada(t.getPosicion()));
                table.addCell(t.getEquipo());
                table.addCell(centrada(t.getPartidosJugados()));
                table.addCell(centrada(t.getPartidosGanados()));
                table.addCell(centrada(t.getPartidosEmpatados()));
                table.addCell(centrada(t.getPartidosPerdidos()));
                table.addCell(centrada(t.getGolesFavor()));
                table.addCell(centrada(t.getGolesContra()));
                String dg = t.getDiferenciaGoles() >= 0
                        ? "+" + t.getDiferenciaGoles() : String.valueOf(t.getDiferenciaGoles());
                table.addCell(new Cell().add(new Paragraph(dg).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getPuntos())).setBold()
                        .setTextAlignment(TextAlignment.CENTER)));
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de tabla de posiciones", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── Excel: Estadísticas ─────────────────────────────────────────────────

    public ByteArrayInputStream generarExcelEstadisticas(String torneoNombre,
                                                          List<EstadisticaResponse> datos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Estadísticas");
            XSSFCellStyle hs = headerStyle(wb);
            String[] headers = {"Jugador", "Equipo", "Goles", "Asistencias",
                    "Partidos Jugados", "Tarj. Amarillas", "Tarj. Rojas"};

            sheet.createRow(0).createCell(0).setCellValue("Estadísticas — Torneo: " + torneoNombre);
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                var c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hs);
            }
            int rowNum = 2;
            for (EstadisticaResponse e : datos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.jugador());
                row.createCell(1).setCellValue(e.equipo());
                row.createCell(2).setCellValue(e.goles());
                row.createCell(3).setCellValue(e.asistencias());
                row.createCell(4).setCellValue(e.partidosJugados());
                row.createCell(5).setCellValue(e.tarjetasAmarillas());
                row.createCell(6).setCellValue(e.tarjetasRojas());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel de estadísticas", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── Excel: Fixture ──────────────────────────────────────────────────────

    public ByteArrayInputStream generarExcelFixture(String torneoNombre,
                                                     List<PartidoResponse> partidos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Fixture");
            XSSFCellStyle hs = headerStyle(wb);
            String[] headers = {"Fecha", "Hora", "Local", "Visitante", "Cancha", "Estado"};

            sheet.createRow(0).createCell(0).setCellValue("Fixture — Torneo: " + torneoNombre);
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                var c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hs);
            }
            int rowNum = 2;
            for (PartidoResponse p : partidos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.fecha().toString());
                row.createCell(1).setCellValue(p.hora().toString());
                row.createCell(2).setCellValue(p.equipoLocal());
                row.createCell(3).setCellValue(p.equipoVisitante());
                row.createCell(4).setCellValue(p.cancha());
                row.createCell(5).setCellValue(p.estado());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel de fixture", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── Excel: Tabla de Posiciones ──────────────────────────────────────────

    public ByteArrayInputStream generarExcelTablaPosiciones(String torneoNombre,
                                                             List<TablaPosicionesDTO> datos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Posiciones");
            XSSFCellStyle hs = headerStyle(wb);
            String[] headers = {"Pos", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "PTS"};

            sheet.createRow(0).createCell(0)
                    .setCellValue("Tabla de Posiciones — Torneo: " + torneoNombre);
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                var c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hs);
            }
            int rowNum = 2;
            for (TablaPosicionesDTO t : datos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getPosicion());
                row.createCell(1).setCellValue(t.getEquipo());
                row.createCell(2).setCellValue(t.getPartidosJugados());
                row.createCell(3).setCellValue(t.getPartidosGanados());
                row.createCell(4).setCellValue(t.getPartidosEmpatados());
                row.createCell(5).setCellValue(t.getPartidosPerdidos());
                row.createCell(6).setCellValue(t.getGolesFavor());
                row.createCell(7).setCellValue(t.getGolesContra());
                String dg = t.getDiferenciaGoles() >= 0
                        ? "+" + t.getDiferenciaGoles() : String.valueOf(t.getDiferenciaGoles());
                row.createCell(8).setCellValue(dg);
                row.createCell(9).setCellValue(t.getPuntos());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel de tabla de posiciones", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Cell centrada(int valor) {
        return new Cell().add(new Paragraph(String.valueOf(valor))
                .setTextAlignment(TextAlignment.CENTER));
    }

    private XSSFCellStyle headerStyle(Workbook wb) {
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
