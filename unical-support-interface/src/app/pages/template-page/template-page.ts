import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { FileAddIcon, FileEditIcon, NoteIcon} from '@hugeicons/core-free-icons';
import {CategoryDto} from '../../model/category-dto';
import {TemplateDto} from '../../model/TemplateDto';
import {Category} from '../../services/category';
import {Template} from '../../services/template';



@Component({
  selector: 'app-template-page',
  standalone: false,
  templateUrl: './template-page.html',
  styleUrls: [
    './template-page.css',
    '../../../../public/styles/layout.css',
    '../../../../public/styles/input.css'
  ]
})
export class TemplatePage implements OnInit, OnDestroy {
  protected readonly NoteIcon = NoteIcon;
  protected readonly FileEditIcon = FileEditIcon;
  protected readonly FileAddIcon = FileAddIcon;

  protected templates: TemplateDto[] = [];
  protected categories: CategoryDto[] = [];
  protected skeletons: number[] = Array(5).fill(0);

  protected selectedTemplate: TemplateDto | null = null;
  protected isFetching = false;
  protected isLoading = false;

  protected form: FormGroup;
  private destroy$ = new Subject<void>();

  protected nome_richiesta = "{{nome_richiesta}}"

  constructor(
    private formBuilder: FormBuilder,
    private changeDetector: ChangeDetectorRef,
    private categoryService: Category,
    private templateService: Template
  ) {
    this.form = this.formBuilder.group({
      id: [null],
      name: ['', [Validators.required, Validators.maxLength(50)]],
      categoryId: ['', Validators.required],
      content: ['', [Validators.required, Validators.maxLength(5000)]],
      parameters: this.formBuilder.array([])
    });
  }

  ngOnInit(): void {
    this.isFetching = true;

    this.categoryService.getCategories().subscribe(categories => this.categories = categories);


    this.templateService.getTemplates().subscribe({
      next: templates => {
        this.templates = templates
        this.isFetching = false;
        this.changeDetector.detectChanges();
      },
      error: _ => {
        this.isFetching = false;
        this.changeDetector.detectChanges();
        alert("Errore nel caricamento dei template");
      },
    });

    this.form.get('content')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((content: string) => {
        this.parseParametersFromContent(content);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get parameters(): FormArray {
    return this.form.get('parameters') as FormArray;
  }


  selectTemplate(template: TemplateDto): void {
    this.selectedTemplate = template;

    this.form.reset({}, { emitEvent: false });
    this.parameters.clear();

    this.form.patchValue({
      id: template.id,
      name: template.name,
      categoryId: template.category.id,
      content: template.content
    },{ emitEvent: false });

    if (template.parameters) {
      template.parameters.forEach(p => {
        this.addParameterControl(p.name, p.required);
      });
    }
  }

  reset(): void {
    this.selectedTemplate = null;
    this.form.reset({
      id: null,
      name: '',
      categoryId: '',
      content: '',
      required: true
    });
    this.parameters.clear();
  }

  submit(): void {
    if (this.form.invalid || this.isLoading) return;
    this.isLoading = true;

    const formValue = this.form.value;

    const dto = {
      name: formValue.name,
      categoryId: formValue.categoryId,
      content: formValue.content,
      parameters: formValue.parameters
    };

    if (this.selectedTemplate) {
      const updatePayload = {
        ...dto,
        id: this.selectedTemplate.id
      };

      console.log('Updating:', updatePayload);

      this.templateService.updateTemplate(updatePayload).subscribe({
        next: () => {
          this.isLoading = false;
          this.reset();
          alert('Template aggiornato con successo');
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          alert('Errore durante l\'aggiornamento');
        }
      });

    } else {

      this.templateService.createTemplate(dto).subscribe({
        next: (_) => {
          this.isLoading = false;
          this.reset();
          alert('Template creato con successo');
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          alert('Errore durante la creazione');
        }
      });
    }
  }

  deleteTemplate(): void {
    if (!this.selectedTemplate) return;

    if (confirm('Sei sicuro di voler eliminare questo template?')) {
      this.isLoading = true;

      console.log('Deleting ID:', this.selectedTemplate.id);

      this.templateService.deleteTemplate(this.selectedTemplate.id).subscribe({
        next: () => {
          this.isLoading = false;
          this.reset();
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          alert('Errore durante l\'eliminazione');
        }
      });
    }
  }

  // --- Parameter Parsing Logic ---

  private parseParametersFromContent(content: string | null): void {
    if (!content) {
      this.parameters.clear();
      return;
    }

    const regex = /\{\{([a-z]+(?:_[a-z]+)*)\}\}/g;

    const foundNames = new Set<string>();
    let match;

    while ((match = regex.exec(content)) !== null) {
      foundNames.add(match[1]);
    }

    for (let i = this.parameters.length - 1; i >= 0; i--) {
      const currentName = this.parameters.at(i).value.name;
      if (!foundNames.has(currentName)) {
        this.parameters.removeAt(i);
      } else {
        foundNames.delete(currentName);
      }
    }

    foundNames.forEach(name => {
      this.addParameterControl(name, true);
    });
  }

  private addParameterControl(name: string, required: boolean): void {
    const group = this.formBuilder.group({
      name: [name],
      required: [required]
    });
    this.parameters.push(group);
  }
}
