package com.groupsoft.piedrazul.appointment.application.export;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentCsvWriterTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 2);

    private final AppointmentCsvWriter writer = new AppointmentCsvWriter();

    @Test
    void shouldStartWithUtf8Bom() {
        byte[] file = writer.write(result("Dra. Maria Lopez", List.of()));

        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, Arrays.copyOf(file, 3));
    }

    @Test
    void shouldWriteHeaderAndRowsSeparatedBySemicolon() {
        AppointmentResponseDTO appointment = appointment()
                .patientName("Juan Pérez")
                .patientDocument("12345678")
                .whatsappNumber("3001234567")
                .status(AppointmentStatus.CONFIRMED)
                .notes("Control")
                .build();

        String[] lines = lines(writer.write(result("Dra. María López", List.of(appointment))));

        assertEquals("Médico;Dra. María López", lines[0]);
        assertEquals("Fecha;02/09/2026", lines[1]);
        assertEquals("Total;1", lines[2]);
        assertEquals("", lines[3]);
        assertEquals("Fecha;Hora;Paciente;Documento;WhatsApp;Estado;Notas", lines[4]);
        assertEquals("02/09/2026;09:30;Juan Pérez;12345678;3001234567;Confirmada;Control", lines[5]);
    }

    @Test
    void shouldLeaveEmptyCellsForMissingValues() {
        AppointmentResponseDTO appointment = appointment().build();

        String[] lines = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))));

        assertEquals("02/09/2026;09:30;;;;;", lines[5]);
    }

    @Test
    void shouldQuoteValuesWithSeparatorsOrQuotes() {
        AppointmentResponseDTO appointment = appointment()
                .patientName("Pérez; Juan")
                .notes("Dijo \"urgente\", volver")
                .build();

        String row = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))))[5];

        assertEquals("02/09/2026;09:30;\"Pérez; Juan\";;;;\"Dijo \"\"urgente\"\", volver\"", row);
    }

    @Test
    void shouldReplaceLineBreaksWithSpaces() {
        AppointmentResponseDTO appointment = appointment().notes("Línea 1\r\nLínea 2\nLínea 3").build();

        String[] lines = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))));

        assertEquals(6, lines.length);
        assertTrue(lines[5].endsWith(";Línea 1 Línea 2 Línea 3"));
    }

    @Test
    void shouldNeutralizeFormulasInUserFields() {
        AppointmentResponseDTO appointment = appointment()
                .patientName("=HYPERLINK(\"http://x\",\"clic\")")
                .patientDocument("+1+1")
                .whatsappNumber("-2+3")
                .notes("@SUM(A1:A2)")
                .build();

        String[] lines = lines(writer.write(result("=cmd|' /C calc'!A0", List.of(appointment))));

        assertEquals("Médico;'=cmd|' /C calc'!A0", lines[0]);
        assertEquals("02/09/2026;09:30;\"'=HYPERLINK(\"\"http://x\"\",\"\"clic\"\")\";'+1+1;'-2+3;;'@SUM(A1:A2)", lines[5]);
    }

    @Test
    void shouldNeutralizeFormulaAfterLeadingSpace() {
        AppointmentResponseDTO appointment = appointment().notes(" =cmd").build();

        String row = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))))[5];

        assertTrue(row.endsWith(";'=cmd"), row);
    }

    @Test
    void shouldNeutralizeValueStartingWithTab() {
        AppointmentResponseDTO appointment = appointment().notes("\t=cmd").build();

        String row = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))))[5];

        assertTrue(row.endsWith(";'=cmd"), row);
    }

    @Test
    void shouldNotNeutralizeValidPhoneNumbers() {
        AppointmentResponseDTO appointment = appointment().whatsappNumber("+573001234567").build();

        String row = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))))[5];

        assertEquals("02/09/2026;09:30;;;+573001234567;;", row);
    }

    @Test
    void shouldKeepPlainTextUnchanged() {
        AppointmentResponseDTO appointment = appointment().notes("Dolor de cabeza - leve").build();

        String row = lines(writer.write(result("Dra. Maria Lopez", List.of(appointment))))[5];

        assertTrue(row.endsWith(";Dolor de cabeza - leve"), row);
    }

    private AppointmentResponseDTO.AppointmentResponseDTOBuilder appointment() {
        return AppointmentResponseDTO.builder()
                .id(1L)
                .appointmentDate(LocalDateTime.of(2026, 9, 2, 9, 30));
    }

    private AppointmentSearchResultDTO result(String doctorName, List<AppointmentResponseDTO> appointments) {
        return AppointmentSearchResultDTO.builder()
                .doctorId(1L)
                .doctorName(doctorName)
                .date(DATE)
                .total(appointments.size())
                .appointments(appointments)
                .build();
    }

    private String[] lines(byte[] file) {
        String body = new String(file, 3, file.length - 3, StandardCharsets.UTF_8);
        assertTrue(body.endsWith("\n"));
        return body.substring(0, body.length() - 1).split("\n", -1);
    }
}
