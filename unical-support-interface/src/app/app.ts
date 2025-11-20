import { Component } from '@angular/core';
import { LabelIcon, Mail01Icon } from '@hugeicons/core-free-icons'
import {Settings} from './services/settings';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
  protected readonly LabelIcon = LabelIcon;
  protected readonly Mail01Icon = Mail01Icon;

  constructor(
    protected settingsService: Settings
  ) { }

}
