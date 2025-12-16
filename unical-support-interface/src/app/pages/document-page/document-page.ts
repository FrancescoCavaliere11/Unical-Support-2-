import {ChangeDetectorRef, Component, EventEmitter, OnInit, Output} from '@angular/core';
import {
  Cancel01Icon,
  Delete02Icon,
  DocumentAttachmentIcon,
  File02Icon,
  FileAddIcon, FileUploadIcon, FileViewIcon,
  LabelIcon
} from '@hugeicons/core-free-icons';
import {CategoryDto} from '../../model/category-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Category} from '../../services/category';
import {Document} from '../../services/document';
import {DocumentDto} from '../../model/document-dto';

@Component({
  selector: 'app-document-page',
  standalone: false,
  templateUrl: './document-page.html',
  styleUrls: [
    './document-page.css',
    '../../../../public/styles/layout.css',
    '../../../../public/styles/input.css'
  ],
})
export class DocumentPage implements OnInit {

  protected readonly File02Icon = File02Icon;
  protected readonly DocumentAttachmentIcon = DocumentAttachmentIcon;
  protected readonly Cancel01Icon = Cancel01Icon;
  protected readonly Delete02Icon = Delete02Icon;
  protected readonly FileUploadIcon = FileUploadIcon;
  protected readonly FileViewIcon = FileViewIcon;

  protected isFetching: boolean = false;
  protected isLoading: boolean = false;
  protected dragOver: boolean = false;

  protected categories: CategoryDto[] = []
  protected documents: DocumentDto[] = [];
  protected skeletons: number[] = Array(5).fill(0);

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
      documentLink: [null],
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
    this.isFetching = true;

    this.documentService.getDocuments().subscribe({
      next: documents => {
        this.isFetching = false;
        this.documents = documents;
        console.log(documents)
        this.changeDetectorRef.detectChanges();
      },
      error: error => {
        console.log(error);
        this.isFetching = false;
        alert("Errore nel caricamento dei documenti");
      }
    });

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

  reset() {
    this.form.reset({
      categoryId: '',
      documentLink: null,
      file: null
    });
    this.file.clear();
  }


  // API
  upload() {
    if (this.isUploadDisabled) return;

    this.isLoading = true;

    let documentCreateDto = {
      categoryId: this.form.value.categoryId,
      documentLink: this.form.value.documentLink
    }

    this.documentService.uploadDocuments(this.file, documentCreateDto)
      .subscribe({
        next: () => {
          alert("Documenti caricati con successo");
          this.isLoading = false;
          this.reset()
          this.changeDetectorRef.detectChanges();
        },
        error: err => {
          console.log(err);
          alert("Errore durante l'upload dei documenti");
          this.isLoading = false;
        }
      });
  }

  deleteDocument(document: DocumentDto): void {
    if (!document) return;

    if (confirm('Sei sicuro di voler eliminare questo documento?')) {
      this.isLoading = true;

      this.documentService.deleteDocument(document.id).subscribe({
        next: () => {
          this.isLoading = false;
          this.changeDetectorRef.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          alert('Errore durante l\'eliminazione');
        }
      });
    }
  }

}
