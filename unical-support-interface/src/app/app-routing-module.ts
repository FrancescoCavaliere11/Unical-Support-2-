import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClassificationPage} from './pages/classification-page/classification-page';
import {AnswersPage} from './pages/answers-page/answers-page';
import {TemplatePage} from './pages/template-page/template-page';
import {DocumentPage} from './pages/document-page/document-page';

const routes: Routes = [
  { path: '', redirectTo: 'classifications', pathMatch: 'full' },
  { path: 'classifications', component: ClassificationPage },
  { path: 'answers', component: AnswersPage },
  { path: 'templates', component: TemplatePage },
  { path: 'documents', component: DocumentPage },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
