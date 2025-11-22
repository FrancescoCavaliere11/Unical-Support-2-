import {Component, Input} from '@angular/core';
import {Mail01Icon, LabelIcon} from '@hugeicons/core-free-icons';
import {EmailToClassifyDto} from '../../model/email-to-classify-dto';

@Component({
  selector: 'app-email-to-classify-item',
  standalone: false,
  templateUrl: './email-to-classify-item.html',
  styleUrls: ['./email-to-classify-item.css', '../../../../public/styles/email-item.css'],
})
export class EmailToClassifyItem {
  @Input({required: true}) emailDto!: EmailToClassifyDto;
  @Input({required: true}) isSelected!: boolean;

  protected readonly Mail01Icon = Mail01Icon;
  protected readonly LabelIcon = LabelIcon;
}
