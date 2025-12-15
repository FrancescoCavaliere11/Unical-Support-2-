import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { FileAddIcon, FileEditIcon, NoteIcon} from '@hugeicons/core-free-icons';
import {CategoryDto} from '../../model/category-dto';
import {TemplateDto} from '../../model/template-dto';
import {Category} from '../../services/category';
import {Template} from '../../services/template';


const PERMISSIVE_REGEX = /\{\{\s*([a-zA-Z0-9_\s]+)\s*\}\}/g;
const SNAKE_CASE_REGEX = /^[a-z]+(_[a-z]+)*$/;

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

  protected placeholder = "{{nome_richiesta}}"

  constructor(
    private formBuilder: FormBuilder,
    private changeDetector: ChangeDetectorRef,
    private categoryService: Category,
    private templateService: Template
  ) {
    this.form = this.formBuilder.group({
      id: [null],
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
      categoryId: ['', Validators.required],
      content: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(5000)]],
      description: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(300)]],
      parameters: this.formBuilder.array([])
    });
  }

  ngOnInit(): void {
    this.isFetching = true;

    this.categoryService.getCategories().subscribe(categories => this.categories = categories);

    this.templateService.getTemplates().subscribe({
      next: templates => {
        this.templates = templates;
        this.isFetching = false;
        this.changeDetector.detectChanges();
      },
      error: _ => {
        this.isFetching = false;
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

  private createParameterGroup(name: string, required: boolean): FormGroup {
    return this.formBuilder.group({
      name: [name, [Validators.pattern(SNAKE_CASE_REGEX)]],
      required: [required]
    });
  }

  selectTemplate(template: TemplateDto): void {
    this.selectedTemplate = template;

    this.form.patchValue({
      id: template.id,
      name: template.name,
      categoryId: template.category.id,
      description: template.description,
      content: template.content
    }, { emitEvent: false });

    const newParamsControls: FormGroup[] = [];
    if (template.parameters) {
      template.parameters.forEach(p => {
        newParamsControls.push(this.createParameterGroup(p.name, p.required));
      });
    }

    const newFormArray = this.formBuilder.array(newParamsControls);
    this.form.setControl('parameters', newFormArray, { emitEvent: false });
  }

  reset(): void {
    this.selectedTemplate = null;
    this.form.reset({
      id: null,
      name: '',
      categoryId: '',
      description: '',
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
      description: formValue.description,
      parameters: formValue.parameters
    };

    if (this.selectedTemplate) {
      const updatePayload = { ...dto, id: this.selectedTemplate.id };

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

  private parseParametersFromContent(content: string | null): void {
    if (!content) {
      this.parameters.clear();
      return;
    }

    const foundNamesInText = new Set<string>();
    let match;
    PERMISSIVE_REGEX.lastIndex = 0;

    while ((match = PERMISSIVE_REGEX.exec(content)) !== null) {
      foundNamesInText.add(match[1].trim());
    }

    const currentFormNames = new Set<string>();
    this.parameters.controls.forEach(c => currentFormNames.add(c.value.name));

    const toAdd = [...foundNamesInText].filter(name => !currentFormNames.has(name));
    const toRemove = [...currentFormNames].filter(name => !foundNamesInText.has(name));

    if (toRemove.length === 1 && toAdd.length === 1) {
      const indexToUpdate = this.parameters.controls.findIndex(c => c.value.name === toRemove[0]);

      if (indexToUpdate !== -1) {
        this.parameters.at(indexToUpdate).patchValue({ name: toAdd[0] });
        return;
      }
    }

    for (let i = this.parameters.length - 1; i >= 0; i--) {
      if (toRemove.includes(this.parameters.at(i).value.name)) {
        this.parameters.removeAt(i);
      }
    }

    toAdd.forEach(name => {
      this.parameters.push(this.createParameterGroup(name, true));
    });
  }
}
