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
    this.emailService.getEmails().subscribe({
      next: emails => {
        this.emails = emails
        this.isFetching = false;
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
    this.responses.clear();

    email.classify.singleClassifications.forEach((sc, index) => {
      this.responses.push(
        this.formBuilder.control(
          this.selectedEmail?.answer?.singleAnswers[index]?.answer || '',
          [
            Validators.required,
            Validators.maxLength(this.responseMaxLength)
          ]
        )
      );
    });

    this.form.patchValue({
      id: email.id
    });
  }

  submit() {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;

    if(this.selectedEmail !== null) {
      if(!this.selectedEmail.answer) {
        alert("Errore nella generazione delle risposte")
        this.isLoading = false;
        return;
      }


      if(this.selectedEmail.answer.answered) {
        alert("Hai già inviato le risposte per questa email")
        this.isLoading = false;
        return;
      }

      this.selectedEmail.answer.singleAnswers.forEach((sa, index) => {
        sa.answer = this.responses.at(index).value;
      });

      let updateAnswerDto = {
        id: this.selectedEmail.answer.id,
        singleAnswers: this.selectedEmail.answer.singleAnswers.map((sa, index) => {
          let templateId: string | null = sa.template ? sa.template.id : null;

          if(sa.answer !== this.originalEmail?.answer?.singleAnswers[index].answer)
            templateId = null;

          return ({
            answer: sa.answer,
            template_id: templateId
          })
        })
      };

      this.emailService.updateAndSendResponse(updateAnswerDto).subscribe({
        next: (updatedEmail: EmailDto) => {
          this.isLoading = false;

          this.selectedEmail = updatedEmail;

          const index = this.emails.findIndex(e => e.id === updatedEmail.id);
          if (index !== -1) {
            this.emails = [
              ...this.emails.slice(0, index),
              updatedEmail,
              ...this.emails.slice(index + 1),
            ];
          }

          this.changeDetectorRef.detectChanges();
          alert("Risposte inviate con successo");
        },
        error: _ => {
          this.isLoading = false;
          alert("Errore nell'invio delle risposte");
        }
      })
    } else {
      this.isLoading = false;
    }

  }
}
