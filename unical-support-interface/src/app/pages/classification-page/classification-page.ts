import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {LabelIcon} from '@hugeicons/core-free-icons';
import {EmailDto} from '../../model/email-dto';
import {Email} from '../../services/email';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Category} from '../../services/category';
import {CategoryDto} from '../../model/category-dto';
import {EmailToClassifyDto} from '../../model/email-to-classify-dto';

@Component({
  selector: 'app-classification-page',
  standalone: false,
  templateUrl: './classification-page.html',
  styleUrls: [
    './classification-page.css',
    '../../../../public/styles/layout.css',
    '../../../../public/styles/input.css'
  ],
})
export class ClassificationPage implements OnInit {
  protected readonly LabelIcon = LabelIcon;

  protected emails: EmailToClassifyDto[] = []
  protected categories: CategoryDto[] = []
  protected skeletons: number[] = []

  protected selectedEmail: EmailToClassifyDto | null = null;

  protected form: FormGroup = new FormGroup({});

  protected isLoading: boolean = false;
  protected isFetching: boolean = false;

  constructor(
    private emailService: Email,
    private categoryService: Category,
    private formBuilder: FormBuilder,
    private changeDetectorRef: ChangeDetectorRef,
  ) {
    this.skeletons = Array(15).fill(0);

    this.form = this.formBuilder.group({
      id: [''],
      categoryId: ['', Validators.required],
      description: ['', Validators.required]
    })
  }

  ngOnInit(): void {
    this.isFetching = true;

    this.categoryService.getCategories()
      .subscribe(categories => this.categories = categories);

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

  selectEmail(email: EmailToClassifyDto) {
    this.selectedEmail = email;
    this.form.patchValue({
      id: this.selectedEmail.id,
      //categoryId: this.selectedEmail.classifierResult?.id ?? '', TODO gestire il multiselect
      explanation: this.selectedEmail.classifierResult?.explanation ?? ''
    })
  }

  submit() {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;
    let updateDto = this.form.value;

    this.emailService.updateCategoryForEmail(updateDto)
      .subscribe({
        next: ()=> {
          this.emails = this.emails.filter(email => email.id !== this.selectedEmail!.id);
          this.isLoading = false;
        },
        error: (error)=> {
          alert(error);
          this.isLoading = false;
        }
      })
  }
}
