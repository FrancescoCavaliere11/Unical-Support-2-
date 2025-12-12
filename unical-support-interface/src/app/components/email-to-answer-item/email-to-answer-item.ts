import {Component, Input} from '@angular/core';
import {Mail01Icon} from '@hugeicons/core-free-icons';
import {EmailDto} from '../../model/email-dto';


@Component({
  selector: 'app-email-to-answer-item',
  standalone: false,
  templateUrl: './email-to-answer-item.html',
  styleUrls: ['./email-to-answer-item.css', '../../../../public/styles/email-item.css'],
})
export class EmailToAnswerItem {
  @Input({required: true}) emailDto!: EmailDto;
  @Input({required: true}) isSelected!: boolean;

  protected readonly Mail01Icon = Mail01Icon;
}
