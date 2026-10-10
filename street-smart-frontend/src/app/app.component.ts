import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'street-smart';
  skipToContent(event: Event): void {
    const main = document.querySelector('main');
    if (main) {
      event.preventDefault();
      main.id = 'main-content';
      main.setAttribute('tabindex', '-1');
      main.focus();
    }
  }
}
