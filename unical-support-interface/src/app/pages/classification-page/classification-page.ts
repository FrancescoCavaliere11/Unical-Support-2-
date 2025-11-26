import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Add01Icon, Delete02Icon, LabelIcon} from '@hugeicons/core-free-icons';
import {Email} from '../../services/email';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
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
  protected readonly Delete02Icon = Delete02Icon;
  protected readonly Add01Icon = Add01Icon;

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
      classifications: this.formBuilder.array([]),
      categoryId: ['', Validators.required],
      text: ['', Validators.required]
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

  get classifications(): FormArray {
    return this.form.get('classifications') as FormArray;
  }

  private createClassificationGroup(data?: any): FormGroup {
    return this.formBuilder.group({
      categoryId: [data?.category?.id || '', Validators.required],
      text: [data?.text || '', Validators.required],
      confidence: [data?.confidence || 100]
    });
  }

  addClassification() {
    this.classifications.push(this.createClassificationGroup());
  }

  removeClassification(index: number) {
    this.classifications.removeAt(index);
  }

  selectEmail(email: EmailToClassifyDto) {
    this.selectedEmail = email;

    this.form.patchValue({ id: this.selectedEmail.id });

    this.classifications.clear();

    if (this.selectedEmail.singleClassifications) {
      this.selectedEmail.singleClassifications.forEach(classification => {
        this.classifications.push(this.createClassificationGroup(classification));
      });
    }
  }

  submit() {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;

    const formValue = this.form.value;

    const payload = {
      id: formValue.id,
      updateSingleClassificationDtos: formValue.classifications.map((item: any) => ({
        categoryId: item.categoryId,
        text: item.text
      }))
    };

    this.emailService.updateCategoryForEmail(payload)
      .subscribe({
        next: () => {
          this.emails = this.emails.filter(email => email.id !== this.selectedEmail!.id);

          this.selectedEmail = null;
          this.classifications.clear();
          this.isLoading = false;
        },
        error: (error: any) => {
          console.error(error);
          alert("Errore durante il salvataggio");
          this.isLoading = false;
        }
      });
  }
}
