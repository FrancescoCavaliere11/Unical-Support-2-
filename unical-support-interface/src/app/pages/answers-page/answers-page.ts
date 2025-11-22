import {ChangeDetectorRef, Component} from '@angular/core';
import {EmailDto} from '../../model/email-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Email} from '../../services/email';
import {Mail01Icon} from '@hugeicons/core-free-icons';

@Component({
  selector: 'app-answers-page',
  standalone: false,
  templateUrl: './answers-page.html',
  styleUrl: './answers-page.css',
})
export class AnswersPage {
  protected readonly Mail01Icon = Mail01Icon;

  protected emails: EmailDto[] = []
  protected skeletons: number[] = []

  protected selectedEmail: EmailDto | null = null;

  protected form: FormGroup = new FormGroup({});

  protected isLoading: boolean = false;
  protected isFetching: boolean = false;

  constructor(
    private emailService: Email,
    private formBuilder: FormBuilder,
    private changeDetectorRef: ChangeDetectorRef,
  ) {
    this.skeletons = Array(15).fill(0);

    this.form = this.formBuilder.group({
      id: [''],
    })
  }

  ngOnInit(): void {

  }

  selectEmail(email: EmailDto) {
    this.selectedEmail = email;
    this.form.patchValue({

    })
  }

  submit() {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;
  }
}
