import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.model';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  roleLabel(role: UserRole): string {
    switch (role) {
      case 'TURISTA':
        return 'Turista';
      case 'GUIA':
        return 'Guía';
      case 'PROPIETARIO':
        return 'Propietario de un servicio';
    }
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/']);
  }
}
