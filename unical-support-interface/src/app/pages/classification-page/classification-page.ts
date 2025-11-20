import { Component } from '@angular/core';
import {LabelIcon} from '@hugeicons/core-free-icons';

@Component({
  selector: 'app-classification-page',
  standalone: false,
  templateUrl: './classification-page.html',
  styleUrls: ['./classification-page.css', '../../../../public/styles/skeleton.css'],
})
export class ClassificationPage {
  protected readonly LabelIcon = LabelIcon;

  protected emails: number[] = [1,2,3,4,5,6]


}
