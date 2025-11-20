import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { ClassificationPage } from './pages/classification-page/classification-page';
import { AnswersPage } from './pages/answers-page/answers-page';
import {HugeiconsIconComponent} from '@hugeicons/angular';
import { EmailToClassifyItem } from './components/email-to-classify-item/email-to-classify-item';

@NgModule({
  declarations: [
    App,
    ClassificationPage,
    AnswersPage,
    EmailToClassifyItem
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HugeiconsIconComponent
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
