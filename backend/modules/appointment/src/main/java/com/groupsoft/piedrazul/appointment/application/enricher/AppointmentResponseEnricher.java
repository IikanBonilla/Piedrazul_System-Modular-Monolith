package com.groupsoft.piedrazul.appointment.application.enricher;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;

/**
 * OCP: puedes agregar nuevos enriquecedores (ej. especialidad, sala)
 * sin modificar el caso de uso de busqueda.
 */
public interface AppointmentResponseEnricher {

    AppointmentResponseDTO enrich(Appointment appointment, AppointmentResponseDTO baseDto);
}
