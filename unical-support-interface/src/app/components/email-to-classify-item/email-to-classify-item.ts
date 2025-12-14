import {Component, Input} from '@angular/core';
import {Mail01Icon, LabelIcon} from '@hugeicons/core-free-icons';
import {EmailDto} from '../../model/email-dto';

@Component({
  selector: 'app-email-to-classify-item',
  standalone: false,
  templateUrl: './email-to-classify-item.html',
  styleUrls: ['./email-to-classify-item.css', '../../../../public/styles/email-item.css'],
})
export class EmailToClassifyItem {
  @Input({required: true}) emailDto!: EmailDto;
  @Input({required: true}) isSelected!: boolean;

  protected readonly LabelIcon = LabelIcon;
}
