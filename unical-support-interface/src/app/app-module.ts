import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { ClassificationPage } from './pages/classification-page/classification-page';
import { AnswersPage } from './pages/answers-page/answers-page';
import {HugeiconsIconComponent} from '@hugeicons/angular';
import { EmailToClassifyItem } from './components/email-to-classify-item/email-to-classify-item';
import {ReactiveFormsModule} from "@angular/forms";
import { EmailToAnswerItem } from './components/email-to-answer-item/email-to-answer-item';
import {AddressesFormatPipe} from "./model/pipes/AddressesFormatPipe";
import {ConfidenceFormatPipe} from './model/pipes/ConfidenceFormatPipe';
import { UploadDocument } from './components/upload-document/upload-document';
import { TemplatePage } from './pages/template-page/template-page';

@NgModule({
  declarations: [
    App,
    ClassificationPage,
    AnswersPage,
    EmailToClassifyItem,
    EmailToAnswerItem,
    UploadDocument,
    TemplatePage,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HugeiconsIconComponent,
    ReactiveFormsModule,
    AddressesFormatPipe,
    ConfidenceFormatPipe
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
