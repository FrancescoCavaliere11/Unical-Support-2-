import {Component, Input} from '@angular/core';
import {EmailDto} from '../../model/email-dto';

@Component({
  selector: 'app-email-to-classify-item',
  standalone: false,
  templateUrl: './email-to-classify-item.html',
  styleUrl: './email-to-classify-item.css',
})
export class EmailToClassifyItem {
  @Input({required: true})
  emailDto!: EmailDto;

  @Input({required: true})
  isSelected!: boolean;


}
