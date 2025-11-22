import {Component, Input} from '@angular/core';
import {EmailDto} from '../../model/email-dto';
import {Mail01Icon, LabelIcon} from '@hugeicons/core-free-icons';

@Component({
  selector: 'app-email-to-classify-item',
  standalone: false,
  templateUrl: './email-to-classify-item.html',
  styleUrls: ['./email-to-classify-item.css', '../../../../public/styles/email-item.scss'],
})
export class EmailToClassifyItem {
  @Input({required: true}) emailDto!: EmailDto;
  @Input({required: true}) isSelected!: boolean;

  protected readonly Mail01Icon = Mail01Icon;
  protected readonly LabelIcon = LabelIcon;
}
