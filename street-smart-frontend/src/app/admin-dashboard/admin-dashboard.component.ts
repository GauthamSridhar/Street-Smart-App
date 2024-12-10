import { Component, AfterViewInit } from '@angular/core';
import Chart from 'chart.js/auto';
import { NavbarComponent } from "../navbar/navbar.component";

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  imports: [NavbarComponent],
})
export class AdminDashboardComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    this.renderShopsGrowthChart();
    this.renderUsersGrowthChart();
  }
  totalUsers = 1450;
  totalShops = 320;
  usersLastWeek = 45;
  shopsLastWeek = 12;
  totalApprovals = 275;
  totalRejections = 45;

  private renderShopsGrowthChart(): void {
    const ctx = document.getElementById('shopsGrowthChart') as HTMLCanvasElement;

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [
          {
            label: 'Shops Growth',
            data: [5, 12, 16, 20, 24, 28, 32, 33, 36, 42, 47, 52], // Dummy data
            borderColor: 'rgba(255, 255, 255, 0.8)',
            backgroundColor: 'rgba(255, 255, 255, 0.2)',
            tension: 0.4, // This makes the curve smooth
            fill: true,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            labels: {
              color: 'white',
            },
          },
        },
        scales: {
          x: {
            ticks: {
              color: 'white',
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)',
            },
          },
          y: {
            ticks: {
              color: 'white',
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)',
            },
          },
        },
      },
    });
  }

  private renderUsersGrowthChart(): void {
    const ctx = document.getElementById('usersGrowthChart') as HTMLCanvasElement;

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [
          {
            label: 'Users Growth',
            data: [10, 15, 30, 45, 54, 80, 88, 99, 109, 130, 149, 160], // Dummy data
            borderColor: 'rgba(255, 255, 255, 0.8)',
            backgroundColor: 'rgba(255, 255, 255, 0.2)',
            tension: 0.4, // Smooth curve
            fill: true,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            labels: {
              color: 'white',
            },
          },
        },
        scales: {
          x: {
            ticks: {
              color: 'white',
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)',
            },
          },
          y: {
            ticks: {
              color: 'white',
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)',
            },
          },
        },
      },
    });
  }
}
