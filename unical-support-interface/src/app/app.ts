import { Component } from '@angular/core';
import {CloudUploadIcon, LabelIcon, Mail01Icon, NoteIcon} from '@hugeicons/core-free-icons'
import {Settings} from './services/settings';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrls: ['./app.css', '../../public/styles/input.css'],
})
export class App {
  protected readonly LabelIcon = LabelIcon;
  protected readonly Mail01Icon = Mail01Icon;

  protected isUploadDocumentPopupOpen: boolean = false;

  constructor(
    protected settingsService: Settings
  ) { }

  openUploadDocumentPopup() {
    this.isUploadDocumentPopupOpen = true;
  }

  closeUploadDocumentPopup() {
    this.isUploadDocumentPopupOpen = false;
  }

  protected readonly NoteIcon = NoteIcon;
  protected readonly CloudUploadIcon = CloudUploadIcon;
}
