import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { By } from '@angular/platform-browser';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { lucideCheck } from '@ng-icons/lucide';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HlmIconComponent } from './hlm-icon.component';

@Component({
  selector: 'hlm-mock',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HlmIconComponent],
  providers: [provideIcons({ lucideCheck })],
  template: `
    <hlm-icon
      class="test"
      ngIconClass="test2"
      name="lucideCheck"
      [size]="size"
      color="red"
      strokeWidth="2"
    />
  `,
})
class HlmMockComponent {
  @Input() public size = 'base';
}

describe('HlmIconComponent', () => {
  let r: { fixture: ComponentFixture<HlmMockComponent>; container: HTMLElement };

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [HlmMockComponent] }).compileComponents();
    const fixture = TestBed.createComponent(HlmMockComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    r = { fixture, container: fixture.nativeElement };
  });

  it('should create', () => {
    expect(r).toBeTruthy();
  });

  it('should render the icon', () => {
    expect(r.container.querySelector('svg')).toBeTruthy();
  });

  it('should pass the size, color and strokeWidth props and the classes to the ng-icon component', () => {
    const debugEl = r.fixture.debugElement.query(By.directive(NgIconComponent));
    const component = debugEl.componentInstance as NgIconComponent;
    expect(component.color()).toBe('red');
    expect(component.strokeWidth()).toBe('2');
    expect(component.size()).toBe('100%');
    expect(debugEl.nativeElement.classList).toContain('test2');
  });

  it('should add the appropriate size variant class', () => {
    expect(r.container.querySelector('hlm-icon')?.classList).toContain('h-6');
    expect(r.container.querySelector('hlm-icon')?.classList).toContain('w-6');
  });

  it('should compose the user classes', () => {
    expect(r.container.querySelector('hlm-icon')?.classList).toContain('inline-flex');
    expect(r.container.querySelector('hlm-icon')?.classList).toContain('test');
  });

  it('should forward the size property if the size is not a pre-defined size', async () => {
    r.fixture.componentRef.setInput('size', '2rem');
    r.fixture.detectChanges();
    const debugEl = r.fixture.debugElement.query(By.directive(NgIconComponent));
    expect(debugEl.componentInstance.size()).toBe('2rem');
    expect(r.container.querySelector('hlm-icon')?.classList).not.toContain('h-6');
    expect(r.container.querySelector('hlm-icon')?.classList).not.toContain('w-6');
  });
});
