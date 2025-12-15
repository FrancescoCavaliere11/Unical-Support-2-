import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {EmailDto} from '../../model/email-dto';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Email} from '../../services/email';
import {FilterMailCircleIcon, Mail01Icon} from '@hugeicons/core-free-icons';


@Component({
  selector: 'app-answers-page',
  standalone: false,
  templateUrl: './answers-page.html',
  styleUrls: [
    './answers-page.css',
    '../../../../public/styles/layout.css',
    '../../../../public/styles/input.css'
  ],
})
export class AnswersPage implements OnInit {
  protected readonly Mail01Icon = Mail01Icon;
  protected readonly FilterMailCircleIcon = FilterMailCircleIcon;

  protected emails: EmailDto[] = []
  protected skeletons: number[] = []

  protected selectedEmail: EmailDto | null = null;
  protected originalEmail: EmailDto | null = null;

  protected form: FormGroup = new FormGroup({});

  protected isLoading: boolean = false;
  protected isFetching: boolean = false;
  protected isFiltered: boolean = false;

  protected responseMaxLength = 5000;

  constructor(
    private emailService: Email,
    private formBuilder: FormBuilder,
    private changeDetectorRef: ChangeDetectorRef,
  ) {
    this.skeletons = Array(15).fill(0);

    this.form = this.formBuilder.group({
      id: [''],
      responses: this.formBuilder.array(['', Validators.required])
    })
  }

  get responses() {
    return this.form.get('responses') as FormArray;
  }

  get classification() {
    return this.selectedEmail?.classify;
  }

  get answer() {
    return this.selectedEmail?.answer
  }

  get singleClassifications() {
    return this.selectedEmail?.classify.singleClassifications;
  }

  ngOnInit(): void {
    this.isFetching = true;

    this.emailService.getEmails().subscribe({
      next: emails => {
        this.emails = emails;
        this.isFetching = false;

        if (this.selectedEmail) {
          const updatedSelected = this.emails.find(e => e.id === this.selectedEmail?.id);
          if (updatedSelected) {
            this.selectedEmail = updatedSelected;
          }
        }

        this.changeDetectorRef.detectChanges();
      },
      error: _ => {
        this.isFetching = false;
        this.changeDetectorRef.detectChanges();
        alert("Errore nel caricamento delle email");
      },
    });
  }

  selectEmail(email: EmailDto) {
    this.selectedEmail = email;
    this.originalEmail = email;

    const newControls: any[] = [];

    if (email.classify && email.classify.singleClassifications) {
      email.classify.singleClassifications.forEach((_, index) => {
        const existingAnswer = email.answer?.singleAnswers?.[index]?.answer || '';

        newControls.push(
          this.formBuilder.control(
            existingAnswer,
            [
              Validators.required,
              Validators.maxLength(this.responseMaxLength)
            ]
          )
        );
      });
    }

    this.form.setControl('responses', this.formBuilder.array(newControls));

    this.form.patchValue({
      id: email.id,
    });
  }

  submit() {
    if (this.form.invalid || this.isLoading || !this.selectedEmail) return;

    if (!this.selectedEmail.answer) {
      alert("Errore: struttura dati email incompleta (manca oggetto answer)");
      return;
    }

    if (this.selectedEmail.answer.answered) {
      alert("Hai già inviato le risposte per questa email");
      return;
    }

    this.isLoading = true;

    const updateAnswerDto = {
      id: this.selectedEmail.answer.id,
      singleAnswers: this.selectedEmail.answer.singleAnswers.map((sa, index) => {
        const formValue = this.responses.at(index).value;
        let templateId: string | null = sa.template ? sa.template.id : null;

        if (this.originalEmail?.answer?.singleAnswers[index] &&
          formValue !== this.originalEmail.answer.singleAnswers[index].answer) {
          templateId = null;
        }

        return {
          answer: formValue,
          template_id: templateId
        };
      })
    };

    this.emailService.updateAndSendResponse(updateAnswerDto).subscribe({
      next: (updatedEmail: EmailDto) => {
        this.isLoading = false;

        this.selectedEmail = updatedEmail;
        this.originalEmail = updatedEmail;

        alert("Risposte inviate con successo");
        this.changeDetectorRef.detectChanges();
      },
      error: _ => {
        this.isLoading = false;
        this.changeDetectorRef.detectChanges();
        alert("Errore nell'invio delle risposte");
      }
    });
  }
}
