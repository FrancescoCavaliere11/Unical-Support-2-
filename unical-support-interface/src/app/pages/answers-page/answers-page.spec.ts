import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnswersPage } from './answers-page';

describe('AnswersPage', () => {
  let component: AnswersPage;
  let fixture: ComponentFixture<AnswersPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AnswersPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnswersPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
