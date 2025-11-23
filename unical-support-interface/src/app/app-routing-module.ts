import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClassificationPage} from './pages/classification-page/classification-page';
import {AnswersPage} from './pages/answers-page/answers-page';

const routes: Routes = [
  { path: '', redirectTo: 'classification', pathMatch: 'full' },
  { path: 'classification', component: ClassificationPage },
  { path: 'answers', component: AnswersPage },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
