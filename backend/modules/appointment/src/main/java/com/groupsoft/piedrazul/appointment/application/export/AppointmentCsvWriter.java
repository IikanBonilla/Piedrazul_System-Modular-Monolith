package com.groupsoft.piedrazul.appointment.application.export;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * CSV con separador ';' y BOM UTF-8 para que Excel en español abra las columnas.
 */
@Component
public class AppointmentCsvWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final Pattern PHONE = Pattern.compile("^\\+?\\d+$");

    public byte[] write(AppointmentSearchResultDTO result) {
        StringBuilder csv = new StringBuilder();
        csv.append(cell("Médico")).append(';').append(userCell(result.getDoctorName())).append('\n');
        csv.append(cell("Fecha")).append(';').append(cell(result.getDate().format(DATE))).append('\n');
        csv.append(cell("Total")).append(';').append(result.getTotal()).append('\n');
        csv.append('\n');
        csv.append("Fecha;Hora;Paciente;Documento;WhatsApp;Estado;Notas\n");
        for (AppointmentResponseDTO appointment : result.getAppointments()) {
            csv.append(cell(appointment.getAppointmentDate().format(DATE))).append(';');
            csv.append(cell(appointment.getAppointmentDate().format(TIME))).append(';');
            csv.append(userCell(appointment.getPatientName())).append(';');
            csv.append(userCell(appointment.getPatientDocument())).append(';');
            csv.append(userCell(appointment.getWhatsappNumber())).append(';');
            csv.append(cell(statusLabel(appointment.getStatus()))).append(';');
            csv.append(userCell(appointment.getNotes())).append('\n');
        }

        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] file = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, file, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, file, UTF8_BOM.length, body.length);
        return file;
    }

    private String statusLabel(AppointmentStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case CONFIRMED -> "Confirmada";
            case PENDING -> "Pendiente";
            case CANCELLED -> "Cancelada";
            case COMPLETED -> "Completada";
            case RESCHEDULED -> "Reprogramada";
        };
    }

    /**
     * Celda con texto escrito por usuarios: neutraliza fórmulas (CSV injection)
     * anteponiendo una comilla simple. Los teléfonos válidos se dejan intactos.
     */
    private String userCell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        // trim() elimina \t y \r iniciales, por eso se revisan sobre el valor original.
        boolean leadingControl = value.charAt(0) == '\t' || value.charAt(0) == '\r';
        String sanitized = flatten(value);
        if (sanitized.isEmpty()) {
            return "";
        }
        boolean formula = "=+-@".indexOf(sanitized.charAt(0)) >= 0
                && !PHONE.matcher(sanitized).matches();
        return quote(leadingControl || formula ? "'" + sanitized : sanitized);
    }

    private String cell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return quote(flatten(value));
    }

    private String flatten(String value) {
        return value.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').trim();
    }

    private String quote(String sanitized) {
        boolean needsQuotes = sanitized.indexOf(';') >= 0
                || sanitized.indexOf('"') >= 0
                || sanitized.indexOf(',') >= 0;
        if (!needsQuotes) {
            return sanitized;
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }
}
