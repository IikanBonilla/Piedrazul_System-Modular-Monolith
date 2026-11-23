import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppointmentService, AppointmentYearReportDTO } from '../../../core/services/appointment/service';
import { httpErrorMessage } from '../../../core/http/error-message';

interface ChartBar {
  x: number;
  y: number;
  width: number;
  height: number;
  color: string;
  title: string;
}

interface ChartTick {
  y: number;
  label: string;
}

interface ChartMonth {
  x: number;
  label: string;
}

@Component({
  selector: 'app-doctor-report',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './component.html',
  styles: [`
    .chart-title { text-align: center; font-weight: 700; margin: 0 0 0.75rem; }
    .year-chart { width: 100%; height: auto; display: block; }
    .legend { display: flex; flex-wrap: wrap; justify-content: center; gap: 0.65rem 1.1rem; list-style: none; padding: 0.75rem 0 0; margin: 0; }
    .legend li { display: inline-flex; align-items: center; gap: 0.35rem; }
    .swatch { width: 0.75rem; height: 0.75rem; }
  `]
})
export class DoctorReportComponent implements OnInit {
  readonly chartWidth = 760;
  readonly chartHeight = 360;
  readonly plot = { x: 64, y: 12, width: 676, height: 286 };

  private readonly shortMonths = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
  private readonly palette = ['#E53935', '#3949AB', '#43A047', '#F4D03F', '#E040FB', '#29B6F6', '#FB8C00', '#6D4C41'];

  reportType = 'month-doctor';
  year = new Date().getFullYear();
  report: AppointmentYearReportDTO | null = null;
  bars: ChartBar[] = [];
  yTicks: ChartTick[] = [];
  months: ChartMonth[] = [];
  loading = false;
  error = '';

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.year = Number(this.year);
    if (!Number.isInteger(this.year) || this.year < 2000 || this.year > 2100) {
      this.error = 'Elige un año entre 2000 y 2100.';
      this.clearChart();
      return;
    }
    this.loading = true;
    this.error = '';
    this.appointmentService.getAppointmentYearReport(this.year)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (report) => {
          this.report = report;
          this.buildChart(report);
        },
        error: (error) => {
          this.clearChart();
          this.error = httpErrorMessage(error, 'No se pudo generar el reporte.');
        }
      });
  }

  color(index: number): string {
    return this.palette[index % this.palette.length];
  }

  private clearChart(): void {
    this.report = null;
    this.bars = [];
    this.yTicks = [];
    this.months = [];
  }

  private buildChart(report: AppointmentYearReportDTO): void {
    const doctors = report.doctors ?? [];
    const slot = this.plot.width / this.shortMonths.length;
    this.months = this.shortMonths.map((label, index) => ({
      label,
      x: this.plot.x + slot * index + slot / 2
    }));
    if (doctors.length === 0) {
      this.bars = [];
      this.yTicks = [];
      return;
    }

    const highest = Math.max(1, ...doctors.flatMap((doctor) => doctor.counts));
    const scale = this.axisScale(highest);
    this.yTicks = [];
    for (let value = 0; value <= scale.max; value += scale.step) {
      this.yTicks.push({
        y: this.plot.y + this.plot.height - (value / scale.max) * this.plot.height,
        label: value.toLocaleString('es-CO')
      });
    }

    const gap = 1;
    const usable = slot - 8;
    const barWidth = Math.max(3, (usable - gap * (doctors.length - 1)) / doctors.length);
    const groupWidth = doctors.length * barWidth + gap * (doctors.length - 1);

    this.bars = [];
    doctors.forEach((doctor, index) => {
      doctor.counts.forEach((count, month) => {
        if (!count) {
          return;
        }
        const height = (count / scale.max) * this.plot.height;
        const x = this.plot.x + month * slot + (slot - groupWidth) / 2 + index * (barWidth + gap);
        this.bars.push({
          x,
          y: this.plot.y + this.plot.height - height,
          width: barWidth,
          height,
          color: this.color(index),
          title: `${doctor.doctorName}, ${report.months[month] ?? this.shortMonths[month]}: ${count}`
        });
      });
    });
  }

  private axisScale(highest: number): { max: number; step: number } {
    const rough = highest <= 5 ? 5 : highest;
    const magnitude = Math.pow(10, Math.floor(Math.log10(rough)));
    const step = [1, 2, 5, 10].map((factor) => factor * magnitude).find((candidate) => rough / candidate <= 8)
      ?? magnitude * 10;
    return { step, max: Math.max(step, Math.ceil(rough / step) * step) };
  }
}
