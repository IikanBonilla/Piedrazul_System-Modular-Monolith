package com.groupsoft.piedrazul.appointment.application.export;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * CSV con separador ';' y BOM UTF-8 para que Excel en español abra las columnas.
 */
@Component
public class AppointmentCsvWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] write(AppointmentSearchResultDTO result) {
        StringBuilder csv = new StringBuilder();
        csv.append(cell("Médico")).append(';').append(cell(result.getDoctorName())).append('\n');
        csv.append(cell("Fecha")).append(';').append(cell(result.getDate().format(DATE))).append('\n');
        csv.append(cell("Total")).append(';').append(result.getTotal()).append('\n');
        csv.append('\n');
        csv.append("Fecha;Hora;Paciente;Documento;WhatsApp;Estado;Notas\n");
        for (AppointmentResponseDTO appointment : result.getAppointments()) {
            csv.append(cell(appointment.getAppointmentDate().format(DATE))).append(';');
            csv.append(cell(appointment.getAppointmentDate().format(TIME))).append(';');
            csv.append(cell(appointment.getPatientName())).append(';');
            csv.append(cell(appointment.getPatientDocument())).append(';');
            csv.append(cell(appointment.getWhatsappNumber())).append(';');
            csv.append(cell(statusLabel(appointment.getStatus()))).append(';');
            csv.append(cell(appointment.getNotes())).append('\n');
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

    private String cell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = value.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').trim();
        boolean needsQuotes = sanitized.indexOf(';') >= 0
                || sanitized.indexOf('"') >= 0
                || sanitized.indexOf(',') >= 0;
        if (!needsQuotes) {
            return sanitized;
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }
}
