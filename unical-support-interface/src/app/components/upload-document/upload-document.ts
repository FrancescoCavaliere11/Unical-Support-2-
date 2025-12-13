import {ChangeDetectorRef, Component, EventEmitter, Output, OnInit} from '@angular/core';
import {
  Cancel01Icon,
  DocumentAttachmentIcon,
  Upload01Icon
} from '@hugeicons/core-free-icons';
import {CategoryDto} from '../../model/category-dto';
import {Category} from '../../services/category';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Document} from '../../services/document';

@Component({
  selector: 'app-upload-document',
  standalone: false,
  templateUrl: './upload-document.html',
  styleUrls: ['./upload-document.css', '../../../../public/styles/input.css'],
})
export class UploadDocument implements OnInit{
  protected readonly Upload01Icon = Upload01Icon;
  protected readonly Cancel01Icon = Cancel01Icon;
  protected readonly DocumentAttachmentIcon = DocumentAttachmentIcon;

  protected isLoading: boolean = false;
  protected dragOver: boolean = false;
  protected categories: CategoryDto[] = []
  protected form: FormGroup = new FormGroup({});

  @Output() close = new EventEmitter<void>();

  constructor(
    private categoryService: Category,
    private formBuilder: FormBuilder,
    private changeDetectorRef: ChangeDetectorRef,
    private documentService: Document,
  ) {
    this.form = this.formBuilder.group({
      categoryId: ['', Validators.required],
      file: [null, Validators.required]
    })
  }

  get file() {
    return this.form.get('file')?.value || null;
  }

  get isUploadDisabled(): boolean {
    return !this.file || !this.form.get('categoryId')?.value;
  }

  ngOnInit(): void {
    this.categoryService.getCategories()
      .subscribe(categories => this.categories = categories);
  }


  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();

    this.dragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();

    this.dragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;

    const file = event.dataTransfer?.files[0];
    if (!file) return;
    this.form.get('file')?.setValue(file);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    this.form.get('file')?.setValue(input?.files[0]);

    console.log(this.form.value);
  }


  closePopup() {
    this.close.emit();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  removeFile() {
    this.form.get('file')?.setValue(null);
  }


  // API
  upload() {
    if (this.isUploadDisabled) return;

    this.isLoading = true;

    this.documentService.uploadDocuments(this.file, this.form.value.categoryId)
      .subscribe({
        next: () => {
          alert("Documenti caricati con successo");
          this.closePopup();
          this.isLoading = false;
          this.changeDetectorRef.detectChanges();
        },
        error: err => {
          alert("Errore durante l'upload dei documenti");
          this.isLoading = false;
          this.changeDetectorRef.detectChanges();
        }
      });
  }

}
